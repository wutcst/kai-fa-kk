<script setup>
import { computed } from 'vue'
import { getUiAsset } from '../utils/assetMap'

const props = defineProps({
  logs: {
    type: Array,
    required: true,
  },
  message: {
    type: String,
    default: '',
  },
  status: {
    type: String,
    required: true,
  },
})

const LOG_SLOTS = {
  title: { x: -20, y: 38, w: 360, h: 32 },
  currentMessage: { x: 34, y: 96, w: 252, h: 58 },
  logList: { x: 35, y: 172, w: 260, h: 415 },
  saveButton: { x: 35, y: 489, w: 72, h: 34 },
  restartButton: { x: 125, y: 489, w: 72, h: 34 },
  quitButton: { x: 213, y: 489, w: 72, h: 34 },
}

const DEFAULT_HINT = '石门前的蓝光忽明忽暗，暗语机关似乎正在等待回应。'

const slotStyle = (slot) => ({
  left: `${slot.x}px`,
  top: `${slot.y}px`,
  width: `${slot.w}px`,
  height: `${slot.h}px`,
})

const normalizedLogs = computed(() => props.logs.map((log, index) => {
  if (typeof log === 'string') {
    return {
      id: index,
      time: `#${index + 1}`,
      message: log,
      type: 'normal',
    }
  }

  return {
    id: log.id ?? index,
    time: log.time ?? `#${index + 1}`,
    message: log.message ?? log.content,
    type: log.type || 'normal',
  }
}))
const visibleLogs = computed(() => normalizedLogs.value.slice(-6))
const currentHint = computed(() => props.message || DEFAULT_HINT)
const logFrame = computed(() => getUiAsset('logPanelFrame'))
</script>

<template>
  <section
    class="game-log-template"
    aria-labelledby="game-log-title"
  >
    <img
      v-if="logFrame"
      class="game-log-template__frame"
      :src="logFrame"
      alt=""
      aria-hidden="true"
    >

    <div
      id="game-log-title"
      class="game-log-template__title"
      :style="slotStyle(LOG_SLOTS.title)"
    >
      游戏日志
    </div>

    <div
      class="game-log-template__current"
      :style="slotStyle(LOG_SLOTS.currentMessage)"
    >
      {{ currentHint }}
    </div>

    <div
      class="game-log-template__list"
      :style="slotStyle(LOG_SLOTS.logList)"
    >
      <div
        v-for="log in visibleLogs"
        :key="log.id"
        class="game-log-template__entry"
      >
        <time class="game-log-template__time">
          {{ log.time }}
        </time>
        <span class="game-log-template__message">
          {{ log.message }}
        </span>
      </div>
    </div>

    <button
      class="game-log-template__button"
      type="button"
      :style="slotStyle(LOG_SLOTS.saveButton)"
    >
      保存
    </button>
    <button
      class="game-log-template__button"
      type="button"
      :style="slotStyle(LOG_SLOTS.restartButton)"
    >
      重开
    </button>
    <button
      class="game-log-template__button game-log-template__button--quit"
      type="button"
      :style="slotStyle(LOG_SLOTS.quitButton)"
    >
      放弃
    </button>
  </section>
</template>
