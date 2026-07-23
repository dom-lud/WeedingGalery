import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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
    media: vi.fn(),
    downloadUrl: vi.fn(
      (eventId: string, galleryId: string) =>
        `/api/events/${eventId}/galleries/${galleryId}/download`,
    ),
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
  afterEach(cleanup)

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
    const menuTrigger = screen.getByRole('button', { name: 'More actions for Morning' })
    expect(menuTrigger).toHaveAttribute('aria-haspopup', 'menu')
    fireEvent.click(menuTrigger)
    expect(menuTrigger).toHaveAttribute('aria-expanded', 'true')
    expect(menuTrigger).toHaveAttribute('aria-controls', 'gallery-actions-menu-gallery-1')
    expect(screen.getByRole('menuitem', { name: 'Edit gallery' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Archive gallery' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Delete gallery' })).not.toBeInTheDocument()
  })

  it('requires explicit confirmation before deleting a gallery', async () => {
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
    vi.mocked(galleriesApi.remove).mockResolvedValue({} as never)
    renderManager()

    await screen.findByText('Reception')
    const menuTrigger = screen.getByRole('button', { name: 'More actions for Reception' })
    fireEvent.click(menuTrigger)
    fireEvent.click(screen.getByRole('menuitem', { name: 'Delete gallery' }))
    expect(galleriesApi.remove).not.toHaveBeenCalled()

    let confirmation = screen.getByRole('dialog', { name: 'Delete gallery?' })
    fireEvent.click(within(confirmation).getByRole('button', { name: 'Cancel' }))
    await waitFor(() => expect(menuTrigger).toHaveFocus())

    fireEvent.click(menuTrigger)
    fireEvent.click(screen.getByRole('menuitem', { name: 'Delete gallery' }))
    confirmation = screen.getByRole('dialog', { name: 'Delete gallery?' })
    fireEvent.click(within(confirmation).getByRole('button', { name: 'Delete gallery' }))
    await waitFor(() => expect(galleriesApi.remove).toHaveBeenCalledWith('event-1', 'gallery-1'))
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'Create gallery' })).toHaveFocus(),
    )
  })

  it('restores focus after an edit even when the gallery refresh is delayed', async () => {
    const originalGallery = {
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
    }
    let resolveReload!: (value: { data: Array<typeof originalGallery> }) => void
    const delayedReload = new Promise<{ data: Array<typeof originalGallery> }>((resolve) => {
      resolveReload = resolve
    })
    vi.mocked(galleriesApi.list)
      .mockResolvedValueOnce({ data: [originalGallery] } as never)
      .mockReturnValueOnce(delayedReload as never)
    vi.mocked(galleriesApi.update).mockResolvedValue({ data: {} } as never)
    renderManager()

    await screen.findByText('Reception')
    fireEvent.click(screen.getByRole('button', { name: 'More actions for Reception' }))
    fireEvent.click(screen.getByRole('menuitem', { name: 'Edit gallery' }))
    fireEvent.change(screen.getByRole('textbox', { name: /Gallery name/ }), {
      target: { value: 'Reception edited' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Save gallery' }))

    await waitFor(() => expect(galleriesApi.update).toHaveBeenCalled())
    expect(screen.getByRole('dialog', { name: 'Edit gallery' })).toBeVisible()
    resolveReload({ data: [{ ...originalGallery, name: 'Reception edited' }] })

    await waitFor(() =>
      expect(
        screen.getByRole('button', { name: 'More actions for Reception edited' }),
      ).toHaveFocus(),
    )
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

  it('lets an owner preview gallery media and download the gallery archive', async () => {
    const activeGallery = {
      id: 'gallery-1',
      eventId: 'event-1',
      name: 'Reception',
      slug: 'reception',
      description: null,
      sortOrder: 0,
      status: 'ACTIVE' as const,
      currentUserRole: 'OWNER' as const,
      createdAt: '',
      updatedAt: '',
    }
    vi.mocked(galleriesApi.list).mockResolvedValue({ data: [activeGallery] } as never)
    vi.mocked(galleriesApi.media).mockResolvedValue({
      data: [
        {
          id: 'media-1',
          fileName: 'ceremony.jpg',
          mediaType: 'IMAGE',
          status: 'PROCESSING',
          size: 512,
          uploadedAt: '2026-07-21T12:00:00Z',
          thumbnailUrl: '/api/events/event-1/galleries/gallery-1/media/media-1/thumbnail',
          contentUrl: '/api/events/event-1/galleries/gallery-1/media/media-1/content',
        },
        {
          id: 'media-2',
          fileName: 'first-dance.mp4',
          mediaType: 'VIDEO',
          status: 'PROCESSED',
          size: 1024,
          uploadedAt: '2026-07-21T12:05:00Z',
          thumbnailUrl: '/api/events/event-1/galleries/gallery-1/media/media-2/thumbnail',
          contentUrl: '/api/events/event-1/galleries/gallery-1/media/media-2/content',
        },
      ],
    } as never)
    renderManager()

    await screen.findByText('Reception')
    fireEvent.click(screen.getByRole('button', { name: 'View gallery' }))
    await waitFor(() => expect(galleriesApi.media).toHaveBeenCalledWith('event-1', 'gallery-1'))

    const download = await screen.findByRole('link', { name: 'Download gallery' })
    expect(download).toHaveAttribute('href', '/api/events/event-1/galleries/gallery-1/download')
    expect(screen.getByRole('img', { name: 'ceremony.jpg' })).toHaveAttribute(
      'src',
      '/api/events/event-1/galleries/gallery-1/media/media-1/thumbnail',
    )

    fireEvent.click(screen.getByRole('button', { name: 'Open ceremony.jpg' }))
    expect(screen.getAllByRole('img', { name: 'ceremony.jpg' }).at(-1)).toHaveAttribute(
      'src',
      '/api/events/event-1/galleries/gallery-1/media/media-1/content',
    )
    fireEvent.click(screen.getByRole('button', { name: 'Close' }))
    await waitFor(() => expect(document.querySelectorAll('[role="dialog"]')).toHaveLength(1))

    fireEvent.click(screen.getByRole('button', { name: 'Open first-dance.mp4' }))
    const videos = document.querySelectorAll('video')
    expect(videos).toHaveLength(2)
    expect(videos[1]).toHaveAttribute(
      'src',
      '/api/events/event-1/galleries/gallery-1/media/media-2/content',
    )
  })

  it('updates publication, guest permissions and access-code lifecycle at their boundaries', async () => {
    const activeGallery = {
      id: 'gallery-1',
      eventId: 'event-1',
      name: 'Reception',
      slug: 'reception',
      description: 'Evening',
      sortOrder: 1,
      status: 'ACTIVE' as const,
      currentUserRole: 'OWNER' as const,
      createdAt: '',
      updatedAt: '',
    }
    const settings = {
      publicViewEnabled: true,
      uploadEnabled: true,
      downloadEnabled: false,
      moderationMode: 'REQUIRED' as const,
      accessTokenConfigured: false,
      accessCodeConfigured: true,
      publishedAt: null,
      expiresAt: null,
      version: 3,
    }
    vi.mocked(galleriesApi.list).mockResolvedValue({ data: [activeGallery] } as never)
    vi.mocked(galleriesApi.settings).mockResolvedValue({ data: settings } as never)
    vi.mocked(galleriesApi.updateSettings).mockImplementation(
      async (_eventId, _galleryId, payload) =>
        ({
          data: { ...settings, ...payload, version: 4 },
        }) as never,
    )
    vi.mocked(galleriesApi.setAccessCode).mockResolvedValue({} as never)
    vi.mocked(galleriesApi.removeAccessCode).mockResolvedValue({} as never)
    renderManager()

    await screen.findByText('Reception')
    fireEvent.click(screen.getByRole('button', { name: 'Access settings' }))
    await screen.findByText('Guest view on')
    fireEvent.click(screen.getByRole('switch', { name: 'Enable guest view' }))
    expect(screen.getByRole('switch', { name: 'Allow guest uploads' })).toBeDisabled()
    fireEvent.click(screen.getByRole('switch', { name: 'Allow downloads later' }))
    fireEvent.change(screen.getByLabelText('Publish from'), {
      target: { value: '2026-09-10T12:00' },
    })
    fireEvent.change(screen.getByLabelText('Expire at'), {
      target: { value: '2026-09-10T11:59' },
    })
    expect(screen.getByText('Expiration must be later than publication.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Save access settings' })).toBeDisabled()
    fireEvent.change(screen.getByLabelText('Expire at'), {
      target: { value: '2026-09-10T12:01' },
    })
    fireEvent.mouseDown(screen.getByRole('combobox', { name: 'Moderation' }))
    fireEvent.click(screen.getByRole('option', { name: 'No review' }))
    fireEvent.change(screen.getByLabelText('Publish from'), {
      target: { value: '' },
    })
    fireEvent.change(screen.getByLabelText('Expire at'), {
      target: { value: '' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Save access settings' }))
    await waitFor(() =>
      expect(galleriesApi.updateSettings).toHaveBeenCalledWith(
        'event-1',
        'gallery-1',
        expect.objectContaining({
          publicViewEnabled: false,
          uploadEnabled: false,
          downloadEnabled: true,
          moderationMode: 'NONE',
          publishedAt: null,
          expiresAt: null,
          version: 3,
        }),
      ),
    )

    const codeInput = screen.getByLabelText('New access code')
    fireEvent.change(codeInput, { target: { value: 'short' } })
    expect(screen.getByRole('button', { name: 'Set code' })).toBeDisabled()
    fireEvent.change(codeInput, { target: { value: 'valid-code' } })
    fireEvent.click(screen.getByRole('button', { name: 'Set code' }))
    await waitFor(() =>
      expect(galleriesApi.setAccessCode).toHaveBeenCalledWith('event-1', 'gallery-1', 'valid-code'),
    )
    expect(codeInput).toHaveValue('')

    fireEvent.click(screen.getByRole('button', { name: 'Remove code' }))
    const confirmation = screen.getByRole('dialog', { name: 'Remove access code?' })
    fireEvent.click(within(confirmation).getByRole('button', { name: 'Remove code' }))
    await waitFor(() =>
      expect(galleriesApi.removeAccessCode).toHaveBeenCalledWith('event-1', 'gallery-1'),
    )
  })

  it('archives only after confirmation and surfaces lifecycle and settings failures', async () => {
    const activeGallery = {
      id: 'gallery-1',
      eventId: 'event-1',
      name: 'Reception',
      slug: 'reception',
      description: null,
      sortOrder: 0,
      status: 'ACTIVE' as const,
      currentUserRole: 'OWNER' as const,
      createdAt: '',
      updatedAt: '',
    }
    vi.mocked(galleriesApi.list).mockResolvedValue({ data: [activeGallery] } as never)
    vi.mocked(galleriesApi.archive).mockRejectedValue({
      response: { data: { message: 'Gallery cannot be archived.' } },
    })
    vi.mocked(galleriesApi.settings).mockRejectedValue(new Error('offline'))
    renderManager()

    await screen.findByText('Reception')
    fireEvent.click(screen.getByRole('button', { name: 'More actions for Reception' }))
    fireEvent.click(screen.getByRole('menuitem', { name: 'Archive gallery' }))
    let confirmation = screen.getByRole('dialog', { name: 'Archive gallery?' })
    fireEvent.click(within(confirmation).getByRole('button', { name: 'Cancel' }))
    expect(galleriesApi.archive).not.toHaveBeenCalled()
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    fireEvent.click(screen.getByRole('button', { name: 'More actions for Reception' }))
    fireEvent.click(screen.getByRole('menuitem', { name: 'Archive gallery' }))
    confirmation = screen.getByRole('dialog', { name: 'Archive gallery?' })
    fireEvent.click(within(confirmation).getByRole('button', { name: 'Archive gallery' }))
    expect(await screen.findByText('Gallery cannot be archived.')).toBeInTheDocument()
    fireEvent.click(within(confirmation).getByRole('button', { name: 'Cancel' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: 'Access settings' }))
    await waitFor(() => expect(galleriesApi.settings).toHaveBeenCalled())
    expect(await screen.findByText('Gallery operation failed.')).toBeInTheDocument()
  })

  it('copies a rotated link and gives a manual-copy fallback when clipboard rejects', async () => {
    const activeGallery = {
      id: 'gallery-1',
      eventId: 'event-1',
      name: 'Reception',
      slug: 'reception',
      description: null,
      sortOrder: 0,
      status: 'ACTIVE' as const,
      currentUserRole: 'OWNER' as const,
      createdAt: '',
      updatedAt: '',
    }
    const settings = {
      publicViewEnabled: true,
      uploadEnabled: true,
      downloadEnabled: false,
      moderationMode: 'NONE' as const,
      accessTokenConfigured: false,
      accessCodeConfigured: false,
      publishedAt: null,
      expiresAt: null,
      version: 0,
    }
    const writeText = vi
      .fn()
      .mockResolvedValueOnce(undefined)
      .mockRejectedValueOnce(new Error('denied'))
    Object.defineProperty(navigator, 'clipboard', { configurable: true, value: { writeText } })
    vi.mocked(galleriesApi.list).mockResolvedValue({ data: [activeGallery] } as never)
    vi.mocked(galleriesApi.settings).mockResolvedValue({ data: settings } as never)
    vi.mocked(galleriesApi.rotateAccessToken).mockResolvedValue({
      data: { accessToken: 'secret', sharePath: '/g/reception#token=secret' },
    } as never)
    renderManager()

    await screen.findByText('Reception')
    fireEvent.click(screen.getByRole('button', { name: 'Access settings' }))
    fireEvent.click(await screen.findByRole('button', { name: 'Rotate private share link' }))
    const copy = await screen.findByRole('button', { name: 'Copy link' })
    fireEvent.click(copy)
    expect(await screen.findByText('Private share link copied.')).toBeInTheDocument()
    fireEvent.click(copy)
    expect(
      await screen.findByText('The link could not be copied. Select and copy it manually.'),
    ).toBeInTheDocument()
  })

  it('keeps archived galleries and events read-only and closes dialogs without mutation', async () => {
    const archivedGallery = {
      id: 'gallery-1',
      eventId: 'event-1',
      name: 'Archive',
      slug: 'archive',
      description: 'Old',
      sortOrder: 4,
      status: 'ARCHIVED' as const,
      currentUserRole: 'OWNER' as const,
      createdAt: '',
      updatedAt: '',
    }
    vi.mocked(galleriesApi.list).mockResolvedValue({ data: [archivedGallery] } as never)
    vi.mocked(galleriesApi.settings).mockResolvedValue({
      data: {
        publicViewEnabled: false,
        uploadEnabled: false,
        downloadEnabled: false,
        moderationMode: 'REQUIRED',
        accessTokenConfigured: true,
        accessCodeConfigured: false,
        publishedAt: null,
        expiresAt: null,
        version: 1,
      },
    } as never)
    renderManager({ ...event, status: 'ARCHIVED' })

    await screen.findByText('Archive')
    expect(screen.getByRole('button', { name: 'Create gallery' })).toBeDisabled()
    fireEvent.click(screen.getByRole('button', { name: 'Access settings' }))
    expect(await screen.findByText(/read-only access settings/i)).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Close' }))
    expect(screen.queryByRole('dialog', { name: 'Access settings' })).not.toBeInTheDocument()
  })
})
