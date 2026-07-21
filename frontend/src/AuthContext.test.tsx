import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import api from './api'
import { AuthProvider, useAuth } from './AuthContext'

vi.mock('./api', () => ({ default: { get: vi.fn(), post: vi.fn() } }))

function Consumer() {
  const { user, isLoading, login, logout, checkAuth } = useAuth()
  return (
    <div>
      <span>{isLoading ? 'loading' : (user?.email ?? 'anonymous')}</span>
      <button onClick={() => login({ email: 'local@example.com' })}>login locally</button>
      <button onClick={() => void logout()}>logout</button>
      <button onClick={() => void checkAuth()}>check</button>
    </div>
  )
}

describe('AuthProvider', () => {
  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('bootstraps CSRF before identity and supports local login and server logout', async () => {
    vi.mocked(api.get)
      .mockResolvedValueOnce({ data: null } as never)
      .mockResolvedValueOnce({ data: { email: 'server@example.com' } } as never)
    vi.mocked(api.post).mockResolvedValue({ data: null } as never)
    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    expect(await screen.findByText('server@example.com')).toBeInTheDocument()
    expect(api.get).toHaveBeenNthCalledWith(1, 'auth/csrf')
    expect(api.get).toHaveBeenNthCalledWith(2, 'auth/me')
    fireEvent.click(screen.getByRole('button', { name: 'login locally' }))
    expect(screen.getByText('local@example.com')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'logout' }))
    await waitFor(() => expect(screen.getByText('anonymous')).toBeInTheDocument())
    expect(api.post).toHaveBeenCalledWith('auth/logout')
  })

  it('fails closed for auth and keeps the user when logout fails', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined)
    vi.mocked(api.get).mockRejectedValue(new Error('offline'))
    vi.mocked(api.post).mockRejectedValue(new Error('logout offline'))
    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>,
    )

    expect(await screen.findByText('anonymous')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'login locally' }))
    fireEvent.click(screen.getByRole('button', { name: 'logout' }))
    await waitFor(() => expect(consoleError).toHaveBeenCalled())
    expect(screen.getByText('local@example.com')).toBeInTheDocument()
  })

  it('rejects use outside the provider', () => {
    expect(() => render(<Consumer />)).toThrow('useAuth must be used within an AuthProvider')
  })
})
