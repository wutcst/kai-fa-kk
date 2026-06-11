<script setup>
import { computed } from 'vue'
import { getUiAsset } from '../utils/assetMap'

const props = defineProps({
  description: {
    type: String,
    default: '',
  },
})

const NARRATION_SLOTS = {
  text: { x: 195, y: 20, w: 540, h: 60 },
}

const slotStyle = (slot) => ({
  left: `${slot.x}px`,
  top: `${slot.y}px`,
  width: `${slot.w}px`,
  height: `${slot.h}px`,
})

const narrationFrame = computed(() => getUiAsset('narrationBarFrame'))
const roomDescription = computed(() => props.description || '暂无房间信息')
</script>

<template>
  <section
    class="room-narration-template"
    aria-label="当前房间描述"
  >
    <img
      v-if="narrationFrame"
      class="room-narration-template__frame"
      :src="narrationFrame"
      alt=""
      aria-hidden="true"
    >

    <p
      class="room-narration-template__text"
      :style="slotStyle(NARRATION_SLOTS.text)"
    >
      {{ roomDescription }}
    </p>
  </section>
</template>
