<script setup>
import { computed } from 'vue'
import oldMapImage from '../assets/images/map/old-map.png'
import {
  roomMapLabels,
  roomMapPositions,
} from '../constants/gameMap'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  currentRoomId: {
    type: String,
    default: '',
  },
})

defineEmits(['close'])

const currentRoomPosition = computed(() => roomMapPositions[props.currentRoomId] || null)
const currentRoomName = computed(() => roomMapLabels[props.currentRoomId] || props.currentRoomId || '未知房间')

const markerStyle = (roomId) => {
  const position = roomMapPositions[roomId]
  if (!position) return {}

  return {
    left: `${position.x}%`,
    top: `${position.y}%`,
  }
}

const currentMarkerStyle = computed(() => markerStyle(props.currentRoomId))
</script>

<template>
  <div
    v-if="open"
    class="game-map-modal-backdrop"
    role="presentation"
    @click.self="$emit('close')"
  >
    <section
      class="game-map-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="game-map-modal-title"
    >
      <header class="game-map-modal__header">
        <div>
          <p class="game-map-modal__eyebrow">
            OLD EXPLORER'S MAP
          </p>
          <h2 id="game-map-modal-title">
            残旧地图
          </h2>
        </div>
        <button
          type="button"
          aria-label="关闭残旧地图"
          @click="$emit('close')"
        >
          ×
        </button>
      </header>

      <div class="game-map-modal__body">
        <div class="game-map-frame">
          <img
            class="game-map-frame__image"
            :src="oldMapImage"
            alt="标有公开探索路线的残旧地图"
          >

          <span
            v-if="currentRoomPosition"
            class="game-map-current-marker"
            :style="currentMarkerStyle"
            aria-label="当前位置"
          >
            <span class="game-map-current-marker__dot" />
          </span>
        </div>

        <p class="game-map-modal__current-room">
          当前位置：<strong>{{ currentRoomName }}</strong>
        </p>
      </div>
    </section>
  </div>
</template>
