import { describe, expect, it, vi } from 'vitest'
import api from './api'
import { customizationApi } from './customizationApi'

vi.mock('./api', () => ({
  default: { get: vi.fn(), put: vi.fn() },
}))

describe('customizationApi', () => {
  it('uses the gallery customization contract and preserves the optimistic version', async () => {
    const payload = { theme: 'EDITORIAL', layout: 'GRID', version: 7 }
    vi.mocked(api.get).mockResolvedValue({ data: {} } as never)
    vi.mocked(api.put).mockResolvedValue({ data: {} } as never)

    await customizationApi.get('gallery with spaces')
    await customizationApi.update('gallery with spaces', payload as never)

    expect(api.get).toHaveBeenCalledWith('galleries/gallery%20with%20spaces/customization')
    expect(api.put).toHaveBeenCalledWith('galleries/gallery%20with%20spaces/customization', payload)
  })
})
