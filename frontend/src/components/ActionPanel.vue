<script setup>
import { computed } from 'vue'
import { getUiAsset } from '../utils/assetMap'

const props = defineProps({
  currentRoom: {
    type: Object,
    required: true,
  },
  passwordUnlocked: {
    type: Boolean,
    default: false,
  },
  status: {
    type: String,
    required: true,
  },
  roomItemsHighlighted: {
    type: Boolean,
    default: false,
  },
  passwordPromptOpen: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['toggle-room-items', 'open-inventory'])

const ACTION_SLOTS = {
  title: { x: 0, y: 35, w: 320, h: 30 },

  row1: { x: 45, y: 90, w: 240, h: 30 },
  row2: { x: 45, y: 143, w: 240, h: 30 },
  row3: { x: 45, y: 187, w: 240, h: 30 },
  row4: { x: 45, y: 238, w: 240, h: 30 },
  row5: { x: 45, y: 290, w: 240, h: 30 },

  label1: { x: 58, y: 82, w: 150, h: 42 },
  label2: { x: 58, y: 133, w: 150, h: 42 },
  label3: { x: 58, y: 182, w: 150, h: 42 },
  label4: { x: 58, y: 231, w: 150, h: 42 },
  label5: { x: 58, y: 280, w: 150, h: 42 },

  key1: { x: 241, y: 88, w: 34, h: 32 },
  key2: { x: 241, y: 135, w: 34, h: 32 },
  key3: { x: 241, y: 185, w: 34, h: 32 },
  key4: { x: 241, y: 235, w: 34, h: 32 },
  key5: { x: 241, y: 285, w: 34, h: 32 },

  north: { x: 132, y: 345, w: 52, h: 52 },
  west: { x: 79, y: 382, w: 52, h: 52 },
  south: { x: 132, y: 420, w: 52, h: 52 },
  east: { x: 185, y: 382, w: 52, h: 52 },
}

const slotStyle = (slot) => ({
  left: `${slot.x}px`,
  top: `${slot.y}px`,
  width: `${slot.w}px`,
  height: `${slot.h}px`,
})

const actionFrame = computed(() => getUiAsset('actionPanelFrame'))
const isPlaying = computed(() => props.status === 'IN_PROGRESS' || props.status === '探索中')
const exitMap = computed(() => new Map(
  (props.currentRoom.exits || []).map((exit) => [exit.direction, exit]),
))
const roomItems = computed(() => props.currentRoom.items || props.currentRoom.visibleItems || [])
const canInputPassword = computed(() => (
  isPlaying.value
  && props.currentRoom.requiresPassword
  && !props.passwordUnlocked
))

const actionRows = computed(() => [
  {
    id: 'return',
    label: '返回上一房间',
    key: 'R',
    rowSlot: ACTION_SLOTS.row1,
    labelSlot: ACTION_SLOTS.label1,
    keySlot: ACTION_SLOTS.key1,
    disabled: !isPlaying.value,
  },
  {
    id: 'home',
    label: '回到初始房间',
    key: 'H',
    rowSlot: ACTION_SLOTS.row2,
    labelSlot: ACTION_SLOTS.label2,
    keySlot: ACTION_SLOTS.key2,
    disabled: !isPlaying.value,
  },
  {
    id: 'items',
    label: '查看房间物品',
    key: 'Q',
    rowSlot: ACTION_SLOTS.row3,
    labelSlot: ACTION_SLOTS.label3,
    keySlot: ACTION_SLOTS.key3,
    disabled: !isPlaying.value || !roomItems.value.length,
    active: props.roomItemsHighlighted,
    onClick: () => emit('toggle-room-items'),
  },
  {
    id: 'bag',
    label: '打开背包',
    key: 'E',
    rowSlot: ACTION_SLOTS.row4,
    labelSlot: ACTION_SLOTS.label4,
    keySlot: ACTION_SLOTS.key4,
    disabled: false,
    onClick: () => emit('open-inventory'),
  },
  {
    id: 'password',
    label: props.passwordUnlocked ? '暗语已解锁' : '输入暗语',
    key: 'P',
    rowSlot: ACTION_SLOTS.row5,
    labelSlot: ACTION_SLOTS.label5,
    keySlot: ACTION_SLOTS.key5,
    disabled: !canInputPassword.value,
    active: props.passwordPromptOpen,
    available: canInputPassword.value && !props.passwordPromptOpen,
  },
])

const directions = [
  { direction: 'north', label: '北', slot: ACTION_SLOTS.north },
  { direction: 'west', label: '西', slot: ACTION_SLOTS.west },
  { direction: 'south', label: '南', slot: ACTION_SLOTS.south },
  { direction: 'east', label: '东', slot: ACTION_SLOTS.east },
]
</script>

<template>
  <section
    class="action-panel-template"
    aria-label="房间操作"
  >
    <img
      v-if="actionFrame"
      class="action-panel-template__frame"
      :src="actionFrame"
      alt=""
      aria-hidden="true"
    >

    <div
      class="action-panel-template__title"
      :style="slotStyle(ACTION_SLOTS.title)"
    >
      行动
    </div>

    <template
      v-for="action in actionRows"
      :key="action.id"
    >
      <button
        class="action-panel-template__row"
        type="button"
        :aria-label="action.label"
        :class="{
          'action-panel-template__row--active': action.active,
          'action-panel-template__row--available': action.available,
          'action-panel-template__row--disabled': action.disabled,
        }"
        :disabled="action.disabled"
        :style="slotStyle(action.rowSlot)"
        @click="action.onClick?.()"
      />
      <span
        class="action-panel-template__label"
        :class="{
          'action-panel-template__label--active': action.active,
          'action-panel-template__label--disabled': action.disabled,
        }"
        :style="slotStyle(action.labelSlot)"
      >
        {{ action.label }}
      </span>
      <span
        class="action-panel-template__key"
        :class="{
          'action-panel-template__key--active': action.active,
          'action-panel-template__key--disabled': action.disabled,
        }"
        :style="slotStyle(action.keySlot)"
      >
        {{ action.key }}
      </span>
    </template>

    <button
      v-for="direction in directions"
      :key="direction.direction"
      class="action-panel-template__dir"
      type="button"
      :class="{
        'action-panel-template__dir--disabled': !isPlaying || !exitMap.has(direction.direction),
      }"
      :disabled="!isPlaying || !exitMap.has(direction.direction)"
      :style="slotStyle(direction.slot)"
    >
      {{ direction.label }}
    </button>
  </section>
</template>
