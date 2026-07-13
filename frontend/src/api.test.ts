import axios, { AxiosHeaders, type InternalAxiosRequestConfig } from 'axios'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { ensureCsrfCookie, prepareCsrfProtectedRequest } from './api'

describe('CSRF request preparation', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/'
  })

  it('fetches csrf cookie before the first mutating request and attaches the header', async () => {
    vi.spyOn(axios, 'get').mockImplementation(async () => {
      document.cookie = 'XSRF-TOKEN=test-csrf-token; path=/'
      return { data: null } as never
    })

    const config = {
      method: 'post',
      headers: new AxiosHeaders(),
    } as InternalAxiosRequestConfig

    const preparedConfig = await prepareCsrfProtectedRequest(config)

    expect(axios.get).toHaveBeenCalledWith('/api/auth/csrf', { withCredentials: true })
    expect(AxiosHeaders.from(preparedConfig.headers).get('X-XSRF-TOKEN')).toBe('test-csrf-token')
  })

  it('reuses an in-flight csrf initialization instead of issuing duplicate requests', async () => {
    let resolveRequest: (() => void) | undefined

    vi.spyOn(axios, 'get').mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveRequest = () => {
            document.cookie = 'XSRF-TOKEN=shared-token; path=/'
            resolve({ data: null } as never)
          }
        }),
    )

    const firstCall = ensureCsrfCookie()
    const secondCall = ensureCsrfCookie()

    expect(axios.get).toHaveBeenCalledTimes(1)

    resolveRequest?.()
    await Promise.all([firstCall, secondCall])
  })

  it('does not fetch csrf cookie for read-only requests', async () => {
    const getSpy = vi.spyOn(axios, 'get')
    const config = {
      method: 'get',
      headers: new AxiosHeaders(),
    } as InternalAxiosRequestConfig

    const preparedConfig = await prepareCsrfProtectedRequest(config)

    expect(getSpy).not.toHaveBeenCalled()
    expect(AxiosHeaders.from(preparedConfig.headers).get('X-XSRF-TOKEN')).toBeUndefined()
  })

  it('does not refetch csrf cookie when it already exists', async () => {
    document.cookie = 'XSRF-TOKEN=existing-token; path=/'
    const getSpy = vi.spyOn(axios, 'get')
    const config = {
      method: 'post',
      headers: new AxiosHeaders(),
    } as InternalAxiosRequestConfig

    const preparedConfig = await prepareCsrfProtectedRequest(config)

    expect(getSpy).not.toHaveBeenCalled()
    expect(AxiosHeaders.from(preparedConfig.headers).get('X-XSRF-TOKEN')).toBe('existing-token')
  })
})
