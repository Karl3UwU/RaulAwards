<template>
  <div v-if="show" class="overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h3>{{ isUpdate ? 'Update Winner' : 'Add Winner' }}</h3>
        <button @click="$emit('close')" class="close-btn">&times;</button>
      </div>
      
      <form @submit.prevent="onSubmit" class="form">
        <div class="form-group">
          <label for="sundayDate">Sunday Date</label>
          <input 
            id="sundayDate"
            v-model="formData.sundayDate" 
            type="date" 
            required 
            :disabled="isUpdate"
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label for="type">Winner Type</label>
          <select 
            id="type" 
            v-model="formData.type" 
            required 
            :disabled="isUpdate"
            class="form-input"
          >
            <option value="OVERALL">Group Winner</option>
            <option value="RAUL">Raul's Winner</option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="title">Image Title (optional)</label>
          <input 
            id="title"
            v-model="formData.title" 
            type="text" 
            placeholder="Enter image title..."
            class="form-input"
          />
        </div>
        
        <div class="form-group">
          <label for="image">Image File {{ isUpdate ? '(optional - leave empty to keep current image)' : '' }}</label>
          <input 
            id="image"
            ref="fileInput"
            type="file" 
            accept="image/*" 
            :required="!isUpdate"
            @change="onFileChange"
            class="form-input"
          />
          <div v-if="selectedFile" class="file-info">
            Selected: {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
          </div>
          <div v-if="isUpdate && !selectedFile" class="file-info">
            Current image will be kept
          </div>
        </div>
        
        <div v-if="previewUrl" class="preview">
          <img :src="previewUrl" alt="Preview" class="preview-image" />
        </div>
        
        <div class="form-actions">
          <button 
            v-if="isUpdate" 
            type="button" 
            @click="showDeleteConfirmation = true" 
            class="btn btn-danger"
            :disabled="loading"
          >
            Delete
          </button>
          <button type="button" @click="$emit('close')" class="btn btn-secondary">
            Cancel
          </button>
          <button type="submit" :disabled="loading" class="btn btn-primary">
            {{ loading ? 'Saving...' : (isUpdate ? 'Update Winner' : 'Add Winner') }}
          </button>
        </div>
      </form>
    </div>
    
    <!-- Delete Confirmation Modal -->
    <ConfirmationModal
      :show="showDeleteConfirmation"
      title="Delete Winner"
      message="Are you sure you want to delete this winner entry? This action cannot be undone."
      confirm-text="Delete"
      @close="showDeleteConfirmation = false"
      @confirm="onDeleteConfirm"
    />
  </div>
</template>

<script>
import api from '../services/api'
import ConfirmationModal from './ConfirmationModal.vue'

export default {
  name: 'WinnerModal',
  components: {
    ConfirmationModal
  },
  props: {
    show: { type: Boolean, default: false },
    winner: { type: Object, default: null },
    sundayDate: { type: String, default: '' },
    type: { type: String, default: 'OVERALL' }
  },
  data() {
    return {
      formData: {
        sundayDate: '',
        type: this.type,
        title: ''
      },
      selectedFile: null,
      previewUrl: null,
      loading: false,
      showDeleteConfirmation: false
    }
  },
  computed: {
    isUpdate() {
      return this.winner !== null
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.resetForm()
      }
    },
    type(newVal) {
      this.formData.type = newVal
    },
    winner: {
      handler(newVal) {
        if (newVal) {
          this.formData.sundayDate = newVal.sundayDate
          this.formData.type = newVal.type
          this.formData.title = newVal.image?.title || ''
        }
      },
      immediate: true
    },
    sundayDate: {
      handler(newVal) {
        if (newVal && !this.isUpdate) {
          this.formData.sundayDate = newVal
        }
      },
      immediate: true
    }
  },
  methods: {
    resetForm() {
      this.formData = {
        sundayDate: this.sundayDate || '',
        type: 'OVERALL',
        title: ''
      }
      this.selectedFile = null
      this.previewUrl = null
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = ''
      }
    },
    
    onFileChange(event) {
      const file = event.target.files[0]
      this.selectedFile = file
      
      if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
          this.previewUrl = e.target.result
        }
        reader.readAsDataURL(file)
      } else {
        this.previewUrl = null
      }
    },
    
    async onSubmit() {
      if (!this.isUpdate && !this.selectedFile) {
        alert('Please select an image file')
        return
      }
      
      this.loading = true
      
      try {
        if (this.isUpdate && !this.selectedFile) {
          // Update title only
          await api.updateWinnerTitle(this.formData.sundayDate, this.formData.type, this.formData.title)
        } else {
          // Create or update with image
          const formData = new FormData()
          formData.append('sundayDate', this.formData.sundayDate)
          formData.append('type', this.formData.type)
          formData.append('image', this.selectedFile)
          if (this.formData.title) {
            formData.append('title', this.formData.title)
          }
          
          if (this.isUpdate) {
            await api.updateWinner(formData)
          } else {
            await api.createWinner(formData)
          }
        }
        
        this.$emit('success')
        this.$emit('close')
      } catch (error) {
        console.error('Error saving winner:', error)
        alert('Failed to save winner. Please try again.')
      } finally {
        this.loading = false
      }
    },
    
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    async onDeleteConfirm() {
      this.loading = true
      this.showDeleteConfirmation = false
      
      try {
        await api.deleteWinner(this.winner.sundayDate, this.winner.type)
        this.$emit('success')
        this.$emit('close')
      } catch (error) {
        console.error('Error deleting winner:', error)
        alert('Failed to delete winner. Please try again.')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2c3e50;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #3498db;
}

.form-input:disabled {
  background: #f8f9fa;
  color: #6c757d;
}

.file-info {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #666;
}

.preview {
  margin: 1rem 0;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #5a6268;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #c82333;
}

@media (max-width: 768px) {
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
  }
}
</style>
