import axios, { AxiosHeaders, type InternalAxiosRequestConfig } from 'axios'

function getCookie(name: string) {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop()?.split(';').shift()
  return undefined
}

let csrfInitializationPromise: Promise<void> | null = null

export async function ensureCsrfCookie() {
  if (getCookie('XSRF-TOKEN')) {
    return
  }

  if (!csrfInitializationPromise) {
    csrfInitializationPromise = axios
      .get('/api/auth/csrf', {
        withCredentials: true,
      })
      .then(() => undefined)
      .finally(() => {
        csrfInitializationPromise = null
      })
  }

  await csrfInitializationPromise
}

export async function prepareCsrfProtectedRequest(config: InternalAxiosRequestConfig) {
  if (config.method && ['post', 'put', 'delete', 'patch'].includes(config.method.toLowerCase())) {
    await ensureCsrfCookie()

    const csrfToken = getCookie('XSRF-TOKEN')
    if (csrfToken) {
      const headers = AxiosHeaders.from(config.headers)
      headers.set('X-XSRF-TOKEN', csrfToken)
      config.headers = headers
    }
  }

  return config
}

export const api = axios.create({
  baseURL: '/api/',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use(prepareCsrfProtectedRequest)

export default api
