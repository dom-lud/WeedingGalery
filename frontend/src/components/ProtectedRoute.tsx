import { CircularProgress, Stack, Typography } from '@mui/material'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export default function ProtectedRoute() {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return (
      <Stack
        component="main"
        spacing={2}
        role="status"
        aria-live="polite"
        sx={{ minHeight: '100dvh', alignItems: 'center', justifyContent: 'center' }}
      >
        <CircularProgress aria-label="Loading your workspace" />
        <Typography color="text.secondary">Opening your workspace…</Typography>
      </Stack>
    )
  }

  return user ? <Outlet /> : <Navigate to="/login" replace />
}
