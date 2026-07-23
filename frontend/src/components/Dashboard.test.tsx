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

  it('uses the compact mobile flow to open details and return focus to the event list', async () => {
    const originalMatchMedia = window.matchMedia
    const originalRequestAnimationFrame = window.requestAnimationFrame
    const originalScrollTo = window.scrollTo
    window.matchMedia = vi.fn().mockImplementation((query: string) => ({
      matches: query.includes('max-width'),
      media: query,
      onchange: null,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      addListener: vi.fn(),
      removeListener: vi.fn(),
      dispatchEvent: vi.fn(),
    }))
    window.requestAnimationFrame = ((callback: FrameRequestCallback) => {
      callback(0)
      return 1
    }) as typeof window.requestAnimationFrame
    window.scrollTo = vi.fn()
    const birthday = {
      ...ownerEvent,
      id: 'event-2',
      name: 'Birthday',
      type: 'BIRTHDAY' as const,
      eventDate: null,
      description: null,
    }
    vi.mocked(eventsApi.list).mockResolvedValue({ data: [ownerEvent, birthday] } as never)

    try {
      renderDashboard()

      expect(await screen.findAllByRole('button', { name: 'Manage' })).toHaveLength(2)
      expect(screen.queryByText('Galleries for Summer Wedding')).not.toBeInTheDocument()
      fireEvent.click(screen.getAllByRole('button', { name: 'Manage' })[0])
      expect(await screen.findByText('Galleries for Summer Wedding')).toBeInTheDocument()
      fireEvent.click(screen.getByRole('button', { name: 'Back to events' }))
      expect(screen.queryByText('Galleries for Summer Wedding')).not.toBeInTheDocument()
      expect(window.scrollTo).toHaveBeenCalled()
    } finally {
      window.matchMedia = originalMatchMedia
      window.requestAnimationFrame = originalRequestAnimationFrame
      window.scrollTo = originalScrollTo
    }
  })

  it('delegates logout from the authenticated shell', async () => {
    vi.mocked(eventsApi.list).mockResolvedValue({ data: [] } as never)
    renderDashboard()
    await screen.findByText('Your first event starts here')
    fireEvent.click(screen.getByRole('button', { name: 'Log out' }))
    expect(auth.logout).toHaveBeenCalledOnce()
  })

  it('selects another event, edits every writable field and deletes it only after confirmation', async () => {
    const birthday = {
      ...ownerEvent,
      id: 'event-2',
      name: 'Birthday',
      type: 'BIRTHDAY' as const,
      eventDate: null,
      description: null,
    }
    vi.mocked(eventsApi.list)
      .mockResolvedValueOnce({ data: [ownerEvent, birthday] } as never)
      .mockResolvedValue({ data: [ownerEvent, birthday] } as never)
    vi.mocked(eventsApi.update).mockResolvedValue({ data: birthday } as never)
    renderDashboard()

    await screen.findByText('Galleries for Summer Wedding')
    fireEvent.click(screen.getByRole('button', { name: 'Manage' }))
    expect(await screen.findByText('Galleries for Birthday')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Edit details' }))
    expect(screen.getByRole('dialog', { name: 'Edit event' })).toBeVisible()
    fireEvent.change(screen.getByRole('textbox', { name: /Event name/ }), {
      target: { value: 'Birthday updated' },
    })
    fireEvent.change(screen.getByLabelText('Event date'), { target: { value: '2026-09-10' } })
    fireEvent.change(screen.getByRole('textbox', { name: 'Description' }), {
      target: { value: 'All details' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(eventsApi.update).toHaveBeenCalledWith('event-2', {
        name: 'Birthday updated',
        type: 'BIRTHDAY',
        eventDate: '2026-09-10',
        description: 'All details',
        privacyMode: 'PRIVATE',
      }),
    )
    expect(await screen.findByText('Event updated.')).toBeInTheDocument()
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: 'Edit event' })).not.toBeInTheDocument(),
    )

    fireEvent.click(screen.getByRole('tab', { name: 'Settings' }))
    fireEvent.click(screen.getByRole('button', { name: 'Delete event' }))
    let dialog = screen.getByRole('dialog', { name: 'Delete event?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Cancel' }))
    expect(eventsApi.remove).not.toHaveBeenCalled()
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    fireEvent.click(screen.getByRole('button', { name: 'Delete event' }))
    dialog = screen.getByRole('dialog', { name: 'Delete event?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Delete event' }))
    await waitFor(() => expect(eventsApi.remove).toHaveBeenCalledWith('event-2'))
  })

  it('keeps dialogs actionable and reports create, member and confirmation failures', async () => {
    vi.mocked(eventsApi.list).mockResolvedValue({ data: [ownerEvent] } as never)
    vi.mocked(eventsApi.create).mockRejectedValue(new Error('offline'))
    vi.mocked(eventsApi.members).mockRejectedValueOnce({ response: { data: {} } })
    vi.mocked(eventsApi.addManager).mockRejectedValue({
      response: { data: { message: 'Manager already belongs to this event.' } },
    })
    vi.mocked(eventsApi.archive).mockRejectedValue(new Error('offline'))
    renderDashboard()

    await screen.findByText('Galleries for Summer Wedding')
    expect(await screen.findByText('Operation failed.')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Create event' }))
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: 'Create event' })).not.toBeInTheDocument(),
    )
    fireEvent.click(screen.getByRole('button', { name: 'Create event' }))
    fireEvent.change(screen.getByRole('textbox', { name: /Event name/ }), {
      target: { value: 'Failure case' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Save' }))
    expect(await screen.findByText('Operation failed.')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: 'Create event' })).not.toBeInTheDocument(),
    )
    fireEvent.click(screen.getByRole('tab', { name: 'People' }))
    fireEvent.change(screen.getByRole('textbox', { name: 'Manager email' }), {
      target: { value: 'duplicate@example.com' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add manager' }))
    expect(await screen.findByText('Manager already belongs to this event.')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('tab', { name: 'Settings' }))
    fireEvent.click(screen.getByRole('button', { name: 'Archive event' }))
    const dialog = screen.getByRole('dialog', { name: 'Archive event?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Archive event' }))
    await waitFor(() => expect(eventsApi.archive).toHaveBeenCalled())
    expect(await screen.findByText('Operation failed.')).toBeInTheDocument()
  })

  it('renders archived events as read-only and preserves alternate event types', async () => {
    const archived = {
      ...ownerEvent,
      type: 'CORPORATE' as const,
      status: 'ARCHIVED' as const,
    }
    vi.mocked(eventsApi.list).mockResolvedValue({ data: [archived] } as never)
    renderDashboard()

    await screen.findByText('Galleries for Summer Wedding')
    expect(screen.getAllByText('Corporate')).toHaveLength(2)
    expect(screen.getByRole('button', { name: 'Edit details' })).toBeDisabled()
    fireEvent.click(screen.getByRole('tab', { name: 'Settings' }))
    expect(screen.getByRole('button', { name: 'Archive event' })).toBeDisabled()
  })
})
