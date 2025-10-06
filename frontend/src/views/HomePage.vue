<template>
  <div class="home-page">
    <nav class="navbar">
      <h1 @click="goToDashboard" class="clickable-title">Raul Awards</h1>
      <div class="nav-actions"> 
        <button class="icon-btn" @click="openCalendar" title="Open calendar">📅</button>
        <router-link to="/winners" class="nav-link">View All Winners</router-link>
        <button @click="logout" class="logout-btn">Logout</button>
      </div>
    </nav>

    <div class="container">
      <h2 v-if="!dateParam">Current Week Winners</h2>
      <h2 v-else>Winners for {{ formatDate(dateParam) }}</h2>

      <div v-if="loading" class="loading">Loading...</div>
      
      <div v-else-if="isSundayError" class="sunday-error">
        <h3>Not a sunday, go away</h3>
        <button @click="goToCurrentWeek" class="btn-primary">Go to Current Week</button>
      </div>
      
      <div v-else-if="error" class="error">
        {{ error }}
      </div>

      <div v-else class="cards-container">
        <WinnerCard 
          title="Group Winner" 
          :winner="overallWinner"
          type="OVERALL"
          @add="openAddModal"
          @update="openUpdateModal"
          @open-image="openImageModal"
        />
        <WinnerCard 
          title="Raul's Winner" 
          :winner="raulWinner"
          type="RAUL"
          @add="openAddModal"
          @update="openUpdateModal"
          @open-image="openImageModal"
        />
      </div>
    </div>

    <CalendarModal :show="showCalendar" :archive="archive" @close="showCalendar=false" @select="onSelectDate" />
    <WinnerModal 
      :show="showWinnerModal" 
      :winner="selectedWinner"
      :sunday-date="currentSundayDate"
      :type="selectedType"
      @close="closeWinnerModal"
      @success="onWinnerSaved"
    />
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
import CalendarModal from '../components/CalendarModal.vue'
import WinnerCard from '../components/WinnerCard.vue'
import WinnerModal from '../components/WinnerModal.vue'
import ImageModal from '../components/ImageModal.vue'

export default {
  name: 'HomePage',
  components: { CalendarModal, WinnerCard, WinnerModal, ImageModal },
  data() {
    return {
      winners: [],
      loading: true,
      error: null,
      dateParam: null,
      showCalendar: false,
      archive: [],
      showWinnerModal: false,
      selectedWinner: null,
      selectedType: 'OVERALL',
      showImageModal: false,
      selectedImageUrl: '',
      selectedImageTitle: '',
      selectedImageId: null,
      isSundayError: false
    }
  },
  mounted() {
    this.loadWinners()
  },
  computed: {
    overallWinner() {
      return this.winners.find(w => w.type === 'OVERALL') || null
    },
    raulWinner() {
      return this.winners.find(w => w.type === 'RAUL') || null
    },
    currentSundayDate() {
      return this.dateParam || this.getCurrentSunday()
    }
  },
  watch: {
    '$route.query.date'() {
      this.loadWinners()
    },
    '$route.params.date'() {
      this.loadWinners()
    }
  },
  methods: {
    async loadWinners() {
      this.loading = true
      this.error = null
      this.isSundayError = false
      
      try {
        let response
        
        // Check for date param or query parameter
        const date = this.$route.params.date || this.$route.query.date
        this.dateParam = date
        
        if (date) {
          // Validate that the date is a Sunday
          if (!this.isSunday(date)) {
            this.isSundayError = true
            this.loading = false
            return
          }
          
          // Load winners for specific date
          response = await api.getWinnersByDate(date)
        } else {
          // Load current week winners
          response = await api.getCurrentWinners()
        }
        
        this.winners = response.data
      } catch (err) {
        console.error('Error loading winners:', err)
        this.error = 'Failed to load winners. Please try again.'
      } finally {
        this.loading = false
      }
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

    async openCalendar() {
      try {
        // Build a sensible default range: last 2 years to next 1 year
        const now = new Date()
        const start = new Date(now)
        start.setFullYear(start.getFullYear() - 2)
        const end = new Date(now)
        end.setFullYear(end.getFullYear() + 1)
        const startIso = start.toISOString().slice(0,10)
        const endIso = end.toISOString().slice(0,10)
        const res = await api.getArchive(startIso, endIso)
        this.archive = res.data
        this.showCalendar = true
      } catch (e) {
        console.error('Failed to load archive', e)
      }
    },

    onSelectDate(iso) {
      this.showCalendar = false
      this.$router.push({ name: 'DashboardByDate', params: { date: iso } })
    },

    getCurrentSunday() {
      const today = new Date()
      const day = today.getDay()
      const diff = today.getDate() - day
      const sunday = new Date(today.setDate(diff))
      return sunday.toISOString().slice(0, 10)
    },

    openAddModal(type) {
      this.selectedWinner = null
      this.selectedType = type
      this.showWinnerModal = true
    },

    openUpdateModal(winner) {
      this.selectedWinner = winner
      this.selectedType = winner.type
      this.showWinnerModal = true
    },

    closeWinnerModal() {
      this.showWinnerModal = false
      this.selectedWinner = null
      this.selectedType = 'OVERALL'
    },

    onWinnerSaved() {
      // Reload the page to ensure fresh data and proper URL handling
      window.location.reload()
    },

    logout() {
      authService.logout()
      this.$router.push('/login')
    },

    async openImageModal(image) {
      this.selectedImageUrl = await api.getImageUrl(image.id)
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


    isSunday(dateString) {
      const date = new Date(dateString)
      return date.getDay() === 0 // 0 = Sunday
    },

    goToCurrentWeek() {
      this.$router.push('/dashboard')
    },

    goToDashboard() {
      this.$router.push('/dashboard')
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

.clickable-title {
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.clickable-title:hover {
  opacity: 0.8;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  background: #34495e;
  transition: background 0.3s;
}

.nav-actions { 
  display: flex; 
  gap: 0.5rem; 
  align-items: center; 
}

.icon-btn { 
  background: #34495e; 
  color: white; 
  border: 0; 
  border-radius: 6px; 
  padding: 0.4rem 0.6rem; 
  cursor: pointer; 
}

.icon-btn:hover { 
  background: #4a5f7f; 
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 80px); /* Subtract navbar height */
}

.container h2 {
  text-align: center;
  margin: 0 0 2rem 0;
  font-size: 2rem;
  font-weight: 600;
  color: #2c3e50;
}

.loading, .error {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

.error {
  color: #e74c3c;
}

.sunday-error {
  text-align: center;
  padding: 3rem 2rem;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  margin: 0 auto;
  max-width: 600px;
  width: 100%;
}

.sunday-error h3 {
  color: #856404;
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
}

.sunday-error p {
  color: #856404;
  margin: 0 0 2rem 0;
  font-size: 1.1rem;
}

.btn-primary {
  background: #007bff;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #0056b3;
}

.cards-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin: 0 auto;
  max-width: 1000px;
  width: 100%;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .container {
    padding: 1rem;
    min-height: calc(100vh - 70px);
  }
  
  .container h2 {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
  }
  
  .cards-container {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .sunday-error {
    padding: 2rem 1rem;
    margin: 0 1rem;
  }
}
</style>