<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { DASHBOARD_BASE, HUD_POSITIONS, rectStyle } from '../constants/dashboardLayout'
import { getRoomBackground } from '../utils/assetMap'
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
})

defineEmits([
  'exit-game',
  'save-game',
  'restart-level',
  'abandon-adventure',
  'take-item',
  'use-item',
  'drop-item',
])

const areRoomItemsHighlighted = ref(false)
const dashboardScale = ref(1)
const isBackpackOpen = ref(false)
const showDebugHud = ref(false)

const currentRoom = computed(() => props.gameState?.currentRoom || null)
const currentLevel = computed(() => props.gameState?.currentLevel)
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

const closeBackpack = () => {
  isBackpackOpen.value = false
}

onMounted(() => {
  showDebugHud.value = new URLSearchParams(window.location.search).get('debugHud') === '1'
  updateDashboardScale()
  window.addEventListener('resize', updateDashboardScale)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateDashboardScale)
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
        :current-room="currentRoom"
        :password-prompt-open="false"
        :password-unlocked="gameState?.passwordUnlocked"
        :room-items-highlighted="areRoomItemsHighlighted"
        :status="gameState?.status"
        :style="rectStyle(HUD_POSITIONS.actionPanel)"
        @open-inventory="openBackpack"
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
