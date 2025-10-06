import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../views/HomePage.vue'
import AllWinnersPage from '../views/AllWinnersPage.vue'
import LoginPage from '../views/LoginPage.vue'
import auth from '../services/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'LoginPage', component: LoginPage },
  { path: '/dashboard', name: 'Dashboard', component: HomePage },
  { path: '/winners', name: 'AllWinnersPage', component: AllWinnersPage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  if (to.path === '/login') {
    // If already authenticated, skip login
    const valid = await auth.validateToken()
    if (valid) return next('/dashboard')
    return next()
  }

  // Protect all other routes
  const valid = await auth.validateToken()
  if (!valid) return next('/login')
  return next()
})

export default router