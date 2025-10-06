const TOKEN_KEY = 'raulawards_token'
const ROLE_KEY = 'raulawards_role'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRole() {
  return localStorage.getItem(ROLE_KEY)
}

export function setRole(role) {
  if (role) localStorage.setItem(ROLE_KEY, role)
}

export function clearRole() {
  localStorage.removeItem(ROLE_KEY)
}


