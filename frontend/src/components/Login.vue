<script setup>
import { ref } from 'vue'
import api from '../services/api'

const emit = defineEmits(['login-success'])

const username = ref('')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await api.post('/auth/login', {
      username: username.value,
      password: password.value
    })

    localStorage.setItem('jwt_token', response.data.token)

    emit('login-success')
  } catch (error) {
    if (error.response && error.response.status === 401) {
      errorMessage.value = "Invalid username or password."
    } else {
      errorMessage.value = "An error occurred connecting to the server."
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="card login-card">
      <h2>🔒 Stream Recorder Login</h2>
      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>Username</label>
          <input type="text" v-model="username" required autofocus />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input type="password" v-model="password" required />
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Logging in...' : 'Log In' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; min-height: 80vh; font-family: sans-serif; }
.login-card { width: 100%; max-width: 400px; padding: 30px; }
.login-form { display: flex; flex-direction: column; gap: 15px; margin-top: 20px; }
.form-group { display: flex; flex-direction: column; }
input { padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 16px; }
button { padding: 12px; background: #007bff; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; font-weight: bold; }
button:disabled { background: #aaa; }
.error-banner { background: #ffdddd; color: #d8000c; padding: 10px; border-radius: 4px; text-align: center; }
</style>