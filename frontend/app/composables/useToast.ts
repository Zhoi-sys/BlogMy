export interface ToastItem {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

let toastId = 0

export function useToast() {
  const items = useState<ToastItem[]>('inkwell-toasts', () => [])

  function remove(id: number) {
    items.value = items.value.filter(item => item.id !== id)
  }

  function show(message: string, type: ToastItem['type'] = 'info') {
    const id = ++toastId
    items.value = [...items.value, { id, message, type }]
    if (import.meta.client) {
      window.setTimeout(() => remove(id), 3200)
    }
  }

  return {
    items,
    show,
    success: (message: string) => show(message, 'success'),
    error: (message: string) => show(message, 'error'),
    info: (message: string) => show(message, 'info'),
    remove
  }
}
