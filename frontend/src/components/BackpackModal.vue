<script setup>
import { computed, ref } from 'vue'
import { getItemIcon } from '../utils/assetMap'

const props = defineProps({
  items: {
    type: Array,
    required: true,
  },
  player: {
    type: Object,
    required: true,
  },
  actionLoading: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['close', 'use-item', 'drop-item'])

const itemFilters = [
  { key: 'ALL', label: '全部', types: [] },
  { key: 'TREASURE', label: '宝物', types: ['TREASURE', '宝物'] },
  { key: 'SUPPLY', label: '补给', types: ['SUPPLY', '补给'] },
  { key: 'EQUIPMENT', label: '装备', types: ['EQUIPMENT', '装备'] },
  { key: 'KEY', label: '关键物品', types: ['KEY', '关键物品'] },
]

const activeFilter = ref('ALL')

const normalizeItemType = (type) => String(type || '').trim().toUpperCase()

const formatItemType = (type) => {
  const typeMap = {
    KEY: '关键物品',
    TREASURE: '宝物',
    SUPPLY: '补给',
    EQUIPMENT: '装备',
  }

  return typeMap[normalizeItemType(type)] || type || '未知'
}

const getItemValue = (item) => (
  item.value
  ?? item.price
  ?? item.worth
  ?? item.moneyValue
  ?? 0
)

const filteredItems = computed(() => {
  const selectedFilter = itemFilters.find((filter) => filter.key === activeFilter.value)
  if (!selectedFilter || selectedFilter.key === 'ALL') {
    return props.items
  }

  return props.items.filter((item) => (
    selectedFilter.types.includes(item.type)
    || selectedFilter.types.includes(normalizeItemType(item.type))
  ))
})
</script>

<template>
  <div
    class="backpack-modal"
    role="presentation"
    @click.self="$emit('close')"
  >
    <section
      class="backpack-modal__panel inventory-modal"
      aria-labelledby="backpack-title"
      role="dialog"
      aria-modal="true"
    >
      <div class="backpack-modal__header">
        <div>
          <h2
            id="backpack-title"
            class="inventory-modal__title"
          >
            背包物品
          </h2>
          <p class="inventory-modal__weight">
            负重 {{ player.currentWeight }} / {{ player.maxWeight }}
          </p>
        </div>
        <button
          type="button"
          aria-label="关闭背包"
          @click="$emit('close')"
        >
          ×
        </button>
      </div>

      <div
        class="inventory-filter"
        aria-label="背包物品分类"
      >
        <button
          v-for="filter in itemFilters"
          :key="filter.key"
          class="inventory-filter__button"
          :class="{ 'is-active': activeFilter === filter.key }"
          type="button"
          @click="activeFilter = filter.key"
        >
          {{ filter.label }}
        </button>
      </div>

      <div
        v-if="filteredItems.length"
        class="inventory-items-grid"
      >
        <article
          v-for="item in filteredItems"
          :key="item.id"
          class="inventory-item-card"
        >
          <div class="inventory-item-card__top">
            <div class="inventory-item-card__icon-wrap">
              <img
                class="inventory-item-card__icon"
                :src="getItemIcon(item)"
                :alt="item.name"
              >
            </div>
            <div class="inventory-item-card__info">
              <h3 class="inventory-item-card__name">
                {{ item.name }}
              </h3>
              <div class="inventory-item-card__meta">
                {{ formatItemType(item.type) }} · 重量 {{ item.weight ?? 0 }}
              </div>
            </div>
          </div>

          <p class="inventory-item-card__desc">
            {{ item.description }}
          </p>

          <div class="inventory-item-card__value">
            <span v-if="item.staminaEffect > 0">恢复体力 <strong>{{ item.staminaEffect }}</strong></span>
            <span v-if="item.maxWeightEffect > 0">增加负重 <strong>{{ item.maxWeightEffect }}</strong></span>
            <span v-if="getItemValue(item) > 0">价值 <strong>{{ getItemValue(item) }}</strong></span>
            <span v-if="!item.staminaEffect && !item.maxWeightEffect && !getItemValue(item)">无直接效果</span>
          </div>

          <div class="inventory-item-card__actions">
            <button
              type="button"
              :disabled="actionLoading"
              @click="$emit('use-item', item.id)"
            >
              使用
            </button>
            <button
              type="button"
              :disabled="actionLoading"
              @click="$emit('drop-item', item.id)"
            >
              丢弃
            </button>
          </div>
        </article>
      </div>

      <p
        v-else
        class="hud-empty inventory-modal__empty"
      >
        {{ items.length ? '当前分类下暂无物品。' : '背包为空。' }}
      </p>
    </section>
  </div>
</template>
