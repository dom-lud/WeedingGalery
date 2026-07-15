import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { EventData } from '../eventsApi'
import { galleriesApi } from '../galleriesApi'
import { appTheme } from '../theme'
import GalleryManager from './GalleryManager'

vi.mock('../galleriesApi', () => ({
  galleriesApi: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    archive: vi.fn(),
    remove: vi.fn(),
    settings: vi.fn(),
    updateSettings: vi.fn(),
    rotateAccessToken: vi.fn(),
    setAccessCode: vi.fn(),
    removeAccessCode: vi.fn(),
  },
}))

const event: EventData = {
  id: 'event-1',
  name: 'Wedding',
  type: 'WEDDING',
  eventDate: null,
  description: null,
  status: 'DRAFT',
  privacyMode: 'PRIVATE',
  currentUserRole: 'OWNER',
  createdAt: '2026-07-13T12:00:00Z',
  updatedAt: '2026-07-13T12:00:00Z',
}

function renderManager(currentEvent = event) {
  return render(
    <ThemeProvider theme={appTheme}>
      <GalleryManager event={currentEvent} />
    </ThemeProvider>,
  )
}

describe('GalleryManager', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows the empty state and creates a gallery through the API layer', async () => {
    vi.mocked(galleriesApi.list).mockResolvedValue({ data: [] } as never)
    vi.mocked(galleriesApi.create).mockResolvedValue({ data: {} } as never)
    renderManager()

    expect(await screen.findByText('No galleries yet.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Create gallery' }))
    fireEvent.change(screen.getByRole('textbox', { name: /Gallery name/ }), {
      target: { value: 'Reception' },
    })
    fireEvent.change(screen.getByRole('spinbutton', { name: 'Gallery order' }), {
      target: { value: '20' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Save gallery' }))

    await waitFor(() =>
      expect(galleriesApi.create).toHaveBeenCalledWith('event-1', {
        name: 'Reception',
        description: null,
        sortOrder: 20,
      }),
    )
    expect(await screen.findByText('Gallery created.')).toBeInTheDocument()
    await waitFor(() => expect(galleriesApi.list).toHaveBeenCalledTimes(2))
  })

  it('does not expose lifecycle actions to a manager', async () => {
    vi.mocked(galleriesApi.list).mockResolvedValue({
      data: [
        {
          id: 'gallery-1',
          eventId: 'event-1',
          name: 'Morning',
          slug: 'morning-abcd1234',
          description: null,
          sortOrder: 10,
          status: 'ACTIVE',
          currentUserRole: 'MANAGER',
          createdAt: '2026-07-13T12:00:00Z',
          updatedAt: '2026-07-13T12:00:00Z',
        },
      ],
    } as never)
    renderManager({ ...event, currentUserRole: 'MANAGER' })

    expect(await screen.findByText('Morning')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Edit gallery' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Archive gallery' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Delete gallery' })).not.toBeInTheDocument()
  })

  it('renders an actionable error state', async () => {
    vi.mocked(galleriesApi.list).mockRejectedValue(new Error('offline'))
    renderManager()
    expect(await screen.findByText('Gallery operation failed.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument()
  })

  it('lets the owner rotate a one-time private share link', async () => {
    vi.mocked(galleriesApi.list).mockResolvedValue({
      data: [
        {
          id: 'gallery-1',
          eventId: 'event-1',
          name: 'Reception',
          slug: 'reception',
          description: null,
          sortOrder: 0,
          status: 'ACTIVE',
          currentUserRole: 'OWNER',
          createdAt: '',
          updatedAt: '',
        },
      ],
    } as never)
    vi.mocked(galleriesApi.settings).mockResolvedValue({
      data: {
        publicViewEnabled: false,
        uploadEnabled: false,
        downloadEnabled: false,
        moderationMode: 'REQUIRED',
        accessTokenConfigured: false,
        accessCodeConfigured: false,
        publishedAt: null,
        expiresAt: null,
        version: 0,
      },
    } as never)
    vi.mocked(galleriesApi.rotateAccessToken).mockResolvedValue({
      data: { accessToken: 'raw', sharePath: '/g/reception#token=raw' },
    } as never)
    renderManager()

    await screen.findByText('Reception')
    fireEvent.click(screen.getByRole('button', { name: 'Access settings' }))
    fireEvent.click(await screen.findByRole('button', { name: 'Rotate private share link' }))
    expect(
      await screen.findByDisplayValue(`${window.location.origin}/g/reception#token=raw`),
    ).toBeInTheDocument()
    expect(screen.getByText(/shown only once/i)).toBeInTheDocument()
  })

  it('shows settings to a manager without mutation controls', async () => {
    vi.mocked(galleriesApi.list).mockResolvedValue({
      data: [
        {
          id: 'gallery-1',
          eventId: 'event-1',
          name: 'Morning',
          slug: 'morning',
          description: null,
          sortOrder: 0,
          status: 'ACTIVE',
          currentUserRole: 'MANAGER',
          createdAt: '',
          updatedAt: '',
        },
      ],
    } as never)
    vi.mocked(galleriesApi.settings).mockResolvedValue({
      data: {
        publicViewEnabled: true,
        uploadEnabled: true,
        downloadEnabled: false,
        moderationMode: 'REQUIRED',
        accessTokenConfigured: true,
        accessCodeConfigured: true,
        publishedAt: null,
        expiresAt: null,
        version: 0,
      },
    } as never)
    renderManager({ ...event, currentUserRole: 'MANAGER' })

    await screen.findByText('Morning')
    fireEvent.click(screen.getByRole('button', { name: 'View access settings' }))
    expect(await screen.findByText('Guest view on')).toBeInTheDocument()
    expect(screen.getByText(/Only the event owner/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Save access settings' })).not.toBeInTheDocument()
  })
})
