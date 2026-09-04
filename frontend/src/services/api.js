const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()

  if (!response.ok) {
    const message = typeof payload === 'string' ? payload : payload?.detail || payload?.message || 'Request failed.'
    throw new Error(message)
  }

  return payload
}

export function listContacts() {
  return request('/contacts')
}

export function getContact(id) {
  return request(`/contacts/${id}`)
}

export function createContact(contact) {
  return request('/contacts', {
    method: 'POST',
    body: JSON.stringify(contact),
  })
}

export function updateContact(id, contact) {
  return request(`/contacts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(contact),
  })
}

export function deleteContact(id) {
  return request(`/contacts/${id}`, {
    method: 'DELETE',
  })
}
