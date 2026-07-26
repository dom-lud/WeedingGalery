import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Skeleton,
  Stack,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material'
import { useAuth } from '../AuthContext'
import {
  adminApi,
  pageItems,
  type AdminAuditEntry,
  type AdminDashboardSummary,
  type AdminEvent,
  type AdminUser,
} from '../adminApi'
import AppShell from './layout/AppShell'

type AdminSessionUser = { email: string; systemRole?: 'ADMIN' | 'USER' }
type TabKey = 'users' | 'events' | 'audit'

function errorMessage(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response
    return response?.data?.message ?? 'Administrative data is unavailable.'
  }
  return 'Administrative data is unavailable.'
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`
  const units = ['KB', 'MB', 'GB', 'TB']
  let value = bytes
  let unit = -1
  do {
    value /= 1024
    unit += 1
  } while (value >= 1024 && unit < units.length - 1)
  return `${value.toFixed(value >= 10 ? 0 : 1)} ${units[unit]}`
}

function normalizeSummary(summary: AdminDashboardSummary) {
  const legacy = summary as AdminDashboardSummary & { activeUsers?: number; storageBytes?: number }
  return {
    users: summary.users ?? 0,
    lockedUsers: summary.lockedUsers ?? 0,
    events: summary.events ?? 0,
    galleries: summary.galleries ?? 0,
    media: summary.media ?? 0,
    storageUsedBytes: summary.storageUsedBytes ?? legacy.storageBytes ?? 0,
    auditEvents: summary.auditEvents ?? 0,
  }
}

export default function AdminDashboard() {
  const { user, logout } = useAuth()
  const sessionUser = user as AdminSessionUser | null
  const isAdmin = sessionUser?.systemRole === 'ADMIN'
  const [summary, setSummary] = useState<AdminDashboardSummary | null>(null)
  const [users, setUsers] = useState<AdminUser[]>([])
  const [events, setEvents] = useState<AdminEvent[]>([])
  const [audit, setAudit] = useState<AdminAuditEntry[]>([])
  const [tab, setTab] = useState<TabKey>('users')
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [confirmation, setConfirmation] = useState<AdminUser | null>(null)
  const [actionBusy, setActionBusy] = useState(false)

  const load = useCallback(async () => {
    if (!isAdmin) return
    setLoading(true)
    setError(null)
    try {
      const [summaryResponse, usersResponse, eventsResponse, auditResponse] = await Promise.all([
        adminApi.dashboard(),
        adminApi.users({ query: query || undefined, page: 0, size: 25 }),
        adminApi.events({ query: query || undefined, page: 0, size: 25 }),
        adminApi.audit({ query: query || undefined, page: 0, size: 25 }),
      ])
      setSummary(normalizeSummary(summaryResponse.data))
      setUsers(pageItems(usersResponse.data))
      setEvents(pageItems(eventsResponse.data))
      setAudit(pageItems(auditResponse.data))
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }, [isAdmin, query])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void load()
  }, [load])

  const visibleRows = useMemo(() => {
    if (tab === 'users') return users
    if (tab === 'events') return events
    return audit
  }, [audit, events, tab, users])

  const toggleUser = async () => {
    if (!confirmation || actionBusy) return
    setActionBusy(true)
    try {
      if (!confirmation.locked) await adminApi.blockUser(confirmation.id)
      else await adminApi.unblockUser(confirmation.id)
      setConfirmation(null)
      await load()
    } catch (requestError) {
      setError(errorMessage(requestError))
    } finally {
      setActionBusy(false)
    }
  }

  if (!isAdmin) {
    return (
      <AppShell email={sessionUser?.email} onLogout={() => void logout()}>
        <Alert severity="error" role="alert">
          You do not have permission to access the administration panel.
        </Alert>
      </AppShell>
    )
  }

  return (
    <AppShell email={sessionUser.email} onLogout={() => void logout()}>
      <Stack spacing={3}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          sx={{ justifyContent: 'space-between' }}
        >
          <Box>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 700 }}>
              System administration
            </Typography>
            <Typography variant="h2" component="h1">
              Administration
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              Monitor the platform and manage system accounts.
            </Typography>
          </Box>
          <Button variant="outlined" onClick={() => void load()} disabled={loading}>
            Refresh
          </Button>
        </Stack>

        {error && (
          <Alert
            severity="error"
            role="alert"
            action={
              <Button color="inherit" onClick={() => void load()}>
                Retry
              </Button>
            }
          >
            {error}
          </Alert>
        )}

        {loading && !summary ? (
          <Box
            role="status"
            aria-label="Loading administration data"
            sx={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
              gap: 2,
            }}
          >
            {[1, 2, 3, 4, 5, 6].map((item) => (
              <Skeleton key={item} variant="rounded" height={104} />
            ))}
          </Box>
        ) : summary ? (
          <Box
            component="section"
            aria-label="Platform summary"
            sx={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
              gap: 2,
            }}
          >
            {[
              ['Users', summary.users],
              ['Locked users', summary.lockedUsers],
              ['Events', summary.events],
              ['Galleries', summary.galleries],
              ['Media files', summary.media],
              ['Storage', formatBytes(summary.storageUsedBytes ?? 0)],
              ['Audit events', summary.auditEvents],
            ].map(([label, value]) => (
              <Paper key={label} variant="outlined" sx={{ p: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  {label}
                </Typography>
                <Typography variant="h4" component="p" sx={{ mt: 1 }}>
                  {value}
                </Typography>
              </Paper>
            ))}
          </Box>
        ) : null}

        <Paper
          component="section"
          aria-labelledby="admin-data-title"
          variant="outlined"
          sx={{ overflow: 'hidden' }}
        >
          <Stack spacing={2} sx={{ p: { xs: 2, sm: 3 } }}>
            <Stack
              direction={{ xs: 'column', md: 'row' }}
              spacing={2}
              sx={{ justifyContent: 'space-between', alignItems: { md: 'center' } }}
            >
              <Typography id="admin-data-title" variant="h4" component="h2">
                System data
              </Typography>
              <TextField
                label="Search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                size="small"
                slotProps={{ htmlInput: { 'aria-label': 'Search administrative data' } }}
              />
            </Stack>
            <Tabs
              value={tab}
              onChange={(_, value: TabKey) => setTab(value)}
              aria-label="Administration sections"
            >
              <Tab value="users" label="Users" />
              <Tab value="events" label="Events" />
              <Tab value="audit" label="Audit" />
            </Tabs>
            <Box role="tabpanel" aria-label={`${tab} data`} sx={{ overflowX: 'auto' }}>
              {visibleRows.length === 0 ? (
                <Typography color="text.secondary" sx={{ py: 3 }}>
                  No records found.
                </Typography>
              ) : tab === 'users' ? (
                <Stack component="ul" spacing={1} sx={{ listStyle: 'none', p: 0, m: 0 }}>
                  {(visibleRows as AdminUser[]).map((item) => (
                    <Paper component="li" key={item.id} variant="outlined" sx={{ p: 1.5 }}>
                      <Stack
                        direction={{ xs: 'column', sm: 'row' }}
                        spacing={1}
                        sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
                      >
                        <Box>
                          <Typography sx={{ fontWeight: 700 }}>{item.email}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            {item.systemRole}
                          </Typography>
                        </Box>
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                          <Chip
                            size="small"
                            label={item.locked ? 'LOCKED' : 'ACTIVE'}
                            color={item.locked ? 'default' : 'success'}
                          />
                          <Button
                            size="small"
                            color={item.locked ? 'primary' : 'error'}
                            onClick={() => setConfirmation(item)}
                          >
                            {item.locked ? 'Unblock' : 'Block'}
                          </Button>
                        </Stack>
                      </Stack>
                    </Paper>
                  ))}
                </Stack>
              ) : tab === 'events' ? (
                <Stack component="ul" spacing={1} sx={{ listStyle: 'none', p: 0, m: 0 }}>
                  {(visibleRows as AdminEvent[]).map((item) => (
                    <Paper component="li" key={item.id} variant="outlined" sx={{ p: 1.5 }}>
                      <Typography sx={{ fontWeight: 700 }}>{item.name}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Owner: {item.ownerEmail} · {item.status} · {item.eventDate}
                      </Typography>
                    </Paper>
                  ))}
                </Stack>
              ) : (
                <Stack component="ul" spacing={1} sx={{ listStyle: 'none', p: 0, m: 0 }}>
                  {(visibleRows as AdminAuditEntry[]).map((item) => (
                    <Paper component="li" key={item.id} variant="outlined" sx={{ p: 1.5 }}>
                      <Typography sx={{ fontWeight: 700 }}>{item.eventType}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {item.actorEmail ?? 'System'} · {item.result ?? 'Recorded'}
                      </Typography>
                    </Paper>
                  ))}
                </Stack>
              )}
            </Box>
          </Stack>
        </Paper>
      </Stack>

      <Dialog
        open={Boolean(confirmation)}
        onClose={actionBusy ? undefined : () => setConfirmation(null)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle>
          {confirmation && !confirmation.locked ? 'Block user?' : 'Unblock user?'}
        </DialogTitle>
        <DialogContent>
          <Typography>
            {confirmation?.email} will {confirmation && !confirmation.locked ? 'lose' : 'regain'}{' '}
            access to the platform.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmation(null)} disabled={actionBusy}>
            Cancel
          </Button>
          <Button
            onClick={() => void toggleUser()}
            disabled={actionBusy}
            color={confirmation && !confirmation.locked ? 'error' : 'primary'}
            variant="contained"
            autoFocus
          >
            {actionBusy
              ? 'Working...'
              : confirmation && !confirmation.locked
                ? 'Block user'
                : 'Unblock user'}
          </Button>
        </DialogActions>
      </Dialog>
    </AppShell>
  )
}
