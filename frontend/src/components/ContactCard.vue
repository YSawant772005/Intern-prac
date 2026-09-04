<template>
  <article class="contact-card">
    <div class="contact-card__header">
      <div>
        <p class="page-subtitle" style="margin: 0 0 6px">Contact</p>
        <h3 class="contact-name">{{ contact.name }}</h3>
      </div>
      <span class="pill-link" style="pointer-events: none">{{ contact.phone_number }}</span>
    </div>

    <div class="contact-meta">
      <div class="meta-row">
        <span class="meta-label">Email</span>
        <span class="meta-value">{{ contact.email || 'Not provided' }}</span>
      </div>
      <div class="meta-row">
        <span class="meta-label">Address</span>
        <span class="meta-value">{{ contact.address || 'Not provided' }}</span>
      </div>
    </div>

    <div class="contact-actions">
      <RouterLink class="button-secondary" :to="{ name: 'contact-detail', params: { id: contact.id } }">View</RouterLink>
      <RouterLink class="button-secondary" :to="{ name: 'contact-edit', params: { id: contact.id } }">Edit</RouterLink>
      <button
        class="button-danger"
        type="button"
        :disabled="deleting"
        @click="$emit('delete', contact)"
      >
        {{ deleting ? 'Deleting...' : 'Delete' }}
      </button>
    </div>
  </article>
</template>

<script setup>
defineProps({
  contact: {
    type: Object,
    required: true,
  },
  deleting: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['delete'])
</script>
