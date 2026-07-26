import api from './api'

export const APPEARANCE_TEXT_LIMIT = 500

export type AppearanceTheme = 'EDITORIAL' | 'MINIMAL' | 'ROMANTIC'
export type AppearanceLayout = 'GRID' | 'MASONRY' | 'TIMELINE'

export interface GalleryCustomization {
  theme: AppearanceTheme
  layout: AppearanceLayout
  primaryColor: string
  accentColor: string
  backgroundColor: string
  welcomeText: string
  showTitle: boolean
  showUpload: boolean
  showDownload: boolean
  version: number
}

export type GalleryCustomizationPayload = Omit<GalleryCustomization, 'version'> & {
  version: number
}

const path = (galleryId: string) => `galleries/${encodeURIComponent(galleryId)}/customization`

export const customizationApi = {
  get: (galleryId: string) => api.get<GalleryCustomization>(path(galleryId)),
  update: (galleryId: string, payload: GalleryCustomizationPayload) =>
    api.put<GalleryCustomization>(path(galleryId), payload),
}
