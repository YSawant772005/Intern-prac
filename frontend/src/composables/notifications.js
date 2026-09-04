import { inject, provide } from 'vue'

const notificationKey = Symbol('notification')

export function provideNotification(notify) {
  provide(notificationKey, notify)
}

export function useNotification() {
  const notify = inject(notificationKey)
  if (!notify) {
    throw new Error('Notification provider is missing.')
  }
  return notify
}
