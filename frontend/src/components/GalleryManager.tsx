import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import type { EventData } from '../eventsApi'
import { galleriesApi, type GalleryData, type GalleryWritePayload } from '../galleriesApi'

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
              </CardContent>
              <CardActions sx={{ flexWrap: 'wrap' }}>
                <Button
                  size="small"
                  onClick={() => openEdit(gallery)}
                  disabled={gallery.status === 'ARCHIVED' || event.status === 'ARCHIVED'}
                >
                  Edit gallery
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
    </Stack>
  )
}
