<script setup>
import { computed } from 'vue'
import { getUiAsset } from '../utils/assetMap'

const props = defineProps({
  currentLevel: {
    type: Number,
    default: 1,
  },
  passwordUnlocked: {
    type: Boolean,
    default: false,
  },
  player: {
    type: Object,
    required: true,
  },
  status: {
    type: String,
    default: 'IN_PROGRESS',
  },
  inventoryItems: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['open-inventory'])

const STATUS_SLOTS = {
  title: { x: 0, y: 28, w: 320, h: 28 },

  nameLabel: { x: 62, y: 79, w: 78, h: 22 },
  nameValue: { x: 151, y: 79, w: 130, h: 22 },

  levelLabel: { x: 62, y: 112, w: 78, h: 22 },
  levelValue: { x: 151, y: 112, w: 130, h: 22 },

  moneyLabel: { x: 62, y: 145, w: 78, h: 22 },
  moneyValue: { x: 151, y: 145, w: 130, h: 22 },

  passwordLabel: { x: 62, y: 178, w: 88, h: 22 },
  passwordValue: { x: 151, y: 178, w: 150, h: 22 },

  hpValue: { x: 215, y: 235, w: 64, h: 20 },
  hpFill: { x: 90, y: 242, w: 160, h: 8 },

  weightValue: { x: 210, y: 280, w: 64, h: 20 },
  weightFill: { x: 90, y: 288, w: 160, h: 8 },

  backpackLabel: { x: 35, y: 330, w: 250, h: 34 },
  backpackCount: { x: 250, y: 329, w: 34, h: 34 },
}

const TRACK_FILL_WIDTH = STATUS_SLOTS.hpFill.w
const toPercent = (value, maxValue) => {
  if (!maxValue) return 0
  return Math.min(100, Math.max(0, Math.round((value / maxValue) * 100)))
}
const slotStyle = (slot) => ({
  left: `${slot.x}px`,
  top: `${slot.y}px`,
  width: `${slot.w}px`,
  height: `${slot.h}px`,
})
const money = computed(() => props.player.money ?? props.player.gold ?? 0)
const staminaPercent = computed(() => toPercent(props.player.stamina, props.player.maxStamina))
const weightPercent = computed(() => toPercent(props.player.currentWeight, props.player.maxWeight))
const hpFillStyle = computed(() => ({
  ...slotStyle(STATUS_SLOTS.hpFill),
  width: `${Math.round((TRACK_FILL_WIDTH * staminaPercent.value) / 100)}px`,
}))
const weightFillStyle = computed(() => ({
  ...slotStyle(STATUS_SLOTS.weightFill),
  width: `${Math.round((TRACK_FILL_WIDTH * weightPercent.value) / 100)}px`,
}))
const statusFrame = computed(() => getUiAsset('playerStatusFrame'))
</script>

<template>
  <section
    class="game-hud player-status player-status-template"
    aria-labelledby="player-status-title"
  >
    <img
      v-if="statusFrame"
      class="player-status-template__frame"
      :src="statusFrame"
      alt=""
      aria-hidden="true"
    >

    <div
      id="player-status-title"
      class="player-status-template__slot player-status-template__title"
      :style="slotStyle(STATUS_SLOTS.title)"
    >
      玩家状态
    </div>

    <div
      class="player-status-template__slot player-status-template__label"
      :style="slotStyle(STATUS_SLOTS.nameLabel)"
    >
      玩家名
    </div>
    <div
      class="player-status-template__slot player-status-template__value"
      :style="slotStyle(STATUS_SLOTS.nameValue)"
    >
      {{ player.name || '寻宝者' }}
    </div>

    <div
      class="player-status-template__slot player-status-template__label"
      :style="slotStyle(STATUS_SLOTS.levelLabel)"
    >
      等级
    </div>
    <div
      class="player-status-template__slot player-status-template__value"
      :style="slotStyle(STATUS_SLOTS.levelValue)"
    >
      第 {{ currentLevel }} 关
    </div>

    <div
      class="player-status-template__slot player-status-template__label"
      :style="slotStyle(STATUS_SLOTS.moneyLabel)"
    >
      金币
    </div>
    <div
      class="player-status-template__slot player-status-template__value"
      :style="slotStyle(STATUS_SLOTS.moneyValue)"
    >
      金币 {{ money }}
    </div>

    <div
      class="player-status-template__slot player-status-template__label"
      :style="slotStyle(STATUS_SLOTS.passwordLabel)"
    >
      暗语状态
    </div>
    <div
      class="player-status-template__slot player-status-template__value"
      :style="slotStyle(STATUS_SLOTS.passwordValue)"
    >
      暗语 {{ passwordUnlocked ? '已解锁' : '未解锁' }}
    </div>

    <div
      class="player-status-template__slot player-status-template__value player-status-template__meter-value"
      :style="slotStyle(STATUS_SLOTS.hpValue)"
    >
      {{ player.stamina }} / {{ player.maxStamina }}
    </div>
    <div
      class="player-status-template__slot player-status-template__meter-fill player-status-template__meter-fill--hp"
      :style="hpFillStyle"
    />

    <div
      class="player-status-template__slot player-status-template__value player-status-template__meter-value"
      :style="slotStyle(STATUS_SLOTS.weightValue)"
    >
      {{ player.currentWeight }} / {{ player.maxWeight }}
    </div>
    <div
      class="player-status-template__slot player-status-template__meter-fill player-status-template__meter-fill--weight"
      :style="weightFillStyle"
    />

    <button
      class="player-status-template__slot player-status-template__backpack-entry"
      type="button"
      :style="slotStyle(STATUS_SLOTS.backpackLabel)"
      @click="$emit('open-inventory')"
    >
      背包
    </button>
    <span
      class="player-status-template__slot player-status-template__backpack-count"
      :style="slotStyle(STATUS_SLOTS.backpackCount)"
    >
      {{ inventoryItems.length }}
    </span>
  </section>
</template>
