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
})
