<script setup>
defineProps({
  actions: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <section
    class="panel action-panel"
    aria-labelledby="action-panel-title"
  >
    <div class="panel-heading">
      <span>Actions</span>
      <h2 id="action-panel-title">
        行动提示
      </h2>
    </div>

    <div class="direction-pad">
      <button
        v-for="action in actions.directionActions"
        :key="action.id"
        class="direction-button"
        type="button"
        disabled
      >
        <strong>{{ action.label }}</strong>
        <span>{{ action.hint }}</span>
      </button>
    </div>

    <div class="item-actions">
      <button
        v-for="action in actions.itemActions"
        :key="action.id"
        type="button"
        disabled
      >
        {{ action.label }}
      </button>
    </div>
  </section>
</template>
