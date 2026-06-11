<script setup>
import { computed } from 'vue'
import { getItemIcon } from '../utils/assetMap'

const props = defineProps({
  catalog: {
    type: Array,
    default: () => [],
  },
  player: {
    type: Object,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  actionLoading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

defineEmits(['buy-item', 'sell-item', 'continue-adventure'])

const treasures = computed(() => (
  (props.player.inventory || []).filter((item) => item.type === 'TREASURE')
))

const itemIcon = (item) => getItemIcon({
  ...item,
  id: item.id || item.itemId,
})

const cannotAfford = (item) => (props.player.money ?? 0) < item.price
const exceedsWeight = (item) => (
  (props.player.currentWeight ?? 0) + (item.weight ?? 0) > (props.player.maxWeight ?? 0)
)

const buyTitle = (item) => {
  if (cannotAfford(item)) return '金币不足'
  if (exceedsWeight(item)) return '背包负重不足'
  return `购买 ${item.name}`
}
</script>

<template>
  <section
    class="shop-panel"
    aria-labelledby="shop-panel-title"
  >
    <header class="shop-panel__header">
      <div>
        <p class="shop-panel__eyebrow">
          商店整备
        </p>
        <h2 id="shop-panel-title">
          古商人的货箱
        </h2>
      </div>
      <div class="shop-panel__resources">
        <span>金币 <strong>{{ player.money ?? 0 }}</strong></span>
        <span>负重 <strong>{{ player.currentWeight ?? 0 }} / {{ player.maxWeight ?? 0 }}</strong></span>
      </div>
    </header>

    <p
      v-if="errorMessage"
      class="shop-panel__state shop-panel__state--error"
      role="alert"
    >
      商店暂时无法加载
    </p>

    <div class="shop-panel__columns">
      <section class="shop-panel__section">
        <h3>购买物资</h3>

        <p
          v-if="loading"
          class="shop-panel__state"
        >
          商店商品加载中...
        </p>
        <p
          v-else-if="!catalog.length && !errorMessage"
          class="shop-panel__state"
        >
          暂无可购买商品
        </p>
        <div
          v-else-if="!errorMessage"
          class="shop-panel__list"
        >
          <article
            v-for="item in catalog"
            :key="item.itemId"
            class="shop-item"
          >
            <img
              :src="itemIcon(item)"
              :alt="item.name"
            >
            <div class="shop-item__body">
              <div class="shop-item__heading">
                <h4>{{ item.name }}</h4>
                <strong>{{ item.price }} 金币</strong>
              </div>
              <p>{{ item.description }}</p>
              <div class="shop-item__meta">
                <span>{{ item.type }}</span>
                <span>重量 {{ item.weight }}</span>
                <span v-if="item.staminaEffect > 0">恢复 {{ item.staminaEffect }} 体力</span>
                <span v-if="item.maxWeightEffect > 0">负重上限 +{{ item.maxWeightEffect }}</span>
              </div>
            </div>
            <button
              type="button"
              :disabled="actionLoading || cannotAfford(item) || exceedsWeight(item)"
              :title="buyTitle(item)"
              @click="$emit('buy-item', item.itemId)"
            >
              购买
            </button>
          </article>
        </div>
      </section>

      <section class="shop-panel__section">
        <h3>出售宝物</h3>
        <p
          v-if="!treasures.length"
          class="shop-panel__state"
        >
          背包中暂无可出售宝物
        </p>
        <div
          v-else
          class="shop-panel__list"
        >
          <article
            v-for="item in treasures"
            :key="item.id"
            class="shop-item"
          >
            <img
              :src="itemIcon(item)"
              :alt="item.name"
            >
            <div class="shop-item__body">
              <div class="shop-item__heading">
                <h4>{{ item.name }}</h4>
                <strong>{{ item.moneyValue }} 金币</strong>
              </div>
              <p>{{ item.description }}</p>
              <div class="shop-item__meta">
                <span>宝物</span>
                <span>重量 {{ item.weight }}</span>
              </div>
            </div>
            <button
              type="button"
              :disabled="actionLoading"
              @click="$emit('sell-item', item.id)"
            >
              出售
            </button>
          </article>
        </div>
      </section>
    </div>

    <footer class="shop-panel__footer">
      <button
        type="button"
        :disabled="actionLoading"
        @click="$emit('continue-adventure')"
      >
        {{ actionLoading ? '处理中...' : '继续探险' }}
      </button>
    </footer>
  </section>
</template>
