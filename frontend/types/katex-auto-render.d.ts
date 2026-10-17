declare module 'katex/contrib/auto-render' {
  interface AutoRenderOptions {
    delimiters?: Array<{ left: string; right: string; display: boolean }>
    throwOnError?: boolean
    ignoredTags?: string[]
  }

  export default function renderMathInElement(element: HTMLElement, options?: AutoRenderOptions): void
}
