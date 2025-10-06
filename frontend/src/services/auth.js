import http from './http'
import { getToken, setToken, clearToken, setRole, clearRole } from './token'

async function login(username, password) {
  const res = await http.post('/auth/login', { username, password })
  if (res.data && res.data.success && res.data.token) {
    setToken(res.data.token)
    setRole(res.data.role)
    return true
  }
  return false
}

async function validateToken() {
  const token = getToken()
  if (!token) return false
  try {
    const res = await http.get('/auth/validate', {
      headers: { Authorization: `Bearer ${token}` }
    })
    return !!(res.data && res.data.success)
  } catch (e) {
    return false
  }
}

function logout() {
  clearToken()
  clearRole()
}

export default {
  login,
  validateToken,
  logout,
  getToken,
  setToken,
  clearToken
}


