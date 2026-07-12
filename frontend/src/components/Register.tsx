import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../api'

const Register: React.FC = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await api.post('/auth/register', { email, password })
      setSuccess(true)
      setTimeout(() => navigate('/login'), 2000)
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const errorObj = err as any
      setError(errorObj.response?.data?.message || errorObj.response?.data || 'Registration failed')
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-form-wrapper glass-card">
        <h1 className="title">Create Account</h1>
        <p className="subtitle">Start curating your memories</p>

        {success ? (
          <div style={{ color: 'var(--success)', textAlign: 'center', margin: '2rem 0' }}>
            Registration successful! Redirecting to login...
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input
                type="email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={8}
              />
            </div>
            {error && (
              <div className="error-message" style={{ marginBottom: '1rem' }}>
                {error}
              </div>
            )}
            <button type="submit" className="btn">
              Sign Up
            </button>
          </form>
        )}
        <div className="auth-footer">
          Already have an account?{' '}
          <Link to="/login" className="link">
            Sign in
          </Link>
        </div>
      </div>
    </div>
  )
}

export default Register
