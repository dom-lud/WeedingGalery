import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
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
} from '@mui/material'
import type { EventData } from '../eventsApi'
import {
  galleriesApi,
  type GalleryData,
  type GallerySettings,
  type GallerySettingsPayload,
  type GalleryWritePayload,
} from '../galleriesApi'

const emptyPayload: GalleryWritePayload = { name: '', description: null, sortOrder: 0 }

function errorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message ?? 'Gallery operation failed.'
  }
  return 'Gallery operation failed.'
}

interface GalleryManagerProps {
  event: EventData
}

export default function GalleryManager({ event }: GalleryManagerProps) {
  const [galleries, setGalleries] = useState<GalleryData[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<GalleryData | null>(null)
  const [form, setForm] = useState<GalleryWritePayload>(emptyPayload)
  const [settingsGallery, setSettingsGallery] = useState<GalleryData | null>(null)
  const [settings, setSettings] = useState<GallerySettings | null>(null)
  const [settingsLoading, setSettingsLoading] = useState(false)
  const [rotatedSharePath, setRotatedSharePath] = useState('')
  const [accessCode, setAccessCode] = useState('')
  const validAccessCode = /^[\x20-\x7e]{6,64}$/.test(accessCode)
  const canEditAccessSettings =
    event.currentUserRole === 'OWNER' &&
    event.status !== 'ARCHIVED' &&
    settingsGallery?.status === 'ACTIVE'
  const invalidPublicationWindow = Boolean(
    settings?.publishedAt &&
    settings?.expiresAt &&
    new Date(settings.expiresAt) <= new Date(settings.publishedAt),
  )

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await galleriesApi.list(event.id)
      setGalleries(response.data)
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [event.id])

  const openCreate = () => {
    setEditing(null)
    setForm(emptyPayload)
    setDialogOpen(true)
  }

  const openEdit = (gallery: GalleryData) => {
    setEditing(gallery)
    setForm({
      name: gallery.name,
      description: gallery.description,
      sortOrder: gallery.sortOrder,
    })
    setDialogOpen(true)
  }

  const save = async () => {
    try {
      if (editing) {
        await galleriesApi.update(event.id, editing.id, form)
        setMessage('Gallery updated.')
      } else {
        await galleriesApi.create(event.id, form)
        setMessage('Gallery created.')
      }
      setDialogOpen(false)
      await load()
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const archive = async (galleryId: string) => {
    try {
      await galleriesApi.archive(event.id, galleryId)
      setMessage('Gallery archived.')
      await load()
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const remove = async (galleryId: string) => {
    try {
      await galleriesApi.remove(event.id, galleryId)
      setMessage('Gallery deleted.')
      await load()
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const openSettings = async (gallery: GalleryData) => {
    setSettingsGallery(gallery)
    setSettings(null)
    setSettingsLoading(true)
    setRotatedSharePath('')
    setAccessCode('')
    try {
      const response = await galleriesApi.settings(event.id, gallery.id)
      setSettings(response.data)
    } catch (requestError) {
      setError(errorMessage(requestError))
      setSettingsGallery(null)
    } finally {
      setSettingsLoading(false)
    }
  }

  const saveSettings = async () => {
    if (!settingsGallery || !settings) return
    const payload: GallerySettingsPayload = {
      publicViewEnabled: settings.publicViewEnabled,
      uploadEnabled: settings.uploadEnabled,
      downloadEnabled: settings.downloadEnabled,
      moderationMode: settings.moderationMode,
      publishedAt: settings.publishedAt,
      expiresAt: settings.expiresAt,
      version: settings.version,
    }
    try {
      const response = await galleriesApi.updateSettings(event.id, settingsGallery.id, payload)
      setSettings(response.data)
      setMessage('Gallery access settings updated.')
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const rotateToken = async () => {
    if (!settingsGallery) return
    try {
      const response = await galleriesApi.rotateAccessToken(event.id, settingsGallery.id)
      setRotatedSharePath(response.data.sharePath)
      setSettings((current) => (current ? { ...current, accessTokenConfigured: true } : current))
      setMessage('A new private share link was generated. Copy it now.')
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const copyShareLink = async () => {
    try {
      await navigator.clipboard.writeText(`${window.location.origin}${rotatedSharePath}`)
      setMessage('Private share link copied.')
    } catch {
      setError('The link could not be copied. Select and copy it manually.')
    }
  }

  const saveAccessCode = async () => {
    if (!settingsGallery) return
    try {
      await galleriesApi.setAccessCode(event.id, settingsGallery.id, accessCode)
      setSettings((current) => (current ? { ...current, accessCodeConfigured: true } : current))
      setAccessCode('')
      setMessage('Access code updated.')
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const removeAccessCode = async () => {
    if (!settingsGallery) return
    try {
      await galleriesApi.removeAccessCode(event.id, settingsGallery.id)
      setSettings((current) => (current ? { ...current, accessCodeConfigured: false } : current))
      setMessage('Access code removed.')
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  return (
    <Stack spacing={2} aria-label={`Galleries for ${event.name}`}>
      <Stack
        direction="row"
        spacing={1}
        sx={{ justifyContent: 'space-between', alignItems: 'center' }}
      >
        <Typography variant="h6">Galleries</Typography>
        <Button
          variant="contained"
          size="small"
          onClick={openCreate}
          disabled={event.status === 'ARCHIVED'}
        >
          Create gallery
        </Button>
      </Stack>

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
      {message && (
        <Alert severity="success" onClose={() => setMessage(null)}>
          {message}
        </Alert>
      )}

      {loading ? (
        <CircularProgress size={28} aria-label="Loading galleries" />
      ) : galleries.length === 0 ? (
        <Typography color="text.secondary">No galleries yet.</Typography>
      ) : (
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={1.5} sx={{ flexWrap: 'wrap' }}>
          {galleries.map((gallery) => (
            <Card key={gallery.id} variant="outlined" sx={{ minWidth: { md: 240 }, flex: 1 }}>
              <CardContent>
                <Typography variant="subtitle1" sx={{ fontWeight: 'medium' }}>
                  {gallery.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {gallery.status} · order {gallery.sortOrder}
                </Typography>
                {gallery.description && (
                  <Typography variant="body2">{gallery.description}</Typography>
                )}
                <Stack direction="row" spacing={0.75} sx={{ mt: 1, flexWrap: 'wrap' }}>
                  <Chip size="small" label={gallery.status} />
                  <Chip size="small" variant="outlined" label={`Order ${gallery.sortOrder}`} />
                </Stack>
              </CardContent>
              <CardActions sx={{ flexWrap: 'wrap' }}>
                <Button
                  size="small"
                  onClick={() => openEdit(gallery)}
                  disabled={gallery.status === 'ARCHIVED' || event.status === 'ARCHIVED'}
                >
                  Edit gallery
                </Button>
                <Button size="small" onClick={() => void openSettings(gallery)}>
                  {event.currentUserRole === 'OWNER' ? 'Access settings' : 'View access settings'}
                </Button>
                {event.currentUserRole === 'OWNER' && (
                  <>
                    <Button
                      size="small"
                      onClick={() => void archive(gallery.id)}
                      disabled={gallery.status === 'ARCHIVED'}
                    >
                      Archive gallery
                    </Button>
                    <Button color="error" size="small" onClick={() => void remove(gallery.id)}>
                      Delete gallery
                    </Button>
                  </>
                )}
              </CardActions>
            </Card>
          ))}
        </Stack>
      )}

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editing ? 'Edit gallery' : 'Create gallery'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            <TextField
              label="Gallery name"
              value={form.name}
              required
              slotProps={{ htmlInput: { maxLength: 255 } }}
              onChange={(changeEvent) => setForm({ ...form, name: changeEvent.target.value })}
            />
            <TextField
              label="Gallery description"
              value={form.description ?? ''}
              multiline
              minRows={3}
              slotProps={{ htmlInput: { maxLength: 5000 } }}
              onChange={(changeEvent) =>
                setForm({ ...form, description: changeEvent.target.value || null })
              }
            />
            <TextField
              label="Gallery order"
              type="number"
              value={form.sortOrder}
              slotProps={{ htmlInput: { min: 0, max: 100000 } }}
              onChange={(changeEvent) =>
                setForm({ ...form, sortOrder: Number(changeEvent.target.value) })
              }
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={() => void save()}
            disabled={!form.name.trim() || form.sortOrder < 0 || form.sortOrder > 100000}
          >
            Save gallery
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog
        open={settingsGallery !== null}
        onClose={() => {
          setSettingsGallery(null)
          setRotatedSharePath('')
        }}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>
          {event.currentUserRole === 'OWNER' ? 'Gallery access settings' : 'Gallery access'}
        </DialogTitle>
        <DialogContent>
          {settingsLoading || !settings ? (
            <CircularProgress aria-label="Loading gallery settings" sx={{ my: 3 }} />
          ) : (
            <Stack spacing={2} sx={{ pt: 1 }}>
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                <Chip
                  label={settings.publicViewEnabled ? 'Guest view on' : 'Guest view off'}
                  color={settings.publicViewEnabled ? 'success' : 'default'}
                />
                <Chip
                  label={settings.uploadEnabled ? 'Uploads on' : 'Uploads off'}
                  color={settings.uploadEnabled ? 'success' : 'default'}
                />
                <Chip
                  label={settings.accessCodeConfigured ? 'Code protected' : 'No access code'}
                  variant="outlined"
                />
              </Stack>
              {!canEditAccessSettings ? (
                <Alert severity="info">
                  {event.currentUserRole === 'MANAGER'
                    ? 'Only the event owner can change public access settings.'
                    : 'Archived events and galleries have read-only access settings.'}
                </Alert>
              ) : (
                <>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={settings.publicViewEnabled}
                        onChange={(changeEvent) =>
                          setSettings({
                            ...settings,
                            publicViewEnabled: changeEvent.target.checked,
                            uploadEnabled: changeEvent.target.checked
                              ? settings.uploadEnabled
                              : false,
                          })
                        }
                      />
                    }
                    label="Enable guest view"
                  />
                  <FormControlLabel
                    control={
                      <Switch
                        checked={settings.uploadEnabled}
                        disabled={!settings.publicViewEnabled}
                        onChange={(changeEvent) =>
                          setSettings({ ...settings, uploadEnabled: changeEvent.target.checked })
                        }
                      />
                    }
                    label="Allow guest uploads"
                  />
                  <FormControlLabel
                    control={
                      <Switch
                        checked={settings.downloadEnabled}
                        onChange={(changeEvent) =>
                          setSettings({ ...settings, downloadEnabled: changeEvent.target.checked })
                        }
                      />
                    }
                    label="Allow downloads later"
                  />
                  <FormControl fullWidth>
                    <InputLabel id="moderation-mode-label">Moderation</InputLabel>
                    <Select
                      labelId="moderation-mode-label"
                      label="Moderation"
                      value={settings.moderationMode}
                      onChange={(changeEvent) =>
                        setSettings({
                          ...settings,
                          moderationMode: changeEvent.target
                            .value as GallerySettings['moderationMode'],
                        })
                      }
                    >
                      <MenuItem value="REQUIRED">Review every upload</MenuItem>
                      <MenuItem value="NONE">No review</MenuItem>
                    </Select>
                  </FormControl>
                  <TextField
                    label="Publish from"
                    type="datetime-local"
                    value={settings.publishedAt?.slice(0, 16) ?? ''}
                    slotProps={{ inputLabel: { shrink: true } }}
                    onChange={(changeEvent) =>
                      setSettings({
                        ...settings,
                        publishedAt: changeEvent.target.value
                          ? new Date(changeEvent.target.value).toISOString()
                          : null,
                      })
                    }
                  />
                  <TextField
                    label="Expire at"
                    type="datetime-local"
                    value={settings.expiresAt?.slice(0, 16) ?? ''}
                    slotProps={{ inputLabel: { shrink: true } }}
                    onChange={(changeEvent) =>
                      setSettings({
                        ...settings,
                        expiresAt: changeEvent.target.value
                          ? new Date(changeEvent.target.value).toISOString()
                          : null,
                      })
                    }
                  />
                  {invalidPublicationWindow && (
                    <Alert severity="error">Expiration must be later than publication.</Alert>
                  )}
                  <Button
                    variant="contained"
                    disabled={invalidPublicationWindow}
                    onClick={() => void saveSettings()}
                  >
                    Save access settings
                  </Button>
                  <Button variant="outlined" onClick={() => void rotateToken()}>
                    Rotate private share link
                  </Button>
                  {rotatedSharePath && (
                    <Alert severity="warning">
                      <Stack spacing={1}>
                        <Typography>This link is shown only once. Store it securely.</Typography>
                        <TextField
                          label="New private share link"
                          value={`${window.location.origin}${rotatedSharePath}`}
                          slotProps={{ htmlInput: { readOnly: true } }}
                          fullWidth
                        />
                        <Button color="inherit" onClick={() => void copyShareLink()}>
                          Copy link
                        </Button>
                      </Stack>
                    </Alert>
                  )}
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                    <TextField
                      label="New access code"
                      type="password"
                      value={accessCode}
                      slotProps={{
                        htmlInput: { minLength: 6, maxLength: 64, pattern: '[\\x20-\\x7E]+' },
                      }}
                      onChange={(changeEvent) => setAccessCode(changeEvent.target.value)}
                      fullWidth
                    />
                    <Button
                      variant="outlined"
                      disabled={!validAccessCode}
                      onClick={() => void saveAccessCode()}
                    >
                      Set code
                    </Button>
                    {settings.accessCodeConfigured && (
                      <Button color="error" onClick={() => void removeAccessCode()}>
                        Remove code
                      </Button>
                    )}
                  </Stack>
                </>
              )}
            </Stack>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => {
              setSettingsGallery(null)
              setRotatedSharePath('')
            }}
          >
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  )
}
