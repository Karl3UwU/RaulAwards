<template>
  <div class="modal-overlay" v-if="show" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="image-container">
        <div class="image-wrapper">
          <img :src="imageUrl" :alt="title" class="fullscreen-image" />
        </div>
      </div>
      <div class="modal-actions">
        <button @click="downloadImage" class="download-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7,10 12,15 17,10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          Download Image
        </button>
        <button @click="$emit('close')" class="close-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
          Close
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '../services/api'
import { getToken } from '../services/token'

export default {
  name: 'ImageModal',
  props: {
    show: { type: Boolean, default: false },
    imageUrl: { type: String, required: true },
    title: { type: String, default: 'Image' },
    imageId: { type: Number, required: true }
  },
  methods: {
    async downloadImage() {
      try {
        const response = await api.downloadImage(this.imageId)
        
        // Create blob from response
        const blob = new Blob([response.data])
        const url = window.URL.createObjectURL(blob)
        
        // Create temporary link and trigger download
        const link = document.createElement('a')
        link.href = url
        
        // Extract filename from Content-Disposition header or use title
        const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition']
        let filename = this.title || 'image'
        
        console.log('DEBUG: Content-Disposition header:', contentDisposition)
        console.log('DEBUG: Response headers:', response.headers)
        
        if (contentDisposition) {
          // Try different patterns for filename extraction
          let filenameMatch = contentDisposition.match(/filename="(.+)"/)
          if (!filenameMatch) {
            filenameMatch = contentDisposition.match(/filename=([^;]+)/)
          }
          if (!filenameMatch) {
            filenameMatch = contentDisposition.match(/filename=(.+)/)
          }
          
          if (filenameMatch) {
            filename = filenameMatch[1].replace(/"/g, '') // Remove quotes if present
            console.log('DEBUG: Extracted filename from header:', filename)
          } else {
            console.log('DEBUG: Could not extract filename from header, pattern:', contentDisposition)
          }
        } else {
          console.log('DEBUG: No Content-Disposition header found')
          // Fallback: try to determine extension from Content-Type
          const contentType = response.headers['content-type'] || response.headers['Content-Type']
          if (contentType) {
            let extension = 'jpg' // default
            if (contentType.includes('png')) extension = 'png'
            else if (contentType.includes('gif')) extension = 'gif'
            else if (contentType.includes('webp')) extension = 'webp'
            else if (contentType.includes('svg')) extension = 'svg'
            else if (contentType.includes('heic')) extension = 'heic'
            else if (contentType.includes('avif')) extension = 'avif'
            else if (contentType.includes('tiff')) extension = 'tiff'
            else if (contentType.includes('bmp')) extension = 'bmp'
            
            filename = filename + '.' + extension
            console.log('DEBUG: Generated filename from Content-Type:', filename)
          }
        }
        
        console.log('DEBUG: Final filename:', filename)
        
        link.download = filename
        document.body.appendChild(link)
        link.click()
        
        // Cleanup
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('Error downloading image:', error)
        alert('Failed to download image. Please try again.')
      }
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  padding: 15%;
  box-sizing: border-box;
}

.modal-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  width: auto;
  height: 100%;
}

.image-container {
  position: relative;
  width: 100%;
  height: 100%;
  max-width: 80vw;
  max-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fullscreen-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  cursor: default;
  display: block;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
  align-items: center;
}

.download-btn,
.close-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.download-btn {
  background: #3498db;
  color: white;
}

.download-btn:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.close-btn {
  background: #e74c3c;
  color: white;
}

.close-btn:hover {
  background: #c0392b;
  transform: translateY(-2px);
}

.download-btn svg,
.close-btn svg {
  flex-shrink: 0;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 5%;
  }
  
  .modal-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .download-btn,
  .close-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
