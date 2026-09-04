<template>
  <div v-if="message" :class="['toast', `toast--${message.type}`]" role="status" aria-live="polite">
    <p class="toast__title">{{ message.type === 'error' ? 'Error' : 'Success' }}</p>
    <p class="toast__message">{{ message.text }}</p>
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: Object,
    default: null,
  },
})
</script>
