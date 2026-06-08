<script setup>
import { ref } from 'vue'
import AuthView from '../components/AuthView.vue'
import GameDashboard from '../components/GameDashboard.vue'
import StartMenu from '../components/StartMenu.vue'
import { mockGameState, mockSaves } from '../mock/gameState'

const currentView = ref('auth')
const mockUsername = '寻宝者'

function enterStartMenu() {
  currentView.value = 'start-menu'
}

function startNewGame() {
  currentView.value = 'game'
}

function leaveGame() {
  currentView.value = 'auth'
}
</script>

<template>
  <main class="home-page">
    <AuthView
      v-if="currentView === 'auth'"
      :intro="mockGameState.authIntro"
      @enter-game="enterStartMenu"
    />
    <StartMenu
      v-else-if="currentView === 'start-menu'"
      :saves="mockSaves"
      :username="mockUsername"
      @logout="leaveGame"
      @start-new="startNewGame"
    />
    <GameDashboard
      v-else
      :game-state="mockGameState"
      @logout="leaveGame"
    />
  </main>
</template>
