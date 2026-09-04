<template>
  <form class="form-card" @submit.prevent="handleSubmit">
    <div class="section-header" style="margin-bottom: 24px">
      <div class="page-title-block">
        <h2>{{ title }}</h2>
        <p>{{ subtitle }}</p>
      </div>
      <div class="toolbar">
        <RouterLink class="button-secondary" :to="{ name: 'contact-list' }">Back to list</RouterLink>
      </div>
    </div>

    <p v-if="serverError" class="form-error">{{ serverError }}</p>

    <div class="form-grid">
      <div class="field">
        <label for="name">Name *</label>
        <input
          id="name"
          v-model="form.name"
          type="text"
          maxlength="255"
          placeholder="Enter contact name"
          :disabled="busy"
        />
        <p v-if="errors.name" class="field-error">{{ errors.name }}</p>
      </div>

      <div class="field">
        <label for="phone_number">Phone number *</label>
        <input
          id="phone_number"
          v-model="form.phone_number"
          type="text"
          inputmode="tel"
          placeholder="9876543210"
          :disabled="busy"
        />
        <p v-if="errors.phone_number" class="field-error">{{ errors.phone_number }}</p>
      </div>

      <div class="field">
        <label for="email">Email</label>
        <input
          id="email"
          v-model="form.email"
          type="email"
          placeholder="name@example.com"
          :disabled="busy"
        />
        <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
      </div>

      <div class="field field--full">
        <label for="address">Address</label>
        <textarea
          id="address"
          v-model="form.address"
          placeholder="Enter address"
          :disabled="busy"
        ></textarea>
      </div>
    </div>

    <div class="form-actions" style="margin-top: 24px">
      <button class="button" type="submit" :disabled="busy">{{ busy ? busyLabel : submitLabel }}</button>
      <button class="button-ghost" type="button" :disabled="busy" @click="$emit('cancel')">Cancel</button>
    </div>
  </form>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'

const PHONE_PATTERN = /^\+?[0-9][0-9\s().-]*$/
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const props = defineProps({
  contact: {
    type: Object,
    default: () => ({ name: '', phone_number: '', email: '', address: '' }),
  },
  submitLabel: {
    type: String,
    default: 'Save contact',
  },
  busyLabel: {
    type: String,
    default: 'Saving...',
  },
  busy: {
    type: Boolean,
    default: false,
  },
  serverError: {
    type: String,
    default: '',
  },
  mode: {
    type: String,
    default: 'create',
  },
})

const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  name: '',
  phone_number: '',
  email: '',
  address: '',
})

const errors = reactive({
  name: '',
  phone_number: '',
  email: '',
})

const title = computed(() => (props.mode === 'edit' ? 'Edit Contact' : 'Add Contact'))
const subtitle = computed(() =>
  props.mode === 'edit' ? 'Update the contact information and save the changes.' : 'Fill in the details below to add a new contact.',
)

function syncForm() {
  form.name = props.contact?.name ?? ''
  form.phone_number = props.contact?.phone_number ?? ''
  form.email = props.contact?.email ?? ''
  form.address = props.contact?.address ?? ''
}

watch(
  () => props.contact,
  () => syncForm(),
  { immediate: true, deep: true },
)

function clearErrors() {
  errors.name = ''
  errors.phone_number = ''
  errors.email = ''
}

function validateForm() {
  clearErrors()

  if (!form.name.trim()) {
    errors.name = 'Name is required.'
  } else if (form.name.trim().length > 255) {
    errors.name = 'Name cannot exceed 255 characters.'
  } else if ([...form.name.trim()].some(character => /\d/.test(character))) {
    errors.name = 'Name cannot contain numbers.'
  }

  if (!form.phone_number.trim()) {
    errors.phone_number = 'Phone number is required.'
  } else if (
    !PHONE_PATTERN.test(form.phone_number.trim()) ||
    form.phone_number.replace(/\D/g, '').length !== 10
  ) {
    errors.phone_number = 'Phone number must contain exactly 10 digits.'
  }

  if (form.email.trim() && !EMAIL_PATTERN.test(form.email.trim())) {
    errors.email = 'Email must be valid if provided.'
  }

  return !errors.name && !errors.phone_number && !errors.email
}

function handleSubmit() {
  if (!validateForm()) {
    return
  }

  emit('submit', {
    name: form.name.trim(),
    phone_number: form.phone_number.trim(),
    email: form.email.trim() || null,
    address: form.address.trim() || null,
  })
}
</script>
