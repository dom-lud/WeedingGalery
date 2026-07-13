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
  LinearProgress,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useParams } from 'react-router-dom'
import { publicAccessApi, type PublicGallery } from '../publicAccessApi'
import { uploadApi, type UploadManifestFile } from '../uploadApi'

type AccessState = 'loading' | 'code-required' | 'ready' | 'not-found' | 'rate-limited' | 'error'
type QueueStatus = 'PENDING' | 'UPLOADING' | 'STORED' | 'FAILED' | 'CANCELLED'

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
      error as {
        response?: { status?: number; data?: { code?: string; message?: string } }
      }
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
  if (window.location.hash)
    window.history.replaceState(null, '', window.location.pathname + window.location.search)
  return token ?? sessionStorage.getItem(`gallery-token:${slug}`)
}

function fileId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
}

function fileValidation(file: File) {
  const limit = allowedTypes.get(file.type)
  if (!limit) return 'Only JPEG, PNG, WebP and MP4 files are supported.'
  const extension = file.name.slice(file.name.lastIndexOf('.')).toLowerCase()
  if (allowedExtensions.get(extension) !== file.type) {
    return 'The file extension does not match its declared type.'
  }
  if (file.size === 0) return 'Empty files cannot be uploaded.'
  if (file.size > limit)
    return file.type === 'video/mp4' ? 'Video exceeds 500 MiB.' : 'Image exceeds 25 MiB.'
  return null
}

export default function PublicGalleryPage() {
  const { slug = '' } = useParams()
  const [state, setState] = useState<AccessState>('loading')
  const [gallery, setGallery] = useState<PublicGallery | null>(null)
  const [accessToken, setAccessToken] = useState('')
  const [accessCode, setAccessCode] = useState('')
  const [message, setMessage] = useState('')
  const [queue, setQueue] = useState<QueueFile[]>([])
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)
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
      } else if (details.status === 404) {
        setState('not-found')
      } else if (details.status === 429) {
        setState('rate-limited')
      } else {
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

  const selectFiles = (files: FileList | null) => {
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
      if (signal.aborted) {
        updateQueue(item.id, { status: 'CANCELLED', progress: 0 })
      } else {
        updateQueue(item.id, { status: 'FAILED', error: apiError(error).message })
      }
    }
  }

  const startUpload = async () => {
    const pending = queue.filter((item) => item.status === 'PENDING')
    if (!pending.length) return
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
      <Box sx={{ minHeight: '100dvh', display: 'grid', placeItems: 'center' }}>
        <CircularProgress aria-label="Opening gallery" />
      </Box>
    )
  }

  if (state !== 'ready' || !gallery) {
    const copy =
      state === 'not-found'
        ? ['Gallery unavailable', 'This link is invalid, expired or no longer available.']
        : state === 'rate-limited'
          ? ['Please wait', 'Too many attempts were made. Try again later.']
          : state === 'code-required'
            ? ['Access code required', 'Enter the code shared by the gallery owner.']
            : ['Unable to open gallery', message || 'Check your connection and try again.']
    return (
      <Container
        maxWidth="sm"
        sx={{ minHeight: '100dvh', display: 'grid', placeItems: 'center', py: 3 }}
      >
        <Paper variant="outlined" sx={{ width: '100%', p: { xs: 2.5, sm: 4 } }}>
          <Stack spacing={2} component="main">
            <Typography component="h1" variant="h4">
              {copy[0]}
            </Typography>
            <Typography color="text.secondary">{copy[1]}</Typography>
            {message && state === 'code-required' && <Alert severity="error">{message}</Alert>}
            {state === 'code-required' && (
              <TextField
                label="Access code"
                value={accessCode}
                autoFocus
                slotProps={{ htmlInput: { minLength: 6, maxLength: 64 } }}
                onChange={(event) => setAccessCode(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' && validAccessCode)
                    void enterGallery(accessToken, accessCode)
                }}
              />
            )}
            <Button
              variant="contained"
              disabled={state === 'code-required' && !validAccessCode}
              onClick={() =>
                state === 'code-required'
                  ? void enterGallery(accessToken, accessCode)
                  : void initialize()
              }
            >
              {state === 'code-required' ? 'Open gallery' : 'Try again'}
            </Button>
          </Stack>
        </Paper>
      </Container>
    )
  }

  const storedCount = queue.filter((item) => item.status === 'STORED').length
  const failedCount = queue.filter((item) => item.status === 'FAILED').length
  return (
    <Container maxWidth="md" sx={{ py: { xs: 2, sm: 5 }, minHeight: '100dvh' }}>
      <Stack spacing={3} component="main">
        <Box>
          <Typography component="h1" variant="h3" sx={{ overflowWrap: 'anywhere' }}>
            {gallery.name}
          </Typography>
          {gallery.description && (
            <Typography color="text.secondary" sx={{ mt: 1 }}>
              {gallery.description}
            </Typography>
          )}
        </Box>
        {gallery.moderationMode === 'REQUIRED' && (
          <Alert severity="info">Uploads are reviewed before publication.</Alert>
        )}
        {!gallery.uploadEnabled ? (
          <Alert severity="info">Uploads are not enabled for this gallery.</Alert>
        ) : (
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={2.5}>
                <Box>
                  <Typography component="h2" variant="h5">
                    Share your photos and videos
                  </Typography>
                  <Typography color="text.secondary">
                    JPEG, PNG and WebP up to 25 MiB; MP4 up to 500 MiB. Maximum 50 files.
                  </Typography>
                </Box>
                <Button
                  component="label"
                  variant="contained"
                  size="large"
                  disabled={uploading || sessionId !== null || queue.length >= 50}
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
                {message && <Alert severity="warning">{message}</Alert>}
                {queue.length === 0 ? (
                  <Typography color="text.secondary">No files selected yet.</Typography>
                ) : (
                  <Stack spacing={1.5} aria-label="Upload queue">
                    {queue.map((item) => (
                      <Paper key={item.id} variant="outlined" sx={{ p: 1.5, minWidth: 0 }}>
                        <Stack spacing={1}>
                          <Stack
                            direction="row"
                            spacing={1}
                            sx={{
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              minWidth: 0,
                            }}
                          >
                            <Typography noWrap title={item.file.name}>
                              {item.file.name}
                            </Typography>
                            <Chip
                              size="small"
                              label={item.status}
                              color={
                                item.status === 'STORED'
                                  ? 'success'
                                  : item.status === 'FAILED'
                                    ? 'error'
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
                                <Button
                                  color="inherit"
                                  size="small"
                                  onClick={() => retryFile(item.id)}
                                >
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
                )}
                {queue.length > 0 && (
                  <Typography aria-live="polite">
                    {storedCount} uploaded{failedCount ? `, ${failedCount} failed` : ''}.
                  </Typography>
                )}
                <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                  <Button
                    variant="contained"
                    onClick={() => void startUpload()}
                    disabled={uploading || !queue.some((item) => item.status === 'PENDING')}
                  >
                    Upload pending files
                  </Button>
                  {uploading && (
                    <Button color="error" onClick={() => void cancelUpload()}>
                      Cancel upload
                    </Button>
                  )}
                  {!uploading && sessionId && !queue.some((item) => item.status === 'PENDING') && (
                    <Button
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
              </Stack>
            </CardContent>
          </Card>
        )}
      </Stack>
    </Container>
  )
}
