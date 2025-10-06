import http, { API_BASE_URL } from './http'

export default {
  // Get latest winners (current week)
  getLatestWinners() {
    return http.get('/weekly-winners/current')
  },

  // Get winners for a specific date
  getWinnersByDate(sundayDate) {
    return http.get('/weekly-winners/by-date', {
      params: { sundayDate }
    })
  },

  // Get all winners
  getAllWinners() {
    return http.get('/weekly-winners/all')
  },

  // Get image URL for a winner
  getImageUrl(imageId) {
    return `${API_BASE_URL}/images/${imageId}`
  }
}