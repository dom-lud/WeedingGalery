import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import {
  moderationApi,
  type ModerationAction,
  type ModerationMedia,
  type PublicationStatus,
} from '../moderationApi'
import ConfirmDialog from './ui/ConfirmDialog'

interface ModerationPanelProps {
  eventId: string
  galleryId: string
  galleryName: string
  open: boolean
  onClose: () => void
}

type Filter = 'ALL' | PublicationStatus
const MAX_BULK_SELECTION = 100

const statusLabels: Record<Filter, string> = {
  ALL: 'All statuses',
  PENDING: 'Pending',
  APPROVED: 'Approved',
  HIDDEN: 'Hidden',
  REJECTED: 'Rejected',
}

const actionLabels: Record<ModerationAction, string> = {
  approve: 'Approve',
  hide: 'Hide',
  reject: 'Reject',
  restore: 'Restore',
}

function errorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message ?? 'Moderation operation failed.'
  }
  return 'Moderation operation failed.'
}

function statusColor(status: PublicationStatus) {
  if (status === 'APPROVED') return 'success'
  if (status === 'REJECTED') return 'error'
  if (status === 'PENDING') return 'warning'
  return 'default'
}

export default function ModerationPanel({
  eventId,
  galleryId,
  galleryName,
  open,
  onClose,
}: ModerationPanelProps) {
  const [media, setMedia] = useState<ModerationMedia[]>([])
  const [filter, setFilter] = useState<Filter>('ALL')
  const [reason, setReason] = useState('')
  const [selected, setSelected] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [confirmation, setConfirmation] = useState<ModerationAction | null>(null)
  const [working, setWorking] = useState(false)
  const closeButtonRef = useRef<HTMLButtonElement | null>(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await moderationApi.list(eventId, galleryId)
      setMedia(response.data)
      setSelected([])
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (open) void load()
    // The dialog is intentionally refreshed when a gallery is opened.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, eventId, galleryId])

  const visibleMedia = useMemo(
    () => (filter === 'ALL' ? media : media.filter((item) => item.publicationStatus === filter)),
    [filter, media],
  )
  const allVisibleSelected =
    visibleMedia.length > 0 && visibleMedia.every((item) => selected.includes(item.id))

  const toggleAll = () => {
    if (allVisibleSelected) {
      setSelected((current) => current.filter((id) => !visibleMedia.some((item) => item.id === id)))
    } else {
      setSelected((current) =>
        [...new Set([...current, ...visibleMedia.map((item) => item.id)])].slice(
          0,
          MAX_BULK_SELECTION,
        ),
      )
    }
  }

  const toggleOne = (id: string) => {
    setSelected((current) => {
      if (current.includes(id)) return current.filter((item) => item !== id)
      if (current.length >= MAX_BULK_SELECTION) {
        setError(`You can select up to ${MAX_BULK_SELECTION} media at once.`)
        return current
      }
      return [...current, id]
    })
  }

  const perform = async (action: ModerationAction) => {
    if (working || selected.length === 0) return
    setWorking(true)
    setError(null)
    try {
      if (selected.length === 1) {
        await moderationApi.action(eventId, galleryId, selected[0], action, reason || undefined)
      } else {
        await moderationApi.bulkAction(eventId, galleryId, {
          mediaIds: selected,
          action,
          reason: reason || undefined,
        })
      }
      await load()
      setSelected([])
      setConfirmation(null)
      setReason('')
      setMessage(`${actionLabels[action]} completed.`)
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setWorking(false)
    }
  }

  const requestAction = (action: ModerationAction) => setConfirmation(action)

  return (
    <>
      <Dialog
        open={open}
        onClose={working ? undefined : onClose}
        fullWidth
        maxWidth="md"
        aria-labelledby="moderation-dialog-title"
        slotProps={{ transition: { onExited: () => closeButtonRef.current?.focus() } }}
      >
        <DialogTitle id="moderation-dialog-title">Moderate {galleryName}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            <Alert severity="info">
              The server verifies event ownership and moderation permissions.
            </Alert>
            <FormControl fullWidth size="small">
              <InputLabel id="moderation-status-filter-label">Status</InputLabel>
              <Select
                labelId="moderation-status-filter-label"
                label="Status"
                value={filter}
                onChange={(event) => setFilter(event.target.value as Filter)}
              >
                {(Object.keys(statusLabels) as Filter[]).map((value) => (
                  <MenuItem key={value} value={value}>
                    {statusLabels[value]}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="Decision reason"
              value={reason}
              multiline
              minRows={2}
              helperText={`Required for hide/reject · ${reason.length}/500`}
              slotProps={{ htmlInput: { maxLength: 500 } }}
              onChange={(event) => setReason(event.target.value)}
            />

            {error && (
              <Alert
                severity="error"
                action={
                  <Button color="inherit" onClick={() => void load()}>
                    Retry
                  </Button>
                }
              >
                {error}
              </Alert>
            )}

            {loading ? (
              <Stack role="status" aria-label="Loading moderation media" spacing={1}>
                <Skeleton variant="rounded" height={72} />
                <Skeleton variant="rounded" height={72} />
              </Stack>
            ) : media.length === 0 ? (
              <Paper variant="outlined" sx={{ p: 3, textAlign: 'center', borderStyle: 'dashed' }}>
                <Typography>No media is waiting for moderation.</Typography>
              </Paper>
            ) : visibleMedia.length === 0 ? (
              <Typography color="text.secondary">No media matches this status.</Typography>
            ) : (
              <Stack component="ul" spacing={1} sx={{ p: 0, m: 0, listStyle: 'none' }}>
                <Paper component="li" variant="outlined" sx={{ px: 1, py: 0.5 }}>
                  <Stack
                    direction="row"
                    sx={{ alignItems: 'center', justifyContent: 'space-between' }}
                  >
                    <Checkbox
                      checked={allVisibleSelected}
                      indeterminate={selected.length > 0 && !allVisibleSelected}
                      onChange={toggleAll}
                      slotProps={{ input: { 'aria-label': 'Select all visible media' } }}
                    />
                    <Typography variant="body2" color="text.secondary">
                      {selected.length} selected
                    </Typography>
                  </Stack>
                </Paper>
                {visibleMedia.map((item) => (
                  <Paper component="li" variant="outlined" key={item.id} sx={{ p: 1 }}>
                    <Stack
                      direction={{ xs: 'column', sm: 'row' }}
                      spacing={1.5}
                      sx={{ alignItems: { sm: 'center' } }}
                    >
                      <Checkbox
                        checked={selected.includes(item.id)}
                        onChange={() => toggleOne(item.id)}
                        slotProps={{ input: { 'aria-label': `Select ${item.fileName}` } }}
                      />
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Typography sx={{ overflowWrap: 'anywhere' }}>{item.fileName}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {item.mediaType} {'·'} {item.status}
                        </Typography>
                      </Box>
                      <Chip
                        size="small"
                        label={statusLabels[item.publicationStatus]}
                        color={statusColor(item.publicationStatus)}
                      />
                    </Stack>
                  </Paper>
                ))}
              </Stack>
            )}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 2, flexWrap: 'wrap', gap: 1 }}>
          <Button ref={closeButtonRef} onClick={onClose} disabled={working}>
            Close
          </Button>
          <Box sx={{ flex: 1 }} />
          {(['approve', 'hide', 'reject', 'restore'] as ModerationAction[]).map((action) => (
            <Button
              key={action}
              variant={action === 'approve' ? 'contained' : 'outlined'}
              color={action === 'reject' ? 'error' : 'primary'}
              disabled={
                selected.length === 0 ||
                working ||
                ((action === 'hide' || action === 'reject') && !reason.trim())
              }
              onClick={() => requestAction(action)}
            >
              {actionLabels[action]}
            </Button>
          ))}
          {working && <CircularProgress size={24} aria-label="Applying moderation action" />}
        </DialogActions>
      </Dialog>
      <ConfirmDialog
        open={confirmation !== null}
        title={`${confirmation ? actionLabels[confirmation] : ''} selected media?`}
        description="This action will change publication status for the selected media. Continue?"
        confirmLabel={confirmation ? actionLabels[confirmation] : ''}
        destructive={confirmation === 'reject' || confirmation === 'hide'}
        busy={working}
        onCancel={() => setConfirmation(null)}
        onConfirm={() => confirmation && void perform(confirmation)}
      />
      <Snackbar open={message !== null} autoHideDuration={4000} onClose={() => setMessage(null)}>
        <Alert severity="success" onClose={() => setMessage(null)}>
          {message}
        </Alert>
      </Snackbar>
    </>
  )
}
