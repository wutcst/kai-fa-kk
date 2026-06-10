<script setup>
import { ref } from 'vue'
import { getApiErrorMessage, login, register } from '../api/authApi'
import AuthView from '../components/AuthView.vue'
import GameDashboard from '../components/GameDashboard.vue'
import StartMenu from '../components/StartMenu.vue'
import { mockGameState, mockSaves } from '../mock/gameState'

const AUTH_TOKEN_KEY = 'sesame-auth-token'
const USERNAME_KEY = 'sesame-username'

const getSessionValue = (key) => (
  typeof sessionStorage === 'undefined' ? '' : sessionStorage.getItem(key) || ''
)

const authToken = ref(getSessionValue(AUTH_TOKEN_KEY))
const username = ref(getSessionValue(USERNAME_KEY))
const authLoading = ref(false)
const authErrorMessage = ref('')
const currentView = ref(authToken.value ? 'start-menu' : 'auth')

function enterStartMenu() {
  currentView.value = 'start-menu'
}

function saveAuthSession(result, submittedUsername) {
  if (result?.success !== true || !result.token) {
    throw new Error(result?.message || '登录失败，未获取到登录令牌')
  }

  authToken.value = result.token
  username.value = result.username || submittedUsername
  sessionStorage.setItem(AUTH_TOKEN_KEY, authToken.value)
  sessionStorage.setItem(USERNAME_KEY, username.value)
  enterStartMenu()
}

async function handleLogin(credentials) {
  authLoading.value = true
  authErrorMessage.value = ''

  try {
    const result = await login(credentials.username, credentials.password)
    saveAuthSession(result, credentials.username)
  } catch (error) {
    authErrorMessage.value = getApiErrorMessage(error)
  } finally {
    authLoading.value = false
  }
}

async function handleRegister(credentials) {
  authLoading.value = true
  authErrorMessage.value = ''

  try {
    const registerResult = await register(credentials.username, credentials.password)
    if (registerResult?.success !== true) {
      throw new Error(registerResult?.message || '注册失败，请稍后重试')
    }

    const loginResult = await login(credentials.username, credentials.password)
    saveAuthSession(loginResult, credentials.username)
  } catch (error) {
    authErrorMessage.value = getApiErrorMessage(error)
  } finally {
    authLoading.value = false
  }
}

function startNewGame() {
  currentView.value = 'game'
}

function leaveGame() {
  authToken.value = ''
  username.value = ''
  authErrorMessage.value = ''
  sessionStorage.removeItem(AUTH_TOKEN_KEY)
  sessionStorage.removeItem(USERNAME_KEY)
  currentView.value = 'auth'
}
</script>

<template>
  <main class="home-page">
    <AuthView
      v-if="currentView === 'auth'"
      :error-message="authErrorMessage"
      :intro="mockGameState.authIntro"
      :loading="authLoading"
      @login="handleLogin"
      @register="handleRegister"
    />
    <StartMenu
      v-else-if="currentView === 'start-menu'"
      :saves="mockSaves"
      :username="username"
      @logout="leaveGame"
      @start-new="startNewGame"
    />
    <GameDashboard
      v-else
      :game-state="mockGameState"
      @logout="leaveGame"
    />
  </main>
</template>
