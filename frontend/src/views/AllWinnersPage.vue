<template>
  <div class="all-winners-page">
    <nav class="navbar">
      <h1>All Winners</h1>
      <div class="nav-actions">
        <router-link to="/" class="nav-link">Back to Home</router-link>
        <button @click="logout" class="logout-btn">Logout</button>
      </div>
    </nav>

    <div class="container">
      <h2>Past Weekly Winners</h2>

      <div v-if="loading" class="loading">Loading...</div>
      
      <div v-else-if="error" class="error">
        {{ error }}
      </div>

      <div v-else-if="groupedWinners.length > 0" class="winners-list">
        <div 
          v-for="group in groupedWinners" 
          :key="group.sundayDate"
          class="week-group"
        >
          <div 
            class="week-header"
            @click="goToWeek(group.sundayDate)"
          >
            <h3>{{ formatDate(group.sundayDate) }}</h3>
            <span class="view-link">View →</span>
          </div>
          
          <div class="week-winners">
            <div 
              v-for="winner in group.winners" 
              :key="winner.id"
              class="winner-item"
            >
              <span class="winner-type-badge">{{ winner.type }}</span>
              <img 
                :src="getImageUrl(winner.image.id)" 
                :alt="winner.image.title || 'Winner image'"
                @error="handleImageError"
                @click="openImageModal(winner.image)"
                class="clickable-image"
              />
              <span class="winner-title">{{ winner.image.title || 'Untitled' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="no-winners">
        No winners found.
      </div>
    </div>

    <ImageModal 
      :show="showImageModal" 
      :imageUrl="selectedImageUrl" 
      :title="selectedImageTitle"
      :imageId="selectedImageId"
      @close="closeImageModal"
    />
  </div>
</template>

<script>
import api from '../services/api'
import authService from '../services/auth'
import ImageModal from '../components/ImageModal.vue'

export default {
  name: 'AllWinnersPage',
  components: { ImageModal },
  data() {
    return {
      winners: [],
      groupedWinners: [],
      loading: true,
      error: null,
      showImageModal: false,
      selectedImageUrl: '',
      selectedImageTitle: '',
      selectedImageId: null
    }
  },
  mounted() {
    this.loadAllWinners()
  },
  methods: {
    async loadAllWinners() {
      this.loading = true
      this.error = null
      
      try {
        const response = await api.getAllWinners()
        this.winners = response.data
        
        // Group winners by sundayDate
        const grouped = {}
        response.data.forEach(winner => {
          if (!grouped[winner.sundayDate]) {
            grouped[winner.sundayDate] = []
          }
          grouped[winner.sundayDate].push(winner)
        })
        
        // Convert to array and sort by date (newest first)
        this.groupedWinners = Object.keys(grouped)
          .sort((a, b) => new Date(b) - new Date(a))
          .map(date => ({
            sundayDate: date,
            winners: grouped[date]
          }))
        
      } catch (err) {
        console.error('Error loading winners:', err)
        this.error = 'Failed to load winners. Please try again.'
      } finally {
        this.loading = false
      }
    },

    goToWeek(sundayDate) {
      this.$router.push({ path: '/', query: { date: sundayDate } })
    },

    getImageUrl(imageId) {
      return api.getImageUrl(imageId)
    },

    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', { 
        weekday: 'long',
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
      })
    },

    handleImageError(event) {
      event.target.src = '/placeholder.png'
    },

    logout() {
      authService.logout()
      this.$router.push('/login')
    },

    openImageModal(image) {
      this.selectedImageUrl = api.getImageUrl(image.id) + `?token=${this.getToken()}`
      this.selectedImageTitle = image.title || 'Winner Image'
      this.selectedImageId = image.id
      this.showImageModal = true
    },

    closeImageModal() {
      this.showImageModal = false
      this.selectedImageUrl = ''
      this.selectedImageTitle = ''
      this.selectedImageId = null
    },

    getToken() {
      const { getToken } = require('../services/token')
      return getToken()
    }
  }
}
</script>

<style scoped>
.all-winners-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.navbar {
  background: #2c3e50;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar h1 {
  margin: 0;
}

.nav-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  background: #34495e;
  transition: background 0.3s;
}

.nav-link:hover {
  background: #4a5f7f;
}

.logout-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: #c0392b;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.loading, .error, .no-winners {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

.error {
  color: #e74c3c;
}

.week-group {
  background: white;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.week-header {
  background: #34495e;
  color: white;
  padding: 1rem 1.5rem;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.3s;
}

.week-header:hover {
  background: #4a5f7f;
}

.week-header h3 {
  margin: 0;
}

.view-link {
  font-size: 0.9rem;
  opacity: 0.8;
}

.week-winners {
  display: flex;
  gap: 1rem;
  padding: 1rem;
}

.winner-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.winner-type-badge {
  background: #3498db;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: bold;
  text-transform: uppercase;
}

.winner-item img {
  width: 100%;
  max-width: 200px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.clickable-image {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.clickable-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.winner-title {
  text-align: center;
  font-size: 0.9rem;
  color: #555;
}
</style>


