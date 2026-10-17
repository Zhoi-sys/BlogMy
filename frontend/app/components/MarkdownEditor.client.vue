<template>
  <div>
    <div v-if="errorMessage" class="editor-loading" style="color: var(--danger)">
      {{ errorMessage }}
    </div>
    <div ref="editorRef" class="editor-mount" />
  </div>
</template>

<script setup lang="ts">
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const props = withDefaults(defineProps<{
  modelValue: string
  theme?: 'light' | 'dark'
}>(), {
  theme: 'light'
})

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const editorRef = ref<HTMLElement | null>(null)
const toast = useToast()
const errorMessage = ref('')
let vditor: any = null
let ready = false

function currentValue() {
  return vditor?.getValue?.() || props.modelValue
}

onMounted(async () => {
  // Nuxt ClientOnly/Suspense 下模板 ref 可能比 mounted 钩子晚一个 tick 绑定。
  await nextTick()
  if (!editorRef.value) return
  try {
    const Editor = (Vditor as unknown as { default?: typeof Vditor }).default || Vditor
    vditor = new (Editor as any)(editorRef.value, {
      height: 580,
      mode: 'sv',
      value: props.modelValue || '',
      cache: { enable: false },
      theme: props.theme === 'dark' ? 'dark' : 'classic',
      preview: {
        theme: { current: props.theme === 'dark' ? 'dark' : 'light' },
        math: { engine: 'KaTeX' }
      },
      upload: {
        handler() {
          toast.info('一期仅支持外链图片，请先上传图床再插入图片链接。')
          return null
        }
      },
      input: (value: string) => emit('update:modelValue', value),
      after: () => {
        ready = true
        if (props.modelValue && vditor?.getValue?.() !== props.modelValue) {
          vditor?.setValue?.(props.modelValue)
        }
      }
    })
  } catch (error) {
    errorMessage.value = `编辑器加载失败：${error instanceof Error ? error.message : '未知错误'}`
  }
})

watch(() => props.modelValue, (value) => {
  if (!ready || !vditor) return
  if (vditor.getValue?.() !== value) {
    vditor.setValue?.(value)
  }
})

onBeforeUnmount(() => {
  vditor?.destroy?.()
  vditor = null
})

defineExpose({
  getValue: currentValue,
  insertValue: (value: string) => vditor?.insertValue?.(value),
  setMode: (mode: 'ir' | 'sv') => vditor?.setMode?.(mode),
  focus: () => vditor?.focus?.()
})
</script>
