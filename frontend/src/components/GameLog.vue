<script setup>
defineProps({
  logs: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section
    class="panel game-log"
    aria-labelledby="game-log-title"
  >
    <div class="panel-heading">
      <span>Log</span>
      <h2 id="game-log-title">
        游戏日志
      </h2>
    </div>

    <ol class="log-list">
      <li
        v-for="log in logs"
        :key="log.id"
      >
        <time>{{ log.time }}</time>
        <span>{{ log.content }}</span>
      </li>
    </ol>
  </section>
</template>
