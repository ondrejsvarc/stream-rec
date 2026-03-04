<script setup>
import { ref } from 'vue'
import api from '../services/api'

const emit = defineEmits(['close'])

const pwdData = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdMessage = ref({ text: '', type: '' })
const loading = ref(false)

const isSuccess = ref(false)

const handleChangePassword = async () => {
  if (pwdData.value.newPassword !== pwdData.value.confirmPassword) {
    pwdMessage.value = { text: "New passwords do not match", type: 'error' }
    return
  }

  loading.value = true
  try {
    await api.post('/auth/change-password', {
      oldPassword: pwdData.value.oldPassword,
      newPassword: pwdData.value.newPassword
    })

    isSuccess.value = true
    pwdMessage.value = { text: "Password changed successfully!", type: 'success' }

    setTimeout(() => {
      emit('close')
    }, 2500)

  } catch (error) {
    pwdMessage.value = { text: error.response?.data || "Update failed", type: 'error' }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="modal-overlay" @click.self="!isSuccess && emit('close')">
    <div class="modal-card">

      <div v-if="isSuccess" class="success-container">
        <div class="success-icon">✅</div>
        <h3>{{ pwdMessage.text }}</h3>
      </div>

      <div v-else>
        <h3>Change Password</h3>

        <p v-if="pwdMessage.text" :class="['message', pwdMessage.type]">
          {{ pwdMessage.text }}
        </p>

        <form @submit.prevent="handleChangePassword" class="modal-form">
          <input type="password" v-model="pwdData.oldPassword" placeholder="Current Password" required />
          <input type="password" v-model="pwdData.newPassword" placeholder="New Password" required />
          <input type="password" v-model="pwdData.confirmPassword" placeholder="Confirm New Password" required />

          <div class="modal-actions">
            <button type="button" @click="emit('close')" class="btn-secondary">Cancel</button>
            <button type="submit" :disabled="loading">
              {{ loading ? 'Updating...' : 'Update Password' }}
            </button>
          </div>
        </form>
      </div>

    </div>
  </div>
</template>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-card { background: #1e1e1e; padding: 30px; border-radius: 12px; width: 100%; max-width: 400px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.modal-form { display: flex; flex-direction: column; gap: 15px; margin-top: 15px; }
input { padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 16px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
button { padding: 10px 16px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
button:disabled { background: #aaa; }
.btn-secondary { background: #6c757d; }
.message { padding: 10px; border-radius: 4px; font-size: 0.9em; text-align: center; }
.message.error { background: #f8d7da; color: #721c24; }

.success-container { display: flex; flex-direction: column; justify-content: center; align-items: center; min-height: 200px; text-align: center; animation: fadeIn 0.3s ease-in; }
.success-icon { font-size: 3rem; margin-bottom: 10px; }
.success-container h3 { color: #155724; margin: 0; }

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
</style>