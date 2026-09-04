<template>
  <section>
    <div v-if="loadingContact" class="panel state-card">
      <div class="loading-row">
        <span class="spinner"></span>
        <span>Loading contact...</span>
      </div>
    </div>

    <div v-else-if="loadError" class="panel state-card">
      <h3>Unable to load contact.</h3>
      <p>{{ loadError }}</p>
      <div class="detail-actions" style="justify-content: center; margin-top: 18px">
        <button class="button" type="button" @click="loadContact">Retry</button>
        <RouterLink class="button-secondary" :to="{ name: 'contact-list' }">Back to list</RouterLink>
      </div>
    </div>

    <ContactForm
      v-else
      :mode="mode"
      :busy="saving"
      :busy-label="mode === 'edit' ? 'Updating...' : 'Creating...'"
      :server-error="serverError"
      :submit-label="mode === 'edit' ? 'Update contact' : 'Create contact'"
      :contact="contact"
      @submit="handleSubmit"
      @cancel="handleCancel"
    />
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import ContactForm from '../components/ContactForm.vue'
import { createContact, getContact, updateContact } from '../services/api'
import { useNotification } from '../composables/notifications'

const props = defineProps({
  mode: {
    type: String,
    required: true,
    validator: value => ['create', 'edit'].includes(value),
  },
  id: {
    type: [String, Number],
    default: null,
  },
})

const router = useRouter()
const notify = useNotification()

const contact = ref({ name: '', phone_number: '', email: '', address: '' })
const loadingContact = ref(props.mode === 'edit')
const loadError = ref('')
const saving = ref(false)
const serverError = ref('')

async function loadContact() {
  if (props.mode !== 'edit') {
    return
  }

  loadingContact.value = true
  loadError.value = ''
  try {
    contact.value = await getContact(props.id)
  } catch (err) {
    loadError.value = err instanceof Error ? err.message : 'Unable to load contact.'
  } finally {
    loadingContact.value = false
  }
}

async function handleSubmit(payload) {
  saving.value = true
  serverError.value = ''
  try {
    if (props.mode === 'edit') {
      const updated = await updateContact(props.id, payload)
      notify({ type: 'success', text: 'Contact updated successfully.' })
      router.push({ name: 'contact-detail', params: { id: updated.id } })
      return
    }

    const created = await createContact(payload)
    notify({ type: 'success', text: 'Contact created successfully.' })
    router.push({ name: 'contact-detail', params: { id: created.id } })
  } catch (err) {
    serverError.value = err instanceof Error ? err.message : 'Unable to save contact.'
  } finally {
    saving.value = false
  }
}

function handleCancel() {
  router.push(props.mode === 'edit' && props.id ? { name: 'contact-detail', params: { id: props.id } } : { name: 'contact-list' })
}

watch(
  () => props.id,
  async () => {
    if (props.mode === 'edit') {
      await loadContact()
    }
  },
)

onMounted(loadContact)
</script>
