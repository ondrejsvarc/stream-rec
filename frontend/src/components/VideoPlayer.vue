<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const props = defineProps({
  currentUser: Object
})

const files = ref([])
const currentFile = ref(null)
const errorMessage = ref('')
const token = localStorage.getItem('jwt_token')

const fetchFiles = async () => {
  try {
    const response = await api.get('/files')
    files.value = response.data
  } catch (error) {
    errorMessage.value = "Failed to load library."
  }
}

const playVideo = (file) => {
  currentFile.value = file
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const getVideoUrl = (fileId) => {
  return `http://localhost:8080/api/videos/stream/${fileId}?token=${token}`
}

const updatePublicity = async (fileId, newPublicity) => {
  try {
    await api.put(`/files/${fileId}/publicity`, { publicity: newPublicity })
    // No need to re-fetch, the v-model updates the UI instantly
  } catch (error) {
    alert("Failed to update publicity.")
    fetchFiles() // Revert UI if failed
  }
}

const deleteFile = async (fileId, e) => {
  e.stopPropagation() // Prevent triggering the playVideo click event
  if (confirm("Are you sure? This will delete the video permanently from the hard drive.")) {
    try {
      await api.delete(`/files/${fileId}`)
      if (currentFile.value?.id === fileId) currentFile.value = null
      await fetchFiles()
    } catch (error) {
      alert("Failed to delete file.")
    }
  }
}

const canManage = (file) => {
  return props.currentUser?.role === 'ADMIN' || file.owner?.username === props.currentUser?.username
}

onMounted(() => { fetchFiles() })
</script>

<template>
  <div class="video-container">
    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <div class="layout">
      <div class="player-section card">
        <h2>📺 Now Playing</h2>
        <div v-if="currentFile" class="player-wrapper">
          <video :src="getVideoUrl(currentFile.id)" controls autoplay class="html-player">
            Your browser does not support the video tag.
          </video>
          <div class="now-playing-info">
            <h3>{{ currentFile.filename }}</h3>
            <div class="info-row">
              <span class="text-muted">👤 Owner: {{ currentFile.owner?.username }}</span>
              <span class="text-muted">📅 Date: {{ new Date(currentFile.createdAt).toLocaleString() }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-player">
          <p>Select a video from the library to start watching.</p>
        </div>
      </div>

      <div class="library-section card">
        <h2>🎬 Library <button @click="fetchFiles" class="refresh-btn" title="Refresh">🔄</button></h2>

        <div v-if="files.length > 0" class="job-list">
          <div
              v-for="file in files"
              :key="file.id"
              :class="['job-item', { active: currentFile?.id === file.id }]"
              @click="playVideo(file)"
          >
            <div class="job-details">
              <strong>{{ file.filename }}</strong>
              <span class="job-date">👤 {{ file.owner?.username }} | {{ new Date(file.createdAt).toLocaleDateString() }}</span>
            </div>

            <div class="asset-actions">
              <select
                  v-model="file.publicity"
                  @change="updatePublicity(file.id, file.publicity)"
                  @click.stop
                  :disabled="!canManage(file)"
                  class="privacy-select"
              >
                <option value="PUBLIC">🌍 Public</option>
                <option value="UNLISTED">🔗 Unlisted</option>
                <option value="PRIVATE">🔒 Private</option>
              </select>

              <button
                  v-if="canManage(file)"
                  @click="deleteFile(file.id, $event)"
                  class="btn-danger-sm"
                  title="Delete File">
                🗑️
              </button>
            </div>
          </div>
        </div>
        <div v-else class="empty-library">
          <p>No videos found in your library.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-container { margin-top: 20px; }
.layout { display: flex; gap: 20px; align-items: flex-start; }
@media (max-width: 768px) { .layout { flex-direction: column; } }

.card { background: #606060; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
.player-section { flex: 2; }
.library-section { flex: 1; min-width: 380px; }

.player-wrapper { display: flex; flex-direction: column; gap: 15px; }
.html-player { width: 100%; border-radius: 8px; background: #000; aspect-ratio: 16/9; box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
.now-playing-info { background: #606060; padding: 15px; border-radius: 6px; border: 1px solid #e9ecef; }
.now-playing-info h3 { margin: 0 0 10px 0; word-break: break-all; }
.info-row { display: flex; gap: 20px; }
.text-muted { color: #6c757d; margin: 0; font-size: 0.9em; }

.empty-player { aspect-ratio: 16/9; background: #f1f3f5; display: flex; justify-content: center; align-items: center; border-radius: 8px; color: #6c757d; font-style: italic; border: 2px dashed #dee2e6; }

.refresh-btn { background: none; border: none; cursor: pointer; float: right; font-size: 1.2rem; transition: transform 0.2s; }
.refresh-btn:hover { transform: rotate(180deg); }

.job-list { display: flex; flex-direction: column; gap: 8px; max-height: 600px; overflow-y: auto; padding-right: 5px; }
.job-item { display: flex; flex-direction: column; gap: 10px; padding: 12px; border: 1px solid #606060; border-radius: 6px; cursor: pointer; transition: all 0.2s; background: #fff; }
.job-item:hover { border-color: #0d6efd; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.job-item.active { border-left: 4px solid #0d6efd; background: #f8faff; border-color: #0d6efd; }

.job-details strong { word-break: break-all; }
.job-date { font-size: 0.8em; color: #6c757d; margin-top: 4px; display: block; }

.asset-actions { display: flex; justify-content: space-between; align-items: center; gap: 10px; border-top: 1px solid #eee; padding-top: 8px; }
.privacy-select { padding: 4px; border: 1px solid #ccc; border-radius: 4px; font-size: 0.85em; background: #fff; cursor: pointer; }
.privacy-select:disabled { background: #e9ecef; cursor: not-allowed; }
.btn-danger-sm { background: #dc3545; color: white; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; transition: background 0.2s; }
.btn-danger-sm:hover { background: #c82333; }

.empty-library { color: #6c757d; text-align: center; padding: 30px 0; font-style: italic; }
.error-banner { background: #f8d7da; color: #842029; padding: 12px; border-radius: 4px; margin-bottom: 20px; }
</style>