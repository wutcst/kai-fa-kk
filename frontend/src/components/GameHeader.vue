<script setup>
import { computed } from 'vue'
import { getUiAsset } from '../utils/assetMap'

const props = defineProps({
  chapter: {
    type: String,
    default: '第一章：石门回声',
  },
  location: {
    type: String,
    default: '石门大厅',
  },
  title: {
    type: String,
    default: '',
  },
  status: {
    type: String,
    default: '',
  },
  objective: {
    type: String,
    default: '',
  },
})

defineEmits(['logout'])

const TOPBAR_SLOTS = {
  chapter: { x: 55, y: 9, w: 220, h: 68 },
  title: { x: 50, y: 8, w: 820, h: 68 },
  back: { x: 747, y: 28, w: 92, h: 38 },
}

const slotStyle = (slot) => ({
  left: `${slot.x}px`,
  top: `${slot.y}px`,
  width: `${slot.w}px`,
  height: `${slot.h}px`,
})

const chapterName = computed(() => props.chapter || '第一章：石门回声')
const roomName = computed(() => props.location || '石门大厅')
const topbarFrame = computed(() => getUiAsset('topbarFrame'))
</script>

<template>
  <header class="game-topbar-template">
    <img
      v-if="topbarFrame"
      class="game-topbar-template__frame"
      :src="topbarFrame"
      alt=""
      aria-hidden="true"
    >

    <div
      class="game-topbar-template__chapter"
      :style="slotStyle(TOPBAR_SLOTS.chapter)"
    >
      {{ chapterName }}
    </div>

    <div
      class="game-topbar-template__title"
      :style="slotStyle(TOPBAR_SLOTS.title)"
    >
      {{ roomName }}
    </div>

    <button
      class="game-topbar-template__back"
      type="button"
      :style="slotStyle(TOPBAR_SLOTS.back)"
      @click="$emit('logout')"
    >
      返回入口
    </button>
  </header>
</template>
