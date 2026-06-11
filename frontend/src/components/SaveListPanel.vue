<script setup>
import SaveCard from './SaveCard.vue'

defineProps({
  errorMessage: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  saves: {
    type: Array,
    required: true,
  },
})

defineEmits(['close', 'load-save'])
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
        <p>选择一段旧日卷宗继续前行。</p>
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
      v-if="loading"
      class="save-empty"
    >
      <span>正在读取存档列表...</span>
    </div>

    <div
      v-else-if="errorMessage"
      class="save-empty"
      role="alert"
    >
      <span>{{ errorMessage }}</span>
    </div>

    <div
      v-else-if="saves.length"
      class="save-list"
    >
      <SaveCard
        v-for="save in saves"
        :key="save.saveId"
        :save="save"
        action-label="读取存档"
        show-action
        @load="$emit('load-save', $event)"
      />
    </div>

    <div
      v-else
      class="save-empty"
    >
      <span>暂无存档</span>
      <p>你还没有保存过探险进度。</p>
    </div>
  </section>
</template>
