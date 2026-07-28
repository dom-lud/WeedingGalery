import api from './api'
import type { EventRole } from './eventsApi'

export interface GalleryData {
  id: string
  eventId: string
  name: string
  slug: string
  description: string | null
  sortOrder: number
  status: 'ACTIVE' | 'ARCHIVED'
  currentUserRole: EventRole
  createdAt: string
  updatedAt: string
}

export interface GalleryMedia {
  id: string
  fileName: string
  mediaType: 'IMAGE' | 'VIDEO'
  status: 'STORED' | 'PROCESSING' | 'PROCESSED' | 'PROCESSING_FAILED'
  size: number | null
  uploadedAt: string | null
  thumbnailUrl: string
  contentUrl: string
}

export interface GalleryWritePayload {
  name: string
  description: string | null
  sortOrder: number
}

export type ModerationMode = 'NONE' | 'REQUIRED'

export interface GallerySettings {
  publicViewEnabled: boolean
  uploadEnabled: boolean
  downloadEnabled: boolean
  moderationMode: ModerationMode
  accessTokenConfigured: boolean
  accessCodeConfigured: boolean
  publishedAt: string | null
  expiresAt: string | null
  version: number
}

export interface GallerySettingsPayload {
  publicViewEnabled: boolean
  uploadEnabled: boolean
  downloadEnabled: boolean
  moderationMode: ModerationMode
  publishedAt: string | null
  expiresAt: string | null
  version: number
}

export interface RotatedGalleryToken {
  accessToken: string
  sharePath: string
}

const path = (eventId: string) => `events/${eventId}/galleries`

export const galleriesApi = {
  list: (eventId: string) => api.get<GalleryData[]>(path(eventId)),
  create: (eventId: string, payload: GalleryWritePayload) =>
    api.post<GalleryData>(path(eventId), payload),
  update: (eventId: string, galleryId: string, payload: GalleryWritePayload) =>
    api.put<GalleryData>(`${path(eventId)}/${galleryId}`, payload),
  archive: (eventId: string, galleryId: string) =>
    api.post<GalleryData>(`${path(eventId)}/${galleryId}/archive`),
  remove: (eventId: string, galleryId: string) => api.delete(`${path(eventId)}/${galleryId}`),
  settings: (eventId: string, galleryId: string) =>
    api.get<GallerySettings>(`${path(eventId)}/${galleryId}/settings`),
  updateSettings: (eventId: string, galleryId: string, payload: GallerySettingsPayload) =>
    api.put<GallerySettings>(`${path(eventId)}/${galleryId}/settings`, payload),
  rotateAccessToken: (eventId: string, galleryId: string) =>
    api.post<RotatedGalleryToken>(`${path(eventId)}/${galleryId}/access-token/rotate`),
  setAccessCode: (eventId: string, galleryId: string, accessCode: string) =>
    api.put(`${path(eventId)}/${galleryId}/access-code`, { accessCode }),
  removeAccessCode: (eventId: string, galleryId: string) =>
    api.delete(`${path(eventId)}/${galleryId}/access-code`),
  media: (eventId: string, galleryId: string) =>
    api.get<GalleryMedia[]>(`${path(eventId)}/${galleryId}/media`),
  downloadUrl: (eventId: string, galleryId: string) =>
    `/api/${path(eventId)}/${galleryId}/download`,
  qr: (eventId: string, galleryId: string, format: 'PNG' | 'SVG' = 'PNG', size = 512) =>
    api.get<Blob>(`${path(eventId)}/${galleryId}/qr`, {
      params: { format, size },
      responseType: 'blob',
    }),
}
