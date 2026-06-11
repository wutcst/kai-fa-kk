<script setup>
defineProps({
  message: {
    type: String,
    required: true,
  },
})

defineEmits(['close'])
</script>

<template>
  <div
    class="game-state-modal"
    role="presentation"
  >
    <section
      class="game-notice-modal"
      role="alertdialog"
      aria-modal="true"
      aria-labelledby="game-notice-title"
    >
      <p class="game-notice-modal__eyebrow">
        STAMINA WARNING
      </p>
      <h2 id="game-notice-title">
        救援提示
      </h2>
      <p class="game-notice-modal__message">
        {{ message }}
      </p>
      <button
        type="button"
        @click="$emit('close')"
      >
        我知道了
      </button>
    </section>
  </div>
</template>
