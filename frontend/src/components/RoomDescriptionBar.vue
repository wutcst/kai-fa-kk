<script setup>
import { computed } from 'vue'
import { getUiAsset } from '../utils/assetMap'

const ROOM_DESCRIPTION = '潮湿石墙上刻着断裂的古老文字，蓝色传送门在大厅尽头缓慢旋转。两侧火把摇曳，照亮一扇需要暗语开启的厚重石门。'

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
      {{ ROOM_DESCRIPTION }}
    </p>
  </section>
</template>
