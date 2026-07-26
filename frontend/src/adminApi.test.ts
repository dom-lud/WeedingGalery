import { beforeEach, describe, expect, it, vi } from 'vitest'
import { adminApi, pageItems } from './adminApi'
import api from './api'

vi.mock('./api', () => ({ default: { get: vi.fn(), post: vi.fn() } }))

describe('adminApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('keeps administrative endpoints under the isolated admin namespace', () => {
    adminApi.dashboard()
    adminApi.users({ query: 'ana', page: 0, size: 25 })
    adminApi.blockUser('user-1')
    expect(api.get).toHaveBeenCalledWith('admin/dashboard')
    expect(api.get).toHaveBeenCalledWith('admin/users', {
      params: { query: 'ana', page: 0, size: 25 },
    })
    expect(api.post).toHaveBeenCalledWith('admin/users/user-1/lock')
  })

  it('normalizes both array and paged responses without changing the contract', () => {
    expect(pageItems([{ id: '1' }])).toEqual([{ id: '1' }])
    expect(
      pageItems({ content: [{ id: '2' }], totalElements: 1, totalPages: 1, number: 0 }),
    ).toEqual([{ id: '2' }])
  })
})
