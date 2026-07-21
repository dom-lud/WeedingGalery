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
    fireEvent.click(screen.getByRole('button', { name: 'Upload pending files' }))

    expect(await screen.findByText('1 uploaded, 1 failed.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument()
    expect(uploadApi.uploadFile).toHaveBeenCalledTimes(2)
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
    expect(screen.getByRole('button', { name: 'Upload pending files' })).toBeDisabled()
    expect(uploadApi.createSession).not.toHaveBeenCalled()
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
    fireEvent.click(screen.getByRole('button', { name: 'Upload pending files' }))

    fireEvent.click(await screen.findByRole('button', { name: 'Cancel upload' }))

    await waitFor(() => expect(uploadApi.cancel).toHaveBeenCalledWith('reception', 'session-1'))
    expect(await screen.findByText('Cancelled')).toBeInTheDocument()
    expect(screen.getByText('0 uploaded.')).toBeInTheDocument()
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
    expect(screen.getByRole('button', { name: 'Upload pending files' })).toBeDisabled()
    online = true
    window.dispatchEvent(new Event('online'))
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'Upload pending files' })).toBeEnabled(),
    )
    fireEvent.click(screen.getByRole('button', { name: 'Upload pending files' }))
    const retry = await screen.findByRole('button', { name: 'Retry' })
    fireEvent.click(retry)
    fireEvent.click(screen.getByRole('button', { name: 'Upload pending files' }))
    expect(await screen.findByText('1 uploaded.')).toBeInTheDocument()
    expect(uploadApi.createSession).toHaveBeenCalledOnce()
    expect(uploadApi.uploadFile).toHaveBeenCalledTimes(2)

    fireEvent.click(screen.getByRole('button', { name: 'Start another batch' }))
    expect(screen.getByText('Your upload queue is empty.')).toBeInTheDocument()
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
    fireEvent.click(screen.getByRole('button', { name: 'Upload pending files' }))
    fireEvent.click(await screen.findByRole('button', { name: 'Cancel upload' }))

    expect(await screen.findByText('Cancellation service unavailable.')).toBeInTheDocument()
    expect(screen.getByText('Cancelled')).toBeInTheDocument()
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
