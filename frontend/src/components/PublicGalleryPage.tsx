import { useEffect, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Container,
  Fade,
  LinearProgress,
  Paper,
  Stack,
  TextField,
  Typography,
  useMediaQuery,
} from '@mui/material'
import { useParams } from 'react-router-dom'
import { publicAccessApi, type PublicGallery } from '../publicAccessApi'
import { uploadApi, type UploadManifestFile } from '../uploadApi'

type AccessState = 'loading' | 'code-required' | 'ready' | 'not-found' | 'rate-limited' | 'error'
type QueueStatus = 'PENDING' | 'UPLOADING' | 'STORED' | 'FAILED' | 'CANCELLED'

const statusCopy: Record<QueueStatus, string> = {
  PENDING: 'Ready',
  UPLOADING: 'Uploading',
  STORED: 'Uploaded',
  FAILED: 'Needs attention',
  CANCELLED: 'Cancelled',
}

interface QueueFile {
  id: string
  file: File
  status: QueueStatus
  progress: number
  error?: string
}

const allowedTypes = new Map([
  ['image/jpeg', 25 * 1024 * 1024],
  ['image/png', 25 * 1024 * 1024],
  ['image/webp', 25 * 1024 * 1024],
  ['video/mp4', 500 * 1024 * 1024],
])
const allowedExtensions = new Map([
  ['.jpg', 'image/jpeg'],
  ['.jpeg', 'image/jpeg'],
  ['.png', 'image/png'],
  ['.webp', 'image/webp'],
  ['.mp4', 'video/mp4'],
])

function apiError(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (
      error as { response?: { status?: number; data?: { code?: string; message?: string } } }
    ).response
    return {
      status: response?.status,
      code: response?.data?.code,
      message: response?.data?.message ?? 'The request could not be completed.',
    }
  }
  return {
    message: navigator.onLine ? 'The request could not be completed.' : 'You appear to be offline.',
  }
}

function tokenFromFragment(slug: string) {
  const fragment = new URLSearchParams(window.location.hash.replace(/^#/, ''))
  const token = fragment.get('token')
  if (token) sessionStorage.setItem(`gallery-token:${slug}`, token)
  if (window.location.hash) {
    window.history.replaceState(null, '', window.location.pathname + window.location.search)
  }
  return token ?? sessionStorage.getItem(`gallery-token:${slug}`)
}

function fileId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
}

function fileValidation(file: File) {
  const limit = allowedTypes.get(file.type)
  if (!limit) return 'Only JPEG, PNG, WebP and MP4 files are supported.'
  const extension = file.name.slice(file.name.lastIndexOf('.')).toLowerCase()
  if (allowedExtensions.get(extension) !== file.type)
    return 'The file extension does not match its declared type.'
  if (file.size === 0) return 'Empty files cannot be uploaded.'
  if (file.size > limit)
    return file.type === 'video/mp4' ? 'Video exceeds 500 MiB.' : 'Image exceeds 25 MiB.'
  return null
}

function AccessPanel({
  state,
  message,
  accessCode,
  validAccessCode,
  onCodeChange,
  onSubmit,
}: {
  state: Exclude<AccessState, 'loading' | 'ready'>
  message: string
  accessCode: string
  validAccessCode: boolean
  onCodeChange: (value: string) => void
  onSubmit: () => void
}) {
  const copy =
    state === 'not-found'
      ? ['Gallery unavailable', 'This link is invalid, expired or no longer available.']
      : state === 'rate-limited'
        ? ['Please wait', 'Too many attempts were made. Try again later.']
        : state === 'code-required'
          ? ['Access code required', 'Enter the code shared by the gallery owner.']
          : ['Unable to open gallery', message || 'Check your connection and try again.']

  return (
    <Box
      component="main"
      sx={{
        minHeight: '100dvh',
        display: 'grid',
        placeItems: 'center',
        py: 3,
        background: (theme) =>
          `radial-gradient(circle at 10% 10%, ${theme.palette.secondary.main}22, transparent 38%)`,
      }}
    >
      <Container maxWidth="sm">
        <Paper
          variant="outlined"
          sx={{ p: { xs: 3, sm: 5 }, boxShadow: '0 24px 70px rgba(58,40,48,.1)' }}
        >
          <Stack spacing={2.5}>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 800 }}>
              Wedding Gallery
            </Typography>
            <Typography component="h1" variant="h3">
              {copy[0]}
            </Typography>
            <Typography color="text.secondary">{copy[1]}</Typography>
            {message && state === 'code-required' && (
              <Alert severity="error" role="alert">
                {message}
              </Alert>
            )}
            {state === 'code-required' && (
              <TextField
                label="Access code"
                value={accessCode}
                autoFocus
                autoComplete="one-time-code"
                slotProps={{ htmlInput: { minLength: 6, maxLength: 64 } }}
                onChange={(event) => onCodeChange(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' && validAccessCode) onSubmit()
                }}
              />
            )}
            <Button
              variant="contained"
              size="large"
              disabled={state === 'code-required' && !validAccessCode}
              onClick={onSubmit}
            >
              {state === 'code-required' ? 'Open gallery' : 'Try again'}
            </Button>
          </Stack>
        </Paper>
      </Container>
    </Box>
  )
}

export default function PublicGalleryPage() {
  const reduceMotion = useMediaQuery('(prefers-reduced-motion: reduce)')
  const { slug = '' } = useParams()
  const [state, setState] = useState<AccessState>('loading')
  const [gallery, setGallery] = useState<PublicGallery | null>(null)
  const [accessToken, setAccessToken] = useState('')
  const [accessCode, setAccessCode] = useState('')
  const [message, setMessage] = useState('')
  const [queue, setQueue] = useState<QueueFile[]>([])
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)
  const [dragActive, setDragActive] = useState(false)
  const [online, setOnline] = useState(() => navigator.onLine)
  const abortRef = useRef<AbortController | null>(null)
  const validAccessCode = /^[\x20-\x7e]{6,64}$/.test(accessCode)

  const enterGallery = async (token: string, code?: string) => {
    setState('loading')
    setMessage('')
    try {
      const response = await publicAccessApi.access(slug, {
        accessToken: token,
        ...(code ? { accessCode: code } : {}),
      })
      setGallery(response.data)
      setState('ready')
    } catch (error) {
      const details = apiError(error)
      if (
        details.code === 'GALLERY_ACCESS_CODE_REQUIRED' ||
        details.code === 'GALLERY_ACCESS_DENIED'
      ) {
        setMessage(details.code === 'GALLERY_ACCESS_DENIED' ? 'That access code is not valid.' : '')
        setState('code-required')
      } else if (details.status === 404) setState('not-found')
      else if (details.status === 429) setState('rate-limited')
      else {
        setMessage(details.message)
        setState('error')
      }
    }
  }

  const initialize = async () => {
    const token = tokenFromFragment(slug)
    if (token) {
      setAccessToken(token)
      await enterGallery(token)
      return
    }
    try {
      const response = await publicAccessApi.get(slug)
      setGallery(response.data)
      setState('ready')
    } catch (error) {
      const details = apiError(error)
      setState(
        details.status === 429 ? 'rate-limited' : details.status === 404 ? 'not-found' : 'error',
      )
      setMessage(details.message)
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void initialize()
    return () => abortRef.current?.abort()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [slug])

  useEffect(() => {
    const updateOnlineState = () => setOnline(navigator.onLine)
    window.addEventListener('online', updateOnlineState)
    window.addEventListener('offline', updateOnlineState)
    return () => {
      window.removeEventListener('online', updateOnlineState)
      window.removeEventListener('offline', updateOnlineState)
    }
  }, [])

  const selectFiles = (files: FileList | File[] | null) => {
    if (!files) return
    const selected = Array.from(files)
    const selectedBytes = selected.reduce((total, file) => total + file.size, 0)
    const queuedBytes = queue.reduce((total, item) => total + item.file.size, 0)
    if (selectedBytes + queuedBytes > 2 * 1024 * 1024 * 1024) {
      setMessage('A batch cannot exceed 2 GiB in total.')
      return
    }
    const next = selected.slice(0, Math.max(0, 50 - queue.length)).map((file) => {
      const error = fileValidation(file)
      return {
        id: fileId(),
        file,
        status: error ? 'FAILED' : 'PENDING',
        progress: 0,
        error,
      } as QueueFile
    })
    setQueue((current) => [...current, ...next])
    if (files.length > next.length) setMessage('A session can contain at most 50 files.')
  }

  const updateQueue = (id: string, update: Partial<QueueFile>) =>
    setQueue((current) => current.map((item) => (item.id === id ? { ...item, ...update } : item)))

  const uploadOne = async (activeSessionId: string, item: QueueFile, signal: AbortSignal) => {
    updateQueue(item.id, { status: 'UPLOADING', progress: 0, error: undefined })
    try {
      await uploadApi.uploadFile(
        slug,
        activeSessionId,
        item.id,
        item.file,
        (progress) => {
          updateQueue(item.id, { progress })
        },
        signal,
      )
      updateQueue(item.id, { status: 'STORED', progress: 100 })
    } catch (error) {
      if (signal.aborted) updateQueue(item.id, { status: 'CANCELLED', progress: 0 })
      else updateQueue(item.id, { status: 'FAILED', error: apiError(error).message })
    }
  }

  const startUpload = async () => {
    const pending = queue.filter((item) => item.status === 'PENDING')
    if (!pending.length || uploading) return
    setUploading(true)
    setMessage('')
    const controller = new AbortController()
    abortRef.current = controller
    try {
      let activeSessionId = sessionId
      if (!activeSessionId) {
        const manifest: UploadManifestFile[] = pending.map((item) => ({
          clientFileId: item.id,
          fileName: item.file.name,
          declaredContentType: item.file.type,
          size: item.file.size,
        }))
        const response = await uploadApi.createSession(slug, manifest, fileId())
        activeSessionId = response.data.id
        setSessionId(activeSessionId)
      }
      let nextFile = 0
      const worker = async () => {
        while (nextFile < pending.length) {
          const item = pending[nextFile++]
          await uploadOne(activeSessionId!, item, controller.signal)
        }
      }
      await Promise.all(Array.from({ length: Math.min(3, pending.length) }, () => worker()))
    } catch (error) {
      setMessage(apiError(error).message)
    } finally {
      setUploading(false)
      abortRef.current = null
    }
  }

  const retryFile = (id: string) =>
    updateQueue(id, { status: 'PENDING', progress: 0, error: undefined })

  const cancelUpload = async () => {
    abortRef.current?.abort()
    if (sessionId) {
      try {
        await uploadApi.cancel(slug, sessionId)
      } catch (error) {
        setMessage(apiError(error).message)
      }
    }
    setQueue((current) =>
      current.map((item) => (item.status === 'STORED' ? item : { ...item, status: 'CANCELLED' })),
    )
    setUploading(false)
  }

  if (state === 'loading') {
    return (
      <Stack
        component="main"
        spacing={2}
        role="status"
        aria-live="polite"
        sx={{ minHeight: '100dvh', alignItems: 'center', justifyContent: 'center' }}
      >
        <CircularProgress aria-label="Opening gallery" />
        <Typography color="text.secondary">Opening this private gallery…</Typography>
      </Stack>
    )
  }

  if (state !== 'ready') {
    return (
      <AccessPanel
        state={state}
        message={message}
        accessCode={accessCode}
        validAccessCode={validAccessCode}
        onCodeChange={setAccessCode}
        onSubmit={() =>
          state === 'code-required' ? void enterGallery(accessToken, accessCode) : void initialize()
        }
      />
    )
  }

  if (!gallery) {
    return (
      <AccessPanel
        state="error"
        message="We could not open this gallery. Please try again."
        accessCode={accessCode}
        validAccessCode={validAccessCode}
        onCodeChange={setAccessCode}
        onSubmit={() => void initialize()}
      />
    )
  }

  const storedCount = queue.filter((item) => item.status === 'STORED').length
  const failedCount = queue.filter((item) => item.status === 'FAILED').length
  const totalProgress = queue.length
    ? Math.round(queue.reduce((total, item) => total + item.progress, 0) / queue.length)
    : 0

  return (
    <Box component="main" sx={{ minHeight: '100dvh', pb: 6 }}>
      <Box
        sx={{
          color: 'primary.contrastText',
          background: (theme) =>
            `linear-gradient(145deg, ${theme.palette.primary.dark}, ${theme.palette.primary.main} 65%, ${theme.palette.secondary.dark})`,
        }}
      >
        <Fade in timeout={reduceMotion ? 0 : 420} appear={!reduceMotion}>
          <Container maxWidth="md" sx={{ py: { xs: 5, sm: 8 } }}>
            <Typography variant="overline" sx={{ letterSpacing: '0.16em', fontWeight: 800 }}>
              A private collection
            </Typography>
            <Typography
              component="h1"
              variant="h1"
              sx={{ mt: 1, color: 'inherit', overflowWrap: 'anywhere' }}
            >
              {gallery.name}
            </Typography>
            <Typography
              sx={{ mt: 2, color: 'rgba(255,255,255,.78)', maxWidth: 680, fontSize: '1.05rem' }}
            >
              {gallery.description || 'Share the moments you captured and help complete the story.'}
            </Typography>
          </Container>
        </Fade>
      </Box>

      <Container maxWidth="md" sx={{ mt: { xs: -2, sm: -3 } }}>
        <Stack spacing={2.5}>
          {!online && (
            <Alert severity="warning" role="status">
              You are offline. Selected files will stay here, but uploading needs a connection.
            </Alert>
          )}
          {gallery.moderationMode === 'REQUIRED' && (
            <Alert severity="info">Uploads are reviewed before publication.</Alert>
          )}
          {!gallery.uploadEnabled ? (
            <Alert severity="info">Uploads are not enabled for this gallery.</Alert>
          ) : (
            <Card>
              <CardContent sx={{ p: { xs: 2, sm: 4 }, '&:last-child': { pb: { xs: 2, sm: 4 } } }}>
                <Stack spacing={3}>
                  <Box>
                    <Typography component="h2" variant="h3">
                      Share your photos and videos
                    </Typography>
                    <Typography color="text.secondary" sx={{ mt: 1 }}>
                      JPEG, PNG and WebP up to 25 MiB; MP4 up to 500 MiB. Maximum 50 files.
                    </Typography>
                  </Box>

                  <Paper
                    variant="outlined"
                    onDragEnter={(event) => {
                      event.preventDefault()
                      setDragActive(true)
                    }}
                    onDragOver={(event) => event.preventDefault()}
                    onDragLeave={() => setDragActive(false)}
                    onDrop={(event) => {
                      event.preventDefault()
                      setDragActive(false)
                      selectFiles(event.dataTransfer.files)
                    }}
                    sx={{
                      p: { xs: 3, sm: 5 },
                      textAlign: 'center',
                      borderStyle: 'dashed',
                      borderWidth: 2,
                      borderColor: dragActive ? 'primary.main' : 'divider',
                      bgcolor: dragActive ? 'action.hover' : 'background.default',
                    }}
                  >
                    <Stack spacing={1.5} sx={{ alignItems: 'center' }}>
                      <Box
                        aria-hidden="true"
                        sx={{
                          width: 54,
                          height: 54,
                          display: 'grid',
                          placeItems: 'center',
                          borderRadius: '50%',
                          bgcolor: 'primary.main',
                          color: 'primary.contrastText',
                          fontSize: '1.5rem',
                          fontWeight: 800,
                        }}
                      >
                        +
                      </Box>
                      <Typography variant="h6">Add moments from your device</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Choose from your camera or gallery. On desktop, you can also drop files
                        here.
                      </Typography>
                      <Button
                        component="label"
                        variant="contained"
                        size="large"
                        disabled={uploading || sessionId !== null || queue.length >= 50 || !online}
                      >
                        Choose files
                        <input
                          hidden
                          type="file"
                          multiple
                          accept="image/jpeg,image/png,image/webp,video/mp4"
                          onChange={(event) => selectFiles(event.target.files)}
                        />
                      </Button>
                    </Stack>
                  </Paper>

                  {message && (
                    <Alert severity="warning" role="alert">
                      {message}
                    </Alert>
                  )}

                  {queue.length === 0 ? (
                    <Typography color="text.secondary" sx={{ textAlign: 'center' }}>
                      Your upload queue is empty.
                    </Typography>
                  ) : (
                    <Stack spacing={2}>
                      <Paper variant="outlined" sx={{ p: 2, bgcolor: 'background.default' }}>
                        <Stack spacing={1}>
                          <Stack direction="row" sx={{ justifyContent: 'space-between' }}>
                            <Typography sx={{ fontWeight: 800 }}>
                              {queue.length} selected
                            </Typography>
                            <Typography color="text.secondary">{totalProgress}%</Typography>
                          </Stack>
                          <LinearProgress
                            variant="determinate"
                            value={totalProgress}
                            aria-label="Overall upload progress"
                          />
                        </Stack>
                      </Paper>
                      <Stack
                        component="ul"
                        spacing={1.5}
                        aria-label="Upload queue"
                        sx={{ p: 0, m: 0, listStyle: 'none' }}
                      >
                        {queue.map((item) => (
                          <Paper
                            component="li"
                            key={item.id}
                            variant="outlined"
                            sx={{ p: 2, minWidth: 0 }}
                          >
                            <Stack spacing={1.25}>
                              <Stack
                                direction={{ xs: 'column', sm: 'row' }}
                                spacing={1}
                                sx={{
                                  justifyContent: 'space-between',
                                  alignItems: { sm: 'center' },
                                  minWidth: 0,
                                }}
                              >
                                <Box sx={{ minWidth: 0 }}>
                                  <Typography
                                    sx={{ fontWeight: 700, overflowWrap: 'anywhere' }}
                                    title={item.file.name}
                                  >
                                    {item.file.name}
                                  </Typography>
                                  <Typography variant="body2" color="text.secondary">
                                    {(item.file.size / (1024 * 1024)).toFixed(1)} MiB
                                  </Typography>
                                </Box>
                                <Chip
                                  size="small"
                                  label={statusCopy[item.status]}
                                  color={
                                    item.status === 'STORED'
                                      ? 'success'
                                      : item.status === 'FAILED'
                                        ? 'error'
                                        : item.status === 'UPLOADING'
                                          ? 'primary'
                                          : 'default'
                                  }
                                />
                              </Stack>
                              {(item.status === 'UPLOADING' || item.status === 'STORED') && (
                                <LinearProgress
                                  variant="determinate"
                                  value={item.progress}
                                  aria-label={`Upload progress for ${item.file.name}`}
                                />
                              )}
                              {item.error && (
                                <Alert
                                  severity="error"
                                  action={
                                    <Button color="inherit" onClick={() => retryFile(item.id)}>
                                      Retry
                                    </Button>
                                  }
                                >
                                  {item.error}
                                </Alert>
                              )}
                            </Stack>
                          </Paper>
                        ))}
                      </Stack>
                    </Stack>
                  )}

                  {queue.length > 0 && (
                    <Typography aria-live="polite" role="status">
                      {storedCount} uploaded{failedCount ? `, ${failedCount} failed` : ''}.
                    </Typography>
                  )}

                  {queue.length > 0 && (
                    <Box
                      data-testid="upload-actions"
                      sx={{
                        position: { xs: 'sticky', sm: 'static' },
                        zIndex: { xs: 10, sm: 'auto' },
                        bottom: { xs: 8, sm: 'auto' },
                        p: { xs: 1.5, sm: 0 },
                        bgcolor: { xs: 'background.paper', sm: 'transparent' },
                        border: { xs: '1px solid', sm: 0 },
                        borderColor: 'divider',
                        borderRadius: { xs: 2, sm: 0 },
                        boxShadow: { xs: '0 12px 32px rgba(58, 40, 48, 0.14)', sm: 'none' },
                      }}
                    >
                      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                        <Button
                          variant="contained"
                          size="large"
                          onClick={() => void startUpload()}
                          disabled={
                            uploading || !online || !queue.some((item) => item.status === 'PENDING')
                          }
                          sx={{ flex: 1 }}
                        >
                          {uploading ? 'Uploading…' : 'Upload pending files'}
                        </Button>
                        {uploading && (
                          <Button
                            color="error"
                            variant="outlined"
                            onClick={() => void cancelUpload()}
                          >
                            Cancel upload
                          </Button>
                        )}
                        {!uploading &&
                          sessionId &&
                          !queue.some((item) => item.status === 'PENDING') && (
                            <Button
                              variant="outlined"
                              onClick={() => {
                                setQueue([])
                                setSessionId(null)
                                setMessage('')
                              }}
                            >
                              Start another batch
                            </Button>
                          )}
                      </Stack>
                    </Box>
                  )}
                </Stack>
              </CardContent>
            </Card>
          )}
        </Stack>
      </Container>
    </Box>
  )
}
