<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import api from './services/api'
import Login from './components/Login.vue'
import Navbar from './components/Navbar.vue'
import Dashboard from './components/Dashboard.vue'
import VideoPlayer from './components/VideoPlayer.vue'
import ChangePasswordModal from './components/ChangePasswordModal.vue'
import UserManagementModal from './components/UserManagementModal.vue'

const isAuthenticated = ref(!!localStorage.getItem('jwt_token'))
const currentUser = ref({ username: '', role: '' })
const currentView = ref('dashboard')

const showPasswordModal = ref(false)
const showUserModal = ref(false)

const fetchCurrentUser = async () => {
  try {
    const response = await api.get('/auth/me')
    currentUser.value = response.data
  } catch (error) { console.error("Failed to fetch user details") }
}

const handleAuthExpiration = () => { isAuthenticated.value = false }

onMounted(() => {
  window.addEventListener('auth-expired', handleAuthExpiration)
  if (isAuthenticated.value) fetchCurrentUser()
})

onUnmounted(() => { window.removeEventListener('auth-expired', handleAuthExpiration) })

const onLoginSuccess = () => {
  isAuthenticated.value = true
  fetchCurrentUser()
}

const handleLogout = () => {
  localStorage.removeItem('jwt_token')
  isAuthenticated.value = false
  currentUser.value = { username: '', role: '' }
}
</script>

<template>
  <div class="container">
    <Login v-if="!isAuthenticated" @login-success="onLoginSuccess" />

    <div v-else>
      <Navbar
          :currentUser="currentUser"
          @logout="handleLogout"
          @open-password-modal="showPasswordModal = true"
          @open-user-modal="showUserModal = true"
          @navigate="(view) => currentView = view"
      />

      <Dashboard v-if="currentView === 'dashboard'" />
      <VideoPlayer v-if="currentView === 'recordings'" />

      <ChangePasswordModal v-if="showPasswordModal" @close="showPasswordModal = false" />
      <UserManagementModal v-if="showUserModal" @close="showUserModal = false" :currentUser="currentUser" />
    </div>
  </div>
</template>

<style>
body { background-color: #1e1e1e; margin: 0; }
.container { max-width: 900px; margin: 0 auto; padding: 20px; font-family: sans-serif; }
</style>