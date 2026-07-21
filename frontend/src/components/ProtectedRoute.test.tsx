import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import ProtectedRoute from './ProtectedRoute'

const authState = vi.hoisted(() => ({ user: null as { email: string } | null, isLoading: false }))

vi.mock('../AuthContext', () => ({ useAuth: () => authState }))

describe('ProtectedRoute', () => {
  it('renders the protected outlet for an authenticated user', () => {
    authState.user = { email: 'owner@example.com' }
    authState.isLoading = false
    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<h1>Protected workspace</h1>} />
          </Route>
          <Route path="/login" element={<h1>Login</h1>} />
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: 'Protected workspace' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Login' })).not.toBeInTheDocument()
  })
})
