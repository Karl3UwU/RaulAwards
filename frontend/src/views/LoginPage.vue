<template>
  <div class="login-page">
    <div class="card">
      <h1>Raul Awards</h1>
      <p class="subtitle">Please sign in</p>
      <div v-if="error" class="error">{{ error }}</div>
      <form @submit.prevent="onSubmit">
        <label>
          Username
          <input v-model="username" type="text" autocomplete="username" />
        </label>
        <label>
          Password
          <input v-model="password" type="password" autocomplete="current-password" />
        </label>
        <button :disabled="loading" type="submit">
          {{ loading ? 'Signing in...' : 'Sign in' }}
        </button>
      </form>
    </div>
  </div>
  
</template>

<script>
import auth from '../services/auth'

export default {
  name: 'LoginPage',
  data() {
    return {
      username: '',
      password: '',
      loading: false,
      error: null
    }
  },
  async mounted() {
    // If already logged in, go to dashboard
    const valid = await auth.validateToken()
    if (valid) {
      this.$router.replace('/dashboard')
    }
  },
  methods: {
    async onSubmit() {
      this.loading = true
      this.error = null
      try {
        const ok = await auth.login(this.username, this.password)
        if (ok) {
          this.$router.replace('/dashboard')
        } else {
          this.error = 'Invalid username or password'
        }
      } catch (e) {
        this.error = 'Login failed. Please try again.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
.card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}
h1 { margin: 0 0 0.25rem 0; }
.subtitle { margin: 0 0 1.5rem 0; color: #666; }
form { display: flex; flex-direction: column; gap: 1rem; }
label { display: flex; flex-direction: column; gap: 0.5rem; font-weight: 600; }
input { padding: 0.6rem 0.75rem; border: 1px solid #ddd; border-radius: 6px; }
button { padding: 0.75rem; background: #2c3e50; color: #fff; border: 0; border-radius: 6px; cursor: pointer; }
button[disabled] { opacity: 0.7; cursor: default; }
.error { color: #e74c3c; margin-bottom: 0.5rem; }
</style>


