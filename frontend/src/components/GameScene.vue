<script setup>
defineProps({
  room: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <section
    class="scene-panel"
    aria-labelledby="scene-title"
  >
    <div class="scene-visual dungeon-background">
      <div
        class="scene-lighting"
        aria-hidden="true"
      />
      <div class="scene-overlay">
        <span class="danger-badge">危险等级：{{ room.dangerLevel }}</span>
        <h2 id="scene-title">
          {{ room.name }}
        </h2>
        <p>{{ room.description }}</p>
      </div>
    </div>

    <div class="scene-details">
      <section aria-labelledby="exits-title">
        <h3 id="exits-title">
          可通行出口
        </h3>
        <div class="exit-list">
          <span
            v-for="exit in room.exits"
            :key="exit.direction"
          >
            {{ exit.direction }}：{{ exit.name }}
          </span>
        </div>
      </section>

      <section aria-labelledby="visible-items-title">
        <h3 id="visible-items-title">
          可见物品
        </h3>
        <div class="visible-item-list">
          <span
            v-for="item in room.visibleItems"
            :key="item.id"
          >
            {{ item.name }} / {{ item.type }}
          </span>
        </div>
      </section>
    </div>
  </section>
</template>
