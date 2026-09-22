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

    <form class="contact-search" @submit.prevent="runSearch">
      <label class="sr-only" for="contact-search">Search contacts</label>
      <input id="contact-search" v-model="searchInput" type="search" maxlength="100" placeholder="Search contacts" :disabled="loading" />
      <label class="sr-only" for="contact-sort">Sort contacts</label>
      <select id="contact-sort" v-model="sort" class="sort-select" :disabled="loading" @change="changeSort">
        <option value="name,asc">Name: A → Z</option>
        <option value="name,desc">Name: Z → A</option>
      </select>
      <button class="button-secondary" type="submit" :disabled="loading">Search</button>
      <button v-if="search" class="button-ghost" type="button" :disabled="loading" @click="clearSearch">Clear</button>
    </form>

    <p v-if="!loading && !error" class="contact-summary">{{ summaryText }}</p>

    <div v-if="loading" class="state-card">
      <div class="loading-row">
        <span class="spinner"></span>
        <span>Loading contacts...</span>
      </div>
    </div>

    <div v-else-if="error" class="state-card">
      <h3>Unable to load contacts.</h3>
      <p>{{ error }}</p>
      <button class="button" type="button" @click="loadContacts">Retry</button>
    </div>

    <div v-else-if="contacts.length === 0" class="state-card">
      <h3>{{ search ? 'No matching contacts found.' : 'No contacts found.' }}</h3>
      <p>{{ search ? 'Try a different search.' : 'Add your first contact.' }}</p>
      <RouterLink v-if="!search" class="button" :to="{ name: 'contact-create' }">+ Add Contact</RouterLink>
    </div>

    <template v-else>
      <div class="contact-grid">
        <ContactCard
          v-for="contact in contacts"
          :key="contact.id"
          :contact="contact"
          :deleting="deletingId === contact.id"
          @delete="confirmDelete"
        />
      </div>

      <nav v-if="totalPages > 1" class="pagination" aria-label="Contact pages">
        <button class="button-secondary" type="button" :disabled="loading || page === 0" @click="changePage(page - 1)">Previous</button>
        <span>Page {{ page + 1 }} of {{ totalPages }}</span>
        <button class="button-secondary" type="button" :disabled="loading || page >= totalPages - 1" @click="changePage(page + 1)">Next</button>
      </nav>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import ContactCard from '../components/ContactCard.vue'
import { deleteContact, listContacts } from '../services/api'
import { useNotification } from '../composables/notifications'

const PAGE_SIZE = 20
const contacts = ref([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const searchInput = ref('')
const search = ref('')
const sort = ref('name,asc')
const loading = ref(true)
const error = ref('')
const deletingId = ref(null)
const notify = useNotification()

const summaryText = computed(() => {
  const count = totalElements.value.toLocaleString('en-IN')
  return search.value
    ? `${count} contact${totalElements.value === 1 ? '' : 's'} found`
    : `${count} contact${totalElements.value === 1 ? '' : 's'} total`
})

async function loadContacts() {
  loading.value = true
  error.value = ''
  try {
    const result = await listContacts({ page: page.value, size: PAGE_SIZE, search: search.value, sort: sort.value })
    contacts.value = result.content
    totalPages.value = result.totalPages
    totalElements.value = result.totalElements
    if (page.value >= totalPages.value && totalPages.value > 0) {
      page.value = totalPages.value - 1
      await loadContacts()
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load contacts.'
  } finally {
    loading.value = false
  }
}

function runSearch() {
  page.value = 0
  search.value = searchInput.value.trim()
  loadContacts()
}

function clearSearch() {
  searchInput.value = ''
  search.value = ''
  page.value = 0
  loadContacts()
}

function changeSort() {
  page.value = 0
  loadContacts()
}

function changePage(nextPage) {
  page.value = nextPage
  loadContacts()
}

async function confirmDelete(contact) {
  if (deletingId.value !== null) {
    return
  }
  const confirmed = window.confirm(`Delete ${contact.name}? This action cannot be undone.`)
  if (!confirmed) {
    return
  }

  deletingId.value = contact.id
  try {
    await deleteContact(contact.id)
    notify({ type: 'success', text: 'Contact deleted successfully.' })
    await loadContacts()
  } catch (err) {
    notify({ type: 'error', text: err instanceof Error ? err.message : 'Unable to delete contact.' })
  } finally {
    deletingId.value = null
  }
}

onMounted(loadContacts)
</script>
