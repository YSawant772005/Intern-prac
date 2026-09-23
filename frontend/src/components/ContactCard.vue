<template>
  <article class="contact-list-item">
    <div class="contact-list-item__main">
      <RouterLink class="contact-item-title" :to="{ name: 'contact-detail', params: { id: contact.id } }">
        {{ contact.name }}
      </RouterLink>
      <p class="contact-item-phone">{{ contact.phone_number }}</p>
      <p class="contact-item-email">{{ contact.email || 'No email provided' }}</p>
      <p class="contact-item-address">{{ contact.address || 'No address provided' }}</p>
    </div>

    <div class="contact-list-item__actions">
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
