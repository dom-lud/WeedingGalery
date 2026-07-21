import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { describe, expect, it, vi } from 'vitest'
import App from './App'
import { appTheme } from './theme'

vi.mock('./api', () => {
  return {
    default: {
      get: vi.fn(() => Promise.reject(new Error('Network Error'))),
      post: vi.fn(),
    },
  }
})

describe('App foundation shell', () => {
  it('renders the loading state and then redirects to login', async () => {
    render(
      <ThemeProvider theme={appTheme}>
        <App />
      </ThemeProvider>,
    )

    expect(screen.getByRole('status')).toHaveTextContent(/Opening your workspace/i)

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /Sign in to your gallery/i })).toBeInTheDocument()
    })
    cleanup()
    await new Promise((resolve) => setTimeout(resolve, 0))
  })
})
