import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { EventData, EventMember } from '../eventsApi'
import { eventsApi } from '../eventsApi'
import { appTheme } from '../theme'
import Dashboard from './Dashboard'

const auth = vi.hoisted(() => ({ logout: vi.fn() }))

vi.mock('../AuthContext', () => ({
  useAuth: () => ({ user: { email: 'owner@example.com' }, logout: auth.logout }),
}))

vi.mock('../eventsApi', () => ({
  eventsApi: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    archive: vi.fn(),
    remove: vi.fn(),
    members: vi.fn(),
    addManager: vi.fn(),
    removeManager: vi.fn(),
    transferOwnership: vi.fn(),
  },
}))

vi.mock('./GalleryManager', () => ({
  default: ({ event }: { event: EventData }) => <div>Galleries for {event.name}</div>,
}))

const ownerEvent: EventData = {
  id: 'event-1',
  name: 'Summer Wedding',
  type: 'WEDDING',
  eventDate: '2026-08-01',
  description: 'A private celebration',
  status: 'DRAFT',
  privacyMode: 'PRIVATE',
  currentUserRole: 'OWNER',
  createdAt: '2026-07-01T12:00:00Z',
  updatedAt: '2026-07-01T12:00:00Z',
}

const members: EventMember[] = [
  {
    id: null,
    userId: 'owner-1',
    email: 'owner@example.com',
    role: 'OWNER',
    joinedAt: '2026-07-01T12:00:00Z',
  },
  {
    id: 'membership-1',
    userId: 'manager-1',
    email: 'manager@example.com',
    role: 'MANAGER',
    joinedAt: '2026-07-02T12:00:00Z',
  },
]

function renderDashboard() {
  return render(
    <ThemeProvider theme={appTheme}>
      <Dashboard />
    </ThemeProvider>,
  )
}

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(eventsApi.members).mockResolvedValue({ data: members } as never)
    vi.mocked(eventsApi.addManager).mockResolvedValue({ data: members[1] } as never)
    vi.mocked(eventsApi.removeManager).mockResolvedValue({ data: null } as never)
    vi.mocked(eventsApi.transferOwnership).mockResolvedValue({ data: ownerEvent } as never)
    vi.mocked(eventsApi.archive).mockResolvedValue({ data: ownerEvent } as never)
    vi.mocked(eventsApi.remove).mockResolvedValue({ data: null } as never)
  })

  afterEach(cleanup)

  it('shows a server error, retries and reaches the empty-state create flow', async () => {
    vi.mocked(eventsApi.list)
      .mockRejectedValueOnce({ response: { data: { message: 'Events are unavailable.' } } })
      .mockResolvedValueOnce({ data: [] } as never)
    renderDashboard()

    expect(await screen.findByText('Events are unavailable.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText('Your first event starts here')).toBeInTheDocument()
    expect(eventsApi.list).toHaveBeenCalledTimes(2)
  })

  it('creates an event with normalized optional values and reloads the selected workspace', async () => {
    vi.mocked(eventsApi.list)
      .mockResolvedValueOnce({ data: [] } as never)
      .mockResolvedValue({ data: [ownerEvent] } as never)
    vi.mocked(eventsApi.create).mockResolvedValue({ data: ownerEvent } as never)
    renderDashboard()

    fireEvent.click(await screen.findByRole('button', { name: 'Create your first event' }))
    fireEvent.change(screen.getByRole('textbox', { name: /Event name/ }), {
      target: { value: 'Summer Wedding' },
    })
    fireEvent.change(screen.getByRole('textbox', { name: 'Description' }), {
      target: { value: '' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(eventsApi.create).toHaveBeenCalledWith({
        name: 'Summer Wedding',
        type: 'WEDDING',
        eventDate: null,
        description: null,
        privacyMode: 'PRIVATE',
      }),
    )
    expect(await screen.findByText('Event created.')).toBeInTheDocument()
    expect(await screen.findByText('Galleries for Summer Wedding')).toBeInTheDocument()
  })

  it('lets the owner manage people and lifecycle through explicit confirmations', async () => {
    vi.mocked(eventsApi.list).mockResolvedValue({ data: [ownerEvent] } as never)
    renderDashboard()

    expect(await screen.findByText('Galleries for Summer Wedding')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('tab', { name: 'People' }))
    expect(await screen.findByText('manager@example.com')).toBeInTheDocument()

    fireEvent.change(screen.getByRole('textbox', { name: 'Manager email' }), {
      target: { value: 'new-manager@example.com' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add manager' }))
    await waitFor(() =>
      expect(eventsApi.addManager).toHaveBeenCalledWith('event-1', 'new-manager@example.com'),
    )

    fireEvent.click(screen.getByRole('button', { name: 'Transfer ownership' }))
    let dialog = screen.getByRole('dialog', { name: 'Transfer ownership?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Transfer ownership' }))
    await waitFor(() =>
      expect(eventsApi.transferOwnership).toHaveBeenCalledWith('event-1', 'membership-1'),
    )
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: 'Remove' }))
    dialog = screen.getByRole('dialog', { name: 'Remove manager?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Remove manager' }))
    await waitFor(() =>
      expect(eventsApi.removeManager).toHaveBeenCalledWith('event-1', 'membership-1'),
    )
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())

    fireEvent.click(screen.getByRole('tab', { name: 'Settings' }))
    fireEvent.click(screen.getByRole('button', { name: 'Archive event' }))
    dialog = screen.getByRole('dialog', { name: 'Archive event?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Archive event' }))
    await waitFor(() => expect(eventsApi.archive).toHaveBeenCalledWith('event-1'))
  })

  it('does not expose owner-only people or lifecycle controls to a manager', async () => {
    const managerEvent = { ...ownerEvent, currentUserRole: 'MANAGER' as const }
    vi.mocked(eventsApi.list).mockResolvedValue({ data: [managerEvent] } as never)
    renderDashboard()

    await screen.findByText('Galleries for Summer Wedding')
    fireEvent.click(screen.getByRole('tab', { name: 'People' }))
    expect(screen.queryByRole('textbox', { name: 'Manager email' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Remove' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Transfer ownership' })).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('tab', { name: 'Settings' }))
    expect(screen.queryByRole('button', { name: 'Archive event' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Delete event' })).not.toBeInTheDocument()
  })

  it('delegates logout from the authenticated shell', async () => {
    vi.mocked(eventsApi.list).mockResolvedValue({ data: [] } as never)
    renderDashboard()
    await screen.findByText('Your first event starts here')
    fireEvent.click(screen.getByRole('button', { name: 'Log out' }))
    expect(auth.logout).toHaveBeenCalledOnce()
  })
})
