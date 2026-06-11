<script setup>
import { computed, ref } from 'vue'
import { getPageBackground } from '../utils/assetMap'
import SaveCard from './SaveCard.vue'
import SaveListPanel from './SaveListPanel.vue'

const props = defineProps({
  errorMessage: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  saveErrorMessage: {
    type: String,
    default: '',
  },
  saveLoading: {
    type: Boolean,
    default: false,
  },
  saves: {
    type: Array,
    required: true,
  },
  username: {
    type: String,
    default: '寻宝者',
  },
})

const emit = defineEmits(['continue-latest', 'load-save', 'logout', 'open-saves', 'start-new'])

const isArchiveOpen = ref(false)

const latestSave = computed(() => props.saves[0] || null)
const displayedMessage = computed(() => props.errorMessage || props.saveErrorMessage)
const startMenuBackground = computed(() => getPageBackground('start-menu'))
const startMenuStyle = computed(() => ({
  '--start-menu-background': `url(${startMenuBackground.value})`,
}))

const openArchive = () => {
  isArchiveOpen.value = true
  emit('open-saves')
}

const closeArchive = () => {
  isArchiveOpen.value = false
}

const startNewGame = () => {
  emit('start-new')
}

const continueLatestSave = () => {
  emit('continue-latest')
}

const loadSave = (saveId) => {
  emit('load-save', saveId)
}
</script>

<template>
  <section
    class="start-menu"
    :style="startMenuStyle"
    aria-labelledby="start-menu-title"
  >
    <div class="start-menu__stage">
      <section class="start-menu__panel">
        <span class="start-menu__eyebrow">登录成功</span>
        <h1 id="start-menu-title">
          探险入口
        </h1>
        <p>欢迎回来，{{ username }}。开启新的探险，或沿着旧日卷宗继续前行。</p>
        <p
          v-if="displayedMessage"
          class="start-menu__error"
          role="alert"
        >
          {{ displayedMessage }}
        </p>

        <div class="start-menu__buttons">
          <button
            type="button"
            class="start-menu-button start-menu-button--primary"
            :disabled="loading"
            @click="startNewGame"
          >
            {{ loading ? '正在开启秘窟...' : '开始新游戏' }}
          </button>
          <button
            type="button"
            class="start-menu-button start-menu-button--continue"
            :disabled="saveLoading || !latestSave"
            @click="continueLatestSave"
          >
            {{ saveLoading ? '正在读取存档...' : '继续最近存档' }}
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

        <SaveCard
          v-if="latestSave"
          class="start-menu__recent-save"
          :save="latestSave"
          label="最近卷宗"
          variant="recent"
        />
        <div
          v-else
          class="start-menu__recent-save save-empty"
        >
          <span>{{ saveLoading ? '正在读取存档...' : '暂无存档' }}</span>
          <p v-if="saveErrorMessage">
            {{ saveErrorMessage }}
          </p>
          <p v-else-if="!saveLoading">
            当前账号还没有保存过探险进度。
          </p>
        </div>
      </section>
    </div>

    <div
      v-if="isArchiveOpen"
      class="start-menu-modal"
      role="presentation"
      @click.self="closeArchive"
    >
      <SaveListPanel
        :error-message="saveErrorMessage"
        :loading="saveLoading"
        :saves="saves"
        @close="closeArchive"
        @load-save="loadSave"
      />
    </div>
  </section>
</template>
