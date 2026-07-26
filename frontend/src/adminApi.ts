import api from './api'

export type AdminUserStatus = 'ACTIVE' | 'BLOCKED'

export interface AdminDashboardSummary {
  users: number
  lockedUsers?: number
  events: number
  galleries: number
  media: number
  storageUsedBytes?: number
  auditEvents?: number
}

export interface AdminUser {
  id: string
  email: string
  systemRole: 'ADMIN' | 'USER'
  locked?: boolean
  lockedUntil?: string | null
  createdAt: string
}

export interface AdminEvent {
  id: string
  name: string
  ownerEmail: string
  type: string
  status: string
  ownerUserId: string
  eventDate: string
  createdAt: string
  updatedAt: string
}

export interface AdminAuditEntry {
  id: string
  eventType: string
  actorEmail: string | null
  resourceType: string | null
  resourceId: string | null
  createdAt: string
  result: string | null
}

export interface AdminPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}

export interface AdminListParams {
  query?: string
  page?: number
  size?: number
}

export const adminApi = {
  dashboard: () => api.get<AdminDashboardSummary>('admin/dashboard'),
  users: (params?: AdminListParams) =>
    api.get<AdminPage<AdminUser> | AdminUser[]>('admin/users', { params }),
  events: (params?: AdminListParams) =>
    api.get<AdminPage<AdminEvent> | AdminEvent[]>('admin/events', { params }),
  audit: (params?: AdminListParams) =>
    api.get<AdminPage<AdminAuditEntry> | AdminAuditEntry[]>('admin/audit', { params }),
  blockUser: (userId: string) => api.post<AdminUser>(`admin/users/${userId}/lock`),
  unblockUser: (userId: string) => api.post<AdminUser>(`admin/users/${userId}/unlock`),
}

export function pageItems<T>(response: AdminPage<T> | T[]) {
  return Array.isArray(response) ? response : response.content
}
