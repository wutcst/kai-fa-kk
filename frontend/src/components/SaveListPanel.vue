<script setup>
import { statusLabels } from '../mock/gameState'

defineProps({
  saves: {
    type: Array,
    required: true,
  },
})

defineEmits(['close', 'start-new'])
</script>

<template>
  <section
    class="save-list-panel"
    aria-labelledby="save-list-title"
    role="dialog"
    aria-modal="true"
  >
    <div class="save-list-panel__header">
      <div>
        <span>旧日卷宗</span>
        <h2 id="save-list-title">历史存档</h2>
        <p>选择一段旧日卷宗继续前行。当前仅展示 mock 存档，后续将在 Issue #12 接入真实存档接口。</p>
      </div>
      <button
        type="button"
        class="save-list-panel__close"
        aria-label="关闭历史存档"
        @click="$emit('close')"
      >
        ×
      </button>
    </div>

    <div
      v-if="saves.length"
      class="save-list"
    >
      <article
        v-for="save in saves"
        :key="save.saveId"
        class="save-card"
      >
        <div class="save-card__topline">
          <h3>{{ save.saveName }}</h3>
          <span
            class="save-status"
            :class="`save-status--${save.status.toLowerCase().replace('_', '-')}`"
          >
            {{ statusLabels[save.status] || save.status }}
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
            type="button"
            @click="$emit('start-new')"
          >
            读取存档
          </button>
        </div>
      </article>
    </div>

    <div
      v-else
      class="save-empty"
    >
      <span>暂无可读取存档</span>
      <p>你还没有保存过探险进度。可以开始一段新的秘窟探索。</p>
      <button
        type="button"
        @click="$emit('start-new')"
      >
        开始新游戏
      </button>
    </div>
  </section>
</template>
