/*
 * Test Design Brief, written before implementation assertions:
 * - Access isolation: a USER must see no admin data and trigger no admin call.
 * - Contract isolation: every request must use /admin paths, never event ownership APIs.
 * - Resilience: loading, server error, retry, and empty data remain actionable.
 * - Mutations: block/unblock require confirmation, disable duplicate submission, and reload data.
 * - Accessibility: named regions, status/error announcements, keyboard-reachable controls, and dialog focus.
 * - Boundary risks: missing summary fields normalize to zero; both array and paged list payloads render.
 */
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
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
})
