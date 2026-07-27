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

export interface AdminAlert {
  id: string
  notificationType: string
  severity: 'INFO' | 'WARNING' | 'CRITICAL'
  status: 'OPEN' | 'ACKNOWLEDGED'
  title: string
  message: string
  resourceType: string | null
  resourceId: string | null
  createdAt: string
  acknowledgedAt: string | null
  acknowledgedBy: string | null
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
  alerts: (params?: { page?: number; size?: number }) =>
    api.get<AdminPage<AdminAlert>>('admin/alerts', { params }),
  acknowledgeAlert: (alertId: string) =>
    api.post<AdminAlert>(`admin/alerts/${alertId}/acknowledge`),
}

export function pageItems<T>(response: AdminPage<T> | T[]) {
  return Array.isArray(response) ? response : response.content
}
