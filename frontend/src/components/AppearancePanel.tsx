import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  TextField,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material'
import type { GalleryData } from '../galleriesApi'
import {
  APPEARANCE_TEXT_LIMIT,
  customizationApi,
  type AppearanceLayout,
  type AppearanceTheme,
  type GalleryCustomization,
} from '../customizationApi'

const DEFAULT_CUSTOMIZATION: GalleryCustomization = {
  theme: 'EDITORIAL',
  layout: 'GRID',
  primaryColor: '#74465A',
  accentColor: '#B47B4C',
  backgroundColor: '#F7F4F2',
  welcomeText: '',
  showTitle: true,
  showUpload: true,
  showDownload: false,
  version: 0,
}

const themes: Array<{ value: AppearanceTheme; label: string }> = [
  { value: 'EDITORIAL', label: 'Editorial' },
  { value: 'MINIMAL', label: 'Minimal' },
  { value: 'ROMANTIC', label: 'Romantic' },
]

const layouts: Array<{ value: AppearanceLayout; label: string }> = [
  { value: 'GRID', label: 'Grid' },
  { value: 'MASONRY', label: 'Masonry' },
  { value: 'TIMELINE', label: 'Timeline' },
]

const isHexColor = (value: unknown): value is string =>
  typeof value === 'string' && /^#[0-9A-Fa-f]{6}$/.test(value)

const isTheme = (value: unknown): value is AppearanceTheme =>
  themes.some((option) => option.value === value)

const isLayout = (value: unknown): value is AppearanceLayout =>
  layouts.some((option) => option.value === value)

function normalizeCustomization(value: Partial<GalleryCustomization>): GalleryCustomization {
  return {
    theme: isTheme(value.theme) ? value.theme : DEFAULT_CUSTOMIZATION.theme,
    layout: isLayout(value.layout) ? value.layout : DEFAULT_CUSTOMIZATION.layout,
    primaryColor: isHexColor(value.primaryColor)
      ? value.primaryColor
      : DEFAULT_CUSTOMIZATION.primaryColor,
    accentColor: isHexColor(value.accentColor)
      ? value.accentColor
      : DEFAULT_CUSTOMIZATION.accentColor,
    backgroundColor: isHexColor(value.backgroundColor)
      ? value.backgroundColor
      : DEFAULT_CUSTOMIZATION.backgroundColor,
    welcomeText:
      typeof value.welcomeText === 'string'
        ? value.welcomeText.slice(0, APPEARANCE_TEXT_LIMIT)
        : DEFAULT_CUSTOMIZATION.welcomeText,
    showTitle: typeof value.showTitle === 'boolean' ? value.showTitle : true,
    showUpload: typeof value.showUpload === 'boolean' ? value.showUpload : true,
    showDownload: typeof value.showDownload === 'boolean' ? value.showDownload : false,
    version:
      typeof value.version === 'number' && Number.isInteger(value.version) ? value.version : 0,
  }
}

function errorStatus(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    return (error as { response?: { status?: number } }).response?.status
  }
  return undefined
}

function errorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message ?? 'Appearance settings could not be loaded.'
  }
  return 'Appearance settings could not be loaded.'
}

interface AppearancePanelProps {
  gallery: GalleryData | null
  open: boolean
  initialReadOnly?: boolean
  onClose: () => void
  onSaved?: () => void
}

export default function AppearancePanel({
  gallery,
  open,
  initialReadOnly = false,
  onClose,
  onSaved,
}: AppearancePanelProps) {
  const theme = useTheme()
  const fullScreen = useMediaQuery(theme.breakpoints.down('sm'))
  const [form, setForm] = useState<GalleryCustomization>(DEFAULT_CUSTOMIZATION)
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [readOnly, setReadOnly] = useState(initialReadOnly)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const load = async () => {
    if (!gallery) return
    setLoading(true)
    setError(null)
    try {
      const response = await customizationApi.get(gallery.id)
      setForm(normalizeCustomization(response.data))
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!open || !gallery) return
    // The effect resets transient dialog state when a different gallery is opened.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setReadOnly(initialReadOnly)
    setMessage(null)
    void load()
    // load is intentionally scoped to the opened gallery.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [gallery?.id, initialReadOnly, open])

  const update = <K extends keyof GalleryCustomization>(key: K, value: GalleryCustomization[K]) =>
    setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    if (!gallery || readOnly || saving) return
    setSaving(true)
    setError(null)
    try {
      const response = await customizationApi.update(gallery.id, form)
      setForm(normalizeCustomization(response.data))
      setMessage('Appearance saved.')
      onSaved?.()
    } catch (requestError) {
      const status = errorStatus(requestError)
      if (status === 403) {
        setReadOnly(true)
        setMessage(
          'Only the gallery owner can save appearance settings. Showing the latest values.',
        )
        await load()
      } else if (status === 409) {
        setError('These appearance settings changed elsewhere. Reload before saving again.')
      } else {
        setError(errorMessage(requestError))
      }
    } finally {
      setSaving(false)
    }
  }

  const close = () => {
    if (!saving) onClose()
  }

  return (
    <Dialog
      open={open && gallery !== null}
      onClose={close}
      fullScreen={fullScreen}
      fullWidth
      maxWidth="md"
    >
      <DialogTitle>{readOnly ? 'Gallery appearance' : 'Customize gallery appearance'}</DialogTitle>
      <DialogContent>
        {loading ? (
          <Stack sx={{ py: 4, alignItems: 'center' }}>
            <CircularProgress aria-label="Loading appearance settings" />
          </Stack>
        ) : (
          <Stack spacing={2.5} sx={{ pt: 1 }}>
            {error && (
              <Alert
                severity="error"
                action={
                  <Button color="inherit" size="small" onClick={() => void load()}>
                    Retry
                  </Button>
                }
              >
                {error}
              </Alert>
            )}
            {message && <Alert severity="info">{message}</Alert>}
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              <FormControl fullWidth>
                <InputLabel id="appearance-theme-label">Theme</InputLabel>
                <Select
                  labelId="appearance-theme-label"
                  label="Theme"
                  value={form.theme}
                  disabled={readOnly}
                  onChange={(event) => update('theme', event.target.value as AppearanceTheme)}
                >
                  {themes.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <FormControl fullWidth>
                <InputLabel id="appearance-layout-label">Layout</InputLabel>
                <Select
                  labelId="appearance-layout-label"
                  label="Layout"
                  value={form.layout}
                  disabled={readOnly}
                  onChange={(event) => update('layout', event.target.value as AppearanceLayout)}
                >
                  {layouts.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Stack>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
              {(['primaryColor', 'accentColor', 'backgroundColor'] as const).map((key) => (
                <TextField
                  key={key}
                  label={
                    key === 'primaryColor'
                      ? 'Primary color'
                      : key === 'accentColor'
                        ? 'Accent color'
                        : 'Background color'
                  }
                  type="color"
                  value={form[key]}
                  disabled={readOnly}
                  onChange={(event) => update(key, event.target.value)}
                  slotProps={{ htmlInput: { 'aria-label': key } }}
                />
              ))}
            </Stack>
            <TextField
              label="Welcome text"
              multiline
              minRows={3}
              value={form.welcomeText}
              disabled={readOnly}
              helperText={`${form.welcomeText.length}/${APPEARANCE_TEXT_LIMIT}`}
              slotProps={{ htmlInput: { maxLength: APPEARANCE_TEXT_LIMIT } }}
              onChange={(event) => update('welcomeText', event.target.value)}
            />
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
              <FormControlLabel
                control={
                  <Switch
                    checked={form.showTitle}
                    disabled={readOnly}
                    onChange={(event) => update('showTitle', event.target.checked)}
                  />
                }
                label="Show title"
              />
              <FormControlLabel
                control={
                  <Switch
                    checked={form.showUpload}
                    disabled={readOnly}
                    onChange={(event) => update('showUpload', event.target.checked)}
                  />
                }
                label="Show upload"
              />
              <FormControlLabel
                control={
                  <Switch
                    checked={form.showDownload}
                    disabled={readOnly}
                    onChange={(event) => update('showDownload', event.target.checked)}
                  />
                }
                label="Show download"
              />
            </Stack>
            <Stack
              component="section"
              aria-label="Layout preview"
              spacing={1}
              sx={{
                p: 2,
                border: 1,
                borderColor: 'divider',
                borderRadius: 1,
                bgcolor: form.backgroundColor,
                color: form.primaryColor,
                minWidth: 0,
              }}
            >
              {form.showTitle && (
                <Typography variant="h6" component="h3" sx={{ overflowWrap: 'anywhere' }}>
                  {gallery?.name}
                </Typography>
              )}
              {form.welcomeText && (
                <Typography sx={{ overflowWrap: 'anywhere' }}>{form.welcomeText}</Typography>
              )}
              <Stack
                direction={form.layout === 'TIMELINE' ? 'column' : 'row'}
                spacing={1}
                sx={{
                  flexWrap: 'wrap',
                  '& > *': {
                    flex: form.layout === 'MASONRY' ? '1 1 30%' : '1 1 0',
                    minWidth: 56,
                    height: form.layout === 'MASONRY' ? 72 : 56,
                    bgcolor: form.accentColor,
                    borderRadius: 0.5,
                  },
                }}
              >
                <span aria-hidden="true" />
                <span aria-hidden="true" />
                <span aria-hidden="true" />
              </Stack>
              <Typography variant="caption">Preview: {form.layout.toLowerCase()}</Typography>
            </Stack>
          </Stack>
        )}
      </DialogContent>
      <DialogActions sx={{ p: 2, flexDirection: { xs: 'column-reverse', sm: 'row' } }}>
        <Button onClick={close} disabled={saving} sx={{ width: { xs: '100%', sm: 'auto' } }}>
          Close
        </Button>
        {!readOnly && (
          <Button
            variant="contained"
            onClick={() => void save()}
            disabled={loading || saving}
            aria-busy={saving}
            sx={{ width: { xs: '100%', sm: 'auto' } }}
          >
            {saving ? 'Saving...' : 'Save appearance'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  )
}
