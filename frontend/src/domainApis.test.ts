import { afterEach, describe, expect, it, vi } from 'vitest'
import api from './api'
import { eventsApi } from './eventsApi'
import { galleriesApi, type GallerySettingsPayload } from './galleriesApi'
import { publicAccessApi } from './publicAccessApi'
import { uploadApi } from './uploadApi'

vi.mock('./api', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))

describe('domain API contracts', () => {
  afterEach(() => vi.clearAllMocks())

  it('uses the scoped event and membership endpoints with contract payloads', () => {
    const payload = {
      name: 'Wedding',
      type: 'WEDDING' as const,
      eventDate: null,
      description: null,
      privacyMode: 'PRIVATE' as const,
    }

    eventsApi.list()
    eventsApi.create(payload)
    eventsApi.update('event/unsafe', payload)
    eventsApi.archive('event-1')
    eventsApi.remove('event-1')
    eventsApi.members('event-1')
    eventsApi.addManager('event-1', 'manager@example.com')
    eventsApi.removeManager('event-1', 'membership-1')
    eventsApi.transferOwnership('event-1', 'membership-1')

    expect(api.get).toHaveBeenNthCalledWith(1, 'events')
    expect(api.get).toHaveBeenNthCalledWith(2, 'events/event-1/members')
    expect(api.post).toHaveBeenCalledWith('events', payload)
    expect(api.put).toHaveBeenCalledWith('events/event/unsafe', payload)
    expect(api.post).toHaveBeenCalledWith('events/event-1/archive')
    expect(api.post).toHaveBeenCalledWith('events/event-1/members', {
      email: 'manager@example.com',
      role: 'MANAGER',
    })
    expect(api.post).toHaveBeenCalledWith('events/event-1/ownership-transfer', {
      targetMembershipId: 'membership-1',
    })
    expect(api.delete).toHaveBeenCalledWith('events/event-1')
    expect(api.delete).toHaveBeenCalledWith('events/event-1/members/membership-1')
  })

  it('keeps every gallery operation scoped by event and gallery identifiers', () => {
    const write = { name: 'Reception', description: null, sortOrder: 10 }
    const settings: GallerySettingsPayload = {
      publicViewEnabled: true,
      uploadEnabled: true,
      downloadEnabled: false,
      moderationMode: 'REQUIRED',
      publishedAt: null,
      expiresAt: null,
      version: 2,
    }

    galleriesApi.list('event-1')
    galleriesApi.create('event-1', write)
    galleriesApi.update('event-1', 'gallery-1', write)
    galleriesApi.archive('event-1', 'gallery-1')
    galleriesApi.remove('event-1', 'gallery-1')
    galleriesApi.settings('event-1', 'gallery-1')
    galleriesApi.updateSettings('event-1', 'gallery-1', settings)
    galleriesApi.rotateAccessToken('event-1', 'gallery-1')
    galleriesApi.setAccessCode('event-1', 'gallery-1', 'secret-code')
    galleriesApi.removeAccessCode('event-1', 'gallery-1')

    const base = 'events/event-1/galleries/gallery-1'
    expect(api.get).toHaveBeenCalledWith('events/event-1/galleries')
    expect(api.post).toHaveBeenCalledWith('events/event-1/galleries', write)
    expect(api.put).toHaveBeenCalledWith(base, write)
    expect(api.post).toHaveBeenCalledWith(`${base}/archive`)
    expect(api.delete).toHaveBeenCalledWith(base)
    expect(api.get).toHaveBeenCalledWith(`${base}/settings`)
    expect(api.put).toHaveBeenCalledWith(`${base}/settings`, settings)
    expect(api.post).toHaveBeenCalledWith(`${base}/access-token/rotate`)
    expect(api.put).toHaveBeenCalledWith(`${base}/access-code`, { accessCode: 'secret-code' })
    expect(api.delete).toHaveBeenCalledWith(`${base}/access-code`)
  })

  it('encodes public slugs and sends the access grant only in the request body', () => {
    publicAccessApi.access('summer wedding/2026', {
      accessToken: 'raw-token',
      accessCode: '1234',
    })
    publicAccessApi.get('summer wedding/2026')

    const path = 'public/galleries/summer%20wedding%2F2026'
    expect(api.post).toHaveBeenCalledWith(`${path}/access`, {
      accessToken: 'raw-token',
      accessCode: '1234',
    })
    expect(api.get).toHaveBeenCalledWith(path)
  })

  it('preserves idempotency, multipart content and progress semantics for uploads', () => {
    const manifest = [
      {
        clientFileId: 'client/1',
        fileName: 'photo.jpg',
        declaredContentType: 'image/jpeg',
        size: 3,
      },
    ]
    const progress = vi.fn()
    const signal = new AbortController().signal
    const file = new File([new Uint8Array([1, 2, 3])], 'photo.jpg', { type: 'image/jpeg' })

    uploadApi.createSession('slug / one', manifest, 'idem-1')
    uploadApi.getSession('slug / one', 'session-1')
    uploadApi.uploadFile('slug / one', 'session-1', 'client/1', file, progress, signal)
    uploadApi.cancel('slug / one', 'session-1')

    const base = 'public/galleries/slug%20%2F%20one/upload-sessions'
    expect(api.post).toHaveBeenCalledWith(
      base,
      { files: manifest },
      { headers: { 'Idempotency-Key': 'idem-1' } },
    )
    expect(api.get).toHaveBeenCalledWith(`${base}/session-1`)
    const uploadCall = vi.mocked(api.put).mock.calls[0]
    expect(uploadCall[0]).toBe(`${base}/session-1/files/client%2F1`)
    expect(uploadCall[1]).toBeInstanceOf(FormData)
    expect((uploadCall[1] as FormData).get('file')).toBe(file)
    expect(uploadCall[2]).toMatchObject({
      headers: { 'Content-Type': 'multipart/form-data' },
      signal,
    })
    const onUploadProgress = uploadCall[2]?.onUploadProgress
    onUploadProgress?.({ loaded: 1, total: 3 } as never)
    onUploadProgress?.({ loaded: 2 } as never)
    expect(progress).toHaveBeenCalledOnce()
    expect(progress).toHaveBeenCalledWith(33)
    expect(api.post).toHaveBeenCalledWith(`${base}/session-1/cancel`)
  })
})
