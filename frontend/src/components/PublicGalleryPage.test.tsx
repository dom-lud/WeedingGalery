import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { publicAccessApi } from '../publicAccessApi'
import { uploadApi } from '../uploadApi'
import { appTheme } from '../theme'
import PublicGalleryPage from './PublicGalleryPage'

vi.mock('../publicAccessApi', () => ({
  publicAccessApi: { access: vi.fn(), get: vi.fn() },
}))

vi.mock('../uploadApi', () => ({
  uploadApi: {
    createSession: vi.fn(),
    getSession: vi.fn(),
    uploadFile: vi.fn(),
    cancel: vi.fn(),
  },
}))

const gallery = {
  slug: 'reception',
  name: 'Reception gallery',
  description: 'Share the evening with us.',
  uploadEnabled: true,
  downloadEnabled: false,
  moderationMode: 'REQUIRED' as const,
  publishedAt: null,
  expiresAt: null,
  media: [],
}

function renderPage() {
  return render(
    <ThemeProvider theme={appTheme}>
      <MemoryRouter initialEntries={['/g/reception']}>
        <Routes>
          <Route path="/g/:slug" element={<PublicGalleryPage />} />
        </Routes>
      </MemoryRouter>
    </ThemeProvider>,
  )
}

describe('PublicGalleryPage', () => {
  afterEach(() => cleanup())

  beforeEach(() => {
    vi.clearAllMocks()
    sessionStorage.clear()
    window.history.replaceState(null, '', '/g/reception#token=private-token')
    vi.mocked(publicAccessApi.get).mockResolvedValue({ data: gallery } as never)
  })

  it('extracts the private token, removes it from the address and opens the gallery', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    renderPage()

    expect(await screen.findByRole('heading', { name: 'Reception gallery' })).toBeInTheDocument()
    expect(publicAccessApi.access).toHaveBeenCalledWith('reception', {
      accessToken: 'private-token',
    })
    expect(window.location.hash).toBe('')
    expect(sessionStorage.getItem('gallery-token:reception')).toBe('private-token')
  })

  it('asks for a code and keeps a wrong-code error actionable', async () => {
    vi.mocked(publicAccessApi.access)
      .mockRejectedValueOnce({
        response: { status: 401, data: { code: 'GALLERY_ACCESS_CODE_REQUIRED' } },
      })
      .mockRejectedValueOnce({ response: { status: 401, data: { code: 'GALLERY_ACCESS_DENIED' } } })
      .mockResolvedValueOnce({ data: gallery } as never)
    renderPage()

    const code = await screen.findByLabelText('Access code')
    fireEvent.change(code, { target: { value: 'wrong1' } })
    fireEvent.click(screen.getByRole('button', { name: 'Open gallery' }))
    expect(await screen.findByText('That access code is not valid.')).toBeInTheDocument()
    fireEvent.change(screen.getByLabelText('Access code'), { target: { value: 'secret1' } })
    fireEvent.click(screen.getByRole('button', { name: 'Open gallery' }))
    expect(await screen.findByRole('heading', { name: 'Reception gallery' })).toBeInTheDocument()
  })

  it('uploads valid files independently and exposes retry for a partial failure', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    vi.mocked(uploadApi.uploadFile)
      .mockImplementationOnce(async (_slug, _session, _id, _file, progress) => {
        progress(100)
        return { data: { status: 'STORED' } } as never
      })
      .mockRejectedValueOnce({
        response: { status: 503, data: { message: 'Storage is temporarily unavailable.' } },
      })
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, {
      target: {
        files: [
          new File(['photo'], 'first.jpg', { type: 'image/jpeg' }),
          new File(['photo'], 'second.jpg', { type: 'image/jpeg' }),
        ],
      },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))

    await waitFor(() => expect(screen.queryByText('first.jpg')).not.toBeInTheDocument())
    expect(screen.getByText('second.jpg')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument()
    expect(uploadApi.uploadFile).toHaveBeenCalledTimes(2)
  })

  it('shows a live blurred preview while adding and clears it after success', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    let uploadedId = ''
    vi.mocked(uploadApi.uploadFile).mockImplementationOnce(
      async (_slug, _session, id, _file, progress) => {
        uploadedId = id
        progress(100)
        return { data: { status: 'PROCESSING' } } as never
      },
    )
    let sessionPoll = 0
    vi.mocked(uploadApi.getSession).mockImplementation(async () => {
      sessionPoll += 1
      return {
        data: {
          id: 'session-1',
          status: 'COMPLETED',
          expiresAt: '2026-07-21T12:00:00Z',
          files: [
            {
              clientFileId: uploadedId,
              fileName: 'photo.jpg',
              size: 5,
              status: sessionPoll === 1 ? 'PROCESSING' : 'PROCESSED',
            },
          ],
        },
      } as never
    })
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, {
      target: { files: [new File(['photo'], 'photo.jpg', { type: 'image/jpeg' })] },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))

    expect(await screen.findByLabelText('Adding photo.jpg')).toBeInTheDocument()
    await waitFor(
      () => expect(screen.queryByLabelText('Adding photo.jpg')).not.toBeInTheDocument(),
      { timeout: 2500 },
    )
    expect(screen.queryByText('photo.jpg')).not.toBeInTheDocument()
    expect(screen.getByText('Your upload queue is empty.')).toBeInTheDocument()
  })

  it('hides technical backend processing failures after the upload is accepted', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    vi.mocked(uploadApi.uploadFile).mockImplementationOnce(
      async (_slug, _session, _id, _file, progress) => {
        progress(100)
        return {
          data: {
            status: 'PROCESSING_FAILED',
            errorCode: 'MEDIA_PROCESSING_UNSUPPORTED_FORMAT',
          },
        } as never
      },
    )
    vi.mocked(uploadApi.getSession).mockRejectedValue(new Error('poll unavailable'))
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, {
      target: { files: [new File(['photo'], 'photo.jpg', { type: 'image/jpeg' })] },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))

    await waitFor(() => expect(screen.queryByText('photo.jpg')).not.toBeInTheDocument())
    expect(screen.getByText('Your upload queue is empty.')).toBeInTheDocument()
    expect(screen.queryByText('MEDIA_PROCESSING_UNSUPPORTED_FORMAT')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Try again' })).not.toBeInTheDocument()
  })

  it('starts adding valid files automatically and resets the local queue after completion', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    vi.mocked(uploadApi.uploadFile).mockImplementationOnce(
      async (_slug, _session, _id, _file, progress) => {
        progress(100)
        return { data: { status: 'STORED' } } as never
      },
    )
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, {
      target: { files: [new File(['photo'], 'auto.jpg', { type: 'image/jpeg' })] },
    })

    expect(await screen.findByLabelText('Adding auto.jpg')).toBeInTheDocument()
    await waitFor(() => expect(uploadApi.uploadFile).toHaveBeenCalledTimes(1))
    await waitFor(() => expect(screen.queryByText('auto.jpg')).not.toBeInTheDocument())
    expect(screen.getByText('Your upload queue is empty.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Choose files' })).toBeEnabled()
  })

  it('reports upload session creation failures without changing selected files', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockRejectedValue({
      response: { data: { message: 'Upload sessions are temporarily unavailable.' } },
    })
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, {
      target: { files: [new File(['photo'], 'photo.jpg', { type: 'image/jpeg' })] },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))

    expect(
      await screen.findByText('Upload sessions are temporarily unavailable.'),
    ).toBeInTheDocument()
    expect(screen.getByText('photo.jpg')).toBeInTheDocument()
    expect(screen.getByText('0 added.')).toBeInTheDocument()
  })

  it('preserves backend processing statuses when cancelling remaining active uploads', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    vi.mocked(uploadApi.cancel).mockResolvedValue({ data: { status: 'CANCELLED' } } as never)
    vi.mocked(uploadApi.uploadFile)
      .mockImplementationOnce(async (_slug, _session, _id, _file, progress) => {
        progress(100)
        return { data: { status: 'PROCESSING' } } as never
      })
      .mockImplementationOnce(
        async (_slug, _session, _id, _file, _progress, signal) =>
          await new Promise((_, reject) => {
            signal?.addEventListener('abort', () => reject(new Error('aborted')))
          }),
      )
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, {
      target: {
        files: [
          new File(['photo'], 'ready.jpg', { type: 'image/jpeg' }),
          new File(['photo'], 'active.jpg', { type: 'image/jpeg' }),
        ],
      },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))
    expect(await screen.findByLabelText('Adding ready.jpg')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Cancel upload' }))

    expect(screen.getByText('active.jpg')).toBeInTheDocument()
    expect(screen.getByText('1 added, 1 getting ready.')).toBeInTheDocument()
  })

  it('shows a generic unavailable state for an invalid or private gallery', async () => {
    vi.mocked(publicAccessApi.access).mockRejectedValue({
      response: { status: 404, data: { code: 'PUBLIC_GALLERY_NOT_FOUND' } },
    })
    renderPage()
    expect(await screen.findByRole('heading', { name: 'Gallery unavailable' })).toBeInTheDocument()
    await waitFor(() =>
      expect(screen.getByText(/invalid, expired or no longer available/i)).toBeVisible(),
    )
  })

  it('shows a recoverable error when the access response has no gallery payload', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: null } as never)
    renderPage()

    expect(
      await screen.findByRole('heading', { name: 'Unable to open gallery' }),
    ).toBeInTheDocument()
    expect(
      screen.getByText('We could not open this gallery. Please try again.'),
    ).toBeInTheDocument()
  })

  it('shows a dedicated throttling state without leaking gallery details', async () => {
    vi.mocked(publicAccessApi.access).mockRejectedValue({
      response: { status: 429, data: { code: 'RATE_LIMIT_EXCEEDED' } },
    })
    renderPage()

    expect(await screen.findByRole('heading', { name: 'Please wait' })).toBeInTheDocument()
    expect(screen.getByText(/too many attempts/i)).toBeInTheDocument()
    expect(screen.queryByText(gallery.name)).not.toBeInTheDocument()
  })

  it('rejects unsupported files before creating an upload session', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, {
      target: { files: [new File(['<svg/>'], 'payload.svg', { type: 'image/svg+xml' })] },
    })

    expect(
      await screen.findByText('Only JPEG, PNG, WebP and MP4 files are supported.'),
    ).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add now' })).toBeDisabled()
    expect(uploadApi.createSession).not.toHaveBeenCalled()
  })

  it('allows removing a selected local file before an upload session exists', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement

    fireEvent.change(input, {
      target: { files: [new File(['photo'], 'remove-me.jpg', { type: 'image/jpeg' })] },
    })

    expect(await screen.findByText('remove-me.jpg')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Remove remove-me.jpg' }))
    expect(screen.queryByText('remove-me.jpg')).not.toBeInTheDocument()
    expect(screen.getByText('Your upload queue is empty.')).toBeInTheDocument()
    expect(uploadApi.createSession).not.toHaveBeenCalled()
  })

  it('does not retry an oversized local file into a ready upload state', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    const oversized = new File(['x'], 'too-large.jpg', { type: 'image/jpeg' })
    Object.defineProperty(oversized, 'size', { value: 25 * 1024 * 1024 + 1 })

    fireEvent.change(input, { target: { files: [oversized] } })

    expect(await screen.findByText('Image exceeds 25 MiB.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Try again' })).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add now' })).toBeDisabled()
    expect(uploadApi.createSession).not.toHaveBeenCalled()
  })

  it('renders already added public media for a guest with gallery access', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({
      data: {
        ...gallery,
        media: [
          {
            id: 'media-1',
            fileName: 'ceremony.jpg',
            mediaType: 'IMAGE',
            status: 'PROCESSED',
            size: 1048576,
            uploadedAt: '2026-07-21T12:00:00Z',
            thumbnailUrl: '/api/public/galleries/reception/media/media-1/thumbnail',
            contentUrl: '/api/public/galleries/reception/media/media-1/content',
          },
        ],
      },
    } as never)

    renderPage()

    expect(await screen.findByRole('heading', { name: 'Reception gallery' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Gallery' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Open ceremony.jpg' }))
    expect(screen.getByRole('img', { name: 'ceremony.jpg' })).toHaveAttribute(
      'src',
      '/api/public/galleries/reception/media/media-1/content',
    )
  })

  it('opens video media in a lightbox and closes it without leaving stale preview state', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({
      data: {
        ...gallery,
        media: [
          {
            id: 'media-video',
            fileName: 'first-dance.mp4',
            mediaType: 'VIDEO',
            status: 'PROCESSED',
            size: 2048,
            uploadedAt: '2026-07-21T12:05:00Z',
            thumbnailUrl: '/api/public/galleries/reception/media/media-video/thumbnail',
            contentUrl: '/api/public/galleries/reception/media/media-video/content',
          },
        ],
      },
    } as never)

    renderPage()

    expect(await screen.findByRole('heading', { name: 'Reception gallery' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Open first-dance.mp4' }))
    const videos = document.querySelectorAll('video')
    expect(videos).toHaveLength(2)
    expect(videos[1]).toHaveAttribute(
      'src',
      '/api/public/galleries/reception/media/media-video/content',
    )
    fireEvent.click(screen.getByRole('button', { name: 'Close' }))
    await waitFor(() => expect(screen.queryByText('first-dance.mp4')).not.toBeInTheDocument())
    expect(document.querySelectorAll('video')).toHaveLength(1)
  })

  it('caps a batch at fifty files and explains the limit', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    const files = Array.from(
      { length: 51 },
      (_, index) => new File(['x'], `photo-${index}.jpg`, { type: 'image/jpeg' }),
    )

    fireEvent.change(input, { target: { files } })

    expect(await screen.findByText('A session can contain at most 50 files.')).toBeInTheDocument()
    expect(screen.getByText('50 selected')).toBeInTheDocument()
  })

  it('aborts active requests, cancels the server session and preserves a clear status', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    vi.mocked(uploadApi.cancel).mockResolvedValue({ data: { status: 'CANCELLED' } } as never)
    vi.mocked(uploadApi.uploadFile).mockImplementation(
      async (_slug, _session, _id, _file, _progress, signal) =>
        await new Promise((_, reject) => {
          signal?.addEventListener('abort', () => reject(new Error('aborted')))
        }),
    )
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, {
      target: { files: [new File(['photo'], 'photo.jpg', { type: 'image/jpeg' })] },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))

    fireEvent.click(await screen.findByRole('button', { name: 'Cancel upload' }))

    await waitFor(() => expect(uploadApi.cancel).toHaveBeenCalledWith('reception', 'session-1'))
    expect(await screen.findByText('photo.jpg')).toBeInTheDocument()
    expect(screen.getByText('0 added.')).toBeInTheDocument()
  })

  it('opens a public gallery without a token and retries a transient initialization failure', async () => {
    window.history.replaceState(null, '', '/g/reception')
    vi.mocked(publicAccessApi.get)
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce({ data: gallery } as never)
    renderPage()

    expect(
      await screen.findByRole('heading', { name: 'Unable to open gallery' }),
    ).toBeInTheDocument()
    expect(screen.getByText('The request could not be completed.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Try again' }))
    expect(await screen.findByRole('heading', { name: 'Reception gallery' })).toBeInTheDocument()
    expect(publicAccessApi.get).toHaveBeenCalledTimes(2)
    expect(publicAccessApi.access).not.toHaveBeenCalled()
  })

  it('submits a boundary-length access code with Enter but blocks shorter values', async () => {
    vi.mocked(publicAccessApi.access)
      .mockRejectedValueOnce({
        response: { status: 401, data: { code: 'GALLERY_ACCESS_CODE_REQUIRED' } },
      })
      .mockResolvedValueOnce({ data: gallery } as never)
    renderPage()

    const code = await screen.findByLabelText('Access code')
    fireEvent.change(code, { target: { value: '12345' } })
    fireEvent.keyDown(code, { key: 'Enter' })
    expect(publicAccessApi.access).toHaveBeenCalledTimes(1)
    expect(screen.getByRole('button', { name: 'Open gallery' })).toBeDisabled()
    fireEvent.change(code, { target: { value: '123456' } })
    fireEvent.keyDown(code, { key: 'Enter' })
    expect(await screen.findByRole('heading', { name: 'Reception gallery' })).toBeInTheDocument()
    expect(publicAccessApi.access).toHaveBeenLastCalledWith('reception', {
      accessToken: 'private-token',
      accessCode: '123456',
    })
  })

  it('classifies empty, mismatched, oversized and over-total-limit selections before upload', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement

    const empty = new File([], 'empty.jpg', { type: 'image/jpeg' })
    const mismatch = new File(['x'], 'photo.png', { type: 'image/jpeg' })
    const oversized = new File(['x'], 'large.jpg', { type: 'image/jpeg' })
    Object.defineProperty(oversized, 'size', { value: 25 * 1024 * 1024 + 1 })
    fireEvent.change(input, { target: { files: [empty, mismatch, oversized] } })

    expect(await screen.findByText('Empty files cannot be uploaded.')).toBeInTheDocument()
    expect(
      screen.getByText('The file extension does not match its declared type.'),
    ).toBeInTheDocument()
    expect(screen.getByText('Image exceeds 25 MiB.')).toBeInTheDocument()
    expect(uploadApi.createSession).not.toHaveBeenCalled()

    const enormous = new File(['x'], 'video.mp4', { type: 'video/mp4' })
    Object.defineProperty(enormous, 'size', { value: 2 * 1024 * 1024 * 1024 + 1 })
    fireEvent.change(input, { target: { files: [enormous] } })
    expect(await screen.findByText('A batch cannot exceed 2 GiB in total.')).toBeInTheDocument()
  })

  it('supports drag and drop, reacts to offline state and retries a failed file in the same session', async () => {
    let online = true
    Object.defineProperty(navigator, 'onLine', { configurable: true, get: () => online })
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    vi.mocked(uploadApi.uploadFile)
      .mockRejectedValueOnce(new Error('temporary'))
      .mockImplementationOnce(async (_slug, _session, _id, _file, progress) => {
        progress(100)
        return { data: { status: 'STORED' } } as never
      })
    renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const dropZone = screen.getByText('Add moments from your device').closest('.MuiPaper-root')!
    const file = new File(['photo'], 'drop.jpg', { type: 'image/jpeg' })
    fireEvent.dragEnter(dropZone, { dataTransfer: { files: [file] } })
    fireEvent.dragOver(dropZone, { dataTransfer: { files: [file] } })
    fireEvent.dragLeave(dropZone)
    fireEvent.drop(dropZone, { dataTransfer: { files: [file] } })
    expect(await screen.findByText('drop.jpg')).toBeInTheDocument()

    online = false
    window.dispatchEvent(new Event('offline'))
    expect(await screen.findByText(/You are offline/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add now' })).toBeDisabled()
    online = true
    window.dispatchEvent(new Event('online'))
    await waitFor(() => expect(screen.getByRole('button', { name: 'Add now' })).toBeEnabled())
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))
    const retry = await screen.findByRole('button', { name: 'Try again' })
    fireEvent.click(retry)
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))
    await waitFor(() => expect(screen.queryByText('drop.jpg')).not.toBeInTheDocument())
    expect(screen.getByText('Your upload queue is empty.')).toBeInTheDocument()
    expect(uploadApi.createSession).toHaveBeenCalledOnce()
    expect(uploadApi.uploadFile).toHaveBeenCalledTimes(2)
  })

  it('reports a server cancellation failure while still cancelling local work', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({ data: gallery } as never)
    vi.mocked(uploadApi.createSession).mockResolvedValue({ data: { id: 'session-1' } } as never)
    vi.mocked(uploadApi.cancel).mockRejectedValue({
      response: { data: { message: 'Cancellation service unavailable.' } },
    })
    vi.mocked(uploadApi.uploadFile).mockImplementation(
      async (_slug, _session, _id, _file, _progress, signal) =>
        await new Promise((_, reject) => {
          signal?.addEventListener('abort', () => reject(new Error('aborted')))
        }),
    )
    const view = renderPage()
    await screen.findByRole('heading', { name: 'Reception gallery' })
    const input = view.container.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, {
      target: { files: [new File(['photo'], 'photo.jpg', { type: 'image/jpeg' })] },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Add now' }))
    fireEvent.click(await screen.findByRole('button', { name: 'Cancel upload' }))

    expect(await screen.findByText('Cancellation service unavailable.')).toBeInTheDocument()
    expect(screen.getByText('photo.jpg')).toBeInTheDocument()
  })

  it('shows a gallery with uploads disabled and fallback description without queue controls', async () => {
    vi.mocked(publicAccessApi.access).mockResolvedValue({
      data: { ...gallery, description: '', uploadEnabled: false, moderationMode: 'NONE' },
    } as never)
    renderPage()

    expect(await screen.findByText('Uploads are not enabled for this gallery.')).toBeInTheDocument()
    expect(screen.getByText(/Share the moments you captured/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Choose files' })).not.toBeInTheDocument()
  })
})
