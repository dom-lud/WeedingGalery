import { AppBar, Avatar, Box, Button, Container, Stack, Toolbar, Typography } from '@mui/material'
import type { ReactNode } from 'react'

interface AppShellProps {
  email?: string
  onLogout: () => void
  children: ReactNode
}

export default function AppShell({ email, onLogout, children }: AppShellProps) {
  return (
    <Box
      sx={{
        minHeight: '100dvh',
        background: (theme) =>
          `radial-gradient(circle at 7% 0%, ${theme.palette.secondary.main}12, transparent 28%), ${theme.palette.background.default}`,
      }}
    >
      <AppBar
        position="sticky"
        color="inherit"
        elevation={0}
        sx={{
          borderBottom: '1px solid',
          borderColor: 'divider',
          bgcolor: 'rgba(255, 254, 253, 0.88)',
          backdropFilter: 'blur(16px)',
        }}
      >
        <Container maxWidth="xl" disableGutters>
          <Toolbar sx={{ minHeight: { xs: 68, sm: 76 }, px: { xs: 2, sm: 4 } }}>
            <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', minWidth: 0 }}>
              <Box
                aria-hidden="true"
                sx={{
                  width: 42,
                  height: 42,
                  flex: '0 0 auto',
                  display: 'grid',
                  placeItems: 'center',
                  bgcolor: 'primary.main',
                  color: 'primary.contrastText',
                  borderRadius: '14px 14px 14px 5px',
                  fontWeight: 850,
                }}
              >
                W
              </Box>
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.1 }}>
                  Wedding Gallery
                </Typography>
                <Typography variant="caption" color="text.secondary" noWrap>
                  Event workspace
                </Typography>
              </Box>
            </Stack>
            <Box sx={{ flexGrow: 1 }} />
            <Stack direction="row" spacing={1.25} sx={{ alignItems: 'center' }}>
              <Avatar
                sx={{
                  display: { xs: 'none', sm: 'grid' },
                  width: 36,
                  height: 36,
                  bgcolor: 'secondary.main',
                  fontSize: '0.85rem',
                  fontWeight: 800,
                }}
              >
                {email?.charAt(0).toUpperCase() ?? 'U'}
              </Avatar>
              <Box sx={{ display: { xs: 'none', md: 'block' }, maxWidth: 220 }}>
                <Typography variant="caption" color="text.secondary">
                  Signed in as
                </Typography>
                <Typography variant="body2" noWrap sx={{ fontWeight: 700 }}>
                  {email}
                </Typography>
              </Box>
              <Button color="inherit" onClick={onLogout}>
                Log out
              </Button>
            </Stack>
          </Toolbar>
        </Container>
      </AppBar>
      <Container component="main" maxWidth="xl" sx={{ py: { xs: 2.5, sm: 4 }, px: { sm: 4 } }}>
        {children}
      </Container>
    </Box>
  )
}
