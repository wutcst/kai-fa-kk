<script setup>
import { computed } from 'vue'
import { getModalBackground } from '../utils/assetMap'

const props = defineProps({
  status: {
    type: String,
    required: true,
  },
  message: {
    type: String,
    default: '',
  },
  finalScore: {
    type: Number,
    default: 0,
  },
  highScore: {
    type: Number,
    default: 0,
  },
  player: {
    type: Object,
    default: () => ({}),
  },
  currentLevel: {
    type: Number,
    default: 1,
  },
  roomName: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['return-start-menu', 'start-new-game', 'restart-level'])

const isWon = computed(() => props.status === 'WON')
const title = computed(() => (isWon.value ? '探险成功' : '探险失败'))
const modalBackground = computed(() => getModalBackground(isWon.value ? 'win' : 'failed'))
const panelStyle = computed(() => (
  modalBackground.value
    ? { '--result-modal-background': `url(${modalBackground.value})` }
    : {}
))
</script>

<template>
  <div
    class="game-state-modal"
    role="presentation"
  >
    <section
      class="game-result-modal"
      :class="isWon ? 'game-result-modal--won' : 'game-result-modal--failed'"
      :style="panelStyle"
      role="dialog"
      aria-modal="true"
      aria-labelledby="game-result-title"
    >
      <p class="game-result-modal__eyebrow">
        {{ isWon ? 'ADVENTURE COMPLETE' : 'ADVENTURE ENDED' }}
      </p>
      <h2 id="game-result-title">
        {{ title }}
      </h2>
      <p class="game-result-modal__message">
        {{ message || (isWon ? '你已完成本次秘窟探险。' : '本次探险已经结束。') }}
      </p>

      <dl
        v-if="isWon"
        class="game-result-modal__stats"
      >
        <div>
          <dt>本局得分</dt>
          <dd>{{ finalScore }}</dd>
        </div>
        <div>
          <dt>历史最高分</dt>
          <dd>{{ highScore }}</dd>
        </div>
        <div>
          <dt>当前金币</dt>
          <dd>{{ player?.money ?? 0 }}</dd>
        </div>
      </dl>

      <dl
        v-else
        class="game-result-modal__stats game-result-modal__stats--failed"
      >
        <div>
          <dt>当前关卡</dt>
          <dd>第 {{ currentLevel }} 关</dd>
        </div>
        <div>
          <dt>当前房间</dt>
          <dd>{{ roomName || '未知房间' }}</dd>
        </div>
      </dl>

      <div class="game-result-modal__actions">
        <button
          type="button"
          :disabled="loading"
          @click="$emit('return-start-menu')"
        >
          返回开始菜单
        </button>
        <button
          v-if="isWon"
          class="game-result-modal__primary"
          type="button"
          :disabled="loading"
          @click="$emit('start-new-game')"
        >
          {{ loading ? '正在开始...' : '再来一局' }}
        </button>
        <button
          v-else
          class="game-result-modal__primary"
          type="button"
          :disabled="loading"
          @click="$emit('restart-level')"
        >
          {{ loading ? '正在重开...' : '再试一次' }}
        </button>
      </div>
    </section>
  </div>
</template>
