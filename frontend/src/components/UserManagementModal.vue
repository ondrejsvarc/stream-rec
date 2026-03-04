<script setup>
import { ref, onMounted } from 'vue'
import api from '../services/api'

const props = defineProps({
  currentUser: Object
})
const emit = defineEmits(['close'])

const userList = ref([])
const newUser = ref({ username: '', password: '', role: 'USER' })
const loading = ref(false)
const errorMessage = ref('')

const fetchUsers = async () => {
  try {
    const response = await api.get('/admin/users')
    userList.value = response.data
  } catch (error) {
    errorMessage.value = "Failed to fetch users."
  }
}

const handleCreateUser = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    await api.post('/admin/users', newUser.value)
    newUser.value = { username: '', password: '', role: 'USER' }
    await fetchUsers()
  } catch (error) {
    errorMessage.value = error.response?.data || "Failed to create user."
  } finally {
    loading.value = false
  }
}

const deleteUser = async (id) => {
  if (confirm("Are you sure you want to delete this user? This cannot be undone.")) {
    try {
      await api.delete(`/admin/users/${id}`)
      await fetchUsers()
    } catch (error) {
      errorMessage.value = "Failed to delete user."
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-card user-mgmt-card">
      <h3>👥 User Management</h3>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <div class="add-user-section">
        <h4>Add New User</h4>
        <form @submit.prevent="handleCreateUser" class="form-group-row">
          <input type="text" v-model="newUser.username" placeholder="Username" required />
          <input type="password" v-model="newUser.password" placeholder="Password" required />
          <select v-model="newUser.role">
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select>
          <button type="submit" :disabled="loading">Add</button>
        </form>
      </div>

      <h4>Existing Users</h4>
      <div class="user-list">
        <div v-for="user in userList" :key="user.id" class="user-item">
          <span>
            <strong>{{ user.username }}</strong>
            <span class="role-tag">{{ user.role }}</span>
          </span>
          <button
              v-if="user.username !== currentUser.username"
              @click="deleteUser(user.id)"
              class="btn-danger-sm">
            Delete
          </button>
        </div>
      </div>

      <button @click="emit('close')" class="btn-secondary close-btn">Close</button>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-card { background: #1e1e1e; padding: 30px; border-radius: 12px; width: 100%; max-width: 600px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.add-user-section { background: #6d6d6d; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #e9ecef; }
.form-group-row { display: flex; gap: 10px; margin-top: 10px; }
input, select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; flex: 1; font-size: 14px; }
button { padding: 8px 16px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
button:disabled { background: #aaa; }
.btn-secondary { background: #6c757d; }
.btn-danger-sm { background: #dc3545; color: white; border: none; padding: 6px 10px; border-radius: 4px; font-size: 0.8em; cursor: pointer; }
.error-banner { background: #ffdddd; color: #d8000c; padding: 10px; border-radius: 4px; margin-bottom: 15px; font-size: 0.9em; }
.user-list { max-height: 250px; overflow-y: auto; border: 1px solid #eee; border-radius: 4px; }
.user-item { display: flex; justify-content: space-between; align-items: center; padding: 12px; border-bottom: 1px solid #eee; }
.user-item:last-child { border-bottom: none; }
.role-tag { font-size: 0.75em; background: #e9ecef; padding: 2px 6px; border-radius: 4px; margin-left: 8px; color: #495057; }
.close-btn { margin-top: 20px; width: 100%; padding: 10px; }
</style>