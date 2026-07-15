import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
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
  useMediaQuery,
  useTheme,
} from '@mui/material'
import { useAuth } from '../AuthContext'
import {
  eventsApi,
  type EventData,
  type EventMember,
  type EventType,
  type EventWritePayload,
} from '../eventsApi'
import GalleryManager from './GalleryManager'
import AppShell from './layout/AppShell'
import ConfirmDialog from './ui/ConfirmDialog'

const emptyPayload: EventWritePayload = {
  name: '',
  type: 'WEDDING',
  eventDate: null,
  description: null,
  privacyMode: 'PRIVATE',
}

interface Confirmation {
  title: string
  description: string
  confirmLabel: string
  destructive?: boolean
  action: () => Promise<void>
}

function errorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message ?? 'Operation failed.'
  }
  return 'Operation failed.'
}

function readableType(type: EventType) {
  return type.charAt(0) + type.slice(1).toLowerCase()
}

export default function Dashboard() {
  const { user, logout } = useAuth()
  const theme = useTheme()
  const fullScreenDialog = useMediaQuery(theme.breakpoints.down('sm'))
  const [events, setEvents] = useState<EventData[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [members, setMembers] = useState<EventMember[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [form, setForm] = useState<EventWritePayload>(emptyPayload)
  const [managerEmail, setManagerEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [confirmation, setConfirmation] = useState<Confirmation | null>(null)
  const [confirming, setConfirming] = useState(false)

  const selected = useMemo(
    () => events.find((event) => event.id === selectedId) ?? null,
    [events, selectedId],
  )

  const loadEvents = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await eventsApi.list()
      setEvents(response.data)
      if (selectedId && !response.data.some((event) => event.id === selectedId)) {
        setSelectedId(null)
      }
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }

  const loadMembers = async (eventId: string) => {
    try {
      const response = await eventsApi.members(eventId)
      setMembers(response.data)
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void loadEvents()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    if (selectedId) void loadMembers(selectedId)
  }, [selectedId])

  const openCreate = () => {
    setSelectedId(null)
    setForm(emptyPayload)
    setDialogOpen(true)
  }

  const openEdit = (event: EventData) => {
    setSelectedId(event.id)
    setForm({
      name: event.name,
      type: event.type,
      eventDate: event.eventDate,
      description: event.description,
      privacyMode: 'PRIVATE',
    })
    setDialogOpen(true)
  }

  const saveEvent = async () => {
    if (saving) return
    setSaving(true)
    try {
      const response = selected
        ? await eventsApi.update(selected.id, form)
        : await eventsApi.create(form)
      setDialogOpen(false)
      setSelectedId(response.data.id)
      setMessage(selected ? 'Event updated.' : 'Event created.')
      await loadEvents()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  const addManager = async () => {
    if (!selected || saving) return
    setSaving(true)
    try {
      await eventsApi.addManager(selected.id, managerEmail)
      setManagerEmail('')
      setMessage('Manager added.')
      await loadMembers(selected.id)
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  const confirmAction = async () => {
    if (!confirmation || confirming) return
    setConfirming(true)
    try {
      await confirmation.action()
      setConfirmation(null)
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setConfirming(false)
    }
  }

  const requestRemoveManager = (member: EventMember) => {
    if (!selected || !member.id) return
    setConfirmation({
      title: 'Remove manager?',
      description: `${member.email} will immediately lose access to this event.`,
      confirmLabel: 'Remove manager',
      destructive: true,
      action: async () => {
        await eventsApi.removeManager(selected.id, member.id!)
        setMessage('Manager removed.')
        await loadMembers(selected.id)
      },
    })
  }

  const requestTransfer = (member: EventMember) => {
    if (!selected || !member.id) return
    setConfirmation({
      title: 'Transfer ownership?',
      description: `${member.email} will become the event owner. You will remain a manager.`,
      confirmLabel: 'Transfer ownership',
      action: async () => {
        await eventsApi.transferOwnership(selected.id, member.id!)
        setMessage('Ownership transferred.')
        await loadEvents()
        await loadMembers(selected.id)
      },
    })
  }

  const requestArchive = () => {
    if (!selected) return
    setConfirmation({
      title: 'Archive event?',
      description: 'The event and its galleries will become read-only.',
      confirmLabel: 'Archive event',
      action: async () => {
        await eventsApi.archive(selected.id)
        setMessage('Event archived.')
        await loadEvents()
      },
    })
  }

  const requestDelete = () => {
    if (!selected) return
    setConfirmation({
      title: 'Delete event?',
      description: `Delete ${selected.name}? This removes it from your active workspace.`,
      confirmLabel: 'Delete event',
      destructive: true,
      action: async () => {
        await eventsApi.remove(selected.id)
        setSelectedId(null)
        setMessage('Event deleted.')
        await loadEvents()
      },
    })
  }

  return (
    <AppShell email={user?.email} onLogout={() => void logout()}>
      <Stack spacing={{ xs: 3, sm: 4 }}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          sx={{ alignItems: { sm: 'flex-end' }, justifyContent: 'space-between' }}
        >
          <Box>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 800 }}>
              Event workspace
            </Typography>
            <Typography variant="h2" component="h1">
              Your events
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 1, maxWidth: 620 }}>
              Create a private space, invite a manager and prepare galleries for your guests.
            </Typography>
          </Box>
          <Button variant="contained" size="large" onClick={openCreate}>
            Create event
          </Button>
        </Stack>

        {error && (
          <Alert
            severity="error"
            action={
              <Button color="inherit" onClick={() => void loadEvents()}>
                Retry
              </Button>
            }
          >
            {error}
          </Alert>
        )}

        {loading ? (
          <Box
            aria-label="Loading events"
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' },
              gap: 2,
            }}
          >
            {[0, 1, 2, 3].map((item) => (
              <Skeleton key={item} variant="rounded" height={172} />
            ))}
          </Box>
        ) : events.length === 0 ? (
          <Paper
            variant="outlined"
            sx={{ p: { xs: 3, sm: 5 }, textAlign: 'center', borderStyle: 'dashed' }}
          >
            <Typography variant="h4" component="h2">
              Your first event starts here
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 1, mb: 3 }}>
              Create a private event, then add galleries and a secure guest upload link.
            </Typography>
            <Button variant="contained" onClick={openCreate}>
              Create your first event
            </Button>
          </Paper>
        ) : (
          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: 'minmax(0, 1fr)', md: '300px minmax(0, 1fr)' },
              gap: { xs: 3, md: 4 },
              alignItems: 'start',
            }}
          >
            <Stack component="section" aria-labelledby="event-list-title" spacing={1.5}>
              <Typography id="event-list-title" variant="h5" component="h2">
                Events
              </Typography>
              {events.map((event) => {
                const active = selectedId === event.id
                return (
                  <Card
                    component="article"
                    key={event.id}
                    data-testid={`event-card-${event.id}`}
                    sx={{
                      borderColor: active ? 'primary.main' : 'divider',
                      boxShadow: active
                        ? (theme) => `0 0 0 2px ${theme.palette.primary.main}20`
                        : undefined,
                    }}
                  >
                    <CardContent>
                      <Typography variant="h6" component="h3" sx={{ overflowWrap: 'anywhere' }}>
                        {event.name}
                      </Typography>
                      <Stack
                        direction="row"
                        spacing={0.75}
                        sx={{ mt: 1, flexWrap: 'wrap', gap: 0.75 }}
                      >
                        <Chip size="small" label={readableType(event.type)} variant="outlined" />
                        <Chip
                          size="small"
                          label={event.status === 'ARCHIVED' ? 'Archived' : event.currentUserRole}
                          color={event.status === 'ARCHIVED' ? 'default' : 'primary'}
                        />
                      </Stack>
                    </CardContent>
                    <CardActions sx={{ px: 2, pb: 2 }}>
                      <Button
                        variant={active ? 'contained' : 'outlined'}
                        onClick={() => setSelectedId(event.id)}
                      >
                        Manage
                      </Button>
                      <Button
                        onClick={() => openEdit(event)}
                        disabled={event.status === 'ARCHIVED'}
                      >
                        Edit
                      </Button>
                    </CardActions>
                  </Card>
                )
              })}
            </Stack>

            {selected ? (
              <Paper
                component="section"
                aria-label={`Manage ${selected.name}`}
                variant="outlined"
                sx={{ p: { xs: 2, sm: 3 }, minWidth: 0 }}
              >
                <Stack spacing={3}>
                  <Stack
                    direction={{ xs: 'column', sm: 'row' }}
                    spacing={2}
                    sx={{ justifyContent: 'space-between', alignItems: { sm: 'flex-start' } }}
                  >
                    <Box sx={{ minWidth: 0 }}>
                      <Typography variant="overline" color="text.secondary">
                        Selected event
                      </Typography>
                      <Typography variant="h3" component="h2" sx={{ overflowWrap: 'anywhere' }}>
                        {selected.name}
                      </Typography>
                      <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                        {readableType(selected.type)} • {selected.status.toLowerCase()} •{' '}
                        {selected.currentUserRole.toLowerCase()}
                      </Typography>
                    </Box>
                    <Button
                      variant="outlined"
                      onClick={() => openEdit(selected)}
                      disabled={selected.status === 'ARCHIVED'}
                    >
                      Edit details
                    </Button>
                  </Stack>

                  <Divider />
                  <GalleryManager event={selected} />
                  <Divider />

                  <Stack component="section" aria-labelledby="people-title" spacing={2}>
                    <Box>
                      <Typography id="people-title" variant="h5" component="h2">
                        People
                      </Typography>
                      <Typography color="text.secondary">
                        Owners control lifecycle and access; managers help organize galleries.
                      </Typography>
                    </Box>
                    <Stack component="ul" spacing={1.25} sx={{ p: 0, m: 0, listStyle: 'none' }}>
                      {members.map((member) => (
                        <Paper
                          component="li"
                          variant="outlined"
                          key={member.id ?? `owner-${member.userId}`}
                          sx={{ p: 1.5 }}
                        >
                          <Stack
                            direction={{ xs: 'column', sm: 'row' }}
                            spacing={1.5}
                            sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
                          >
                            <Box sx={{ minWidth: 0 }}>
                              <Typography sx={{ fontWeight: 700, overflowWrap: 'anywhere' }}>
                                {member.email}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                {member.role.toLowerCase()}
                              </Typography>
                            </Box>
                            {selected.currentUserRole === 'OWNER' &&
                              member.role === 'MANAGER' &&
                              member.id && (
                                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                                  <Button
                                    variant="outlined"
                                    onClick={() => requestTransfer(member)}
                                  >
                                    Transfer ownership
                                  </Button>
                                  <Button
                                    color="error"
                                    onClick={() => requestRemoveManager(member)}
                                  >
                                    Remove
                                  </Button>
                                </Stack>
                              )}
                          </Stack>
                        </Paper>
                      ))}
                    </Stack>

                    {selected.currentUserRole === 'OWNER' && (
                      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                        <TextField
                          label="Manager email"
                          type="email"
                          value={managerEmail}
                          onChange={(event) => setManagerEmail(event.target.value)}
                        />
                        <Button
                          variant="contained"
                          onClick={() => void addManager()}
                          disabled={!managerEmail || saving}
                          sx={{ flex: { sm: '0 0 auto' } }}
                        >
                          Add manager
                        </Button>
                      </Stack>
                    )}
                  </Stack>

                  {selected.currentUserRole === 'OWNER' && (
                    <Paper
                      variant="outlined"
                      sx={{ p: 2, bgcolor: 'error.main', color: 'error.contrastText' }}
                    >
                      <Stack
                        direction={{ xs: 'column', sm: 'row' }}
                        spacing={1}
                        sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
                      >
                        <Box>
                          <Typography sx={{ fontWeight: 800 }}>Event lifecycle</Typography>
                          <Typography variant="body2" sx={{ opacity: 0.84 }}>
                            Archive to make it read-only, or remove it from the workspace.
                          </Typography>
                        </Box>
                        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                          <Button
                            variant="outlined"
                            color="inherit"
                            onClick={requestArchive}
                            disabled={selected.status === 'ARCHIVED'}
                          >
                            Archive
                          </Button>
                          <Button variant="contained" color="inherit" onClick={requestDelete}>
                            Delete event
                          </Button>
                        </Stack>
                      </Stack>
                    </Paper>
                  )}
                </Stack>
              </Paper>
            ) : (
              <Paper
                variant="outlined"
                sx={{ p: { xs: 3, sm: 5 }, textAlign: 'center', borderStyle: 'dashed' }}
              >
                <Typography variant="h4" component="h2">
                  Choose an event to manage
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Its galleries, people and lifecycle controls will appear here.
                </Typography>
              </Paper>
            )}
          </Box>
        )}
      </Stack>

      <Dialog
        open={dialogOpen}
        onClose={saving ? undefined : () => setDialogOpen(false)}
        fullScreen={fullScreenDialog}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>{selected ? 'Edit event' : 'Create event'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            <TextField
              label="Event name"
              value={form.name}
              slotProps={{ htmlInput: { maxLength: 255 } }}
              onChange={(event) => setForm({ ...form, name: event.target.value })}
              required
            />
            <FormControl fullWidth>
              <InputLabel id="event-type-label">Event type</InputLabel>
              <Select
                labelId="event-type-label"
                label="Event type"
                value={form.type}
                onChange={(event) => setForm({ ...form, type: event.target.value as EventType })}
              >
                <MenuItem value="WEDDING">Wedding</MenuItem>
                <MenuItem value="BIRTHDAY">Birthday</MenuItem>
                <MenuItem value="CORPORATE">Corporate</MenuItem>
                <MenuItem value="OTHER">Other</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="Event date"
              type="date"
              value={form.eventDate ?? ''}
              slotProps={{ inputLabel: { shrink: true } }}
              onChange={(event) => setForm({ ...form, eventDate: event.target.value || null })}
            />
            <TextField
              label="Description"
              multiline
              minRows={4}
              value={form.description ?? ''}
              slotProps={{ htmlInput: { maxLength: 5000 } }}
              onChange={(event) => setForm({ ...form, description: event.target.value || null })}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 2, flexDirection: { xs: 'column-reverse', sm: 'row' } }}>
          <Button
            onClick={() => setDialogOpen(false)}
            disabled={saving}
            sx={{ width: { xs: '100%', sm: 'auto' } }}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={() => void saveEvent()}
            disabled={!form.name.trim() || saving}
            aria-busy={saving}
            sx={{ width: { xs: '100%', sm: 'auto' } }}
          >
            {saving ? 'Saving…' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={confirmation !== null}
        title={confirmation?.title ?? ''}
        description={confirmation?.description ?? ''}
        confirmLabel={confirmation?.confirmLabel ?? ''}
        destructive={confirmation?.destructive}
        busy={confirming}
        onCancel={() => setConfirmation(null)}
        onConfirm={() => void confirmAction()}
      />

      <Snackbar open={message !== null} autoHideDuration={4000} onClose={() => setMessage(null)}>
        <Alert severity="success" onClose={() => setMessage(null)}>
          {message}
        </Alert>
      </Snackbar>
    </AppShell>
  )
}
