<script setup>
import { computed, ref } from 'vue'
import { getItemIcon, getUiAsset } from '../utils/assetMap'

const props = defineProps({
  room: {
    type: Object,
    required: true,
  },
  roomItems: {
    type: Array,
    required: true,
  },
  roomItemsHighlighted: {
    type: Boolean,
    default: true,
  },
  actionLoading: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['take-item'])

const selectedItemId = ref(null)

const sceneItems = computed(() => props.roomItems.map((item) => ({
  ...item,
  icon: getItemIcon(item),
})))
const roomItemSlotFrame = computed(() => getUiAsset('roomItemSlot'))

const selectRoomItem = (item) => {
  selectedItemId.value = item.id
}

const clearSelectedRoomItem = () => {
  selectedItemId.value = null
}

const getDetailPlacement = (index) => (index % 4 >= 2 ? 'left' : 'right')
</script>

<template>
  <section
    class="game-scene"
    aria-label="当前房间物品展示区"
    @click="clearSelectedRoomItem"
  >
    <div
      v-if="roomItemsHighlighted && sceneItems.length"
      class="room-items-grid"
    >
      <div
        v-for="(item, index) in sceneItems"
        :key="item.id"
        class="room-item-cell"
      >
        <button
          class="room-item-slot"
          type="button"
          :aria-pressed="selectedItemId === item.id"
          :class="{ 'is-selected': selectedItemId === item.id }"
          @click.stop="selectRoomItem(item)"
        >
          <img
            v-if="roomItemSlotFrame"
            class="room-item-slot__frame"
            :src="roomItemSlotFrame"
            alt=""
            aria-hidden="true"
          >
          <img
            class="room-item-slot__icon"
            :src="item.icon"
            :alt="item.name"
          >
        </button>

        <article
          v-if="selectedItemId === item.id"
          class="room-item-detail"
          :class="`room-item-detail--${getDetailPlacement(index)}`"
          @click.stop
        >
          <button
            class="room-item-detail__close"
            type="button"
            aria-label="关闭物品详情"
            @click.stop="clearSelectedRoomItem"
          >
            ×
          </button>
          <h3 class="room-item-detail__title">
            {{ item.name }}
          </h3>
          <div class="room-item-detail__meta">
            类型 {{ item.type }} · 重量 {{ item.weight ?? 0 }} · 价值 {{ item.moneyValue ?? 0 }}
          </div>
          <div
            v-if="item.staminaEffect > 0 || item.maxWeightEffect > 0"
            class="room-item-detail__meta"
          >
            <span v-if="item.staminaEffect > 0">恢复体力 {{ item.staminaEffect }}</span>
            <span v-if="item.maxWeightEffect > 0">增加负重 {{ item.maxWeightEffect }}</span>
          </div>
          <p class="room-item-detail__desc">
            {{ item.description }}
          </p>
          <button
            class="room-item-detail__action"
            type="button"
            :disabled="actionLoading"
            @click.stop="$emit('take-item', item.id)"
          >
            拾取
          </button>
        </article>
      </div>
    </div>

    <div
      v-else-if="roomItemsHighlighted"
      class="room-items-empty"
    >
      当前房间没有可拾取物品。
    </div>
  </section>
</template>
