<script setup>
import { getItemIcon, getUiAsset } from '../utils/assetMap'

defineProps({
  items: {
    type: Array,
    required: true,
  },
})

defineEmits(['open-inventory'])
</script>

<template>
  <section
    class="game-hud inventory-panel"
    aria-labelledby="inventory-title"
  >
    <button
      class="inventory-summary"
      type="button"
      @click="$emit('open-inventory')"
    >
      <span class="backpack-entry__icon-wrap">
        <img
          :src="getUiAsset('backpackEntryIcon')"
          alt="背包"
        >
      </span>
      <span
        id="inventory-title"
        class="backpack-entry__label"
      >
        背包
      </span>
      <strong class="backpack-entry__badge">{{ items.length }}</strong>
    </button>
    <p
      v-if="!items.length"
      class="hud-empty"
    >
      背包空空，秘窟还在等待你的发现。
    </p>
    <div
      v-else
      class="inventory-preview-icons"
      aria-hidden="true"
    >
      <span
        v-for="item in items.slice(0, 4)"
        :key="item.id"
        class="inventory-preview-icon"
      >
        <img
          :src="getItemIcon(item)"
          :alt="item.name"
        >
      </span>
    </div>
  </section>
</template>
