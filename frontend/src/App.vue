<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const schedules = ref([])
const newSchedule = ref({
  streamUrl: '',
  startTime: '',
  endTime: ''
})
const loading = ref(false)
const errorMessage = ref('')

const fetchSchedules = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/schedules')
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
    await axios.post('http://localhost:8080/api/schedules', newSchedule.value)
    newSchedule.value = { streamUrl: '', startTime: '', endTime: '' }
    await fetchSchedules()
  } catch (error) {
    console.error("Error creating schedule:", error)
    errorMessage.value = "Failed to create schedule."
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSchedules()
})
</script>

<template>
  <div class="container">
    <h1>Stream Recorder Dashboard</h1>

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <div class="card">
      <h2>Schedule a Recording</h2>
      <form @submit.prevent="submitSchedule" class="schedule-form">
        <div class="form-group">
          <label>Stream URL (YouTube/Twitch):</label>
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
          <td>
            <a :href="job.streamUrl" target="_blank" title="Open Stream">Link</a>
          </td>
          <td>{{ new Date(job.startTime).toLocaleString() }}</td>
          <td>{{ new Date(job.endTime).toLocaleString() }}</td>
          <td>
              <span :class="['status-badge', job.status.toLowerCase()]">
                {{ job.status }}
              </span>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
/* Basic styling */
.container { max-width: 900px; margin: 0 auto; padding: 20px; font-family: sans-serif; }
.card { background: rgba(195, 194, 194, 0.08); padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.schedule-form { display: flex; flex-direction: column; gap: 15px; }
.form-group-row { display: flex; gap: 15px; }
.form-group { display: flex; flex-direction: column; flex: 1; }
input { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
button { padding: 10px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
button:disabled { background: #aaa; }
table { width: 100%; border-collapse: collapse; margin-top: 10px; }
th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
.error-banner { background: #ffdddd; color: #d8000c; padding: 10px; border-radius: 4px; margin-bottom: 15px; }

/* Status Badge Colors */
.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
.scheduled { background: #fff3cd; color: #856404; }
.recording { background: #cce5ff; color: #004085; animation: pulse 2s infinite; }
.completed { background: #d4edda; color: #155724; }
.failed { background: #f8d7da; color: #721c24; }

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}
</style>