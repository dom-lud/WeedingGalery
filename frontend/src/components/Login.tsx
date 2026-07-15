import { useState, type FormEvent } from 'react'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  InputAdornment,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../AuthContext'

function loginError(error: unknown) {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (
      error as { response?: { status?: number; data?: { message?: string } | string } }
    ).response
    if (response?.status === 401) return 'Invalid email or password'
    if (typeof response?.data === 'object') return response.data.message ?? 'Login failed'
    return response?.data || 'Login failed'
  }
  return 'Login failed'
}

export default function Login() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    if (submitting) return
    setError('')
    setSubmitting(true)
    try {
      const response = await api.post('auth/login', { email, password })
      login(response.data)
      navigate('/dashboard')
    } catch (requestError) {
      setError(loginError(requestError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box
      component="main"
      sx={{
        minHeight: '100dvh',
        display: 'grid',
        alignItems: 'center',
        py: { xs: 2, sm: 5 },
        background: (theme) =>
          `radial-gradient(circle at 8% 8%, ${theme.palette.secondary.main}24, transparent 36%), radial-gradient(circle at 94% 92%, ${theme.palette.primary.main}20, transparent 34%)`,
      }}
    >
      <Container maxWidth="lg">
        <Paper
          elevation={0}
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: 'minmax(0, 1.05fr) minmax(420px, 0.95fr)' },
            overflow: 'hidden',
            border: '1px solid',
            borderColor: 'divider',
            boxShadow: '0 28px 80px rgba(58, 40, 48, 0.12)',
          }}
        >
          <Box
            sx={{
              display: { xs: 'none', md: 'flex' },
              minHeight: 610,
              p: 6,
              flexDirection: 'column',
              justifyContent: 'space-between',
              color: 'primary.contrastText',
              background: (theme) =>
                `linear-gradient(145deg, ${theme.palette.primary.dark}, ${theme.palette.primary.main} 58%, ${theme.palette.secondary.dark})`,
            }}
          >
            <Typography variant="overline" sx={{ letterSpacing: '0.16em', fontWeight: 800 }}>
              Wedding Gallery
            </Typography>
            <Stack spacing={2} sx={{ maxWidth: 520 }}>
              <Typography variant="h1" component="p" sx={{ color: 'inherit' }}>
                Every memory, in one private place.
              </Typography>
              <Typography sx={{ color: 'rgba(255,255,255,0.78)', fontSize: '1.08rem' }}>
                Collect photos and videos from the people who shared your day — simply and securely.
              </Typography>
            </Stack>
            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.7)' }}>
              Private by design · effortless for guests
            </Typography>
          </Box>

          <Box sx={{ p: { xs: 3, sm: 5, md: 7 }, alignSelf: 'center' }}>
            <Stack spacing={3.5}>
              <Box>
                <Typography
                  variant="overline"
                  color="primary"
                  sx={{ letterSpacing: '0.14em', fontWeight: 800 }}
                >
                  Welcome back
                </Typography>
                <Typography component="h1" variant="h2" sx={{ mt: 0.5 }}>
                  Sign in to your gallery
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Manage events, galleries and guest uploads.
                </Typography>
              </Box>

              <Stack component="form" spacing={2.5} onSubmit={handleSubmit} noValidate>
                <TextField
                  id="login-email"
                  label="Email"
                  type="email"
                  value={email}
                  autoComplete="email"
                  onChange={(event) => setEmail(event.target.value)}
                  required
                  disabled={submitting}
                />
                <TextField
                  id="login-password"
                  label="Password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  autoComplete="current-password"
                  onChange={(event) => setPassword(event.target.value)}
                  required
                  disabled={submitting}
                  slotProps={{
                    input: {
                      endAdornment: (
                        <InputAdornment position="end">
                          <Button
                            type="button"
                            variant="text"
                            size="small"
                            aria-label={showPassword ? 'Hide password' : 'Show password'}
                            aria-pressed={showPassword}
                            onClick={() => setShowPassword((current) => !current)}
                            sx={{ minHeight: 36, minWidth: 64, px: 1 }}
                          >
                            {showPassword ? 'Hide' : 'Show'}
                          </Button>
                        </InputAdornment>
                      ),
                    },
                  }}
                />
                {error && (
                  <Alert severity="error" role="alert">
                    {error}
                  </Alert>
                )}
                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  disabled={submitting || !email || !password}
                  aria-busy={submitting}
                  startIcon={submitting ? <CircularProgress size={18} color="inherit" /> : null}
                >
                  {submitting ? 'Signing in…' : 'Sign In'}
                </Button>
              </Stack>
            </Stack>
          </Box>
        </Paper>
      </Container>
    </Box>
  )
}
