<template>
  <div class="winner-card">
    <div class="card-header">
      <h3>{{ title }}</h3>
    </div>
    <div class="card-content">
      <div v-if="winner" class="winner-content">
        <div class="image-container" @click="$emit('open-image', winner.image)">
          <img 
            v-if="!imageError"
            :src="getImageUrl(winner.image.id)" 
            :alt="winner.image.title || 'Winner image'"
            @error="handleImageError"
            class="winner-image"
          />
          <div v-else class="image-error">
            <div class="error-icon">⚠️</div>
            <p>Failed to retrieve the image</p>
          </div>
          <div v-if="!imageError" class="image-overlay">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
              <circle cx="12" cy="12" r="3"></circle>
            </svg>
          </div>
        </div>
        <div class="winner-info">
          <p class="winner-title">{{ winner.image.title || 'Untitled' }}</p>
          <p class="winner-date">{{ formatDate(winner.sundayDate) }}</p>
        </div>
      </div>
      <div v-else class="no-winner">
        <div class="no-winner-icon">📷</div>
        <p>No Image</p>
      </div>
      
      <!-- Admin buttons -->
      <div v-if="isAdmin" class="admin-actions">
        <button 
          v-if="!winner" 
          @click="$emit('add', type)" 
          class="admin-btn add-btn"
        >
          Add Winner
        </button>
        <button 
          v-else 
          @click="$emit('update', winner)" 
          class="admin-btn update-btn"
        >
          Update Winner
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api'
import { getToken, getRole } from '../services/token'

export default {
  name: 'WinnerCard',
  props: {
    title: {
      type: String,
      required: true
    },
    winner: {
      type: Object,
      default: null
    },
    type: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      imageError: false
    }
  },
  computed: {
    isAdmin() {
      return getRole() === 'ADMIN'
    }
  },
  methods: {
    getImageUrl(imageId) {
      const token = getToken()
      const base = api.getImageUrl(imageId)
      return token ? `${base}?token=${token}` : base
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
      this.imageError = true
    }
  }
}
</script>

<style scoped>
.winner-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.winner-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.card-header {
  background: #2c3e50;
  color: white;
  padding: 1rem 1.5rem;
  text-align: center;
}

.card-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.winner-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.image-container {
  position: relative;
  cursor: pointer;
  overflow: hidden;
  border-bottom: 1px solid #eee;
}

.image-container:hover .image-overlay {
  opacity: 1;
}

.winner-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-container:hover .winner-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
}

.image-overlay svg {
  width: 48px;
  height: 48px;
}

.image-error {
  width: 100%;
  height: 250px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  color: #6c757d;
  border: 2px dashed #dee2e6;
}

.error-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.image-error p {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 500;
  text-align: center;
}

.winner-info {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.winner-title {
  font-weight: bold;
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: #2c3e50;
  text-align: center;
}

.winner-date {
  color: #7f8c8d;
  font-size: 0.9rem;
  text-align: center;
  margin: 0;
}

.no-winner {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  color: #6c757d;
  padding: 2rem;
  text-align: center;
}

.no-winner-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.no-winner p {
  font-size: 1.1rem;
  font-weight: 500;
  margin: 0;
}

.admin-actions {
  padding: 1rem;
  border-top: 1px solid #eee;
  background: #f8f9fa;
}

.admin-btn {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.add-btn {
  background: #28a745;
  color: white;
}

.add-btn:hover {
  background: #218838;
}

.update-btn {
  background: #ffc107;
  color: #212529;
}

.update-btn:hover {
  background: #e0a800;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .winner-card {
    min-height: 300px;
  }
  
  .image-container .winner-image {
    height: 200px;
  }
  
  .image-error {
    height: 200px;
  }
  
  .image-overlay svg {
    width: 36px;
    height: 36px;
  }
  
  .card-header {
    padding: 0.75rem 1rem;
  }
  
  .card-header h3 {
    font-size: 1rem;
  }
  
  .winner-info {
    padding: 1rem;
  }
  
  .no-winner {
    padding: 1.5rem;
  }
  
  .no-winner-icon {
    font-size: 2.5rem;
  }
}
</style>
