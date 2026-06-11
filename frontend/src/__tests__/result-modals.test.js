import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'

const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf-8')

describe('game result and rescue modals', () => {
  it('renders WON and FAILED result content from the real game state', () => {
    const dashboardSource = readSource('../components/GameDashboard.vue')
    const resultSource = readSource('../components/GameResultModal.vue')

    expect(dashboardSource).toContain("['WON', 'FAILED'].includes")
    expect(dashboardSource).toContain('v-if="isTerminal"')
    expect(dashboardSource).toContain(':final-score="gameState?.finalScore"')
    expect(dashboardSource).toContain(':high-score="gameState?.highScore"')
    expect(resultSource).toContain('探险成功')
    expect(resultSource).toContain('探险失败')
    expect(resultSource).toContain('本局得分')
    expect(resultSource).toContain('历史最高分')
    expect(resultSource).toContain('{{ message')
  })

  it('detects rescue messages but ignores ordinary messages', () => {
    const dashboardSource = readSource('../components/GameDashboard.vue')

    expect(dashboardSource).toContain("'体力不足'")
    expect(dashboardSource).toContain("'体力耗尽'")
    expect(dashboardSource).toContain("'当前关重新开始'")
    expect(dashboardSource).toContain("'救援'")
    expect(dashboardSource).toContain('rescueKeywords.some')
    expect(dashboardSource).toContain("return candidates.find")
  })

  it('closes rescue notices and does not repeat the same message', () => {
    const dashboardSource = readSource('../components/GameDashboard.vue')
    const noticeSource = readSource('../components/GameNoticeModal.vue')

    expect(dashboardSource).toContain('message === lastNoticeMessage.value')
    expect(dashboardSource).toContain('isRescueNoticeOpen.value = false')
    expect(dashboardSource).toContain('@close="closeRescueNotice"')
    expect(noticeSource).toContain('我知道了')
    expect(noticeSource).toContain("@click=\"$emit('close')\"")
  })

  it('prioritizes terminal result modals over rescue notices and exploration controls', () => {
    const dashboardSource = readSource('../components/GameDashboard.vue')

    expect(dashboardSource).toContain('<ActionPanel')
    expect(dashboardSource).toContain('v-if="isPlaying"')
    expect(dashboardSource).toContain('<GameResultModal')
    expect(dashboardSource).toContain('v-if="isTerminal"')
    expect(dashboardSource).toContain('<GameNoticeModal')
    expect(dashboardSource).toContain('v-else-if="isRescueNoticeOpen"')
    expect(dashboardSource).toContain('if (isTerminal.value) return')
  })

  it('reuses HomeView start and restart handlers and clears local result state on return', () => {
    const homeSource = readSource('../views/HomeView.vue')

    expect(homeSource).toContain('@start-new-game="startNewGame"')
    expect(homeSource).toContain('@restart-level="handleRestartLevel"')
    expect(homeSource).toContain('@return-start-menu="returnResultToStartMenu"')
    expect(homeSource).toContain('sessionStorage.removeItem(SESSION_ID_KEY)')
    expect(homeSource).toContain("currentView.value = 'start-menu'")
  })
})
