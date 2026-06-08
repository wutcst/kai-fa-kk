<script setup>
import ActionPanel from './ActionPanel.vue'
import GameHeader from './GameHeader.vue'
import GameLog from './GameLog.vue'
import GameScene from './GameScene.vue'
import InventoryPanel from './InventoryPanel.vue'
import PlayerStatus from './PlayerStatus.vue'

defineProps({
  gameState: {
    type: Object,
    required: true,
  },
})

defineEmits(['logout'])
</script>

<template>
  <section class="game-dashboard">
    <GameHeader
      :chapter="gameState.chapter"
      :location="gameState.player.location"
      :objective="gameState.objective"
      :status="gameState.status"
      :title="gameState.title"
      @logout="$emit('logout')"
    />

    <section
      class="hud-layout"
      aria-label="芝麻开门游戏主界面"
    >
      <ActionPanel :actions="gameState.actions" />
      <GameScene :room="gameState.room" />
      <aside class="right-hud">
        <PlayerStatus :player="gameState.player" />
        <InventoryPanel :items="gameState.inventory" />
      </aside>
      <GameLog :logs="gameState.logs" />
    </section>
  </section>
</template>
