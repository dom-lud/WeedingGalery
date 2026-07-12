import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../AuthContext'

const Login: React.FC = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      const response = await api.post('auth/login', { email, password })
      login(response.data)
      navigate('/dashboard')
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const errorObj = err as any
      if (errorObj.response?.status === 401) {
        setError('Invalid email or password')
      } else {
        setError(errorObj.response?.data?.message || errorObj.response?.data || 'Login failed')
      }
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-form-wrapper glass-card">
        <h1 className="title">Welcome Back</h1>
        <p className="subtitle">Sign in to your gallery</p>
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
            />
          </div>
          {error && (
            <div className="error-message" style={{ marginBottom: '1rem' }}>
              {error}
            </div>
          )}
          <button type="submit" className="btn">
            Sign In
          </button>
        </form>
        <div className="auth-footer">
          Don't have an account?{' '}
          <Link to="/register" className="link">
            Sign up
          </Link>
        </div>
      </div>
    </div>
  )
}

export default Login
