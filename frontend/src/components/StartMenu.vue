<script setup>
import { computed, ref } from 'vue'
import SaveListPanel from './SaveListPanel.vue'
import { statusLabels } from '../mock/gameState'

const props = defineProps({
  saves: {
    type: Array,
    required: true,
  },
  username: {
    type: String,
    default: '寻宝者',
  },
})

defineEmits(['logout', 'start-new'])

const isArchiveOpen = ref(false)

const latestSave = computed(() => props.saves[0] || null)

const openArchive = () => {
  isArchiveOpen.value = true
}

const closeArchive = () => {
  isArchiveOpen.value = false
}
</script>

<template>
  <section
    class="start-menu"
    aria-labelledby="start-menu-title"
  >
    <div class="start-menu__stage">
      <section class="start-menu__panel">
        <span class="start-menu__eyebrow">登录成功</span>
        <h1 id="start-menu-title">探险入口</h1>
        <p>欢迎回来，{{ username }}。开启新的探险，或沿着旧日卷宗继续前行。</p>

        <div class="start-menu__buttons">
          <button
            type="button"
            class="start-menu-button start-menu-button--primary"
            @click="$emit('start-new')"
          >
            开始新游戏
          </button>
          <button
            v-if="latestSave"
            type="button"
            class="start-menu-button start-menu-button--continue"
            @click="$emit('start-new')"
          >
            继续最近存档
          </button>
          <button
            type="button"
            class="start-menu-button start-menu-button--archive"
            @click="openArchive"
          >
            查看全部存档
          </button>
          <button
            type="button"
            class="start-menu-button start-menu-button--ghost"
            @click="$emit('logout')"
          >
            退出登录
          </button>
        </div>

        <article
          v-if="latestSave"
          class="recent-save-card"
        >
          <span>最近卷宗</span>
          <div class="recent-save-card__title-row">
            <h2>{{ latestSave.saveName }}</h2>
            <strong>{{ statusLabels[latestSave.status] || latestSave.status }}</strong>
          </div>
          <p>第 {{ latestSave.currentLevel }} 关 · {{ latestSave.currentRoomName }}</p>
          <p class="recent-save-card__summary">
            金币 {{ latestSave.money }} · 体力 {{ latestSave.stamina }} · 负重
            {{ latestSave.currentWeight }} / {{ latestSave.maxWeight }}
          </p>
          <time>{{ latestSave.updatedAt }}</time>
        </article>
      </section>
    </div>

    <div
      v-if="isArchiveOpen"
      class="start-menu-modal"
      role="presentation"
      @click.self="closeArchive"
    >
      <SaveListPanel
        :saves="saves"
        @close="closeArchive"
        @start-new="$emit('start-new')"
      />
    </div>
  </section>
</template>
