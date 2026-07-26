import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { afterEach, describe, expect, it, beforeEach, vi } from 'vitest'
import type { GalleryData } from '../galleriesApi'
import { customizationApi, type GalleryCustomization } from '../customizationApi'
import { appTheme } from '../theme'
import AppearancePanel from './AppearancePanel'

vi.mock('../customizationApi', async () => {
  const actual = await vi.importActual<typeof import('../customizationApi')>('../customizationApi')
  return { ...actual, customizationApi: { get: vi.fn(), update: vi.fn() } }
})

const gallery: GalleryData = {
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

const customization: GalleryCustomization = {
  theme: 'EDITORIAL',
  layout: 'GRID',
  primaryColor: '#74465A',
  accentColor: '#B47B4C',
  backgroundColor: '#F7F4F2',
  welcomeText: 'Welcome to the gallery',
  showTitle: true,
  showUpload: true,
  showDownload: false,
  version: 4,
}

function renderPanel(initialReadOnly = false) {
  return render(
    <ThemeProvider theme={appTheme}>
      <AppearancePanel gallery={gallery} open initialReadOnly={initialReadOnly} onClose={vi.fn()} />
    </ThemeProvider>,
  )
}

describe('AppearancePanel', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(cleanup)

  it('loads allowlisted values, renders a safe text preview and saves owner changes with version', async () => {
    vi.mocked(customizationApi.get).mockResolvedValue({ data: customization } as never)
    vi.mocked(customizationApi.update).mockImplementation(
      async (_id, payload) =>
        ({
          data: { ...payload, version: 5 },
        }) as never,
    )
    renderPanel()

    expect(await screen.findByRole('dialog')).toBeVisible()
    fireEvent.change(await screen.findByRole('textbox', { name: 'Welcome text' }), {
      target: { value: '<img src=x onerror=alert(1)>' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Save appearance' }))

    await waitFor(() =>
      expect(customizationApi.update).toHaveBeenCalledWith('gallery-1', {
        ...customization,
        welcomeText: '<img src=x onerror=alert(1)>',
      }),
    )
    expect(screen.getByRole('textbox', { name: 'Welcome text' })).toHaveValue(
      '<img src=x onerror=alert(1)>',
    )
    expect(screen.queryByRole('img')).not.toBeInTheDocument()
  })

  it('normalizes unknown API options and enforces the textarea limit', async () => {
    vi.mocked(customizationApi.get).mockResolvedValue({
      data: { ...customization, theme: 'CUSTOM', layout: 'FREEFORM', primaryColor: 'red' },
    } as never)
    renderPanel()

    expect(await screen.findByRole('combobox', { name: 'Theme' })).toHaveTextContent('Editorial')
    expect(screen.getByRole('combobox', { name: 'Layout' })).toHaveTextContent('Grid')
    const welcome = screen.getByRole('textbox', { name: 'Welcome text' })
    fireEvent.change(welcome, { target: { value: 'x'.repeat(501) } })
    expect(welcome).toHaveValue('x'.repeat(501))
    expect(screen.getByText('501/500')).toBeInTheDocument()
  })

  it('falls back to read-only and reloads when a manager cannot save', async () => {
    vi.mocked(customizationApi.get).mockResolvedValue({ data: customization } as never)
    vi.mocked(customizationApi.update).mockRejectedValue({ response: { status: 403 } })
    renderPanel()

    await screen.findByRole('textbox', { name: 'Welcome text' })
    fireEvent.click(screen.getByRole('button', { name: 'Save appearance' }))

    await waitFor(() => expect(customizationApi.update).toHaveBeenCalled())
    expect(await screen.findByText(/Only the gallery owner can save/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Save appearance' })).not.toBeInTheDocument()
    expect(customizationApi.get).toHaveBeenCalledTimes(2)
  })

  it('shows retry for load errors and an explicit conflict state', async () => {
    vi.mocked(customizationApi.get).mockRejectedValueOnce(new Error('offline'))
    renderPanel()
    expect(await screen.findByText('Appearance settings could not be loaded.')).toBeInTheDocument()
    vi.mocked(customizationApi.get).mockResolvedValue({ data: customization } as never)
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }))
    await screen.findByRole('textbox', { name: 'Welcome text' })

    vi.mocked(customizationApi.update).mockRejectedValue({ response: { status: 409 } })
    fireEvent.click(screen.getByRole('button', { name: 'Save appearance' }))
    expect(await screen.findByText(/changed elsewhere/)).toBeInTheDocument()
  })

  it('keeps the form keyboard-addressable and has labelled controls in read-only mode', async () => {
    vi.mocked(customizationApi.get).mockResolvedValue({ data: customization } as never)
    renderPanel(true)
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByRole('combobox', { name: 'Theme' })).toHaveAttribute(
      'aria-disabled',
      'true',
    )
    expect(within(dialog).getByRole('combobox', { name: 'Layout' })).toHaveAttribute(
      'aria-disabled',
      'true',
    )
    expect(within(dialog).getByRole('textbox', { name: 'Welcome text' })).toBeDisabled()
    expect(within(dialog).getByRole('button', { name: 'Close' })).toBeVisible()
  })

  it('shows a server message for a non-conflict save failure and invokes onSaved on success', async () => {
    const onSaved = vi.fn()
    vi.mocked(customizationApi.get).mockResolvedValue({ data: customization } as never)
    vi.mocked(customizationApi.update)
      .mockRejectedValueOnce({ response: { data: { message: 'Save failed.' } } })
      .mockResolvedValueOnce({ data: customization } as never)
    const view = render(
      <ThemeProvider theme={appTheme}>
        <AppearancePanel gallery={gallery} open onSaved={onSaved} onClose={vi.fn()} />
      </ThemeProvider>,
    )
    await screen.findByRole('textbox', { name: 'Welcome text' })
    fireEvent.click(screen.getByRole('button', { name: 'Save appearance' }))
    expect(await screen.findByText('Save failed.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Save appearance' }))
    await waitFor(() => expect(onSaved).toHaveBeenCalledTimes(1))
    view.unmount()
  })

  it('uses safe defaults for missing values and renders the initial loading state', async () => {
    vi.mocked(customizationApi.get).mockResolvedValue({ data: {} } as never)
    renderPanel()
    expect(await screen.findByRole('combobox', { name: 'Theme' })).toHaveTextContent('Editorial')
    expect(screen.getByRole('switch', { name: 'Show title' })).toBeChecked()
    expect(screen.getByRole('switch', { name: 'Show upload' })).toBeChecked()
    expect(screen.getByRole('switch', { name: 'Show download' })).not.toBeChecked()
  })
})
