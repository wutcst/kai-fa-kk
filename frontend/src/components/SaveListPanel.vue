<script setup>
import SaveCard from './SaveCard.vue'

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
        <h2 id="save-list-title">
          历史存档
        </h2>
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
      <SaveCard
        v-for="save in saves"
        :key="save.saveId"
        :save="save"
        action-label="读取存档"
        show-action
        @load="$emit('start-new')"
      />
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
