import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import Login from './components/Login'

const navigateMock = vi.fn()
const loginMock = vi.fn()
const postMock = vi.fn()

vi.mock('./api', () => ({
  default: {
    post: (...args: unknown[]) => postMock(...args),
  },
}))

vi.mock('./AuthContext', () => ({
  useAuth: () => ({
    login: loginMock,
  }),
}))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return {
    ...actual,
    useNavigate: () => navigateMock,
  }
})

describe('Login form', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    cleanup()
  })

  function renderLogin() {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>,
    )
  }

  it('shows an authentication error and stays on the form when credentials are invalid', async () => {
    postMock.mockRejectedValueOnce({
      response: {
        status: 401,
      },
    })

    renderLogin()

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'admin@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i, { selector: 'input' }), {
      target: { value: 'wrongpass' },
    })
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }))

    await waitFor(() => {
      expect(screen.getByText(/invalid email or password/i)).toBeInTheDocument()
    })

    expect(loginMock).not.toHaveBeenCalled()
    expect(navigateMock).not.toHaveBeenCalled()
  })

  it('shows backend error details for non-authentication failures', async () => {
    postMock.mockRejectedValueOnce({
      response: {
        status: 500,
        data: {
          message: 'Unexpected failure',
        },
      },
    })

    renderLogin()

    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'admin@example.com' } })
    fireEvent.change(screen.getByLabelText(/password/i, { selector: 'input' }), {
      target: { value: 'password123' },
    })
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }))

    await waitFor(() => {
      expect(screen.getByText(/unexpected failure/i)).toBeInTheDocument()
    })
  })

  it('toggles password visibility without losing the current input value', () => {
    renderLogin()

    const passwordInput = screen.getByLabelText(/password/i, {
      selector: 'input',
    }) as HTMLInputElement
    fireEvent.change(passwordInput, { target: { value: 'password123' } })

    expect(passwordInput.type).toBe('password')
    expect(passwordInput.value).toBe('password123')

    fireEvent.click(screen.getByRole('button', { name: /show password/i }))
    expect(passwordInput.type).toBe('text')
    expect(passwordInput.value).toBe('password123')

    fireEvent.click(screen.getByRole('button', { name: /hide password/i }))
    expect(passwordInput.type).toBe('password')
    expect(passwordInput.value).toBe('password123')
  })

  it('prevents duplicate submissions while authentication is pending', async () => {
    let resolveRequest: ((value: { data: { email: string } }) => void) | undefined
    postMock.mockReturnValueOnce(
      new Promise((resolve) => {
        resolveRequest = resolve
      }),
    )
    renderLogin()

    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: 'admin@example.com' },
    })
    fireEvent.change(screen.getByLabelText(/password/i, { selector: 'input' }), {
      target: { value: 'password123' },
    })
    const submit = screen.getByRole('button', { name: /sign in/i })
    fireEvent.click(submit)
    fireEvent.click(submit)

    expect(postMock).toHaveBeenCalledTimes(1)
    expect(submit).toBeDisabled()
    expect(submit).toHaveAttribute('aria-busy', 'true')

    resolveRequest?.({ data: { email: 'admin@example.com' } })
    await waitFor(() => expect(navigateMock).toHaveBeenCalledWith('/dashboard'))
  })
})
