import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControl,
  InputLabel,
  List,
  ListItem,
  ListItemText,
  MenuItem,
  Select,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useAuth } from '../AuthContext'
import {
  eventsApi,
  type EventData,
  type EventMember,
  type EventType,
  type EventWritePayload,
} from '../eventsApi'

const emptyPayload: EventWritePayload = {
  name: '',
  type: 'WEDDING',
  eventDate: null,
  description: null,
  privacyMode: 'PRIVATE',
}

function errorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message ?? 'Operation failed.'
  }
  return 'Operation failed.'
}

export default function Dashboard() {
  const { user, logout } = useAuth()
  const [events, setEvents] = useState<EventData[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [members, setMembers] = useState<EventMember[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [form, setForm] = useState<EventWritePayload>(emptyPayload)
  const [managerEmail, setManagerEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const selected = useMemo(
    () => events.find((event) => event.id === selectedId) ?? null,
    [events, selectedId],
  )

  const loadEvents = async () => {
    setLoading(true)
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
    setForm({
      name: event.name,
      type: event.type,
      eventDate: event.eventDate,
      description: event.description,
      privacyMode: 'PRIVATE',
    })
    setSelectedId(event.id)
    setDialogOpen(true)
  }

  const saveEvent = async () => {
    try {
      const response =
        selected && form.name !== ''
          ? await eventsApi.update(selected.id, form)
          : await eventsApi.create(form)
      setDialogOpen(false)
      setSelectedId(response.data.id)
      setMessage(selected ? 'Event updated.' : 'Event created.')
      await loadEvents()
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const addManager = async () => {
    if (!selected) return
    try {
      await eventsApi.addManager(selected.id, managerEmail)
      setManagerEmail('')
      setMessage('Manager added.')
      await loadMembers(selected.id)
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const removeManager = async (membershipId: string) => {
    if (!selected) return
    try {
      await eventsApi.removeManager(selected.id, membershipId)
      setMessage('Manager removed.')
      await loadMembers(selected.id)
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const transfer = async (membershipId: string) => {
    if (!selected) return
    try {
      await eventsApi.transferOwnership(selected.id, membershipId)
      setMessage('Ownership transferred.')
      await loadEvents()
      await loadMembers(selected.id)
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const archive = async () => {
    if (!selected) return
    try {
      await eventsApi.archive(selected.id)
      setMessage('Event archived.')
      await loadEvents()
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const removeEvent = async () => {
    if (!selected) return
    try {
      await eventsApi.remove(selected.id)
      setSelectedId(null)
      setMessage('Event deleted.')
      await loadEvents()
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  return (
    <Container maxWidth="lg" sx={{ py: { xs: 2, sm: 4 } }}>
      <Stack spacing={3}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          sx={{ justifyContent: 'space-between' }}
        >
          <Box>
            <Typography variant="h4" component="h1">
              My Events
            </Typography>
            <Typography color="text.secondary">{user?.email}</Typography>
          </Box>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Button variant="contained" onClick={openCreate}>
              Create event
            </Button>
            <Button variant="outlined" onClick={() => void logout()}>
              Log out
            </Button>
          </Stack>
        </Stack>

        {loading ? (
          <CircularProgress aria-label="Loading events" />
        ) : events.length === 0 ? (
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6">No events yet</Typography>
              <Typography color="text.secondary">Create the first private event.</Typography>
            </CardContent>
          </Card>
        ) : (
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ flexWrap: 'wrap' }}>
            {events.map((event) => (
              <Card key={event.id} variant="outlined" sx={{ minWidth: { md: 280 }, flex: 1 }}>
                <CardContent>
                  <Typography variant="h6">{event.name}</Typography>
                  <Typography color="text.secondary">
                    {event.currentUserRole} · {event.status}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button onClick={() => setSelectedId(event.id)}>Manage</Button>
                  <Button onClick={() => openEdit(event)} disabled={event.status === 'ARCHIVED'}>
                    Edit
                  </Button>
                </CardActions>
              </Card>
            ))}
          </Stack>
        )}

        {selected && (
          <Card variant="outlined" aria-label={`Manage ${selected.name}`}>
            <CardContent>
              <Stack spacing={2}>
                <Box>
                  <Typography variant="h5">{selected.name}</Typography>
                  <Typography color="text.secondary">
                    {selected.type} · {selected.status}
                  </Typography>
                </Box>
                <Divider />
                <Typography variant="h6">People</Typography>
                <List disablePadding>
                  {members.map((member) => (
                    <ListItem
                      key={member.id ?? `owner-${member.userId}`}
                      disableGutters
                      secondaryAction={
                        selected.currentUserRole === 'OWNER' &&
                        member.role === 'MANAGER' &&
                        member.id ? (
                          <Stack direction="row" spacing={1}>
                            <Button size="small" onClick={() => void transfer(member.id!)}>
                              Transfer ownership
                            </Button>
                            <Button
                              color="error"
                              size="small"
                              onClick={() => void removeManager(member.id!)}
                            >
                              Remove
                            </Button>
                          </Stack>
                        ) : null
                      }
                    >
                      <ListItemText primary={member.email} secondary={member.role} />
                    </ListItem>
                  ))}
                </List>
                {selected.currentUserRole === 'OWNER' && (
                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                    <TextField
                      label="Manager email"
                      value={managerEmail}
                      onChange={(event) => setManagerEmail(event.target.value)}
                      fullWidth
                    />
                    <Button
                      variant="contained"
                      onClick={() => void addManager()}
                      disabled={!managerEmail}
                    >
                      Add manager
                    </Button>
                  </Stack>
                )}
                {selected.currentUserRole === 'OWNER' && (
                  <Stack direction="row" spacing={1}>
                    <Button
                      onClick={() => void archive()}
                      disabled={selected.status === 'ARCHIVED'}
                    >
                      Archive
                    </Button>
                    <Button color="error" onClick={() => void removeEvent()}>
                      Delete event
                    </Button>
                  </Stack>
                )}
              </Stack>
            </CardContent>
          </Card>
        )}
      </Stack>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth maxWidth="sm">
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
            <FormControl>
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
              minRows={3}
              value={form.description ?? ''}
              slotProps={{ htmlInput: { maxLength: 5000 } }}
              onChange={(event) => setForm({ ...form, description: event.target.value || null })}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => void saveEvent()} disabled={!form.name.trim()}>
            Save
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={message !== null} autoHideDuration={4000} onClose={() => setMessage(null)}>
        <Alert severity="success" onClose={() => setMessage(null)}>
          {message}
        </Alert>
      </Snackbar>
      <Snackbar open={error !== null} autoHideDuration={6000} onClose={() => setError(null)}>
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      </Snackbar>
    </Container>
  )
}
