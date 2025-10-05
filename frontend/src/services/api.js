import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export default {
  // Get latest winners (current week)
  getLatestWinners() {
    return api.get('/weekly-winners/latest')
  },

  // Get winners for a specific date
  getWinnersByDate(sundayDate) {
    return api.get('/weekly-winners/by-date', {
      params: { sundayDate }
    })
  },

  // Get all winners
  getAllWinners() {
    return api.get('/weekly-winners/all')
  },

  // Get image URL for a winner
  getImageUrl(imageId) {
    return `${API_BASE_URL}/images/${imageId}`
  }
}