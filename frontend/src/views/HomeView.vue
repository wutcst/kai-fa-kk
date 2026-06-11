<script setup>
import { onMounted, ref, watch } from 'vue'
import { getApiErrorMessage, login, register } from '../api/authApi'
import {
  abandonAdventure,
  backToPreviousRoom,
  buyShopItem,
  continueAdventure,
  dropItem,
  getGameApiErrorMessage,
  getLeaderboard,
  getShopCatalog,
  listGameSaves,
  loadLatestSave,
  loadSave,
  movePlayer,
  restartLevel,
  saveGame,
  sellShopItem,
  startGame,
  submitPassword,
  takeItem,
  useItem,
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
const gameActionLoading = ref(false)
const itemActionLoading = ref(false)
const shortcutActionLoading = ref(false)
const moveLoading = ref(false)
const gameErrorMessage = ref('')
const gameNoticeMessage = ref('')
const saves = ref([])
const saveLoading = ref(false)
const saveErrorMessage = ref('')
const leaderboard = ref([])
const leaderboardLoading = ref(false)
const leaderboardErrorMessage = ref('')
const shopCatalog = ref([])
const shopLoading = ref(false)
const shopActionLoading = ref(false)
const shopErrorMessage = ref('')
const currentView = ref(authToken.value ? 'start-menu' : 'auth')

function enterStartMenu() {
  currentView.value = 'start-menu'
  void loadSaveSummaries()
}

function exitGame() {
  currentView.value = 'start-menu'
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''
  void loadSaveSummaries()
}

function returnResultToStartMenu() {
  sessionId.value = ''
  gameState.value = null
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''
  sessionStorage.removeItem(SESSION_ID_KEY)
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

async function handleSaveGame() {
  const token = getAuthToken()

  if (!token) {
    gameErrorMessage.value = '请先登录后再保存游戏'
    return
  }

  if (!sessionId.value) {
    gameErrorMessage.value = '当前没有可保存的游戏会话'
    return
  }

  gameActionLoading.value = true
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = await saveGame(token, sessionId.value, '自动存档')
    enterLoadedGame(result)
    gameNoticeMessage.value = result?.message || '游戏已保存'
    await loadSaveSummaries()
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    gameActionLoading.value = false
  }
}

async function handleRestartLevel() {
  if (!sessionId.value) {
    gameErrorMessage.value = '当前没有可重开的游戏会话'
    return
  }

  gameActionLoading.value = true
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = await restartLevel(sessionId.value)
    enterLoadedGame(result)
    gameNoticeMessage.value = result?.message || '当前关已重新开始'
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    gameActionLoading.value = false
  }
}

async function handleAbandonAdventure() {
  const confirmed = window.confirm('确定要放弃本次探险吗？当前探险将结束，相关存档可能会被删除。')
  if (!confirmed) return

  if (!sessionId.value) {
    gameErrorMessage.value = '当前没有可放弃的游戏会话'
    return
  }

  gameActionLoading.value = true
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = await abandonAdventure(sessionId.value)
    gameNoticeMessage.value = result?.message || '已放弃本次探险'
    sessionId.value = ''
    gameState.value = null
    sessionStorage.removeItem(SESSION_ID_KEY)
    currentView.value = 'start-menu'
    void loadSaveSummaries()
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    gameActionLoading.value = false
  }
}

async function runItemAction(itemId, action) {
  if (!sessionId.value) {
    gameErrorMessage.value = '当前没有可操作的游戏会话'
    return
  }

  if (!itemId) {
    gameErrorMessage.value = '物品编号无效'
    return
  }

  itemActionLoading.value = true
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = await action(sessionId.value, itemId)
    enterLoadedGame(result)
    gameNoticeMessage.value = result?.message || ''
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    itemActionLoading.value = false
  }
}

function handleTakeItem(itemId) {
  return runItemAction(itemId, takeItem)
}

function handleUseItem(itemId) {
  return runItemAction(itemId, useItem)
}

function handleDropItem(itemId) {
  return runItemAction(itemId, dropItem)
}

async function runShortcutAction(action, ...args) {
  if (!sessionId.value) {
    gameErrorMessage.value = '当前没有可操作的游戏会话'
    return
  }

  shortcutActionLoading.value = true
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = await action(sessionId.value, ...args)
    enterLoadedGame(result)
    gameNoticeMessage.value = result?.message || ''
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    shortcutActionLoading.value = false
  }
}

function handleBackRoom() {
  return runShortcutAction(backToPreviousRoom)
}

async function handleSubmitPassword(password) {
  if (!password?.trim()) {
    gameErrorMessage.value = '暗语不能为空'
    return
  }

  if (!sessionId.value) {
    gameErrorMessage.value = '当前没有可操作的游戏会话'
    return
  }

  shortcutActionLoading.value = true
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = await submitPassword(sessionId.value, password.trim())
    enterLoadedGame(result)
    gameNoticeMessage.value = result?.message
      || (result?.passwordUnlocked ? '最终石门已解锁' : '')
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    shortcutActionLoading.value = false
  }
}

function handleShortcutNotice(message) {
  gameErrorMessage.value = ''
  gameNoticeMessage.value = message
}

async function handleMovePlayer(direction) {
  if (!sessionId.value) {
    gameErrorMessage.value = '当前没有可操作的游戏会话'
    return
  }

  if (gameState.value?.status !== 'IN_PROGRESS') {
    gameErrorMessage.value = '当前不在探索状态，不能移动。'
    return
  }

  if (moveLoading.value) return

  moveLoading.value = true
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = await movePlayer(sessionId.value, direction)
    enterLoadedGame(result)
    gameNoticeMessage.value = result?.message || ''
  } catch (error) {
    gameErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    moveLoading.value = false
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
  gameNoticeMessage.value = ''
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

async function loadLeaderboard() {
  leaderboardLoading.value = true
  leaderboardErrorMessage.value = ''

  try {
    leaderboard.value = await getLeaderboard(3)
  } catch (error) {
    leaderboard.value = []
    leaderboardErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    leaderboardLoading.value = false
  }
}

async function loadShopCatalog() {
  if (!sessionId.value || gameState.value?.status !== 'SHOPPING') {
    shopCatalog.value = []
    return
  }

  shopLoading.value = true
  shopErrorMessage.value = ''

  try {
    shopCatalog.value = await getShopCatalog(sessionId.value)
  } catch (error) {
    shopCatalog.value = []
    shopErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    shopLoading.value = false
  }
}

async function runShopAction(action, itemId) {
  if (!sessionId.value) {
    shopErrorMessage.value = '当前没有可操作的游戏会话'
    return
  }

  if (shopActionLoading.value) return

  shopActionLoading.value = true
  shopErrorMessage.value = ''
  gameErrorMessage.value = ''
  gameNoticeMessage.value = ''

  try {
    const result = itemId
      ? await action(sessionId.value, itemId)
      : await action(sessionId.value)
    enterLoadedGame(result)
    gameNoticeMessage.value = result?.message || ''
  } catch (error) {
    shopErrorMessage.value = getGameApiErrorMessage(error)
  } finally {
    shopActionLoading.value = false
  }
}

function handleBuyShopItem(itemId) {
  return runShopAction(buyShopItem, itemId)
}

function handleSellShopItem(itemId) {
  return runShopAction(sellShopItem, itemId)
}

function handleContinueAdventure() {
  return runShopAction(continueAdventure)
}

watch(
  () => gameState.value?.status,
  (status, previousStatus) => {
    if (status === 'WON' && previousStatus !== 'WON') {
      void loadLeaderboard()
    }
  },
)

watch(
  () => [gameState.value?.status, sessionId.value],
  ([status, currentSessionId], [previousStatus, previousSessionId] = []) => {
    if (
      status === 'SHOPPING'
      && currentSessionId
      && (previousStatus !== 'SHOPPING' || previousSessionId !== currentSessionId)
    ) {
      void loadShopCatalog()
      return
    }

    if (status !== 'SHOPPING') {
      shopCatalog.value = []
      shopErrorMessage.value = ''
    }
  },
)

onMounted(() => {
  void loadLeaderboard()
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
      :action-loading="gameLoading || gameActionLoading"
      :error-message="gameErrorMessage"
      :game-state="gameState"
      :item-action-loading="itemActionLoading"
      :leaderboard="leaderboard"
      :leaderboard-error-message="leaderboardErrorMessage"
      :leaderboard-loading="leaderboardLoading"
      :move-loading="moveLoading"
      :notice-message="gameNoticeMessage"
      :shortcut-loading="shortcutActionLoading"
      :shop-action-loading="shopActionLoading"
      :shop-catalog="shopCatalog"
      :shop-error-message="shopErrorMessage"
      :shop-loading="shopLoading"
      :username="username"
      @abandon-adventure="handleAbandonAdventure"
      @back-room="handleBackRoom"
      @drop-item="handleDropItem"
      @exit-game="exitGame"
      @move-player="handleMovePlayer"
      @restart-level="handleRestartLevel"
      @return-start-menu="returnResultToStartMenu"
      @save-game="handleSaveGame"
      @buy-shop-item="handleBuyShopItem"
      @continue-adventure="handleContinueAdventure"
      @sell-shop-item="handleSellShopItem"
      @shortcut-notice="handleShortcutNotice"
      @submit-password="handleSubmitPassword"
      @start-new-game="startNewGame"
      @take-item="handleTakeItem"
      @use-item="handleUseItem"
    />
  </main>
</template>
