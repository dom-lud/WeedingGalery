import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { appTheme } from '../../theme'
import AppShell from './AppShell'

describe('AppShell', () => {
  afterEach(cleanup)

  it('renders identity and delegates logout without hiding page content', () => {
    const logout = vi.fn()
    render(
      <ThemeProvider theme={appTheme}>
        <AppShell email="alice@example.com" onLogout={logout}>
          <h1>Workspace content</h1>
        </AppShell>
      </ThemeProvider>,
    )

    expect(screen.getByText('A')).toBeInTheDocument()
    expect(screen.getByText('alice@example.com')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Workspace content' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Log out' }))
    expect(logout).toHaveBeenCalledOnce()
  })

  it('uses a safe avatar fallback when identity is temporarily absent', () => {
    render(
      <ThemeProvider theme={appTheme}>
        <AppShell onLogout={vi.fn()}>content</AppShell>
      </ThemeProvider>,
    )
    expect(screen.getByText('U')).toBeInTheDocument()
  })
})
