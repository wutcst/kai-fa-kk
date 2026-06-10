<script setup>
import { computed } from 'vue'
import { getUiAsset } from '../utils/assetMap'

const props = defineProps({
  ranking: {
    type: Array,
    default: () => [],
  },
  fallbackText: {
    type: String,
    default: '探索当前房间，管理资源，寻找暗语线索。后续排行榜将在后续阶段接入。',
  },
})

const RANKING_SLOTS = {
  title: { x: -18, y: 40, w: 360, h: 30 },

  row1: { x: 32, y: 54, w: 296, h: 46 },
  row2: { x: 32, y: 117, w: 296, h: 46 },
  row3: { x: 32, y: 170, w: 296, h: 46 },

  badge1: { x: 41, y: 102, w: 28, h: 28 },
  badge2: { x: 41, y: 159, w: 28, h: 28 },
  badge3: { x: 41, y: 217, w: 28, h: 28 },

  name1: { x: 100, y: 94, w: 150, h: 46 },
  name2: { x: 100, y: 150, w: 150, h: 46 },
  name3: { x: 100, y: 208, w: 150, h: 46 },

  score1: { x: 210, y: 94, w: 58, h: 46 },
  score2: { x: 210, y: 150, w: 58, h: 46 },
  score3: { x: 210, y: 208, w: 58, h: 46 },

  fallbackTitle: { x: 0, y: 76, w: 360, h: 26 },
  fallbackText: { x: 48, y: 110, w: 264, h: 88 },
}

const slotStyle = (slot) => ({
  left: `${slot.x}px`,
  top: `${slot.y}px`,
  width: `${slot.w}px`,
  height: `${slot.h}px`,
})

const rankingFrame = computed(() => getUiAsset('rankingPanelFrame'))
const hasRankingData = computed(() => props.ranking.length > 0)
const visibleRanking = computed(() => props.ranking.slice(0, 3).map((entry, index) => ({
  rank: entry.rank ?? index + 1,
  playerName: entry.playerName || entry.name || '寻宝者',
  score: entry.score ?? entry.finalScore ?? 0,
  current: Boolean(entry.current),
  rowSlot: RANKING_SLOTS[`row${index + 1}`],
  badgeSlot: RANKING_SLOTS[`badge${index + 1}`],
  nameSlot: RANKING_SLOTS[`name${index + 1}`],
  scoreSlot: RANKING_SLOTS[`score${index + 1}`],
})))
</script>

<template>
  <section
    class="ranking-panel-template"
    aria-labelledby="ranking-panel-title"
  >
    <img
      v-if="rankingFrame"
      class="ranking-panel-template__frame"
      :src="rankingFrame"
      alt=""
      aria-hidden="true"
    >

    <template v-if="hasRankingData">
      <div
        id="ranking-panel-title"
        class="ranking-panel-template__title"
        :style="slotStyle(RANKING_SLOTS.title)"
      >
        探险排行
      </div>

      <template
        v-for="entry in visibleRanking"
        :key="entry.rank"
      >
        <div
          class="ranking-panel-template__row"
          :class="{ 'ranking-panel-template__row--current': entry.current }"
          :style="slotStyle(entry.rowSlot)"
        />
        <div
          class="ranking-panel-template__rank"
          :class="{
            'ranking-panel-template__rank--first': entry.rank === 1,
            'ranking-panel-template__rank--current': entry.current,
          }"
          :style="slotStyle(entry.badgeSlot)"
        >
          {{ entry.rank }}
        </div>
        <div
          class="ranking-panel-template__name"
          :class="{ 'ranking-panel-template__name--current': entry.current }"
          :style="slotStyle(entry.nameSlot)"
        >
          {{ entry.playerName }}
        </div>
        <div
          class="ranking-panel-template__score"
          :class="{ 'ranking-panel-template__score--current': entry.current }"
          :style="slotStyle(entry.scoreSlot)"
        >
          {{ entry.score }}
        </div>
      </template>
    </template>

    <template v-else>
      <div
        id="ranking-panel-title"
        class="ranking-panel-template__title"
        :style="slotStyle(RANKING_SLOTS.fallbackTitle)"
      >
        当前目标
      </div>
      <p
        class="ranking-panel-template__fallback-text"
        :style="slotStyle(RANKING_SLOTS.fallbackText)"
      >
        {{ fallbackText }}
      </p>
    </template>
  </section>
</template>
