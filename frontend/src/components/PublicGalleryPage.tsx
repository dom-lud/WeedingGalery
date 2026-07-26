import { useEffect, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Dialog,
  DialogContent,
  DialogTitle,
  Fade,
  IconButton,
  LinearProgress,
  Paper,
  Stack,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
  useMediaQuery,
} from '@mui/material'
import { useParams } from 'react-router-dom'
import { publicAccessApi, type PublicGallery, type PublicMedia } from '../publicAccessApi'
import { uploadApi, type UploadFileStatus, type UploadManifestFile } from '../uploadApi'

type AccessState = 'loading' | 'code-required' | 'ready' | 'not-found' | 'rate-limited' | 'error'
type MediaFilter = 'ALL' | 'IMAGE' | 'VIDEO'
type MediaState = 'loading' | 'ready' | 'error'
type QueueStatus =
  | 'PENDING'
  | 'UPLOADING'
  | 'STORED'
  | 'PROCESSING'
  | 'PROCESSED'
  | 'PROCESSING_FAILED'
  | 'FAILED'
  | 'CANCELLED'

interface QueueFile {
  id: string
  file: File
  status: QueueStatus
  progress: number
  retryable: boolean
  previewUrl: string
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

function queueStatusFromApi(status: UploadFileStatus): QueueStatus {
  if (status === 'RECEIVING') return 'UPLOADING'
  if (status === 'CLEANUP_REQUIRED') return 'FAILED'
  return status
}

function processingTerminal(status: QueueStatus) {
  return status !== 'UPLOADING' && status !== 'STORED' && status !== 'PROCESSING'
}

function createPreviewUrl(file: File) {
  return typeof URL !== 'undefined' && 'createObjectURL' in URL ? URL.createObjectURL(file) : ''
}

function revokePreviewUrl(url: string) {
  if (url && typeof URL !== 'undefined' && 'revokeObjectURL' in URL) URL.revokeObjectURL(url)
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
  const [mediaState, setMediaState] = useState<MediaState>('loading')
  const [mediaFilter, setMediaFilter] = useState<MediaFilter>('ALL')
  const [queue, setQueue] = useState<QueueFile[]>([])
  const [sessionId, setSessionId] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)
  const [dragActive, setDragActive] = useState(false)
  const [online, setOnline] = useState(() => navigator.onLine)
  const [activeMedia, setActiveMedia] = useState<PublicMedia | null>(null)
  const activeMediaTriggerRef = useRef<HTMLButtonElement | null>(null)
  const abortRef = useRef<AbortController | null>(null)
  const queuePreviewUrlsRef = useRef<string[]>([])
  const autoUploadTimerRef = useRef<number | null>(null)
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
      setMediaState('ready')
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
      setMediaState('ready')
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

  useEffect(() => {
    if (activeMedia === null) activeMediaTriggerRef.current?.focus()
  }, [activeMedia])

  useEffect(() => {
    queuePreviewUrlsRef.current = queue.map((item) => item.previewUrl).filter(Boolean)
  }, [queue])

  useEffect(
    () => () => {
      if (autoUploadTimerRef.current) window.clearTimeout(autoUploadTimerRef.current)
      queuePreviewUrlsRef.current.forEach(revokePreviewUrl)
    },
    [],
  )

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
        retryable: false,
        previewUrl: createPreviewUrl(file),
        error,
      } as QueueFile
    })
    setQueue((current) => [...current, ...next])
    if (files.length > next.length) setMessage('A session can contain at most 50 files.')
  }

  const updateQueue = (id: string, update: Partial<QueueFile>) =>
    setQueue((current) => current.map((item) => (item.id === id ? { ...item, ...update } : item)))

  const removeQueuedFile = (id: string) => {
    setQueue((current) => {
      const removed = current.find((item) => item.id === id)
      if (removed) revokePreviewUrl(removed.previewUrl)
      const next = current.filter((item) => item.id !== id)
      if (next.length === 0) setSessionId(null)
      return next
    })
    setMessage('')
  }

  const uploadOne = async (activeSessionId: string, item: QueueFile, signal: AbortSignal) => {
    updateQueue(item.id, { status: 'UPLOADING', progress: 0, error: undefined })
    try {
      const response = await uploadApi.uploadFile(
        slug,
        activeSessionId,
        item.id,
        item.file,
        (progress) => {
          updateQueue(item.id, { progress })
        },
        signal,
      )
      updateQueue(item.id, {
        status: queueStatusFromApi(response.data.status),
        progress: 100,
        error: response.data.errorCode,
      })
      return true
    } catch (error) {
      if (signal.aborted) updateQueue(item.id, { status: 'CANCELLED', progress: 0 })
      else
        updateQueue(item.id, { status: 'FAILED', retryable: true, error: apiError(error).message })
      return false
    }
  }

  const refreshSessionState = async (
    activeSessionId: string,
    trackedIds: Set<string>,
    signal: AbortSignal,
  ) => {
    try {
      for (let attempt = 0; attempt < 8 && !signal.aborted; attempt += 1) {
        const response = await uploadApi.getSession(slug, activeSessionId)
        const serverFiles = response.data.files.filter((file) => trackedIds.has(file.clientFileId))
        setQueue((current) =>
          current.map((item) => {
            const serverFile = serverFiles.find((file) => file.clientFileId === item.id)
            if (!serverFile) return item
            const nextStatus = queueStatusFromApi(serverFile.status)
            return {
              ...item,
              status: nextStatus,
              progress:
                nextStatus === 'PROCESSED' ||
                nextStatus === 'PROCESSING' ||
                nextStatus === 'PROCESSING_FAILED' ||
                nextStatus === 'STORED'
                  ? 100
                  : item.progress,
              error: serverFile.errorCode ?? item.error,
            }
          }),
        )
        if (serverFiles.every((file) => processingTerminal(queueStatusFromApi(file.status)))) return
        await new Promise((resolve) => setTimeout(resolve, 500))
      }
    } catch {
      // Status polling is best-effort; the upload result remains visible and retryable.
    }
  }

  const startUpload = async () => {
    const pending = queue.filter((item) => item.status === 'PENDING')
    if (!pending.length || uploading) return
    if (autoUploadTimerRef.current) {
      window.clearTimeout(autoUploadTimerRef.current)
      autoUploadTimerRef.current = null
    }
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
      const failedIds = new Set<string>()
      const worker = async () => {
        while (nextFile < pending.length) {
          const item = pending[nextFile++]
          const uploaded = await uploadOne(activeSessionId!, item, controller.signal)
          if (!uploaded) failedIds.add(item.id)
        }
      }
      await Promise.all(Array.from({ length: Math.min(3, pending.length) }, () => worker()))
      if (controller.signal.aborted) return
      await refreshSessionState(
        activeSessionId,
        new Set(pending.map((item) => item.id)),
        controller.signal,
      )
      if (controller.signal.aborted) return
      const latestGallery = await publicAccessApi.get(slug)
      setGallery(latestGallery.data)
      setQueue((current) => {
        const next = current.filter((item) => failedIds.has(item.id))
        current
          .filter((item) => !failedIds.has(item.id))
          .forEach((item) => revokePreviewUrl(item.previewUrl))
        if (next.length === 0) setSessionId(null)
        return next
      })
    } catch (error) {
      setMessage(apiError(error).message)
    } finally {
      setUploading(false)
      abortRef.current = null
    }
  }

  const retryFile = (id: string) => {
    const item = queue.find((candidate) => candidate.id === id)
    if (!item?.retryable) return
    const error = fileValidation(item.file)
    updateQueue(id, {
      status: error ? 'FAILED' : 'PENDING',
      progress: 0,
      retryable: !error,
      error: error ?? undefined,
    })
  }

  useEffect(() => {
    if (autoUploadTimerRef.current) {
      window.clearTimeout(autoUploadTimerRef.current)
      autoUploadTimerRef.current = null
    }
    if (
      !gallery?.uploadEnabled ||
      uploading ||
      !online ||
      !queue.some((item) => item.status === 'PENDING')
    ) {
      return
    }
    autoUploadTimerRef.current = window.setTimeout(() => {
      autoUploadTimerRef.current = null
      void startUpload()
    }, 450)
    return () => {
      if (autoUploadTimerRef.current) window.clearTimeout(autoUploadTimerRef.current)
    }
    // startUpload intentionally stays out of deps; this effect reacts to queue state.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [gallery?.uploadEnabled, online, queue, uploading])

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
      current.map((item) =>
        ['STORED', 'PROCESSING', 'PROCESSED', 'PROCESSING_FAILED'].includes(item.status)
          ? item
          : { ...item, status: 'CANCELLED' },
      ),
    )
    setUploading(false)
  }

  const retryMedia = async () => {
    setMediaState('loading')
    try {
      const response = await publicAccessApi.get(slug)
      setGallery(response.data)
      setMediaState('ready')
    } catch (error) {
      setMediaState('error')
      setMessage(apiError(error).message)
    }
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
        <Typography color="text.secondary">Opening this private galleryâ€¦</Typography>
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

  const uploadedCount = queue.filter((item) =>
    ['STORED', 'PROCESSING', 'PROCESSED', 'PROCESSING_FAILED'].includes(item.status),
  ).length
  const processingCount = queue.filter((item) =>
    ['STORED', 'PROCESSING'].includes(item.status),
  ).length
  const failedCount = queue.filter((item) =>
    ['FAILED', 'PROCESSING_FAILED'].includes(item.status),
  ).length
  const totalProgress = queue.length
    ? Math.round(queue.reduce((total, item) => total + item.progress, 0) / queue.length)
    : 0
  const publicMedia = gallery.media ?? []
  const filteredMedia =
    mediaFilter === 'ALL'
      ? publicMedia
      : publicMedia.filter((item) => item.mediaType === mediaFilter)

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
                        disabled={uploading || queue.length >= 50 || !online}
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
                      <Box
                        component="ul"
                        aria-label="Upload queue"
                        sx={{
                          display: 'grid',
                          gridTemplateColumns: {
                            xs: 'repeat(2, minmax(0, 1fr))',
                            sm: 'repeat(3, minmax(0, 1fr))',
                          },
                          gap: 1.5,
                          p: 0,
                          m: 0,
                          listStyle: 'none',
                        }}
                      >
                        {queue.map((item) => (
                          <Box
                            component="li"
                            key={item.id}
                            sx={{
                              position: 'relative',
                              overflow: 'hidden',
                              borderRadius: 1,
                              aspectRatio: '1',
                              bgcolor: 'action.hover',
                              border: '1px solid',
                              borderColor:
                                item.status === 'FAILED' || item.status === 'PROCESSING_FAILED'
                                  ? 'error.main'
                                  : 'divider',
                            }}
                          >
                            {item.previewUrl && item.file.type.startsWith('image/') ? (
                              <Box
                                component="img"
                                src={item.previewUrl}
                                alt={item.file.name}
                                sx={{
                                  width: '100%',
                                  height: '100%',
                                  objectFit: 'cover',
                                  filter:
                                    item.status === 'PENDING' ||
                                    item.status === 'UPLOADING' ||
                                    item.status === 'PROCESSING'
                                      ? 'blur(8px)'
                                      : 'none',
                                  transform:
                                    item.status === 'PENDING' ||
                                    item.status === 'UPLOADING' ||
                                    item.status === 'PROCESSING'
                                      ? 'scale(1.04)'
                                      : 'none',
                                  transition: 'filter 180ms ease, transform 180ms ease',
                                }}
                              />
                            ) : item.previewUrl && item.file.type === 'video/mp4' ? (
                              <Box
                                component="video"
                                src={item.previewUrl}
                                muted
                                playsInline
                                sx={{
                                  width: '100%',
                                  height: '100%',
                                  objectFit: 'cover',
                                  filter: item.status === 'PROCESSED' ? 'none' : 'blur(8px)',
                                  transform: item.status === 'PROCESSED' ? 'none' : 'scale(1.04)',
                                }}
                              />
                            ) : (
                              <Box
                                sx={{
                                  width: '100%',
                                  height: '100%',
                                  display: 'grid',
                                  placeItems: 'center',
                                  color: 'text.secondary',
                                  fontWeight: 800,
                                }}
                              >
                                {item.file.type === 'video/mp4' ? 'MP4' : 'IMG'}
                              </Box>
                            )}
                            {(item.status === 'PENDING' ||
                              item.status === 'UPLOADING' ||
                              item.status === 'PROCESSING') && (
                              <Box
                                sx={{
                                  position: 'absolute',
                                  inset: 0,
                                  display: 'grid',
                                  placeItems: 'center',
                                  bgcolor: 'rgba(0,0,0,.25)',
                                }}
                              >
                                <CircularProgress
                                  size={34}
                                  thickness={5}
                                  aria-label={`Adding ${item.file.name}`}
                                  sx={{ color: 'common.white' }}
                                />
                              </Box>
                            )}
                            <Box
                              sx={{
                                position: 'absolute',
                                left: 0,
                                right: 0,
                                bottom: 0,
                                p: 1,
                                color: 'common.white',
                                background: 'linear-gradient(0deg, rgba(0,0,0,.72), rgba(0,0,0,0))',
                              }}
                            >
                              <Typography
                                variant="caption"
                                sx={{
                                  display: 'block',
                                  fontWeight: 800,
                                  overflow: 'hidden',
                                  textOverflow: 'ellipsis',
                                  whiteSpace: 'nowrap',
                                }}
                                title={item.file.name}
                              >
                                {item.file.name}
                              </Typography>
                              {item.status === 'FAILED' && (
                                <Typography variant="caption" role="alert">
                                  {item.error}
                                </Typography>
                              )}
                            </Box>
                            {!uploading &&
                              ['PENDING', 'FAILED', 'CANCELLED'].includes(item.status) && (
                                <IconButton
                                  aria-label={`Remove ${item.file.name}`}
                                  color="error"
                                  size="small"
                                  onClick={() => removeQueuedFile(item.id)}
                                  sx={{
                                    position: 'absolute',
                                    top: 8,
                                    right: 8,
                                    width: 34,
                                    height: 34,
                                    bgcolor: 'rgba(255,255,255,.9)',
                                    color: 'error.main',
                                    fontWeight: 900,
                                    '&:hover': { bgcolor: 'common.white' },
                                  }}
                                >
                                  X
                                </IconButton>
                              )}
                            {item.error && item.status === 'FAILED' && item.retryable && (
                              <Button
                                variant="contained"
                                size="small"
                                onClick={() => retryFile(item.id)}
                                sx={{ position: 'absolute', top: 8, left: 8, minWidth: 0 }}
                              >
                                Try again
                              </Button>
                            )}
                          </Box>
                        ))}
                      </Box>
                    </Stack>
                  )}

                  {queue.length > 0 && (
                    <Typography aria-live="polite" role="status">
                      {uploadedCount} added
                      {processingCount ? `, ${processingCount} getting ready` : ''}
                      {failedCount ? `, ${failedCount} need attention` : ''}.
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
                          {uploading ? 'Adding...' : 'Add now'}
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
                      </Stack>
                    </Box>
                  )}
                </Stack>
              </CardContent>
            </Card>
          )}
          <Card>
            <CardContent sx={{ p: { xs: 2, sm: 4 }, '&:last-child': { pb: { xs: 2, sm: 4 } } }}>
              <Stack spacing={2}>
                <Box>
                  <Typography component="h2" variant="h3">
                    Gallery
                  </Typography>
                  <Typography color="text.secondary" sx={{ mt: 1 }}>
                    Photos and videos added by guests appear here as the story grows.
                  </Typography>
                </Box>
                <ToggleButtonGroup
                  value={mediaFilter}
                  exclusive
                  aria-label="Filter gallery media"
                  onChange={(_event, value: MediaFilter | null) => {
                    if (value) setMediaFilter(value)
                  }}
                  size="small"
                  sx={{ alignSelf: 'flex-start', maxWidth: '100%', flexWrap: 'wrap' }}
                >
                  <ToggleButton value="ALL" aria-label="Show all media">
                    All
                  </ToggleButton>
                  <ToggleButton value="IMAGE" aria-label="Show images only">
                    Images
                  </ToggleButton>
                  <ToggleButton value="VIDEO" aria-label="Show videos only">
                    Videos
                  </ToggleButton>
                </ToggleButtonGroup>
                {mediaState === 'loading' ? (
                  <Stack
                    role="status"
                    aria-live="polite"
                    spacing={1}
                    sx={{ alignItems: 'center', py: 4 }}
                  >
                    <CircularProgress size={32} aria-label="Loading gallery media" />
                    <Typography color="text.secondary">Loading gallery media...</Typography>
                  </Stack>
                ) : mediaState === 'error' ? (
                  <Stack spacing={1.5} sx={{ alignItems: 'flex-start' }}>
                    <Alert severity="error">We could not load the gallery media.</Alert>
                    <Button variant="outlined" onClick={() => void retryMedia()}>
                      Try again
                    </Button>
                  </Stack>
                ) : filteredMedia.length === 0 ? (
                  <Typography color="text.secondary" role="status" aria-live="polite">
                    {publicMedia.length === 0
                      ? 'No photos or videos have been added yet.'
                      : `No ${mediaFilter === 'IMAGE' ? 'images' : 'videos'} match this filter.`}
                  </Typography>
                ) : (
                  <Box
                    component="ul"
                    aria-label="Gallery media"
                    sx={{
                      display: 'grid',
                      gridTemplateColumns: {
                        xs: 'repeat(2, minmax(0, 1fr))',
                        sm: 'repeat(3, minmax(0, 1fr))',
                      },
                      gap: 1.5,
                      p: 0,
                      m: 0,
                      listStyle: 'none',
                    }}
                  >
                    {filteredMedia.map((item) => (
                      <Box
                        component="li"
                        key={item.id}
                        sx={{
                          minWidth: 0,
                          aspectRatio: '1',
                          borderRadius: 1,
                          overflow: 'hidden',
                          bgcolor: 'action.hover',
                        }}
                      >
                        <Button
                          onClick={(event) => {
                            activeMediaTriggerRef.current = event.currentTarget
                            setActiveMedia(item)
                          }}
                          aria-label={`Open ${item.fileName}`}
                          sx={{
                            width: '100%',
                            height: '100%',
                            p: 0,
                            display: 'block',
                            position: 'relative',
                            borderRadius: 0,
                            textAlign: 'inherit',
                          }}
                        >
                          <Box
                            component="img"
                            src={item.thumbnailUrl}
                            alt={item.fileName}
                            loading="lazy"
                            onError={() => setMediaState('error')}
                            sx={{ width: '100%', height: '100%', objectFit: 'cover' }}
                          />
                          {item.mediaType === 'VIDEO' && (
                            <Box
                              aria-hidden="true"
                              component="span"
                              sx={{
                                position: 'absolute',
                                display: 'grid',
                                placeItems: 'center',
                                width: 42,
                                height: 42,
                                left: '50%',
                                top: '50%',
                                transform: 'translate(-50%, -50%)',
                                color: 'common.white',
                                bgcolor: 'rgba(0,0,0,.58)',
                                borderRadius: '50%',
                                fontSize: 24,
                                fontWeight: 800,
                                lineHeight: 1,
                              }}
                            >
                              &gt;
                            </Box>
                          )}
                        </Button>
                      </Box>
                    ))}
                  </Box>
                )}
              </Stack>
            </CardContent>
          </Card>
        </Stack>
      </Container>
      <Dialog
        open={activeMedia !== null}
        onClose={() => setActiveMedia(null)}
        fullWidth
        maxWidth="md"
        aria-labelledby="public-media-preview-title"
      >
        {activeMedia && (
          <>
            <DialogTitle id="public-media-preview-title" sx={{ pr: 2 }}>
              <Stack
                direction="row"
                spacing={1}
                sx={{ alignItems: 'center', justifyContent: 'space-between' }}
              >
                <Typography component="span" sx={{ overflowWrap: 'anywhere' }}>
                  {activeMedia.fileName}
                </Typography>
                <Button onClick={() => setActiveMedia(null)}>Close</Button>
              </Stack>
            </DialogTitle>
            <DialogContent sx={{ p: { xs: 1, sm: 2 } }}>
              {activeMedia.mediaType === 'VIDEO' ? (
                <Box
                  component="video"
                  src={activeMedia.contentUrl}
                  controls
                  autoPlay
                  sx={{ width: '100%', maxHeight: '78dvh', bgcolor: 'common.black' }}
                />
              ) : (
                <Box
                  component="img"
                  src={activeMedia.contentUrl}
                  alt={activeMedia.fileName}
                  sx={{ width: '100%', maxHeight: '78dvh', objectFit: 'contain', display: 'block' }}
                />
              )}
            </DialogContent>
          </>
        )}
      </Dialog>
    </Box>
  )
}
