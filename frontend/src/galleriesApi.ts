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

export interface GalleryWritePayload {
  name: string
  description: string | null
  sortOrder: number
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
}
