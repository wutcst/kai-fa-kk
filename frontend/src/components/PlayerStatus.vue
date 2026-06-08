<script setup>
defineProps({
  player: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <section
    class="panel player-status"
    aria-labelledby="player-status-title"
  >
    <div class="panel-heading">
      <span>Player</span>
      <h2 id="player-status-title">
        玩家状态
      </h2>
    </div>

    <dl class="status-grid">
      <div>
        <dt>角色</dt>
        <dd>{{ player.name }}</dd>
      </div>
      <div>
        <dt>金币</dt>
        <dd>{{ player.gold }}</dd>
      </div>
      <div>
        <dt>体力</dt>
        <dd>{{ player.stamina }} / {{ player.maxStamina }}</dd>
      </div>
      <div>
        <dt>负重</dt>
        <dd>{{ player.currentWeight }} / {{ player.maxWeight }}</dd>
      </div>
      <div>
        <dt>当前位置</dt>
        <dd>{{ player.location }}</dd>
      </div>
    </dl>
  </section>
</template>
