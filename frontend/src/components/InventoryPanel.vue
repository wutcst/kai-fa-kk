<script setup>
defineProps({
  items: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section
    class="panel inventory-panel"
    aria-labelledby="inventory-title"
  >
    <div class="panel-heading">
      <span>Bag</span>
      <h2 id="inventory-title">
        背包物品
      </h2>
    </div>

    <ul class="item-list">
      <li
        v-for="item in items"
        :key="item.id"
      >
        <div>
          <strong>{{ item.name }}</strong>
          <span>{{ item.type }}</span>
        </div>
        <p>{{ item.description }}</p>
        <small>重量 {{ item.weight }}</small>
      </li>
    </ul>
  </section>
</template>
