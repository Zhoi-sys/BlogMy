export function useTheme() {
  const theme = useState<'light' | 'dark'>('inkwell-theme', () => 'light')

  function apply(next: 'light' | 'dark') {
    theme.value = next
    if (import.meta.client) {
      document.documentElement.dataset.theme = next
      localStorage.setItem('inkwell-theme', next)
    }
  }

  function init() {
    if (!import.meta.client) return
    const current = document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'
    theme.value = current
  }

  function toggle() {
    apply(theme.value === 'dark' ? 'light' : 'dark')
  }

  return { theme, init, toggle }
}
