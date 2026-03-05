<script setup>
import { ref } from 'vue'

const props = defineProps({
  currentUser: Object
})
const emit = defineEmits(['logout', 'open-password-modal', 'open-user-modal', 'navigate'])

const showDropdown = ref(false)
const toggleDropdown = () => { showDropdown.value = !showDropdown.value }

const triggerLogout = () => {
  showDropdown.value = false
  emit('logout')
}
</script>

<template>
  <div class="header">
    <div class="brand-and-nav">
      <h1>Stream Recorder</h1>

      <nav class="main-nav">
        <a href="#" @click.prevent="emit('navigate', 'dashboard')">Schedules</a>
        <a href="#" @click.prevent="emit('navigate', 'recordings')">Recordings Library</a>
      </nav>
    </div>

    <div class="user-menu">
      <button @click="toggleDropdown" class="avatar-btn">
        👤 {{ currentUser.username }} ▼
      </button>
      <div v-if="showDropdown" class="dropdown-content">
        <div class="dropdown-header">
          <strong>{{ currentUser.username }}</strong>
          <span class="role-badge">{{ currentUser.role }}</span>
        </div>
        <hr />
        <a href="#" @click.prevent="emit('open-password-modal'); showDropdown = false">🔑 Change Password</a>
        <a href="#" v-if="currentUser.role === 'ADMIN'" @click.prevent="emit('open-user-modal'); showDropdown = false">👥 Manage Users</a>
        <hr />
        <a href="#" @click.prevent="triggerLogout" class="logout-link">🚪 Logout</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #1e1e1e; padding: 10px 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.user-menu { position: relative; display: inline-block; }
.avatar-btn { background: #f1f3f5; color: #333; padding: 8px 16px; border: none; border-radius: 20px; cursor: pointer; font-weight: bold; }
.dropdown-content { position: absolute; right: 0; top: 110%; background-color: #606060; min-width: 200px; box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.1); border-radius: 8px; z-index: 10; border: 1px solid #eee; }
.dropdown-header { padding: 12px 16px; display: flex; flex-direction: column; gap: 4px; background: #606060; }
.role-badge { font-size: 0.75em; background: #007bff; color: white; padding: 2px 6px; border-radius: 4px; align-self: flex-start; }
.dropdown-content a { color: #333; padding: 12px 16px; text-decoration: none; display: block; }
.dropdown-content a:hover { background-color: #f1f3f5; }
.dropdown-content hr { margin: 0; border: none; border-top: 1px solid #eee; }
.logout-link { color: #dc3545 !important; }
.brand-and-nav { display: flex; align-items: center; gap: 30px; }
.main-nav { display: flex; gap: 15px; }
.main-nav a { text-decoration: none; color: #495057; font-weight: bold; padding: 6px 10px; border-radius: 4px; transition: background 0.2s; }
.main-nav a:hover { background: #e9ecef; }
h1 { margin: 0; font-size: 1.5rem; }
</style>