<script setup>
import { computed } from 'vue'
import { statusLabels } from '../mock/gameState'

const props = defineProps({
  save: {
    type: Object,
    required: true,
  },
  label: {
    type: String,
    default: '',
  },
  actionLabel: {
    type: String,
    default: '读取存档',
  },
  showAction: {
    type: Boolean,
    default: false,
  },
  variant: {
    type: String,
    default: 'default',
  },
})

defineEmits(['load'])

const statusText = computed(() => statusLabels[props.save.status] || props.save.status)
const statusClass = computed(() => String(props.save.status || '').toLowerCase().replace('_', '-'))
</script>

<template>
  <article
    class="save-card"
    :class="`save-card--${variant}`"
  >
    <span
      v-if="label"
      class="save-card__label"
    >
      {{ label }}
    </span>

    <div class="save-card__topline">
      <h3>{{ save.saveName }}</h3>
      <span
        class="save-status"
        :class="`save-status--${statusClass}`"
      >
        {{ statusText }}
      </span>
    </div>

    <p class="save-card__route">
      第 {{ save.currentLevel }} 关 · {{ save.currentRoomName }}
    </p>

    <p class="save-card__summary">
      金币 {{ save.money }} · 体力 {{ save.stamina }} · 负重 {{ save.currentWeight }} / {{ save.maxWeight }}
    </p>

    <div class="save-card__footer">
      <time>{{ save.updatedAt }}</time>
      <button
        v-if="showAction"
        type="button"
        @click="$emit('load', save)"
      >
        {{ actionLabel }}
      </button>
    </div>
  </article>
</template>
