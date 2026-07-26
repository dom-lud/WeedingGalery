/*
 * Test Design Brief, written before implementation assertions:
 * - Access isolation: a USER must see no admin data and trigger no admin call.
 * - Contract isolation: every request must use /admin paths, never event ownership APIs.
 * - Resilience: loading, server error, retry, and empty data remain actionable.
 * - Mutations: block/unblock require confirmation, disable duplicate submission, and reload data.
 * - Accessibility: named regions, status/error announcements, keyboard-reachable controls, and dialog focus.
 * - Boundary risks: missing summary fields normalize to zero; both array and paged list payloads render.
 */
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { adminApi } from '../adminApi'
import { appTheme } from '../theme'
import AdminDashboard from './AdminDashboard'

const auth = vi.hoisted(() => ({
  user: { email: 'admin@example.com', systemRole: 'ADMIN' as 'ADMIN' | 'USER' },
  logout: vi.fn(),
}))

vi.mock('../AuthContext', () => ({ useAuth: () => auth }))
vi.mock('../adminApi', async () => {
  const actual = await vi.importActual<typeof import('../adminApi')>('../adminApi')
  return {
    ...actual,
    adminApi: {
      dashboard: vi.fn(),
      users: vi.fn(),
      events: vi.fn(),
      audit: vi.fn(),
      blockUser: vi.fn(),
      unblockUser: vi.fn(),
    },
  }
})

const summary = { users: 4, activeUsers: 3, events: 2, galleries: 5, media: 18, storageBytes: 2048 }
const user = {
  id: 'user-1',
  email: 'person@example.com',
  systemRole: 'USER' as const,
  status: 'ACTIVE' as const,
  createdAt: '2026-07-01',
  lastLoginAt: null,
}
const lockedUser = { ...user, id: 'user-2', email: 'locked@example.com', locked: true }
const event = {
  id: 'event-1',
  name: 'Wedding',
  ownerEmail: 'owner@example.com',
  type: 'WEDDING',
  status: 'ACTIVE',
  ownerUserId: 'owner-1',
  eventDate: '2026-08-01',
  createdAt: '',
  updatedAt: '',
}
const audit = {
  id: 'audit-1',
  eventType: 'USER_LOCKED',
  actorEmail: null,
  resourceType: 'USER',
  resourceId: 'user-2',
  createdAt: '',
  result: null,
}

function renderDashboard() {
  return render(
    <ThemeProvider theme={appTheme}>
      <AdminDashboard />
    </ThemeProvider>,
  )
}

function resolveData() {
  vi.mocked(adminApi.dashboard).mockResolvedValue({ data: summary } as never)
  vi.mocked(adminApi.users).mockResolvedValue({ data: [user] } as never)
  vi.mocked(adminApi.events).mockResolvedValue({ data: [] } as never)
  vi.mocked(adminApi.audit).mockResolvedValue({ data: [] } as never)
}

describe('AdminDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    auth.user = { email: 'admin@example.com', systemRole: 'ADMIN' }
    resolveData()
  })
  afterEach(cleanup)

  it('fails closed for a regular user without calling admin APIs', async () => {
    auth.user = { email: 'user@example.com', systemRole: 'USER' }
    renderDashboard()
    expect(await screen.findByRole('alert')).toHaveTextContent(/do not have permission/i)
    expect(adminApi.dashboard).not.toHaveBeenCalled()
  })

  it('loads summary and users, including a paged response', async () => {
    vi.mocked(adminApi.users).mockResolvedValue({
      data: { content: [user], totalElements: 1, totalPages: 1, number: 0 },
    } as never)
    renderDashboard()
    expect(await screen.findByText('person@example.com')).toBeInTheDocument()
    expect(screen.getByText(/2(?:\.0)? KB/)).toBeInTheDocument()
    expect(adminApi.users).toHaveBeenCalledWith({ query: undefined, page: 0, size: 25 })
  })

  it('shows an error and retries the full administrative load', async () => {
    vi.mocked(adminApi.dashboard)
      .mockRejectedValueOnce({ response: { data: { message: 'Admin API offline.' } } })
      .mockResolvedValue({ data: summary } as never)
    renderDashboard()
    expect(await screen.findByRole('alert')).toHaveTextContent('Admin API offline.')
    resolveData()
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }))
    await waitFor(() => expect(screen.getByText('person@example.com')).toBeInTheDocument())
    expect(adminApi.dashboard).toHaveBeenCalledTimes(2)
  })

  it('requires confirmation before blocking and reloads after confirmation', async () => {
    renderDashboard()
    await screen.findByText('person@example.com')
    fireEvent.click(screen.getByRole('button', { name: 'Block' }))
    expect(screen.getByRole('dialog')).toHaveTextContent(/will lose access/i)
    fireEvent.click(screen.getByRole('button', { name: 'Block user' }))
    await waitFor(() => expect(adminApi.blockUser).toHaveBeenCalledWith('user-1'))
    expect(adminApi.dashboard).toHaveBeenCalledTimes(2)
  })

  it('renders event and audit tabs, including system fallbacks and large storage units', async () => {
    vi.mocked(adminApi.dashboard).mockResolvedValue({
      data: { users: 1, events: 1, galleries: 1, media: 1, storageUsedBytes: 10 * 1024 ** 4 },
    } as never)
    vi.mocked(adminApi.users).mockResolvedValue({ data: [] } as never)
    vi.mocked(adminApi.events).mockResolvedValue({ data: [event] } as never)
    vi.mocked(adminApi.audit).mockResolvedValue({ data: [audit] } as never)
    renderDashboard()
    await screen.findByText('10 TB')
    fireEvent.click(screen.getByRole('tab', { name: 'Events' }))
    expect(screen.getByText('Wedding')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('tab', { name: 'Audit' }))
    expect(screen.getByText('USER_LOCKED')).toBeInTheDocument()
    const auditPanel = screen.getByRole('tabpanel', { name: 'audit data' })
    expect(auditPanel).toHaveTextContent('System')
    expect(auditPanel).toHaveTextContent('Recorded')
  })

  it('unblocks a locked user and surfaces mutation errors without closing confirmation', async () => {
    vi.mocked(adminApi.users).mockResolvedValue({ data: [lockedUser] } as never)
    vi.mocked(adminApi.unblockUser).mockRejectedValue({
      response: { data: { message: 'Cannot unblock.' } },
    })
    renderDashboard()
    await screen.findByText('locked@example.com')
    fireEvent.click(screen.getByRole('button', { name: 'Unblock' }))
    fireEvent.click(screen.getByRole('button', { name: 'Unblock user' }))
    expect(await screen.findByText('Cannot unblock.')).toBeInTheDocument()
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('uses the generic message for errors without an HTTP response and reloads on search', async () => {
    vi.mocked(adminApi.dashboard).mockRejectedValueOnce(new Error('offline'))
    renderDashboard()
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Administrative data is unavailable.',
    )
    resolveData()
    fireEvent.change(screen.getByRole('textbox', { name: 'Search administrative data' }), {
      target: { value: 'new query' },
    })
    await waitFor(() =>
      expect(adminApi.users).toHaveBeenLastCalledWith({ query: 'new query', page: 0, size: 25 }),
    )
  })
})
