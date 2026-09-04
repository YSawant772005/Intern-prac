<template>
  <div class="app-shell">
    <div class="page">
      <header class="topbar">
        <div class="brand">
          <div class="brand-mark">PB</div>
          <div class="brand-copy">
            <h1>Phonebook Application</h1>
            <p>FastAPI, Vue 3, SQLAlchemy, and PostgreSQL in one simple CRUD demo.</p>
          </div>
        </div>

        <div class="topbar-actions">
          <RouterLink class="button-secondary" :to="{ name: 'contact-list' }">Contacts</RouterLink>
          <RouterLink class="button" :to="{ name: 'contact-create' }">+ Add Contact</RouterLink>
        </div>
      </header>

      <main>
        <RouterView />
      </main>
    </div>

    <div class="toast-stack" aria-live="polite" aria-atomic="true">
      <NotificationToast :message="toast" />
    </div>
  </div>
</template>

<script setup>
import { provide, ref } from 'vue'

import NotificationToast from './components/NotificationToast.vue'
import { provideNotification } from './composables/notifications'

const toast = ref(null)
let timerId = null

function notify(message) {
  toast.value = message
  if (timerId) {
    clearTimeout(timerId)
  }
  timerId = window.setTimeout(() => {
    toast.value = null
    timerId = null
  }, 2800)
}

provideNotification(notify)
provide('notify', notify)
</script>
