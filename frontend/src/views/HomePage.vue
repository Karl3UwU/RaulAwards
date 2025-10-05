<template>
  <div class="home-page">
    <nav class="navbar">
      <h1>Raul Awards</h1>
      <router-link to="/winners" class="nav-link">View All Winners</router-link>
    </nav>

    <div class="container">
      <h2 v-if="!dateParam">Current Week Winners</h2>
      <h2 v-else>Winners for {{ formatDate(dateParam) }}</h2>

      <div v-if="loading" class="loading">Loading...</div>
      
      <div v-else-if="error" class="error">
        {{ error }}
      </div>

      <div v-else-if="winners.length > 0" class="winners-grid">
        <div v-for="winner in winners" :key="winner.id" class="winner-card">
          <div class="winner-type">{{ winner.type }}</div>
          <img 
            :src="getImageUrl(winner.image.id)" 
            :alt="winner.image.title || 'Winner image'"
            @error="handleImageError"
          />
          <div class="winner-info">
            <p class="winner-title">{{ winner.image.title || 'Untitled' }}</p>
            <p class="winner-date">{{ formatDate(winner.sundayDate) }}</p>
          </div>
        </div>
      </div>

      <div v-else class="no-winners">
        No winners found for this date.
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api'

export default {
  name: 'HomePage',
  data() {
    return {
      winners: [],
      loading: true,
      error: null,
      dateParam: null
    }
  },
  mounted() {
    this.loadWinners()
  },
  watch: {
    '$route.query.date'() {
      this.loadWinners()
    }
  },
  methods: {
    async loadWinners() {
      this.loading = true
      this.error = null
      
      try {
        let response
        
        // Check for date query parameter
        const date = this.$route.query.date
        this.dateParam = date
        
        if (date) {
          // Load winners for specific date
          response = await api.getWinnersByDate(date)
        } else {
          // Load latest winners
          response = await api.getLatestWinners()
        }
        
        this.winners = response.data
      } catch (err) {
        console.error('Error loading winners:', err)
        this.error = 'Failed to load winners. Please try again.'
      } finally {
        this.loading = false
      }
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
      event.target.src = '/placeholder.png' // Add a placeholder image
    }
  }
}
</script>

<style scoped>
.home-page {
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

.winners-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
  margin-top: 2rem;
}

.winner-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.winner-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
}

.winner-type {
  background: #3498db;
  color: white;
  padding: 0.5rem;
  text-align: center;
  font-weight: bold;
  text-transform: uppercase;
}

.winner-card img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

.winner-info {
  padding: 1rem;
}

.winner-title {
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.winner-date {
  color: #7f8c8d;
  font-size: 0.9rem;
}
</style>