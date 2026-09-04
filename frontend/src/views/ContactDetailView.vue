<template>
  <section class="detail-card">
    <div v-if="loading" class="state-card">
      <div class="loading-row">
        <span class="spinner"></span>
        <span>Loading contact...</span>
      </div>
    </div>

    <div v-else-if="error" class="state-card">
      <h3>Unable to load contact.</h3>
      <p>{{ error }}</p>
      <div class="detail-actions" style="justify-content: center; margin-top: 18px">
        <button class="button" type="button" @click="loadContact">Retry</button>
        <RouterLink class="button-secondary" :to="{ name: 'contact-list' }">Back to list</RouterLink>
      </div>
    </div>

    <template v-else-if="contact">
      <div class="section-header">
        <div class="page-title-block">
          <h2 class="page-title">{{ contact.name }}</h2>
          <p class="page-subtitle">Contact details and actions.</p>
        </div>
        <div class="toolbar">
          <RouterLink class="button-secondary" :to="{ name: 'contact-edit', params: { id: contact.id } }">Edit</RouterLink>
          <button class="button-danger" type="button" @click="handleDelete">Delete</button>
        </div>
      </div>

      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-item__label">Phone number</span>
          <p class="detail-item__value">{{ contact.phone_number }}</p>
        </div>

        <div class="detail-item">
          <span class="detail-item__label">Email</span>
          <p class="detail-item__value">{{ contact.email || 'Not provided' }}</p>
        </div>

        <div class="detail-item detail-item--full">
          <span class="detail-item__label">Address</span>
          <p class="detail-item__value">{{ contact.address || 'Not provided' }}</p>
        </div>

        <div class="detail-item detail-item--full">
          <span class="detail-item__label">Created at</span>
          <p class="detail-item__value">{{ formattedCreatedAt }}</p>
        </div>
      </div>

      <div class="detail-actions">
        <RouterLink class="button-secondary" :to="{ name: 'contact-list' }">Back to list</RouterLink>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { deleteContact, getContact } from '../services/api'
import { useNotification } from '../composables/notifications'

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
})

const route = useRoute()
const router = useRouter()
const notify = useNotification()

const contact = ref(null)
const loading = ref(true)
const error = ref('')

const formattedCreatedAt = computed(() => {
  if (!contact.value?.created_at) {
    return 'Not available'
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(contact.value.created_at))
})

async function loadContact() {
  loading.value = true
  error.value = ''
  try {
    contact.value = await getContact(props.id)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load contact.'
  } finally {
    loading.value = false
  }
}

async function handleDelete() {
  if (!contact.value) {
    return
  }

  const confirmed = window.confirm(`Delete ${contact.value.name}? This action cannot be undone.`)
  if (!confirmed) {
    return
  }

  try {
    await deleteContact(contact.value.id)
    notify({ type: 'success', text: 'Contact deleted successfully.' })
    router.push({ name: 'contact-list' })
  } catch (err) {
    notify({ type: 'error', text: err instanceof Error ? err.message : 'Unable to delete contact.' })
  }
}

watch(
  () => route.params.id,
  async newId => {
    if (newId) {
      await loadContact()
    }
  },
)

onMounted(loadContact)
</script>
