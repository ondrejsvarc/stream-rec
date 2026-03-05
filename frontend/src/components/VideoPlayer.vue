<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const videos = ref([])
const currentVideo = ref(null)
const errorMessage = ref('')

const token = localStorage.getItem('jwt_token')

const fetchVideos = async () => {
  try {
    const response = await api.get('/videos')
    videos.value = response.data
  } catch (error) {
    console.error(error)
    errorMessage.value = "Failed to load video library."
  }
}

const playVideo = (filename) => {
  currentVideo.value = filename
}

const getVideoUrl = (filename) => {
  return `http://localhost:8080/api/videos/stream/${encodeURIComponent(filename)}?token=${token}`
}

onMounted(() => {
  fetchVideos()
})
</script>

<template>
  <div class="video-container">
    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <div class="layout">
      <div class="player-section card">
        <h2>Now Playing</h2>
        <div v-if="currentVideo" class="player-wrapper">
          <video
              :src="getVideoUrl(currentVideo)"
              controls
              autoplay
              class="html-player">
            Your browser does not support the video tag.
          </video>
          <p class="now-playing-title">{{ currentVideo }}</p>
        </div>
        <div v-else class="empty-player">
          <p>Select a video from the library to start watching.</p>
        </div>
      </div>

      <div class="library-section card">
        <h2>Library <button @click="fetchVideos" class="refresh-btn">🔄</button></h2>

        <ul v-if="videos.length > 0" class="video-list">
          <li
              v-for="video in videos"
              :key="video"
              :class="{ active: currentVideo === video }"
              @click="playVideo(video)"
          >
            🎥 {{ video }}
          </li>
        </ul>
        <div v-else class="empty-library">
          <p>No recordings found. Go schedule a stream!</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-container { margin-top: 20px; }
.layout { display: flex; gap: 20px; align-items: flex-start; }
@media (max-width: 768px) {
  .layout { flex-direction: column; }
}

.card { background: #6f6f6f; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.player-section { flex: 2; }
.library-section { flex: 1; min-width: 250px; }

.player-wrapper { display: flex; flex-direction: column; gap: 10px; }
.html-player { width: 100%; border-radius: 8px; background: #000; aspect-ratio: 16/9; }
.now-playing-title { font-weight: bold; margin: 0; padding: 10px; background: #f8f9fa; border-radius: 4px; word-break: break-all; }

.empty-player { aspect-ratio: 16/9; background: #e9ecef; display: flex; justify-content: center; align-items: center; border-radius: 8px; color: #6c757d; font-style: italic; }

.refresh-btn { background: none; border: none; cursor: pointer; float: right; font-size: 1rem; transition: transform 0.2s; }
.refresh-btn:hover { transform: rotate(45deg); }

.video-list { list-style: none; padding: 0; margin: 0; max-height: 500px; overflow-y: auto; }
.video-list li { padding: 12px; border-bottom: 1px solid #eee; cursor: pointer; transition: background 0.2s; border-radius: 4px; margin-bottom: 2px; word-break: break-all; }
.video-list li:hover { background: #f1f3f5; }
.video-list li.active { background: #e7f1ff; border-left: 4px solid #007bff; font-weight: bold; color: #004085; }

.empty-library { color: #6c757d; text-align: center; padding: 20px 0; font-style: italic; }
.error-banner { background: #ffdddd; color: #d8000c; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
</style>