<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const props = defineProps({
  currentUser: Object
})

const schedules = ref([])
const jobs = ref([]) // NEW: Store historical jobs
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const editingId = ref(null)
const formData = ref({
  name: '', streamUrl: '', startTime: '', duration: null,
  repetition: 'NONE', customRepetitionDays: null, codec: 'NONE', maxTranscodeBitrate: null
})

const fetchData = async () => {
  try {
    const [schedRes, jobsRes] = await Promise.all([
      api.get('/schedules'),
      api.get('/jobs')
    ])
    schedules.value = schedRes.data
    // Sort jobs newest first
    jobs.value = jobsRes.data.sort((a, b) => new Date(b.actualStartTime) - new Date(a.actualStartTime))
  } catch (error) {
    errorMessage.value = "Failed to load dashboard data."
  }
}

const submitSchedule = async () => {
  loading.value = true
  errorMessage.value = ''; successMessage.value = ''

  try {
    const payload = { ...formData.value }
    if (!payload.startTime) payload.startTime = null
    if (!payload.duration) payload.duration = null
    if (payload.repetition !== 'CUSTOM') payload.customRepetitionDays = null
    if (payload.codec === 'NONE') payload.maxTranscodeBitrate = null

    if (editingId.value) {
      await api.put(`/schedules/${editingId.value}`, payload)
      successMessage.value = "Schedule updated successfully!"
    } else {
      await api.post('/schedules', payload)
      successMessage.value = "Schedule created successfully!"
    }

    cancelEdit()
    await fetchData()
    setTimeout(() => { successMessage.value = '' }, 3000)
  } catch (error) {
    errorMessage.value = error.response?.data || "Failed to save schedule."
  } finally {
    loading.value = false
  }
}

const editSchedule = (schedule) => {
  editingId.value = schedule.id
  const formattedStartTime = schedule.startTime ? schedule.startTime.slice(0, 16) : ''
  formData.value = { ...schedule, startTime: formattedStartTime }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const deleteSchedule = async (id) => {
  if (confirm("Are you sure you want to delete this schedule? Future recordings will be canceled.")) {
    try {
      await api.delete(`/schedules/${id}`)
      await fetchData()
    } catch (error) {
      errorMessage.value = error.response?.data || "Failed to delete schedule."
    }
  }
}

const cancelEdit = () => {
  editingId.value = null
  formData.value = { name: '', streamUrl: '', startTime: '', duration: null, repetition: 'NONE', customRepetitionDays: null, codec: 'NONE', maxTranscodeBitrate: null }
}

// Helper to check if user has permission to edit/delete a schedule
const canManage = (schedule) => {
  return props.currentUser?.role === 'ADMIN' || schedule.user?.username === props.currentUser?.username
}

onMounted(() => { fetchData() })
</script>

<template>
  <div>
    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>
    <div v-if="successMessage" class="success-banner">{{ successMessage }}</div>

    <div class="card form-card">
      <h2>{{ editingId ? '✏️ Edit Schedule' : '➕ Create New Schedule' }}</h2>
      <form @submit.prevent="submitSchedule" class="schedule-form">
        <div class="form-row">
          <div class="form-group flex-2"><label>Recording Name</label><input type="text" v-model="formData.name" required /></div>
          <div class="form-group flex-3"><label>Stream URL</label><input type="url" v-model="formData.streamUrl" required /></div>
        </div>
        <div class="form-row">
          <div class="form-group"><label>Start Time (Blank for NOW)</label><input type="datetime-local" v-model="formData.startTime" /></div>
          <div class="form-group"><label>Max Duration (Min)</label><input type="number" v-model="formData.duration" min="1" /></div>
          <div class="form-group">
            <label>Repetition</label>
            <select v-model="formData.repetition">
              <option value="NONE">None</option><option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option><option value="MONTHLY">Monthly</option><option value="CUSTOM">Custom Days</option>
            </select>
          </div>
          <div class="form-group" v-if="formData.repetition === 'CUSTOM'"><label>Every X Days</label><input type="number" v-model="formData.customRepetitionDays" required min="1" /></div>
        </div>
        <div class="form-row advanced-row">
          <div class="form-group"><label>Codec</label><select v-model="formData.codec"><option value="NONE">None</option><option value="AV1">AV1</option><option value="H264">H.264</option><option value="H265">H.265</option></select></div>
          <div class="form-group" v-if="formData.codec !== 'NONE'"><label>Max Bitrate (kbps)</label><input type="number" v-model="formData.maxTranscodeBitrate" min="500" /></div>
        </div>
        <div class="form-actions">
          <button type="button" v-if="editingId" @click="cancelEdit" class="btn-secondary">Cancel</button>
          <button type="submit" :disabled="loading" class="btn-primary">{{ loading ? 'Saving...' : (editingId ? 'Update' : 'Create') }}</button>
        </div>
      </form>
    </div>

    <div class="card">
      <h2>📅 Active Schedules</h2>
      <div class="table-responsive">
        <table>
          <thead><tr><th>Name</th><th>Owner</th><th>Next Run</th><th>Repeat</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>
          <tr v-if="schedules.length === 0"><td colspan="6" style="text-align: center;">No schedules found.</td></tr>
          <tr v-for="schedule in schedules" :key="schedule.id">
            <td><strong>{{ schedule.name }}</strong><br/><a :href="schedule.streamUrl" target="_blank" class="small-link">Link</a></td>
            <td>👤 {{ schedule.user?.username }}</td>
            <td>{{ schedule.startTime ? new Date(schedule.startTime).toLocaleString() : 'Immediate' }}</td>
            <td>{{ schedule.repetition }}</td>
            <td><span :class="['status-badge', schedule.status.toLowerCase()]">{{ schedule.status }}</span></td>
            <td class="action-buttons">
              <button v-if="canManage(schedule)" @click="editSchedule(schedule)" class="btn-sm btn-edit">Edit</button>
              <button v-if="canManage(schedule)" @click="deleteSchedule(schedule.id)" class="btn-sm btn-delete">Delete</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <h2>⏱️ Job Execution History</h2>
      <div class="table-responsive">
        <table>
          <thead><tr><th>Schedule Name</th><th>Owner</th><th>Actual Start Time</th><th>Status</th></tr></thead>
          <tbody>
          <tr v-if="jobs.length === 0"><td colspan="4" style="text-align: center;">No history found.</td></tr>
          <tr v-for="job in jobs.slice(0, 10)" :key="job.id"> <td>{{ job.schedule?.name }}</td>
            <td>👤 {{ job.schedule?.user?.username }}</td>
            <td>{{ new Date(job.actualStartTime).toLocaleString() }}</td>
            <td><span :class="['status-badge', job.status.toLowerCase()]">{{ job.status }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* (Keep your existing Dashboard CSS here, it is perfectly suited for this!) */
.card { background: #606060; padding: 25px; border-radius: 8px; margin-bottom: 25px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
.schedule-form { display: flex; flex-direction: column; gap: 20px; }
.form-row { display: flex; gap: 20px; flex-wrap: wrap; }
.form-group { display: flex; flex-direction: column; flex: 1; min-width: 150px; }
.flex-2 { flex: 2; } .flex-3 { flex: 3; }
.advanced-row { background: #606060; padding: 15px; border-radius: 6px; border: 1px solid #e9ecef; }
label { font-weight: bold; margin-bottom: 5px; font-size: 0.9em; color: #495057; }
input, select { padding: 10px; border: 1px solid #ced4da; border-radius: 4px; font-size: 14px; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
.btn-primary { background: #0d6efd; color: white; border: none; padding: 10px 20px; border-radius: 4px; font-weight: bold; cursor: pointer; }
.btn-secondary { background: #6c757d; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
.btn-sm { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-size: 0.85em; }
.btn-edit { background: #ffc107; color: #000; }
.btn-delete { background: #dc3545; color: white; }
.action-buttons { display: flex; gap: 5px; }
.table-responsive { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; margin-top: 10px; }
th, td { padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; }
.small-link { font-size: 0.85em; color: #0d6efd; text-decoration: none; }
.error-banner { background: #f8d7da; color: #842029; padding: 12px; border-radius: 4px; margin-bottom: 20px; }
.success-banner { background: #d1e7dd; color: #0f5132; padding: 12px; border-radius: 4px; margin-bottom: 20px; }
.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.85em; font-weight: bold; }
.scheduled { background: #cce5ff; color: #004085; }
.done, .completed { background: #d4edda; color: #155724; }
.paused, .processing { background: #fff3cd; color: #856404; }
.failed { background: #f8d7da; color: #842029; }
.recording { background: #cfe2ff; color: #084298; }
</style>