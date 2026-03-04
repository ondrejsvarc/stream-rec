<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const schedules = ref([])
const newSchedule = ref({ streamUrl: '', startTime: '', endTime: '' })
const loading = ref(false)
const errorMessage = ref('')

const fetchSchedules = async () => {
  try {
    const response = await api.get('/schedules')
    schedules.value = response.data
  } catch (error) {
    errorMessage.value = "Failed to load schedules."
  }
}

const submitSchedule = async () => {
  loading.value = true
  try {
    await api.post('/schedules', newSchedule.value)
    newSchedule.value = { streamUrl: '', startTime: '', endTime: '' }
    await fetchSchedules()
  } catch (error) {
    errorMessage.value = "Failed to create schedule."
  } finally {
    loading.value = false
  }
}

onMounted(() => { fetchSchedules() })
</script>

<template>
  <div>
    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <div class="card">
      <h2>Schedule a Recording</h2>
      <form @submit.prevent="submitSchedule" class="schedule-form">
        <input type="url" v-model="newSchedule.streamUrl" required placeholder="Stream URL (https://...)" />
        <div class="form-group-row">
          <input type="datetime-local" v-model="newSchedule.startTime" required />
          <input type="datetime-local" v-model="newSchedule.endTime" required />
        </div>
        <button type="submit" :disabled="loading">{{ loading ? 'Scheduling...' : 'Add Schedule' }}</button>
      </form>
    </div>

    <div class="card">
      <h2>Your Schedules</h2>
      <table>
        <thead><tr><th>ID</th><th>URL</th><th>Start Time</th><th>End Time</th><th>Status</th></tr></thead>
        <tbody>
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
</template>

<style scoped>
.card { background: #6f6f6f; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.schedule-form { display: flex; flex-direction: column; gap: 15px; }
.form-group-row { display: flex; gap: 15px; }
input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; flex: 1; }
button { padding: 10px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
table { width: 100%; border-collapse: collapse; margin-top: 10px; }
th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
.error-banner { background: #ffdddd; color: #d8000c; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
.scheduled { background: #fff3cd; color: #856404; }
.recording { background: #cce5ff; color: #004085; }
.completed { background: #d4edda; color: #155724; }
.failed { background: #f8d7da; color: #721c24; }
</style>