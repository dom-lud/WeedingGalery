import api from './api'

/**
 * Stage 8 FE contract. The backend implementation is still in progress, so
 * this client keeps the DTO explicit instead of inferring permissions locally.
 */
export type PublicationStatus = 'PENDING' | 'APPROVED' | 'HIDDEN' | 'REJECTED'
export type ModerationAction = 'approve' | 'hide' | 'reject' | 'restore'

export interface ModerationMedia {
  id: string
  fileName: string
  mediaType: 'IMAGE' | 'VIDEO'
  status: 'STORED' | 'PROCESSING' | 'PROCESSED' | 'PROCESSING_FAILED'
  publicationStatus: PublicationStatus
  size: number | null
  uploadedAt: string | null
  thumbnailUrl: string
  contentUrl: string
}

export interface BulkModerationPayload {
  mediaIds: string[]
  action: ModerationAction
  reason?: string
}

export const moderationApi = {
  list: (eventId: string, galleryId: string) =>
    api.get<ModerationMedia[]>(`events/${eventId}/galleries/${galleryId}/media`),
  action: (
    eventId: string,
    galleryId: string,
    mediaId: string,
    action: ModerationAction,
    reason?: string,
  ) =>
    api.post<void>(
      `events/${eventId}/galleries/${galleryId}/media/${mediaId}/${action}`,
      reason ? { reason } : undefined,
    ),
  bulkAction: (eventId: string, galleryId: string, payload: BulkModerationPayload) =>
    api.post<void>(`events/${eventId}/galleries/${galleryId}/media/bulk-actions`, {
      ...payload,
      action: payload.action.toUpperCase(),
    }),
}
