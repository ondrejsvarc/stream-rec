<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const apiMessage = ref('Connecting to backend...')

onMounted(async () => {
  try {
    // In local dev, Vite runs on 5173 and Spring on 8080
    const response = await axios.get('http://localhost:8080/api/status')
    apiMessage.value = response.data.message
  } catch (error) {
    apiMessage.value = 'Failed to connect to backend.'
    console.error(error)
  }
})
</script>

<template>
  <main style="text-align: center; margin-top: 50px; font-family: sans-serif;">
    <h1>Stream Recorder</h1>
    <p>Backend Status: <strong>{{ apiMessage }}</strong></p>
  </main>
</template>