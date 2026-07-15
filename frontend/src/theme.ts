import { alpha, createTheme } from '@mui/material/styles'

const plum = '#6C4658'
const plumDark = '#4F3040'
const champagne = '#A9784B'

export const appTheme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: plum, dark: plumDark, contrastText: '#FFFFFF' },
    secondary: { main: champagne, dark: '#7A5232', contrastText: '#FFFFFF' },
    background: { default: '#F8F6F3', paper: '#FFFFFF' },
    text: { primary: '#292326', secondary: '#6D6267' },
    divider: '#E8E0DC',
    success: { main: '#397257' },
    warning: { main: '#9A641E' },
    error: { main: '#B33A45' },
    info: { main: '#496B83' },
  },
  shape: { borderRadius: 16 },
  spacing: 8,
  typography: {
    fontFamily: [
      'Inter',
      'ui-sans-serif',
      'system-ui',
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'sans-serif',
    ].join(','),
    h1: { fontSize: 'clamp(2.25rem, 7vw, 4.5rem)', lineHeight: 1.05, fontWeight: 750 },
    h2: { fontSize: 'clamp(1.75rem, 4vw, 2.75rem)', lineHeight: 1.12, fontWeight: 740 },
    h3: { fontSize: 'clamp(1.45rem, 3vw, 2rem)', lineHeight: 1.2, fontWeight: 720 },
    h4: { fontSize: 'clamp(1.3rem, 2.4vw, 1.7rem)', lineHeight: 1.25, fontWeight: 720 },
    h5: { fontWeight: 700 },
    h6: { fontWeight: 700 },
    button: { fontWeight: 700, textTransform: 'none', letterSpacing: 0 },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        html: { minWidth: 320, scrollBehavior: 'smooth' },
        body: { minWidth: 320 },
        '::selection': { backgroundColor: alpha(plum, 0.18) },
        'a, button, input, textarea, select, [tabindex]': {
          '&:focus-visible': {
            outline: `3px solid ${alpha(plum, 0.42)}`,
            outlineOffset: 3,
          },
        },
        '@media (prefers-reduced-motion: reduce)': {
          '*, *::before, *::after': {
            animationDuration: '0.01ms !important',
            animationIterationCount: '1 !important',
            scrollBehavior: 'auto !important',
            transitionDuration: '0.01ms !important',
          },
        },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { minHeight: 44, borderRadius: 12, paddingInline: 18 },
        sizeLarge: { minHeight: 50, fontSize: '1rem' },
      },
    },
    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          border: '1px solid',
          borderColor: '#E8E0DC',
          boxShadow: '0 12px 34px rgba(58, 40, 48, 0.06)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: { borderRadius: 18 },
      },
    },
    MuiTextField: {
      defaultProps: { fullWidth: true },
    },
    MuiOutlinedInput: {
      styleOverrides: { root: { borderRadius: 12, backgroundColor: '#FFFFFF' } },
    },
    MuiDialog: {
      styleOverrides: {
        paper: { backgroundImage: 'none' },
      },
    },
    MuiChip: {
      styleOverrides: { root: { fontWeight: 650 } },
    },
    MuiAlert: {
      styleOverrides: { root: { borderRadius: 12 } },
    },
    MuiLinearProgress: {
      styleOverrides: { root: { height: 8, borderRadius: 999 } },
    },
  },
})
