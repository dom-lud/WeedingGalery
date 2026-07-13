import api from './api'

export type UploadFileStatus = 'PENDING' | 'UPLOADING' | 'STORED' | 'FAILED' | 'CANCELLED'

export interface UploadManifestFile {
  clientFileId: string
  fileName: string
  declaredContentType: string
  size: number
}

export interface UploadSessionFile {
  clientFileId: string
  fileName: string
  size: number
  status: UploadFileStatus
  errorCode?: string | null
}

export interface UploadSession {
  id: string
  status: 'OPEN' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED'
  expiresAt: string
  files: UploadSessionFile[]
}

export interface UploadResult {
  clientFileId: string
  status: 'STORED'
  detectedContentType?: string
  size?: number
  checksumSha256?: string
  errorCode?: string
}

const sessionsPath = (slug: string) =>
  `public/galleries/${encodeURIComponent(slug)}/upload-sessions`

export const uploadApi = {
  createSession: (slug: string, files: UploadManifestFile[], idempotencyKey: string) =>
    api.post<UploadSession>(
      sessionsPath(slug),
      { files },
      { headers: { 'Idempotency-Key': idempotencyKey } },
    ),
  getSession: (slug: string, sessionId: string) =>
    api.get<UploadSession>(`${sessionsPath(slug)}/${sessionId}`),
  uploadFile: (
    slug: string,
    sessionId: string,
    clientFileId: string,
    file: File,
    onProgress: (progress: number) => void,
    signal?: AbortSignal,
  ) => {
    const body = new FormData()
    body.append('file', file)
    return api.put<UploadResult>(
      `${sessionsPath(slug)}/${sessionId}/files/${encodeURIComponent(clientFileId)}`,
      body,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
        signal,
        onUploadProgress: (event) => {
          if (event.total) onProgress(Math.round((event.loaded * 100) / event.total))
        },
      },
    )
  },
  cancel: (slug: string, sessionId: string) =>
    api.post<UploadSession>(`${sessionsPath(slug)}/${sessionId}/cancel`),
}
