import api from './api'
import type { ModerationMode } from './galleriesApi'

export interface PublicGallery {
  slug: string
  name: string
  description: string | null
  uploadEnabled: boolean
  downloadEnabled: boolean
  moderationMode: ModerationMode
  publishedAt: string | null
  expiresAt: string | null
}

export interface PublicAccessPayload {
  accessToken: string
  accessCode?: string
}

const publicGalleryPath = (slug: string) => `public/galleries/${encodeURIComponent(slug)}`

export const publicAccessApi = {
  access: (slug: string, payload: PublicAccessPayload) =>
    api.post<PublicGallery>(`${publicGalleryPath(slug)}/access`, payload),
  get: (slug: string) => api.get<PublicGallery>(publicGalleryPath(slug)),
}
