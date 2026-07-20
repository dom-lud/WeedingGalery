import { useEffect, useRef, useState } from 'react'
import {
  Alert,
  Box,
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
  Divider,
  FormControl,
  FormControlLabel,
  InputLabel,
  Menu,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Stack,
  Switch,
  TextField,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material'
import type { EventData } from '../eventsApi'
import {
  galleriesApi,
  type GalleryData,
  type GallerySettings,
  type GallerySettingsPayload,
  type GalleryWritePayload,
} from '../galleriesApi'
import ConfirmDialog from './ui/ConfirmDialog'

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
  const theme = useTheme()
  const fullScreenDialog = useMediaQuery(theme.breakpoints.down('sm'))
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
  const [confirmation, setConfirmation] = useState<{
    title: string
    description: string
    confirmLabel: string
    action: () => Promise<void>
  } | null>(null)
  const [confirming, setConfirming] = useState(false)
  const [actionMenu, setActionMenu] = useState<{
    anchor: HTMLElement
    gallery: GalleryData
  } | null>(null)
  const actionTriggerRef = useRef<HTMLElement | null>(null)
  const restoreActionFocus = () => {
    const trigger = actionTriggerRef.current
    actionTriggerRef.current = null
    requestAnimationFrame(() => trigger?.focus())
  }
  const closeEditDialog = () => {
    setDialogOpen(false)
    restoreActionFocus()
  }
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
      closeEditDialog()
      await load()
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const requestArchive = (gallery: GalleryData) =>
    setConfirmation({
      title: 'Archive gallery?',
      description: `${gallery.name} will become read-only and stop accepting guest uploads.`,
      confirmLabel: 'Archive gallery',
      action: async () => {
        await galleriesApi.archive(event.id, gallery.id)
        setMessage('Gallery archived.')
        await load()
      },
    })

  const requestRemove = (gallery: GalleryData) =>
    setConfirmation({
      title: 'Delete gallery?',
      description: `Delete ${gallery.name}? It will no longer be available in this event.`,
      confirmLabel: 'Delete gallery',
      action: async () => {
        await galleriesApi.remove(event.id, gallery.id)
        setMessage('Gallery deleted.')
        await load()
      },
    })

  const confirmAction = async () => {
    if (!confirmation || confirming) return
    setConfirming(true)
    try {
      await confirmation.action()
      setConfirmation(null)
      restoreActionFocus()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setConfirming(false)
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

  const requestRemoveAccessCode = () => {
    if (!settingsGallery) return
    setConfirmation({
      title: 'Remove access code?',
      description:
        'Anyone with the private share link will be able to open this gallery without the code.',
      confirmLabel: 'Remove code',
      action: removeAccessCode,
    })
  }

  return (
    <Stack component="section" spacing={2.5} aria-labelledby="galleries-title">
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={1}
        sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
      >
        <Box>
          <Typography id="galleries-title" variant="h5" component="h2">
            Galleries
          </Typography>
          <Typography color="text.secondary">Organize guest uploads into collections.</Typography>
        </Box>
        <Button variant="contained" onClick={openCreate} disabled={event.status === 'ARCHIVED'}>
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
        <Box
          aria-label="Loading galleries"
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' },
            gap: 1.5,
          }}
        >
          <Skeleton variant="rounded" height={170} />
          <Skeleton variant="rounded" height={170} />
        </Box>
      ) : galleries.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 3, textAlign: 'center', borderStyle: 'dashed' }}>
          <Typography variant="h6">No galleries yet.</Typography>
          <Typography color="text.secondary" sx={{ mt: 0.5 }}>
            Create one for the ceremony, reception or another part of the day.
          </Typography>
        </Paper>
      ) : (
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: 'minmax(0, 1fr)', sm: 'repeat(2, minmax(0, 1fr))' },
            gap: 1.5,
          }}
        >
          {galleries.map((gallery) => (
            <Card
              key={gallery.id}
              component="article"
              aria-label={`Gallery ${gallery.name}`}
              variant="outlined"
              sx={{ minWidth: 0, overflow: 'hidden' }}
            >
              <Box
                aria-hidden="true"
                sx={{
                  height: 76,
                  background: (theme) =>
                    gallery.status === 'ARCHIVED'
                      ? `linear-gradient(135deg, ${theme.palette.action.disabledBackground}, ${theme.palette.background.paper})`
                      : `linear-gradient(135deg, ${theme.palette.primary.main}22, ${theme.palette.secondary.main}24)`,
                }}
              />
              <CardContent sx={{ pb: 1.5 }}>
                <Typography variant="h6" component="h3" sx={{ overflowWrap: 'anywhere' }}>
                  {gallery.name}
                </Typography>
                {gallery.description && (
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mt: 0.75, overflowWrap: 'anywhere' }}
                  >
                    {gallery.description}
                  </Typography>
                )}
                <Stack direction="row" spacing={0.75} sx={{ mt: 1.5, flexWrap: 'wrap', gap: 0.75 }}>
                  <Chip
                    size="small"
                    label={gallery.status === 'ACTIVE' ? 'Active' : 'Archived'}
                    color={gallery.status === 'ACTIVE' ? 'success' : 'default'}
                  />
                  <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center' }}>
                    Display order {gallery.sortOrder}
                  </Typography>
                </Stack>
              </CardContent>
              <CardActions
                sx={{
                  px: 2,
                  pb: 2,
                  pt: 0,
                  gap: 0.75,
                  flexDirection: { xs: 'column', sm: 'row' },
                  alignItems: 'stretch',
                }}
              >
                <Button
                  variant="contained"
                  onClick={() => void openSettings(gallery)}
                  sx={{ width: { xs: '100%', sm: 'auto' } }}
                >
                  {event.currentUserRole === 'OWNER' ? 'Access settings' : 'View access settings'}
                </Button>
                {(gallery.status !== 'ARCHIVED' || event.currentUserRole === 'OWNER') && (
                  <Button
                    id={`gallery-actions-trigger-${gallery.id}`}
                    aria-label={`More actions for ${gallery.name}`}
                    aria-haspopup="menu"
                    aria-expanded={actionMenu?.gallery.id === gallery.id ? 'true' : undefined}
                    aria-controls={
                      actionMenu?.gallery.id === gallery.id
                        ? `gallery-actions-menu-${gallery.id}`
                        : undefined
                    }
                    onClick={(clickEvent) => {
                      actionTriggerRef.current = clickEvent.currentTarget
                      setActionMenu({ anchor: clickEvent.currentTarget, gallery })
                    }}
                    sx={{ width: { xs: '100%', sm: 'auto' } }}
                  >
                    More actions
                  </Button>
                )}
              </CardActions>
            </Card>
          ))}
        </Box>
      )}

      <Menu
        id={actionMenu ? `gallery-actions-menu-${actionMenu.gallery.id}` : undefined}
        anchorEl={actionMenu?.anchor ?? null}
        open={actionMenu !== null}
        onClose={() => {
          setActionMenu(null)
          restoreActionFocus()
        }}
        slotProps={{
          list: {
            'aria-labelledby': actionMenu
              ? `gallery-actions-trigger-${actionMenu.gallery.id}`
              : undefined,
          },
        }}
      >
        {actionMenu && actionMenu.gallery.status !== 'ARCHIVED' && event.status !== 'ARCHIVED' && (
          <MenuItem
            onClick={() => {
              openEdit(actionMenu.gallery)
              setActionMenu(null)
            }}
          >
            Edit gallery
          </MenuItem>
        )}
        {actionMenu &&
          event.currentUserRole === 'OWNER' &&
          actionMenu.gallery.status !== 'ARCHIVED' && (
            <MenuItem
              onClick={() => {
                requestArchive(actionMenu.gallery)
                setActionMenu(null)
              }}
            >
              Archive gallery
            </MenuItem>
          )}
        {actionMenu && event.currentUserRole === 'OWNER' && (
          <MenuItem
            sx={{ color: 'error.main' }}
            onClick={() => {
              requestRemove(actionMenu.gallery)
              setActionMenu(null)
            }}
          >
            Delete gallery
          </MenuItem>
        )}
      </Menu>

      <Dialog
        open={dialogOpen}
        onClose={closeEditDialog}
        fullScreen={fullScreenDialog}
        fullWidth
        maxWidth="sm"
      >
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
        <DialogActions sx={{ p: 2, flexDirection: { xs: 'column-reverse', sm: 'row' } }}>
          <Button onClick={closeEditDialog} sx={{ width: { xs: '100%', sm: 'auto' } }}>
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={() => void save()}
            disabled={!form.name.trim() || form.sortOrder < 0 || form.sortOrder > 100000}
            sx={{ width: { xs: '100%', sm: 'auto' } }}
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
        fullScreen={fullScreenDialog}
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
            <Stack spacing={2.5} sx={{ pt: 1 }}>
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
                  <Typography variant="h6" component="h3">
                    Guest experience
                  </Typography>
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
                  <Divider />
                  <Typography variant="h6" component="h3">
                    Publication window
                  </Typography>
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
                  <Divider />
                  <Typography variant="h6" component="h3">
                    Private access
                  </Typography>
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
                      <Button color="error" onClick={requestRemoveAccessCode}>
                        Remove code
                      </Button>
                    )}
                  </Stack>
                </>
              )}
            </Stack>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
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
      <ConfirmDialog
        open={confirmation !== null}
        title={confirmation?.title ?? ''}
        description={confirmation?.description ?? ''}
        confirmLabel={confirmation?.confirmLabel ?? ''}
        destructive
        busy={confirming}
        onCancel={() => {
          setConfirmation(null)
          restoreActionFocus()
        }}
        onConfirm={() => void confirmAction()}
      />
    </Stack>
  )
}
