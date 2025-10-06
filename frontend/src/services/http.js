import axios from 'axios'
import { getToken } from './token'

export const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8080/api'

const http = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = { ...config.headers, Authorization: `Bearer ${token}` }
  }
  return config
})

export default http


