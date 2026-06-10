<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import dashboardReference from '../../docs/reference/game-dashboard-reference.png'
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
    required: true,
  },
})

defineEmits(['logout'])

const areRoomItemsHighlighted = ref(false)
const dashboardScale = ref(1)
const isBackpackOpen = ref(false)
const showDebugHud = ref(false)

const currentRoom = computed(() => props.gameState.currentRoom || props.gameState.room)
const inventoryItems = computed(() => (
  props.gameState.player?.inventory
  || props.gameState.inventory
  || []
))
const roomItems = computed(() => (
  currentRoom.value?.items
  || currentRoom.value?.visibleItems
  || []
))
const statusLabel = computed(() => (props.gameState.status === 'IN_PROGRESS'
  ? '探索中'
  : props.gameState.status))
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
        :chapter="gameState.chapter"
        :location="currentRoom.name"
        :objective="gameState.objective"
        :status="statusLabel"
        :style="rectStyle(HUD_POSITIONS.topbar)"
        :title="gameState.title"
        @logout="$emit('logout')"
      />

      <div
        class="dashboard-layer dashboard-layer--left-top"
        :style="rectStyle(HUD_POSITIONS.playerStatus)"
      >
        <PlayerStatus
          :current-level="gameState.currentLevel"
          :inventory-items="inventoryItems"
          :password-unlocked="gameState.passwordUnlocked"
          :player="gameState.player"
          :status="gameState.status"
          @open-inventory="openBackpack"
        />
      </div>

      <ActionPanel
        :current-room="currentRoom"
        :password-prompt-open="false"
        :password-unlocked="gameState.passwordUnlocked"
        :room-items-highlighted="areRoomItemsHighlighted"
        :status="gameState.status"
        :style="rectStyle(HUD_POSITIONS.actionPanel)"
        @open-inventory="openBackpack"
        @toggle-room-items="toggleRoomItems"
      />

      <GameScene
        :room="currentRoom"
        :room-items="roomItems"
        :room-items-highlighted="areRoomItemsHighlighted"
        :style="rectStyle(HUD_POSITIONS.roomItems)"
      />

      <BackpackModal
        v-if="isBackpackOpen"
        :items="inventoryItems"
        :player="gameState.player"
        @close="closeBackpack"
      />

      <RankingPanel
        :fallback-text="gameState.objective"
        :ranking="gameState.ranking || gameState.leaderboard || []"
        :style="rectStyle(HUD_POSITIONS.ranking)"
      />

      <GameLog
        :logs="gameState.logs"
        :message="gameState.message"
        :status="gameState.status"
        :style="rectStyle(HUD_POSITIONS.log)"
      />

      <RoomDescriptionBar :style="rectStyle(HUD_POSITIONS.narration)" />

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
