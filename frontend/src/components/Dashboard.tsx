import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Avatar,
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
  FormControl,
  Fade,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Skeleton,
  Snackbar,
  Stack,
  Tab,
  Tabs,
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
  type EventStatistics,
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
  const compactLayout = useMediaQuery(theme.breakpoints.down('lg'))
  const reduceMotion = useMediaQuery('(prefers-reduced-motion: reduce)')
  const [events, setEvents] = useState<EventData[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [members, setMembers] = useState<EventMember[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingEventId, setEditingEventId] = useState<string | null>(null)
  const [form, setForm] = useState<EventWritePayload>(emptyPayload)
  const [managerEmail, setManagerEmail] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [confirmation, setConfirmation] = useState<Confirmation | null>(null)
  const [confirming, setConfirming] = useState(false)
  const [activeTab, setActiveTab] = useState<'galleries' | 'people' | 'settings' | 'statistics'>(
    'galleries',
  )
  const [statistics, setStatistics] = useState<EventStatistics | null>(null)
  const [statisticsLoading, setStatisticsLoading] = useState(false)
  const detailHeadingRef = useRef<HTMLHeadingElement>(null)
  const eventTriggerRefs = useRef<Record<string, HTMLButtonElement | null>>({})

  const selected = useMemo(
    () => events.find((event) => event.id === selectedId) ?? null,
    [events, selectedId],
  )

  const loadEvents = async (preferredSelectedId?: string) => {
    setLoading(true)
    setError(null)
    try {
      const response = await eventsApi.list()
      setEvents(response.data)
      if (preferredSelectedId && response.data.some((event) => event.id === preferredSelectedId)) {
        setSelectedId(preferredSelectedId)
      } else if (selectedId && !response.data.some((event) => event.id === selectedId)) {
        setSelectedId(null)
      } else if (!selectedId && response.data.length > 0 && !compactLayout) {
        setSelectedId(response.data[0].id)
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

  const loadStatistics = async () => {
    if (!selected || statisticsLoading) return
    setStatisticsLoading(true)
    setError(null)
    try {
      setStatistics((await eventsApi.statistics(selected.id)).data)
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setStatisticsLoading(false)
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

  useEffect(() => {
    if (selectedId && compactLayout) {
      requestAnimationFrame(() => {
        window.scrollTo({ top: 0, behavior: 'auto' })
        detailHeadingRef.current?.focus({ preventScroll: true })
      })
    }
  }, [compactLayout, selectedId])

  const selectEvent = (eventId: string) => {
    setActiveTab('galleries')
    setSelectedId(eventId)
  }

  const openCreate = () => {
    setEditingEventId(null)
    setForm(emptyPayload)
    setDialogOpen(true)
  }

  const openEdit = (event: EventData) => {
    setSelectedId(event.id)
    setEditingEventId(event.id)
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
      const response = editingEventId
        ? await eventsApi.update(editingEventId, form)
        : await eventsApi.create(form)
      setDialogOpen(false)
      setMessage(editingEventId ? 'Event updated.' : 'Event created.')
      await loadEvents(response.data.id)
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
        {(!compactLayout || !selected) && (
          <Paper
            elevation={0}
            sx={{
              p: { xs: 2.5, sm: 4 },
              overflow: 'hidden',
              border: '1px solid',
              borderColor: 'divider',
              background: (theme) =>
                `linear-gradient(120deg, ${theme.palette.primary.main}10, ${theme.palette.secondary.main}0D 62%, ${theme.palette.background.paper})`,
            }}
          >
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={2.5}
              sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
            >
              <Fade in timeout={reduceMotion ? 0 : 360} appear={!reduceMotion}>
                <Box>
                  <Typography variant="overline" color="primary" sx={{ fontWeight: 700 }}>
                    Event workspace
                  </Typography>
                  <Typography variant="h2" component="h1" sx={{ mt: 0.25 }}>
                    Your events
                  </Typography>
                  <Typography color="text.secondary" sx={{ mt: 1, maxWidth: 650 }}>
                    Organize galleries, collaborate with your team and prepare a simple guest flow.
                  </Typography>
                </Box>
              </Fade>
              <Button
                variant="contained"
                size="large"
                onClick={openCreate}
                sx={{ width: { xs: '100%', sm: 'auto' }, flex: '0 0 auto' }}
              >
                Create event
              </Button>
            </Stack>
          </Paper>
        )}

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
            role="status"
            aria-label="Loading events"
            aria-live="polite"
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', lg: '320px minmax(0, 1fr)' },
              gap: 3,
            }}
          >
            <Skeleton variant="rounded" height={420} />
            <Skeleton variant="rounded" height={520} />
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
              gridTemplateColumns: { xs: 'minmax(0, 1fr)', lg: '320px minmax(0, 1fr)' },
              gap: 3,
              alignItems: 'start',
            }}
          >
            {(!compactLayout || !selected) && (
              <Paper
                component="section"
                aria-labelledby="event-list-title"
                variant="outlined"
                sx={{
                  p: 1.5,
                  position: { lg: 'sticky' },
                  top: { lg: 100 },
                  maxHeight: { lg: 'calc(100dvh - 124px)' },
                  overflow: { lg: 'auto' },
                }}
              >
                <Stack
                  direction="row"
                  sx={{ px: 1, py: 0.75, alignItems: 'center', justifyContent: 'space-between' }}
                >
                  <Typography id="event-list-title" variant="h5" component="h2">
                    Events
                  </Typography>
                  <Chip size="small" label={events.length} variant="outlined" />
                </Stack>
                <Stack spacing={0.75} sx={{ mt: 1 }}>
                  {events.map((event) => {
                    const active = selectedId === event.id
                    return (
                      <Card
                        component="article"
                        key={event.id}
                        data-testid={`event-card-${event.id}`}
                        aria-current={active ? 'true' : undefined}
                        variant="outlined"
                        sx={{
                          position: 'relative',
                          overflow: 'hidden',
                          borderColor: active ? 'primary.main' : 'transparent',
                          bgcolor: active ? 'rgba(116, 70, 90, 0.075)' : 'transparent',
                          boxShadow: 'none',
                          '&::before': active
                            ? {
                                content: '""',
                                position: 'absolute',
                                inset: '0 auto 0 0',
                                width: 4,
                                bgcolor: 'primary.main',
                              }
                            : undefined,
                          '&:hover': {
                            bgcolor: active ? 'rgba(116, 70, 90, 0.09)' : 'action.hover',
                          },
                        }}
                      >
                        <CardContent sx={{ p: 1.75, pb: 1 }}>
                          <Typography
                            variant="subtitle1"
                            component="h3"
                            sx={{ fontWeight: 700, overflowWrap: 'anywhere', lineHeight: 1.3 }}
                          >
                            {event.name}
                          </Typography>
                          <Stack
                            direction="row"
                            spacing={0.75}
                            sx={{ mt: 1, flexWrap: 'wrap', gap: 0.75 }}
                          >
                            <Chip
                              size="small"
                              label={readableType(event.type)}
                              variant="outlined"
                            />
                            <Chip
                              size="small"
                              label={
                                event.status === 'ARCHIVED' ? 'Archived' : event.currentUserRole
                              }
                              color={event.status === 'ARCHIVED' ? 'default' : 'primary'}
                            />
                          </Stack>
                        </CardContent>
                        <CardActions sx={{ px: 1.75, pb: 1.5, pt: 0 }}>
                          <Button
                            ref={(node) => {
                              eventTriggerRefs.current[event.id] = node
                            }}
                            variant={active ? 'contained' : 'text'}
                            onClick={() => selectEvent(event.id)}
                            sx={{ width: '100%' }}
                          >
                            {active ? 'Selected' : 'Manage'}
                          </Button>
                        </CardActions>
                      </Card>
                    )
                  })}
                </Stack>
              </Paper>
            )}

            {selected ? (
              <Stack
                component="section"
                role="region"
                aria-label={`Manage ${selected.name}`}
                spacing={2}
                sx={{ minWidth: 0 }}
              >
                <Paper
                  variant="outlined"
                  sx={{
                    p: { xs: 2.25, sm: 3 },
                    background: (theme) =>
                      `linear-gradient(135deg, ${theme.palette.primary.main}0E, ${theme.palette.background.paper} 58%)`,
                  }}
                >
                  <Stack spacing={2}>
                    {compactLayout && (
                      <Button
                        onClick={() => {
                          const returningEventId = selected.id
                          setSelectedId(null)
                          requestAnimationFrame(() =>
                            requestAnimationFrame(() =>
                              eventTriggerRefs.current[returningEventId]?.focus(),
                            ),
                          )
                        }}
                        sx={{ alignSelf: 'flex-start', px: 0 }}
                      >
                        Back to events
                      </Button>
                    )}
                    <Stack
                      direction={{ xs: 'column', sm: 'row' }}
                      spacing={2}
                      sx={{ justifyContent: 'space-between', alignItems: { sm: 'flex-start' } }}
                    >
                      <Box sx={{ minWidth: 0 }}>
                        <Typography variant="overline" color="primary" sx={{ fontWeight: 700 }}>
                          Selected event
                        </Typography>
                        <Typography
                          ref={detailHeadingRef}
                          tabIndex={-1}
                          variant="h3"
                          component="h2"
                          sx={{
                            mt: 0.25,
                            overflowWrap: 'anywhere',
                            borderLeft: '3px solid transparent',
                            pl: 1,
                            '&:focus': { borderLeftColor: 'primary.main', outline: 'none' },
                          }}
                        >
                          {selected.name}
                        </Typography>
                        <Stack direction="row" sx={{ mt: 1.5, flexWrap: 'wrap', gap: 0.75 }}>
                          <Chip
                            size="small"
                            label={readableType(selected.type)}
                            variant="outlined"
                          />
                          <Chip
                            size="small"
                            label={selected.status === 'ARCHIVED' ? 'Archived' : 'Draft'}
                          />
                          <Chip size="small" label={selected.currentUserRole} color="primary" />
                        </Stack>
                      </Box>
                      <Button
                        variant="outlined"
                        onClick={() => openEdit(selected)}
                        disabled={selected.status === 'ARCHIVED'}
                        sx={{ width: { xs: '100%', sm: 'auto' }, flex: '0 0 auto' }}
                      >
                        Edit details
                      </Button>
                    </Stack>
                  </Stack>
                </Paper>

                <Paper variant="outlined" sx={{ px: { xs: 1, sm: 2 } }}>
                  <Tabs
                    value={activeTab}
                    onChange={(_, value) => setActiveTab(value)}
                    variant="scrollable"
                    scrollButtons="auto"
                    aria-label="Event workspace sections"
                  >
                    <Tab
                      id="event-tab-galleries"
                      aria-controls="event-panel-galleries"
                      value="galleries"
                      label="Galleries"
                    />
                    <Tab
                      id="event-tab-people"
                      aria-controls="event-panel-people"
                      value="people"
                      label="People"
                    />
                    <Tab
                      id="event-tab-settings"
                      aria-controls="event-panel-settings"
                      value="settings"
                      label="Settings"
                    />
                    <Tab
                      id="event-tab-statistics"
                      aria-controls="event-panel-statistics"
                      value="statistics"
                      label="Statistics"
                    />
                  </Tabs>
                </Paper>

                {activeTab === 'galleries' && (
                  <Paper
                    id="event-panel-galleries"
                    role="tabpanel"
                    aria-labelledby="event-tab-galleries"
                    variant="outlined"
                    sx={{ p: { xs: 2, sm: 3 } }}
                  >
                    <GalleryManager event={selected} />
                  </Paper>
                )}

                {activeTab === 'people' && (
                  <Paper
                    id="event-panel-people"
                    role="tabpanel"
                    aria-labelledby="event-tab-people"
                    variant="outlined"
                    sx={{ p: { xs: 2, sm: 3 } }}
                  >
                    <Stack component="section" aria-labelledby="people-title" spacing={2.5}>
                      <Box>
                        <Typography id="people-title" variant="h5" component="h2">
                          People
                        </Typography>
                        <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                          Owners control lifecycle and access; managers help organize galleries.
                        </Typography>
                      </Box>

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

                      <Stack component="ul" spacing={1} sx={{ p: 0, m: 0, listStyle: 'none' }}>
                        {members.map((member) => (
                          <Paper
                            component="li"
                            variant="outlined"
                            key={member.id ?? `owner-${member.userId}`}
                            sx={{ p: 1.5, boxShadow: 'none' }}
                          >
                            <Stack
                              direction={{ xs: 'column', sm: 'row' }}
                              spacing={1.5}
                              sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
                            >
                              <Stack
                                direction="row"
                                spacing={1.5}
                                sx={{ alignItems: 'center', minWidth: 0 }}
                              >
                                <Avatar
                                  sx={{
                                    width: 40,
                                    height: 40,
                                    bgcolor: 'primary.main',
                                    fontSize: '0.9rem',
                                  }}
                                >
                                  {member.email.charAt(0).toUpperCase()}
                                </Avatar>
                                <Box sx={{ minWidth: 0 }}>
                                  <Typography sx={{ fontWeight: 700, overflowWrap: 'anywhere' }}>
                                    {member.email}
                                  </Typography>
                                  <Chip
                                    size="small"
                                    variant="outlined"
                                    label={member.role === 'OWNER' ? 'Owner' : 'Manager'}
                                  />
                                </Box>
                              </Stack>
                              {selected.currentUserRole === 'OWNER' &&
                                member.role === 'MANAGER' &&
                                member.id && (
                                  <Stack direction={{ xs: 'column', sm: 'row' }} spacing={0.5}>
                                    <Button variant="text" onClick={() => requestTransfer(member)}>
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
                    </Stack>
                  </Paper>
                )}

                {activeTab === 'statistics' && (
                  <Paper
                    id="event-panel-statistics"
                    role="tabpanel"
                    aria-labelledby="event-tab-statistics"
                    variant="outlined"
                    sx={{ p: { xs: 2, sm: 3 } }}
                  >
                    <Stack spacing={2} component="section" aria-labelledby="statistics-title">
                      <Box>
                        <Typography id="statistics-title" variant="h5" component="h2">
                          Event statistics
                        </Typography>
                        <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                          Aggregated activity without guest-identifying data.
                        </Typography>
                      </Box>
                      <Button
                        variant="contained"
                        onClick={() => void loadStatistics()}
                        disabled={statisticsLoading}
                      >
                        {statisticsLoading ? 'Loading statistics...' : 'Load statistics'}
                      </Button>
                      {statistics && (
                        <Box
                          sx={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(145px, 1fr))',
                            gap: 1.5,
                          }}
                        >
                          {[
                            ['Media', statistics.mediaCount],
                            ['Photos', statistics.imageCount],
                            ['Videos', statistics.videoCount],
                            ['Uploads', statistics.uploadFileCount],
                            ['Public views', statistics.publicViewCount],
                            ['Downloads', statistics.downloadCount],
                            ['Processing failures', statistics.processingFailureCount],
                            ['Storage bytes', statistics.storageUsedBytes],
                          ].map(([label, value]) => (
                            <Paper key={label} variant="outlined" sx={{ p: 1.5 }}>
                              <Typography variant="body2" color="text.secondary">
                                {label}
                              </Typography>
                              <Typography variant="h5" component="p" sx={{ mt: 0.5 }}>
                                {value}
                              </Typography>
                            </Paper>
                          ))}
                        </Box>
                      )}
                    </Stack>
                  </Paper>
                )}

                {activeTab === 'settings' && (
                  <Stack
                    id="event-panel-settings"
                    role="tabpanel"
                    aria-labelledby="event-tab-settings"
                    spacing={2}
                  >
                    <Paper variant="outlined" sx={{ p: { xs: 2, sm: 3 } }}>
                      <Typography variant="h5" component="h2">
                        Event settings
                      </Typography>
                      <Typography color="text.secondary" sx={{ mt: 0.5, mb: 2 }}>
                        Update the event name, date and description from one place.
                      </Typography>
                      <Button
                        variant="outlined"
                        onClick={() => openEdit(selected)}
                        disabled={selected.status === 'ARCHIVED'}
                      >
                        Edit event details
                      </Button>
                    </Paper>
                    {selected.currentUserRole === 'OWNER' && (
                      <Paper
                        variant="outlined"
                        sx={{ p: { xs: 2, sm: 3 }, borderColor: 'error.light', boxShadow: 'none' }}
                      >
                        <Stack
                          direction={{ xs: 'column', sm: 'row' }}
                          spacing={2}
                          sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
                        >
                          <Box>
                            <Typography variant="h6" color="error.main">
                              Event lifecycle
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                              Archive to make this event read-only, or permanently remove it.
                            </Typography>
                          </Box>
                          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                            <Button
                              variant="outlined"
                              onClick={requestArchive}
                              disabled={selected.status === 'ARCHIVED'}
                            >
                              Archive event
                            </Button>
                            <Button color="error" onClick={requestDelete}>
                              Delete event
                            </Button>
                          </Stack>
                        </Stack>
                      </Paper>
                    )}
                  </Stack>
                )}
              </Stack>
            ) : (
              !compactLayout && (
                <Paper
                  variant="outlined"
                  sx={{
                    minHeight: 420,
                    p: { xs: 3, sm: 5 },
                    display: 'grid',
                    placeItems: 'center',
                    textAlign: 'center',
                    borderStyle: 'dashed',
                  }}
                >
                  <Box>
                    <Box
                      aria-hidden="true"
                      sx={{
                        width: 64,
                        height: 64,
                        mx: 'auto',
                        mb: 2,
                        display: 'grid',
                        placeItems: 'center',
                        borderRadius: '50%',
                        bgcolor: 'primary.main',
                        color: 'primary.contrastText',
                        fontSize: '1.5rem',
                        fontWeight: 800,
                      }}
                    >
                      W
                    </Box>
                    <Typography variant="h4" component="h2">
                      Choose an event to manage
                    </Typography>
                    <Typography color="text.secondary" sx={{ mt: 1 }}>
                      Its galleries, people and settings will appear here.
                    </Typography>
                  </Box>
                </Paper>
              )
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
        <DialogTitle>{editingEventId ? 'Edit event' : 'Create event'}</DialogTitle>
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
