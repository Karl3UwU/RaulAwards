import http, { API_BASE_URL } from './http'

export default {
  // Get current week winners (latest Sunday period)
  getCurrentWinners() {
    return http.get('/weekly-winners/current')
  },
  
  // Get latest winners (current week) - alias for backward compatibility
  getLatestWinners() {
    return this.getCurrentWinners()
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

  // Get archive between two dates (inclusive Sundays)
  getArchive(startIso, endIso) {
    return http.get('/weekly-winners/archive', { params: { start: startIso, end: endIso } })
  },

  // Create a new winner (admin only)
  createWinner(formData) {
    return http.post('/weekly-winners/create', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // Update an existing winner (admin only)
  updateWinner(formData) {
    return http.put('/weekly-winners/update', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // Delete a winner (admin only)
  deleteWinner(sundayDate, type) {
    return http.delete('/weekly-winners/delete', {
      params: { sundayDate, type }
    })
  },

  // Get image URL for a winner (returns a blob URL for authenticated access)
  async getImageUrl(imageId) {
    try {
      const response = await http.get(`/images/${imageId}`, {
        responseType: 'blob'
      })
      return URL.createObjectURL(response.data)
    } catch (error) {
      console.error('Error loading image:', error)
      return null
    }
  },

  // Download image
  downloadImage(imageId) {
    return http.get(`/images/${imageId}/download`, {
      responseType: 'blob'
    })
  },

  // Update winner title only (no image change)
  updateWinnerTitle(sundayDate, type, title) {
    return http.put('/weekly-winners/update-title', null, {
      params: { sundayDate, type, title }
    })
  }
}