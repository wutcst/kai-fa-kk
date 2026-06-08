<script setup>
import { ref } from 'vue'
import AuthView from '../components/AuthView.vue'
import GameDashboard from '../components/GameDashboard.vue'
import { mockGameState } from '../mock/gameState'

const isAuthenticated = ref(false)

function enterGame() {
  isAuthenticated.value = true
}

function leaveGame() {
  isAuthenticated.value = false
}
</script>

<template>
  <main class="home-page">
    <AuthView
      v-if="!isAuthenticated"
      :intro="mockGameState.authIntro"
      @enter-game="enterGame"
    />
    <GameDashboard
      v-else
      :game-state="mockGameState"
      @logout="leaveGame"
    />
  </main>
</template>
