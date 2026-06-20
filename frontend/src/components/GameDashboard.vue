<script setup>
import {
  computed,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from 'vue'
import { DASHBOARD_BASE, HUD_POSITIONS, rectStyle } from '../constants/dashboardLayout'
import { getRoomBackground } from '../utils/assetMap'
import { getRoomName } from '../utils/roomNameMap'
import ActionPanel from './ActionPanel.vue'
import BackpackModal from './BackpackModal.vue'
import GameHeader from './GameHeader.vue'
import GameLog from './GameLog.vue'
import GameMapModal from './GameMapModal.vue'
import GameNoticeModal from './GameNoticeModal.vue'
import GameResultModal from './GameResultModal.vue'
import GameScene from './GameScene.vue'
import PlayerStatus from './PlayerStatus.vue'
import RankingPanel from './RankingPanel.vue'
import RoomDescriptionBar from './RoomDescriptionBar.vue'
import ShopPanel from './ShopPanel.vue'

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
  moveLoading: {
    type: Boolean,
    default: false,
  },
  leaderboard: {
    type: Array,
    default: () => [],
  },
  leaderboardLoading: {
    type: Boolean,
    default: false,
  },
  leaderboardErrorMessage: {
    type: String,
    default: '',
  },
  shopCatalog: {
    type: Array,
    default: () => [],
  },
  shopLoading: {
    type: Boolean,
    default: false,
  },
  shopActionLoading: {
    type: Boolean,
    default: false,
  },
  shopErrorMessage: {
    type: String,
    default: '',
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
  'move-player',
  'buy-shop-item',
  'sell-shop-item',
  'continue-adventure',
  'return-start-menu',
  'start-new-game',
])

const areRoomItemsHighlighted = ref(false)
const dashboardScale = ref(1)
const isBackpackOpen = ref(false)
const isMapModalOpen = ref(false)
const isHelpOpen = ref(false)
const isPasswordOpen = ref(false)
const passwordInput = ref('')
const passwordError = ref('')
const showDebugHud = ref(false)
const isRescueNoticeOpen = ref(false)
const lastNoticeMessage = ref('')

const currentRoom = computed(() => props.gameState?.currentRoom || null)
const currentLevel = computed(() => props.gameState?.currentLevel)
const isPlaying = computed(() => props.gameState?.status === 'IN_PROGRESS')
const isShopping = computed(() => props.gameState?.status === 'SHOPPING')
const isTerminal = computed(() => ['WON', 'FAILED'].includes(props.gameState?.status))
const canSubmitPassword = computed(() => Boolean(props.gameState?.canSubmitPassword))
const chapterName = computed(() => ({
  1: '第一章：石门回声',
  2: '第二章：月纹回廊',
  3: '第三章：沉金王座',
})[currentLevel.value] || '暂无章节信息')
const inventoryItems = computed(() => (
  props.gameState?.player?.inventory
  || []
))
const hasOldMap = computed(() => inventoryItems.value.some((item) => item?.id === 'old-map'))
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
const availableDirections = computed(() => {
  if (Array.isArray(currentRoom.value?.exitDirections)) {
    return currentRoom.value.exitDirections
  }

  const exits = currentRoom.value?.exits
  if (!exits || Array.isArray(exits) || typeof exits !== 'object') {
    return []
  }

  return Object.keys(exits)
})
const statusLabel = computed(() => (props.gameState?.status === 'IN_PROGRESS'
  ? '探索中'
  : props.gameState?.status || ''))
const roomBackground = computed(() => getRoomBackground(currentRoom.value?.id))
const normalizeLogMessage = (log) => (
  typeof log === 'string' ? log : log?.message || log?.content || ''
)
const rescueKeywords = [
  '体力不足',
  '体力耗尽',
  '重新开始',
  '重开当前关',
  '当前关重新开始',
  '回到本关起点',
  '救援',
]
const rescueNoticeMessage = computed(() => {
  if (isTerminal.value) return ''

  const candidates = [
    props.gameState?.message,
    normalizeLogMessage(props.gameState?.logs?.at?.(-1)),
  ].filter(Boolean)

  return candidates.find((message) => (
    rescueKeywords.some((keyword) => message.includes(keyword))
  )) || ''
})
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

const openMapModal = () => {
  if (!hasOldMap.value) return

  isBackpackOpen.value = false
  isMapModalOpen.value = true
}

const closeMapModal = () => {
  isMapModalOpen.value = false
}

const toggleHelp = () => {
  isHelpOpen.value = !isHelpOpen.value
}

const closeHelp = () => {
  isHelpOpen.value = false
}

const openPassword = () => {
  if (!isPlaying.value || props.shortcutLoading || !canSubmitPassword.value) return

  passwordError.value = ''
  isPasswordOpen.value = true
}

const closePassword = () => {
  isPasswordOpen.value = false
  passwordInput.value = ''
  passwordError.value = ''
}

const closeRescueNotice = () => {
  isRescueNoticeOpen.value = false
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

const arrowDirections = {
  ArrowUp: 'north',
  ArrowDown: 'south',
  ArrowLeft: 'west',
  ArrowRight: 'east',
}

const requestMovePlayer = (direction) => {
  if (
    props.moveLoading
    || isBackpackOpen.value
    || isMapModalOpen.value
    || isHelpOpen.value
    || isPasswordOpen.value
  ) {
    return
  }

  if (!isPlaying.value) {
    emit('shortcut-notice', '当前不在探索状态，不能移动。')
    return
  }

  if (!availableDirections.value.includes(direction)) {
    emit('shortcut-notice', '当前房间没有这个方向的出口')
    return
  }

  emit('move-player', direction)
}

const handleShortcutKey = (event) => {
  if (event.ctrlKey || event.altKey || event.metaKey || isTextEntryTarget(event.target)) {
    return
  }

  if (isMapModalOpen.value) return
  if (isShopping.value) return

  const direction = arrowDirections[event.key]
  if (direction) {
    event.preventDefault()
    requestMovePlayer(direction)
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

watch(
  rescueNoticeMessage,
  (message) => {
    if (!message) {
      lastNoticeMessage.value = ''
      return
    }

    if (message === lastNoticeMessage.value) return

    lastNoticeMessage.value = message
    isRescueNoticeOpen.value = true
  },
  { immediate: true },
)

watch(isTerminal, (terminal) => {
  if (terminal) {
    isRescueNoticeOpen.value = false
  }
})

watch(isPlaying, (playing) => {
  if (!playing) {
    isMapModalOpen.value = false
  }
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

      <button
        v-if="isPlaying && hasOldMap"
        class="game-dashboard-map-button"
        type="button"
        :style="rectStyle(HUD_POSITIONS.mapButton)"
        @click="openMapModal"
      >
        查看地图
      </button>

      <ActionPanel
        v-if="isPlaying"
        :action-loading="shortcutLoading"
        :can-submit-password="canSubmitPassword"
        :current-room="currentRoom"
        :help-open="isHelpOpen"
        :move-loading="moveLoading"
        :password-prompt-open="isPasswordOpen"
        :room-items-highlighted="areRoomItemsHighlighted"
        :status="gameState?.status"
        :style="rectStyle(HUD_POSITIONS.actionPanel)"
        @back-room="requestBackRoom"
        @move-player="$emit('move-player', $event)"
        @open-inventory="toggleBackpack"
        @open-password="openPassword"
        @toggle-help="toggleHelp"
        @toggle-room-items="toggleRoomItems"
      />

      <GameScene
        v-if="isPlaying"
        :action-loading="itemActionLoading"
        :room="currentRoom"
        :room-items="roomItems"
        :room-items-highlighted="areRoomItemsHighlighted"
        :style="rectStyle(HUD_POSITIONS.roomItems)"
        @take-item="$emit('take-item', $event)"
      />

      <BackpackModal
        v-if="isPlaying && isBackpackOpen"
        :action-loading="itemActionLoading"
        :items="inventoryItems"
        :player="gameState?.player || {}"
        @close="closeBackpack"
        @drop-item="$emit('drop-item', $event)"
        @use-item="$emit('use-item', $event)"
        @view-map="openMapModal"
      />

      <GameMapModal
        v-if="isPlaying"
        :current-room-id="gameState?.player?.currentRoomId || currentRoom?.id || ''"
        :open="isMapModalOpen"
        @close="closeMapModal"
      />

      <div
        v-if="isPlaying && isHelpOpen"
        class="backpack-modal"
        role="presentation"
        @click.self="closeHelp"
      >
        <section
          class="backpack-modal__panel shortcut-modal shortcut-modal--help"
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
          <div class="shortcut-modal__help-sections">
            <section class="shortcut-modal__help-section">
              <h3>操作</h3>
              <ul>
                <li>方向按钮或方向键：移动到相邻房间。</li>
                <li>E：打开或关闭背包。</li>
                <li>Q：显示或收起当前房间物品。</li>
                <li>R：返回上一房间。</li>
                <li>H：打开或关闭帮助说明。</li>
                <li>P：输入暗语，仅在机关长廊可用。</li>
              </ul>
            </section>

            <section class="shortcut-modal__help-section">
              <h3>下一步怎么走</h3>
              <ul>
                <li>先查看当前房间出口，沿没有探索过的方向前进。</li>
                <li>遇到物品时，可以先拾取并查看背包说明。</li>
                <li>看到营地后，可以出售宝物、购买补给，再继续进入下一片区域。</li>
                <li>如果卡住，检查关键物品、房间描述和日志线索。</li>
              </ul>
            </section>

            <section class="shortcut-modal__help-section">
              <h3>地图</h3>
              <ul>
                <li>拾取残旧地图后，可以在主界面或背包中点击“查看地图”。</li>
                <li>丢弃残旧地图后，将不能继续查看地图。</li>
                <li>地图用于确认路线和当前位置，不会替玩家自动移动。</li>
                <li>当前所在位置会在地图上标记。</li>
              </ul>
            </section>

            <section class="shortcut-modal__help-section">
              <h3>背包</h3>
              <ul>
                <li>补给可以恢复体力，装备可以增强能力。</li>
                <li>宝物通常用于商店出售或最后结算。</li>
                <li>关键物品通常用于特殊机关，不一定能直接使用。</li>
                <li>背包有负重上限，拾取物品前要留意重量。</li>
              </ul>
            </section>

            <section class="shortcut-modal__help-section">
              <h3>暗语</h3>
              <ul>
                <li>暗语线索藏在房间描述、物品描述和日志提示中。</li>
                <li>留意“芝麻纹”“门”“开门”等关键词。</li>
                <li>真正的暗语通常由“被呼唤的对象”和“要执行的动作”组成。</li>
                <li>最终暗语需要在机关长廊输入。</li>
              </ul>
            </section>
          </div>
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
        v-if="isPlaying && isPasswordOpen"
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

      <ShopPanel
        v-if="isShopping"
        :action-loading="shopActionLoading"
        :catalog="shopCatalog"
        :error-message="shopErrorMessage"
        :loading="shopLoading"
        :player="gameState?.player || {}"
        @buy-item="$emit('buy-shop-item', $event)"
        @continue-adventure="$emit('continue-adventure')"
        @sell-item="$emit('sell-shop-item', $event)"
      />

      <GameResultModal
        v-if="isTerminal"
        :current-level="currentLevel"
        :final-score="gameState?.finalScore"
        :high-score="gameState?.highScore"
        :loading="actionLoading"
        :message="errorMessage || gameState?.message"
        :player="gameState?.player || {}"
        :room-name="currentRoom?.name"
        :status="gameState?.status"
        @restart-level="$emit('restart-level')"
        @return-start-menu="$emit('return-start-menu')"
        @start-new-game="$emit('start-new-game')"
      />

      <GameNoticeModal
        v-else-if="isRescueNoticeOpen"
        :message="rescueNoticeMessage"
        @close="closeRescueNotice"
      />

      <RankingPanel
        :error-message="leaderboardErrorMessage"
        :loading="leaderboardLoading"
        :ranking="leaderboard"
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
