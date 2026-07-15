import { AppBar, Box, Button, Container, Stack, Toolbar, Typography } from '@mui/material'
import type { ReactNode } from 'react'

interface AppShellProps {
  email?: string
  onLogout: () => void
  children: ReactNode
}

export default function AppShell({ email, onLogout, children }: AppShellProps) {
  return (
    <Box sx={{ minHeight: '100dvh' }}>
      <AppBar
        position="sticky"
        color="inherit"
        elevation={0}
        sx={{ borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}
      >
        <Container maxWidth="lg" disableGutters>
          <Toolbar sx={{ minHeight: { xs: 68, sm: 76 }, px: { xs: 2, sm: 3 } }}>
            <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', minWidth: 0 }}>
              <Box
                aria-hidden="true"
                sx={{
                  width: 38,
                  height: 38,
                  flex: '0 0 auto',
                  display: 'grid',
                  placeItems: 'center',
                  bgcolor: 'primary.main',
                  color: 'primary.contrastText',
                  borderRadius: '12px 12px 12px 4px',
                  fontWeight: 850,
                }}
              >
                W
              </Box>
              <Box sx={{ minWidth: 0 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 800, lineHeight: 1.15 }}>
                  Wedding Gallery
                </Typography>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  noWrap
                  sx={{ display: { xs: 'none', sm: 'block' } }}
                >
                  {email}
                </Typography>
              </Box>
            </Stack>
            <Box sx={{ flexGrow: 1 }} />
            <Button variant="outlined" onClick={onLogout}>
              Log out
            </Button>
          </Toolbar>
        </Container>
      </AppBar>
      <Container component="main" maxWidth="lg" sx={{ py: { xs: 3, sm: 5 } }}>
        {children}
      </Container>
    </Box>
  )
}
