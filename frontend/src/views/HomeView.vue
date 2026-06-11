<script setup>
import { onMounted, ref } from 'vue'
import { getApiErrorMessage, login, register } from '../api/authApi'
import {
  getGameApiErrorMessage,
  listGameSaves,
  loadLatestSave,
  loadSave,
  startGame,
} from '../api/gameApi'
import AuthView from '../components/AuthView.vue'
import GameDashboard from '../components/GameDashboard.vue'
import StartMenu from '../components/StartMenu.vue'
import { mockGameState } from '../mock/gameState'

const AUTH_TOKEN_KEY = 'sesame-auth-token'
const USERNAME_KEY = 'sesame-username'
const SESSION_ID_KEY = 'sesame-session-id'

const getSessionValue = (key) => (
  typeof sessionStorage === 'undefined' ? '' : sessionStorage.getItem(key) || ''
)

const authToken = ref(getSessionValue(AUTH_TOKEN_KEY))
const username = ref(getSessionValue(USERNAME_KEY))
const authLoading = ref(false)
const authErrorMessage = ref('')
const gameState = ref(null)
const sessionId = ref(getSessionValue(SESSION_ID_KEY))
const gameLoading = ref(false)
const gameErrorMessage = ref('')
const saves = ref([])
const saveLoading = ref(false)
const saveErrorMessage = ref('')
const currentView = ref(authToken.value ? 'start-menu' : 'auth')

function enterStartMenu() {
  currentView.value = 'start-menu'
  void loadSaveSummaries()
}

function getAuthToken() {
  return authToken.value || getSessionValue(AUTH_TOKEN_KEY)
}

function enterLoadedGame(result) {
  gameState.value = result
  sessionId.value = result?.sessionId || ''

  if (sessionId.value) {
    sessionStorage.setItem(SESSION_ID_KEY, sessionId.value)
  } else {
    sessionStorage.removeItem(SESSION_ID_KEY)
  }

  currentView.value = 'game'
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

async function startNewGame() {
  const token = getAuthToken()

  if (!token) {
    gameErrorMessage.value = '请先登录后再开始游戏'
    return
  }

  authToken.value = token
  gameLoading.value = true
  gameErrorMessage.value = ''

  try {
    const result = await startGame(token)
    enterLoadedGame(result)
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    gameLoading.value = false
  }
}

async function continueLatestSave() {
  const token = getAuthToken()

  if (!token) {
    saveErrorMessage.value = '请先登录后再读取存档'
    return
  }

  authToken.value = token
  saveLoading.value = true
  saveErrorMessage.value = ''

  try {
    const result = await loadLatestSave(token)
    enterLoadedGame(result)
  } catch (error) {
    saveErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    saveLoading.value = false
  }
}

async function loadSelectedSave(saveId) {
  const token = getAuthToken()

  if (!token) {
    saveErrorMessage.value = '请先登录后再读取存档'
    return
  }

  if (saveId === undefined || saveId === null || saveId === '') {
    saveErrorMessage.value = '存档编号无效'
    return
  }

  authToken.value = token
  saveLoading.value = true
  saveErrorMessage.value = ''

  try {
    const result = await loadSave(token, saveId)
    enterLoadedGame(result)
  } catch (error) {
    saveErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    saveLoading.value = false
  }
}

function leaveGame() {
  authToken.value = ''
  username.value = ''
  authErrorMessage.value = ''
  gameState.value = null
  sessionId.value = ''
  gameErrorMessage.value = ''
  saves.value = []
  saveErrorMessage.value = ''
  sessionStorage.removeItem(AUTH_TOKEN_KEY)
  sessionStorage.removeItem(USERNAME_KEY)
  sessionStorage.removeItem(SESSION_ID_KEY)
  currentView.value = 'auth'
}

async function loadSaveSummaries() {
  const token = getAuthToken()

  if (!token) {
    saves.value = []
    saveErrorMessage.value = '请先登录后再查看存档'
    return
  }

  authToken.value = token
  saveLoading.value = true
  saveErrorMessage.value = ''

  try {
    saves.value = await listGameSaves(token)
  } catch (error) {
    saves.value = []
    saveErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  if (currentView.value === 'start-menu') {
    void loadSaveSummaries()
  }
})
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
      :error-message="gameErrorMessage"
      :loading="gameLoading"
      :save-error-message="saveErrorMessage"
      :save-loading="saveLoading"
      :saves="saves"
      :username="username"
      @continue-latest="continueLatestSave"
      @load-save="loadSelectedSave"
      @logout="leaveGame"
      @start-new="startNewGame"
    />
    <GameDashboard
      v-else
      :game-state="gameState || mockGameState"
      :username="username"
      @logout="leaveGame"
    />
  </main>
</template>
