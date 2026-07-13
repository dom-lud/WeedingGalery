import api from './api'

export type EventRole = 'OWNER' | 'MANAGER'
export type EventType = 'WEDDING' | 'BIRTHDAY' | 'CORPORATE' | 'OTHER'

export interface EventData {
  id: string
  name: string
  type: EventType
  eventDate: string | null
  description: string | null
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED'
  privacyMode: 'PRIVATE'
  currentUserRole: EventRole
  createdAt: string
  updatedAt: string
}

export interface EventMember {
  id: string | null
  userId: string
  email: string
  role: EventRole
  joinedAt: string
}

export interface EventWritePayload {
  name: string
  type: EventType
  eventDate: string | null
  description: string | null
  privacyMode: 'PRIVATE'
}

export const eventsApi = {
  list: () => api.get<EventData[]>('events'),
  create: (payload: EventWritePayload) => api.post<EventData>('events', payload),
  update: (id: string, payload: EventWritePayload) => api.put<EventData>(`events/${id}`, payload),
  archive: (id: string) => api.post<EventData>(`events/${id}/archive`),
  remove: (id: string) => api.delete(`events/${id}`),
  members: (id: string) => api.get<EventMember[]>(`events/${id}/members`),
  addManager: (id: string, email: string) =>
    api.post<EventMember>(`events/${id}/members`, { email, role: 'MANAGER' }),
  removeManager: (eventId: string, membershipId: string) =>
    api.delete(`events/${eventId}/members/${membershipId}`),
  transferOwnership: (eventId: string, targetMembershipId: string) =>
    api.post<EventData>(`events/${eventId}/ownership-transfer`, { targetMembershipId }),
}
