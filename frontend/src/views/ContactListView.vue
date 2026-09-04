<template>
  <section class="panel">
    <div class="section-header">
      <div class="page-title-block">
        <h2 class="page-title">Contacts</h2>
        <p class="page-subtitle">View, create, update, and delete phonebook entries.</p>
      </div>
      <div class="toolbar">
        <RouterLink class="button" :to="{ name: 'contact-create' }">+ Add Contact</RouterLink>
      </div>
    </div>

    <div v-if="loading" class="state-card">
      <div class="loading-row">
        <span class="spinner"></span>
        <span>Loading contacts...</span>
      </div>
    </div>

    <div v-else-if="error" class="state-card">
      <h3>Unable to load contacts.</h3>
      <p>Please try again.</p>
      <button class="button" type="button" @click="loadContacts">Retry</button>
    </div>

    <div v-else-if="contacts.length === 0" class="state-card">
      <h3>No contacts found.</h3>
      <p>Add your first contact.</p>
      <RouterLink class="button" :to="{ name: 'contact-create' }">+ Add Contact</RouterLink>
    </div>

    <div v-else class="contact-grid">
      <ContactCard
        v-for="contact in contacts"
        :key="contact.id"
        :contact="contact"
        :deleting="deletingId === contact.id"
        @delete="confirmDelete"
      />
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import ContactCard from '../components/ContactCard.vue'
import { deleteContact, listContacts } from '../services/api'
import { useNotification } from '../composables/notifications'

const contacts = ref([])
const loading = ref(true)
const error = ref('')
const deletingId = ref(null)
const notify = useNotification()

async function loadContacts() {
  loading.value = true
  error.value = ''
  try {
    contacts.value = await listContacts()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load contacts.'
  } finally {
    loading.value = false
  }
}

async function confirmDelete(contact) {
  const confirmed = window.confirm(`Delete ${contact.name}? This action cannot be undone.`)
  if (!confirmed) {
    return
  }

  deletingId.value = contact.id
  try {
    await deleteContact(contact.id)
    contacts.value = contacts.value.filter(item => item.id !== contact.id)
    notify({ type: 'success', text: 'Contact deleted successfully.' })
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to delete contact.'
    notify({ type: 'error', text: error.value })
  } finally {
    deletingId.value = null
  }
}

onMounted(loadContacts)
</script>
