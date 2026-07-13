import { render, screen, waitFor } from '@testing-library/react'
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

    // Initially loading
    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: /Welcome Back/i })).toBeInTheDocument()
    })
  })
})
