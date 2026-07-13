import React from 'react'
import { useAuth } from '../AuthContext'

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth()

  return (
    <div className="dashboard-container">
      <div className="header">
        <h1 className="title" style={{ margin: 0 }}>
          My Galleries
        </h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <span style={{ color: 'var(--text-muted)' }}>{user?.email}</span>
          <button
            className="btn btn-outline"
            onClick={logout}
            style={{ width: 'auto', padding: '0.5rem 1rem' }}
          >
            Log out
          </button>
        </div>
      </div>

      <div
        className="glass-card"
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '300px',
          flexDirection: 'column',
        }}
      >
        <h2 style={{ color: 'var(--text-muted)', marginBottom: '1rem' }}>No events yet</h2>
        <button className="btn" style={{ width: 'auto' }}>
          + Create Event
        </button>
      </div>
    </div>
  )
}

export default Dashboard
