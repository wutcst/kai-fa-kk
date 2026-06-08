<script setup>
defineProps({
  chapter: {
    type: String,
    required: true,
  },
  location: {
    type: String,
    required: true,
  },
  title: {
    type: String,
    required: true,
  },
  status: {
    type: String,
    required: true,
  },
  objective: {
    type: String,
    required: true,
  },
})

defineEmits(['logout'])
</script>

<template>
  <header class="game-header">
    <div class="header-copy">
      <span class="chapter-label">{{ chapter }}</span>
      <span class="location-label">{{ location }}</span>
      <span class="status-pill">{{ status }}</span>
      <h1>{{ title }}</h1>
      <p>{{ objective }}</p>
    </div>
    <button
      class="logout-button"
      type="button"
      @click="$emit('logout')"
    >
      返回入口
    </button>
  </header>
</template>
