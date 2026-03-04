<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Login from './components/Login.vue'
import api from './services/api' // Use our new interceptor API!

const isAuthenticated = ref(!!localStorage.getItem('jwt_token'))

const schedules = ref([])
const newSchedule = ref({ streamUrl: '', startTime: '', endTime: '' })
const loading = ref(false)
const errorMessage = ref('')

const handleAuthExpiration = () => {
  isAuthenticated.value = false
}

onMounted(() => {
  window.addEventListener('auth-expired', handleAuthExpiration)
  if (isAuthenticated.value) {
    fetchSchedules()
  }
})

onUnmounted(() => {
  window.removeEventListener('auth-expired', handleAuthExpiration)
})

const onLoginSuccess = () => {
  isAuthenticated.value = true
  fetchSchedules()
}

const logout = () => {
  localStorage.removeItem('jwt_token')
  isAuthenticated.value = false
  schedules.value = []
}

const fetchSchedules = async () => {
  try {
    const response = await api.get('/schedules')
    schedules.value = response.data
  } catch (error) {
    console.error("Error fetching schedules:", error)
    errorMessage.value = "Failed to load schedules."
  }
}

const submitSchedule = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    await api.post('/schedules', newSchedule.value)
    newSchedule.value = { streamUrl: '', startTime: '', endTime: '' }
    await fetchSchedules()
  } catch (error) {
    console.error("Error creating schedule:", error)
    errorMessage.value = "Failed to create schedule."
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container">
    <Login v-if="!isAuthenticated" @login-success="onLoginSuccess" />

    <div v-else>
      <div class="header">
        <h1>Stream Recorder</h1>
        <button @click="logout" class="logout-btn">Logout</button>
      </div>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <div class="card">
        <h2>Schedule a Recording</h2>
        <form @submit.prevent="submitSchedule" class="schedule-form">
          <div class="form-group">
            <label>Stream URL:</label>
            <input type="url" v-model="newSchedule.streamUrl" required placeholder="https://..." />
          </div>
          <div class="form-group-row">
            <div class="form-group">
              <label>Start Time:</label>
              <input type="datetime-local" v-model="newSchedule.startTime" required />
            </div>
            <div class="form-group">
              <label>End Time:</label>
              <input type="datetime-local" v-model="newSchedule.endTime" required />
            </div>
          </div>
          <button type="submit" :disabled="loading">
            {{ loading ? 'Scheduling...' : 'Add Schedule' }}
          </button>
        </form>
      </div>

      <div class="card">
        <h2>Your Schedules</h2>
        <table>
          <thead>
          <tr>
            <th>ID</th>
            <th>URL</th>
            <th>Start Time</th>
            <th>End Time</th>
            <th>Status</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="schedules.length === 0">
            <td colspan="5" style="text-align: center;">No schedules found.</td>
          </tr>
          <tr v-for="job in schedules" :key="job.id">
            <td>{{ job.id.substring(0, 8) }}...</td>
            <td><a :href="job.streamUrl" target="_blank">Link</a></td>
            <td>{{ new Date(job.startTime).toLocaleString() }}</td>
            <td>{{ new Date(job.endTime).toLocaleString() }}</td>
            <td><span :class="['status-badge', job.status.toLowerCase()]">{{ job.status }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container { max-width: 900px; margin: 0 auto; padding: 20px; font-family: sans-serif; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.logout-btn { background: #dc3545; padding: 8px 16px; }
.card { background: #686868; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.schedule-form { display: flex; flex-direction: column; gap: 15px; }
.form-group-row { display: flex; gap: 15px; }
.form-group { display: flex; flex-direction: column; flex: 1; }
input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
button { padding: 10px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
button:disabled { background: #aaa; }
table { width: 100%; border-collapse: collapse; margin-top: 10px; }
th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
.error-banner { background: #ffdddd; color: #d8000c; padding: 10px; border-radius: 4px; margin-bottom: 15px; }

.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
.scheduled { background: #fff3cd; color: #856404; }
.recording { background: #cce5ff; color: #004085; animation: pulse 2s infinite; }
.completed { background: #d4edda; color: #155724; }
.failed { background: #f8d7da; color: #721c24; }

@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.6; } 100% { opacity: 1; } }
</style>