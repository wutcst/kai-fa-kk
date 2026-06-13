<script setup>
import { computed, ref } from "vue";
import { getItemIcon } from "../utils/assetMap";

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  player: {
    type: Object,
    required: true
  },
  actionLoading: {
    type: Boolean,
    default: false
  }
});

defineEmits(["close", "use-item", "drop-item", "view-map"]);

const itemFilters = [
  { key: "ALL", label: "全部", types: [] },
  { key: "TREASURE", label: "宝物", types: ["TREASURE", "宝物"] },
  { key: "SUPPLY", label: "补给", types: ["SUPPLY", "补给"] },
  { key: "EQUIPMENT", label: "装备", types: ["EQUIPMENT", "装备"] },
  { key: "KEY", label: "关键物品", types: ["KEY", "关键物品"] }
];

const activeFilter = ref("ALL");

const normalizeItemType = type =>
  String(type || "")
    .trim()
    .toUpperCase();

const formatItemType = type => {
  const typeMap = {
    KEY: "关键物品",
    TREASURE: "宝物",
    SUPPLY: "补给",
    EQUIPMENT: "装备"
  };

  return typeMap[normalizeItemType(type)] || type || "未知";
};

const getItemValue = item =>
  Number(item?.value ?? item?.price ?? item?.worth ?? item?.moneyValue ?? 0);

const getNumberValue = value => Number(value || 0);

const isOldMap = item => item?.id === "old-map";

const hasDirectUseEffect = item =>
  getNumberValue(item?.staminaEffect) > 0 ||
  getNumberValue(item?.maxWeightEffect) > 0;

const canUseItem = item => {
  if (!item || isOldMap(item)) return false;

  const type = normalizeItemType(item.type);

  if (type === "SUPPLY") {
    return typeof item.usable === "boolean" ? item.usable : true;
  }

  if (type === "EQUIPMENT") {
    const usableByBackend =
      typeof item.usable === "boolean" ? item.usable : true;
    return usableByBackend && hasDirectUseEffect(item);
  }

  return false;
};

const getUseLabel = item => {
  if (normalizeItemType(item.type) === "EQUIPMENT") return "装备";
  return "使用";
};

const isPassiveKeyItem = item =>
  normalizeItemType(item.type) === "KEY" && !isOldMap(item);

const isPassiveEquipment = item =>
  normalizeItemType(item.type) === "EQUIPMENT" && !hasDirectUseEffect(item);

const getItemEffectText = item => {
  if (!item) {
    return "";
  }

  const type = normalizeItemType(item.type);
  const staminaEffect = getNumberValue(item.staminaEffect);
  const maxWeightEffect = getNumberValue(item.maxWeightEffect);
  const moneyValue = getItemValue(item);

  if (isOldMap(item)) {
    return "可查看秘窟地图，打开地图不会消耗。";
  }

  if (staminaEffect > 0) {
    return `恢复体力 ${staminaEffect}`;
  }

  if (maxWeightEffect > 0) {
    return `增加负重 ${maxWeightEffect}`;
  }

  if (item.id === "bronze-moon-token") {
    return `宝物价值：${moneyValue} 金币；持有可进入月纹密室。`;
  }

  if (item.id === "star-compass") {
    return `宝物价值：${moneyValue} 金币；持有可进入星纹侧殿。`;
  }

  if (moneyValue > 0) {
    return `宝物价值：${moneyValue} 金币，可出售或通关结算。`;
  }

  if (item.id === "iron-boots") {
    return "特殊装备：用于稳定通过松动石板，无需主动使用。";
  }

  if (type === "KEY") {
    return "关键物品：用于机关、路线或线索判断，不一定能直接使用。";
  }

  if (type === "EQUIPMENT") {
    return "装备物品：会在合适场景中发挥作用。";
  }

  return "特殊物品：请结合房间描述和日志判断用途。";
};

const filteredItems = computed(() => {
  const selectedFilter = itemFilters.find(
    filter => filter.key === activeFilter.value
  );
  if (!selectedFilter || selectedFilter.key === "ALL") {
    return props.items;
  }

  return props.items.filter(
    item =>
      selectedFilter.types.includes(item.type) ||
      selectedFilter.types.includes(normalizeItemType(item.type))
  );
});
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
              <div
                class="inventory-item-card__meta"
              >
                {{ formatItemType(item.type) }} · 重量 {{ item.weight ?? 0 }}
              </div>
            </div>
          </div>

          <p class="inventory-item-card__desc">
            {{ item.description }}
          </p>

          <div class="inventory-item-card__value">
            <span>{{ getItemEffectText(item) }}</span>
          </div>

          <div class="inventory-item-card__actions">
            <button
              v-if="isOldMap(item)"
              type="button"
              :disabled="actionLoading"
              @click="$emit('view-map')"
            >
              查看地图
            </button>
            <button
              v-else-if="canUseItem(item)"
              type="button"
              :disabled="actionLoading"
              @click="$emit('use-item', item.id)"
            >
              {{ getUseLabel(item) }}
            </button>
            <span
              v-else-if="isPassiveKeyItem(item)"
              class="inventory-item-card__passive-label"
            >关键物品</span>
            <span
              v-else-if="isPassiveEquipment(item)"
              class="inventory-item-card__passive-label"
            >无需主动使用</span>
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
