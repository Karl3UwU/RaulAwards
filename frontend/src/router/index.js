import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../views/HomePage.vue'
import AllWinners from '../views/AllWinners.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  },
  {
    path: '/winners',
    name: 'AllWinners',
    component: AllWinners
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router