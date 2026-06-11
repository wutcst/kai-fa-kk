<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { DASHBOARD_BASE, HUD_POSITIONS, rectStyle } from '../constants/dashboardLayout'
import { getRoomBackground } from '../utils/assetMap'
import { getRoomName } from '../utils/roomNameMap'
import ActionPanel from './ActionPanel.vue'
import BackpackModal from './BackpackModal.vue'
import GameHeader from './GameHeader.vue'
import GameLog from './GameLog.vue'
import GameScene from './GameScene.vue'
import PlayerStatus from './PlayerStatus.vue'
import RankingPanel from './RankingPanel.vue'
import RoomDescriptionBar from './RoomDescriptionBar.vue'

const props = defineProps({
  gameState: {
    type: Object,
    default: null,
  },
  username: {
    type: String,
    default: '',
  },
  actionLoading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
  noticeMessage: {
    type: String,
    default: '',
  },
  itemActionLoading: {
    type: Boolean,
    default: false,
  },
  shortcutLoading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits([
  'exit-game',
  'save-game',
  'restart-level',
  'abandon-adventure',
  'take-item',
  'use-item',
  'drop-item',
  'back-room',
  'submit-password',
  'shortcut-notice',
])

const areRoomItemsHighlighted = ref(false)
const dashboardScale = ref(1)
const isBackpackOpen = ref(false)
const isHelpOpen = ref(false)
const isPasswordOpen = ref(false)
const passwordInput = ref('')
const passwordError = ref('')
const showDebugHud = ref(false)

const currentRoom = computed(() => props.gameState?.currentRoom || null)
const currentLevel = computed(() => props.gameState?.currentLevel)
const isPlaying = computed(() => props.gameState?.status === 'IN_PROGRESS')
const chapterName = computed(() => ({
  1: '第一章：石门回声',
  2: '第二章：月纹回廊',
  3: '第三章：沉金王座',
})[currentLevel.value] || '暂无章节信息')
const inventoryItems = computed(() => (
  props.gameState?.player?.inventory
  || []
))
const roomItems = computed(() => (
  currentRoom.value?.items
  || currentRoom.value?.visibleItems
  || []
))
const directionNames = {
  north: '北',
  south: '南',
  east: '东',
  west: '西',
}
const availableDestinations = computed(() => {
  const exits = currentRoom.value?.exits
  if (!exits || Array.isArray(exits) || typeof exits !== 'object') {
    return []
  }

  return Object.entries(exits).map(([direction, roomId]) => ({
    direction,
    directionName: directionNames[direction] || direction,
    roomId,
    roomName: getRoomName(roomId),
  }))
})
const statusLabel = computed(() => (props.gameState?.status === 'IN_PROGRESS'
  ? '探索中'
  : props.gameState?.status || ''))
const roomBackground = computed(() => getRoomBackground(currentRoom.value?.id))
const stageStyle = computed(() => ({
  '--dashboard-scale': dashboardScale.value,
  '--room-background': `url(${roomBackground.value})`,
}))

const updateDashboardScale = () => {
  dashboardScale.value = Math.min(
    window.innerWidth / DASHBOARD_BASE.width,
    window.innerHeight / DASHBOARD_BASE.height,
  )
}

const toggleRoomItems = () => {
  areRoomItemsHighlighted.value = !areRoomItemsHighlighted.value
}

const openBackpack = () => {
  isBackpackOpen.value = true
}

const toggleBackpack = () => {
  isBackpackOpen.value = !isBackpackOpen.value
}

const closeBackpack = () => {
  isBackpackOpen.value = false
}

const toggleHelp = () => {
  isHelpOpen.value = !isHelpOpen.value
}

const closeHelp = () => {
  isHelpOpen.value = false
}

const openPassword = () => {
  if (!isPlaying.value || props.shortcutLoading) return

  if (currentRoom.value?.id !== 'mechanism-gallery') {
    emit('shortcut-notice', '这里没有可以输入暗语的机关')
    return
  }

  passwordError.value = ''
  isPasswordOpen.value = true
}

const closePassword = () => {
  isPasswordOpen.value = false
  passwordInput.value = ''
  passwordError.value = ''
}

const submitPasswordInput = () => {
  const password = passwordInput.value.trim()
  if (!password) {
    passwordError.value = '暗语不能为空'
    return
  }

  emit('submit-password', password)
  closePassword()
}

const requestBackRoom = () => {
  if (isPlaying.value && !props.shortcutLoading) {
    emit('back-room')
  }
}

const isTextEntryTarget = (target) => {
  const element = target?.closest?.('input, textarea, select, [contenteditable="true"]')
  return Boolean(element)
}

const handleShortcutKey = (event) => {
  if (event.ctrlKey || event.altKey || event.metaKey || isTextEntryTarget(event.target)) {
    return
  }

  const shortcutActions = {
    r: requestBackRoom,
    h: toggleHelp,
    q: toggleRoomItems,
    e: toggleBackpack,
    p: openPassword,
  }
  const action = shortcutActions[event.key.toLowerCase()]

  if (!action) return

  event.preventDefault()
  action()
}

onMounted(() => {
  showDebugHud.value = new URLSearchParams(window.location.search).get('debugHud') === '1'
  updateDashboardScale()
  window.addEventListener('resize', updateDashboardScale)
  window.addEventListener('keydown', handleShortcutKey)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateDashboardScale)
  window.removeEventListener('keydown', handleShortcutKey)
})
</script>

<template>
  <section
    class="game-dashboard game-dashboard-viewport"
    aria-label="芝麻开门游戏主界面"
  >
    <div
      class="game-dashboard-stage game-dashboard--immersive"
      :style="stageStyle"
    >
      <GameHeader
        :chapter="chapterName"
        :location="currentRoom?.name || '暂无房间信息'"
        :objective="gameState?.objective"
        :status="statusLabel"
        :style="rectStyle(HUD_POSITIONS.topbar)"
        :title="gameState?.title"
        @logout="$emit('exit-game')"
      />

      <div
        class="dashboard-layer dashboard-layer--left-top"
        :style="rectStyle(HUD_POSITIONS.playerStatus)"
      >
        <PlayerStatus
          :current-level="currentLevel"
          :inventory-items="inventoryItems"
          :password-unlocked="gameState?.passwordUnlocked"
          :player="gameState?.player || {}"
          :status="gameState?.status"
          :username="username"
          @open-inventory="openBackpack"
        />
      </div>

      <ActionPanel
        :action-loading="shortcutLoading"
        :current-room="currentRoom"
        :help-open="isHelpOpen"
        :password-prompt-open="isPasswordOpen"
        :password-unlocked="gameState?.passwordUnlocked"
        :room-items-highlighted="areRoomItemsHighlighted"
        :status="gameState?.status"
        :style="rectStyle(HUD_POSITIONS.actionPanel)"
        @back-room="requestBackRoom"
        @open-inventory="toggleBackpack"
        @open-password="openPassword"
        @toggle-help="toggleHelp"
        @toggle-room-items="toggleRoomItems"
      />

      <GameScene
        :action-loading="itemActionLoading"
        :room="currentRoom"
        :room-items="roomItems"
        :room-items-highlighted="areRoomItemsHighlighted"
        :style="rectStyle(HUD_POSITIONS.roomItems)"
        @take-item="$emit('take-item', $event)"
      />

      <BackpackModal
        v-if="isBackpackOpen"
        :action-loading="itemActionLoading"
        :items="inventoryItems"
        :player="gameState?.player || {}"
        @close="closeBackpack"
        @drop-item="$emit('drop-item', $event)"
        @use-item="$emit('use-item', $event)"
      />

      <div
        v-if="isHelpOpen"
        class="backpack-modal"
        role="presentation"
        @click.self="closeHelp"
      >
        <section
          class="backpack-modal__panel shortcut-modal"
          aria-labelledby="help-modal-title"
          role="dialog"
          aria-modal="true"
        >
          <div class="backpack-modal__header">
            <h2
              id="help-modal-title"
              class="inventory-modal__title"
            >
              操作帮助
            </h2>
            <button
              type="button"
              aria-label="关闭帮助"
              @click="closeHelp"
            >
              ×
            </button>
          </div>
          <ul class="shortcut-modal__help-list">
            <li>方向按钮：移动到相邻房间</li>
            <li>R：返回上一房间</li>
            <li>H：打开帮助说明</li>
            <li>Q：查看当前房间物品</li>
            <li>E：打开背包</li>
            <li>P：输入暗语</li>
            <li>保存：保存当前进度</li>
            <li>重开：重新开始当前关</li>
            <li>放弃：结束本次探险</li>
          </ul>
          <div class="shortcut-modal__room-info">
            <div class="shortcut-modal__room-section">
              <h3>当前房间</h3>
              <p>{{ currentRoom?.name || '暂无房间信息' }}</p>
            </div>
            <div class="shortcut-modal__room-section">
              <h3>当前可前往</h3>
              <ul
                v-if="availableDestinations.length"
                class="shortcut-modal__destinations"
              >
                <li
                  v-for="destination in availableDestinations"
                  :key="destination.direction"
                >
                  <strong>{{ destination.directionName }}</strong>
                  <span>{{ destination.direction }}</span>
                  <span aria-hidden="true">→</span>
                  <span>{{ destination.roomName }}</span>
                </li>
              </ul>
              <p v-else>
                当前没有可前往的方向
              </p>
            </div>
          </div>
        </section>
      </div>

      <div
        v-if="isPasswordOpen"
        class="backpack-modal"
        role="presentation"
        @click.self="closePassword"
      >
        <section
          class="backpack-modal__panel shortcut-modal shortcut-modal--password"
          aria-labelledby="password-modal-title"
          role="dialog"
          aria-modal="true"
        >
          <div class="backpack-modal__header">
            <h2
              id="password-modal-title"
              class="inventory-modal__title"
            >
              输入暗语
            </h2>
            <button
              type="button"
              aria-label="关闭暗语输入"
              @click="closePassword"
            >
              ×
            </button>
          </div>
          <form
            class="shortcut-modal__form"
            @submit.prevent="submitPasswordInput"
          >
            <input
              v-model="passwordInput"
              class="shortcut-modal__input"
              type="text"
              placeholder="请输入暗语"
              :disabled="shortcutLoading"
            >
            <p
              v-if="passwordError"
              class="shortcut-modal__error"
            >
              {{ passwordError }}
            </p>
            <div class="shortcut-modal__actions">
              <button
                type="button"
                :disabled="shortcutLoading"
                @click="closePassword"
              >
                取消
              </button>
              <button
                type="submit"
                :disabled="shortcutLoading"
              >
                {{ shortcutLoading ? '提交中...' : '提交暗语' }}
              </button>
            </div>
          </form>
        </section>
      </div>

      <RankingPanel
        :fallback-text="gameState?.currentObjectives"
        :ranking="gameState?.ranking || gameState?.leaderboard || []"
        :style="rectStyle(HUD_POSITIONS.ranking)"
      />

      <GameLog
        :action-loading="actionLoading"
        :logs="gameState?.logs || []"
        :message="errorMessage || noticeMessage || gameState?.message"
        :status="gameState?.status || ''"
        :style="rectStyle(HUD_POSITIONS.log)"
        @abandon-adventure="$emit('abandon-adventure')"
        @restart-level="$emit('restart-level')"
        @save-game="$emit('save-game')"
      />

      <RoomDescriptionBar
        :description="currentRoom?.description"
        :style="rectStyle(HUD_POSITIONS.narration)"
      />

      <img
        v-if="showDebugHud"
        class="game-dashboard-debug-overlay"
        :src="dashboardReference"
        alt=""
        aria-hidden="true"
      >
    </div>
  </section>
</template>
