import { createRouter, createWebHistory } from 'vue-router'

import ContactDetailView from '../views/ContactDetailView.vue'
import ContactFormView from '../views/ContactFormView.vue'
import ContactListView from '../views/ContactListView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'contact-list', component: ContactListView },
    { path: '/contacts/new', name: 'contact-create', component: ContactFormView, props: { mode: 'create' } },
    {
      path: '/contacts/:id',
      name: 'contact-detail',
      component: ContactDetailView,
      props: true,
    },
    {
      path: '/contacts/:id/edit',
      name: 'contact-edit',
      component: ContactFormView,
      props: route => ({ mode: 'edit', id: route.params.id }),
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
