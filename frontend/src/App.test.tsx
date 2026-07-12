import { fireEvent, render, screen } from '@testing-library/react'
import { ThemeProvider } from '@mui/material/styles'
import { describe, expect, it } from 'vitest'
import App from './App'
import { appTheme } from './theme'

describe('App foundation shell', () => {
  it('renders the startup shell and keeps the counter interactive', () => {
    render(
      <ThemeProvider theme={appTheme}>
        <App />
      </ThemeProvider>,
    )

    expect(screen.getByRole('heading', { level: 1, name: /get started/i })).toBeInTheDocument()

    const counterButton = screen.getByRole('button', { name: /count is 0/i })
    fireEvent.click(counterButton)

    expect(screen.getByRole('button', { name: /count is 1/i })).toBeInTheDocument()
  })
})
