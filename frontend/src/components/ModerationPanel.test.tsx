import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import ModerationPanel from './ModerationPanel'
import { moderationApi, type ModerationMedia } from '../moderationApi'

vi.mock('../moderationApi', () => ({
  moderationApi: {
    list: vi.fn(),
    action: vi.fn(),
    bulkAction: vi.fn(),
  },
}))

const media: ModerationMedia[] = [
  {
    id: 'media-pending',
    fileName: 'pending.jpg',
    mediaType: 'IMAGE',
    status: 'PROCESSED',
    publicationStatus: 'PENDING',
    size: 100,
    uploadedAt: null,
    thumbnailUrl: '/pending.jpg',
    contentUrl: '/pending-content.jpg',
  },
  {
    id: 'media-approved',
    fileName: 'approved.jpg',
    mediaType: 'IMAGE',
    status: 'PROCESSING',
    publicationStatus: 'APPROVED',
    size: 200,
    uploadedAt: null,
    thumbnailUrl: '/approved.jpg',
    contentUrl: '/approved-content.jpg',
  },
  {
    id: 'media-rejected',
    fileName: 'rejected.jpg',
    mediaType: 'IMAGE',
    status: 'PROCESSING_FAILED',
    publicationStatus: 'REJECTED',
    size: 300,
    uploadedAt: null,
    thumbnailUrl: '/rejected.jpg',
    contentUrl: '/rejected-content.jpg',
  },
]

function renderPanel() {
  return render(
    <ModerationPanel
      eventId="event-1"
      galleryId="gallery-1"
      galleryName="Reception"
      open
      onClose={vi.fn()}
    />,
  )
}

describe('ModerationPanel', () => {
  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('uses publicationStatus for labels and filtering, independently of processing status', async () => {
    vi.mocked(moderationApi.list).mockResolvedValue({ data: media } as never)
    renderPanel()

    expect(await screen.findByText('pending.jpg')).toBeInTheDocument()
    expect(screen.getByText('Approved')).toBeInTheDocument()
    fireEvent.mouseDown(screen.getByRole('combobox', { name: 'Status' }))
    fireEvent.click(screen.getByRole('option', { name: 'Rejected' }))

    expect(screen.getByText('rejected.jpg')).toBeInTheDocument()
    expect(screen.queryByText('pending.jpg')).not.toBeInTheDocument()
    expect(screen.queryByText('approved.jpg')).not.toBeInTheDocument()
  })

  it('confirms a single approve and scopes the action to event, gallery and media', async () => {
    vi.mocked(moderationApi.list).mockResolvedValue({ data: media } as never)
    vi.mocked(moderationApi.action).mockResolvedValue({} as never)
    renderPanel()

    await screen.findByText('pending.jpg')
    fireEvent.click(screen.getByRole('checkbox', { name: 'Select pending.jpg' }))
    fireEvent.click(screen.getByRole('button', { name: 'Approve' }))
    const dialog = screen.getByRole('dialog', { name: 'Approve selected media?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Approve' }))

    await waitFor(() =>
      expect(moderationApi.action).toHaveBeenCalledWith(
        'event-1',
        'gallery-1',
        'media-pending',
        'approve',
        undefined,
      ),
    )
    expect(await screen.findByText('Approve completed.')).toBeInTheDocument()
  })

  it('requires a reason for reject and submits bulk actions with all selected ids', async () => {
    vi.mocked(moderationApi.list).mockResolvedValue({ data: media } as never)
    vi.mocked(moderationApi.bulkAction).mockResolvedValue({} as never)
    renderPanel()

    await screen.findByText('pending.jpg')
    fireEvent.click(screen.getByRole('checkbox', { name: 'Select pending.jpg' }))
    fireEvent.click(screen.getByRole('checkbox', { name: 'Select approved.jpg' }))
    expect(screen.getByRole('button', { name: 'Reject' })).toBeDisabled()
    fireEvent.change(screen.getByLabelText('Decision reason'), {
      target: { value: 'Not suitable' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Reject' }))
    const dialog = screen.getByRole('dialog', { name: 'Reject selected media?' })
    fireEvent.click(within(dialog).getByRole('button', { name: 'Reject' }))

    await waitFor(() =>
      expect(moderationApi.bulkAction).toHaveBeenCalledWith('event-1', 'gallery-1', {
        mediaIds: ['media-pending', 'media-approved'],
        action: 'reject',
        reason: 'Not suitable',
      }),
    )
  })

  it('shows an error and retries loading without losing the dialog', async () => {
    vi.mocked(moderationApi.list)
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce({ data: [] } as never)
    renderPanel()

    expect(await screen.findByText('Moderation operation failed.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText('No media is waiting for moderation.')).toBeInTheDocument()
    expect(moderationApi.list).toHaveBeenCalledTimes(2)
  })
})
