<template>
  <Teleport to="body">
    <TransitionGroup name="toast" tag="div" class="toast-stack">
      <div v-for="item in items" :key="item.id" class="toast" :class="item.type">
        {{ item.message }}
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup lang="ts">
const { items } = useToast()
</script>
