import { beforeEach, describe, expect, it, vi } from 'vitest'
import { moderationApi } from './moderationApi'
import api from './api'

vi.mock('./api', () => ({ default: { get: vi.fn(), post: vi.fn() } }))

describe('moderationApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('scopes list requests to event and gallery', () => {
    moderationApi.list('event-1', 'gallery-1')
    expect(api.get).toHaveBeenCalledWith('events/event-1/galleries/gallery-1/media')
  })

  it('sends optional single-action reasons only when present', () => {
    moderationApi.action('event-1', 'gallery-1', 'media-1', 'approve')
    moderationApi.action('event-1', 'gallery-1', 'media-2', 'reject', 'Policy violation')
    expect(api.post).toHaveBeenNthCalledWith(
      1,
      'events/event-1/galleries/gallery-1/media/media-1/approve',
      undefined,
    )
    expect(api.post).toHaveBeenNthCalledWith(
      2,
      'events/event-1/galleries/gallery-1/media/media-2/reject',
      { reason: 'Policy violation' },
    )
  })

  it('normalizes bulk action names for the backend', () => {
    moderationApi.bulkAction('event-1', 'gallery-1', {
      mediaIds: ['media-1', 'media-2'],
      action: 'hide',
      reason: 'Owner request',
    })
    expect(api.post).toHaveBeenCalledWith('events/event-1/galleries/gallery-1/media/bulk-actions', {
      mediaIds: ['media-1', 'media-2'],
      action: 'HIDE',
      reason: 'Owner request',
    })
  })
})
