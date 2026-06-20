import { beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'

vi.mock('../api/httpClient', () => ({
  default: {
    request: vi.fn(),
  },
}))

import httpClient from '../api/httpClient'
import { getLeaderboard } from '../api/gameApi'

describe('leaderboard integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('requests the public leaderboard with the requested limit', async () => {
    const entries = [{ rank: 1, username: 'player1', score: 320 }]
    httpClient.request.mockResolvedValue({ data: entries })

    await expect(getLeaderboard()).resolves.toEqual(entries)
    expect(httpClient.request).toHaveBeenCalledWith({
      method: 'get',
      url: '/game/leaderboard',
      data: undefined,
      params: { limit: 3 },
    })
  })

  it('supports loading, error, empty, and real entry states', () => {
    const rankingSource = readFileSync(
      new URL('../components/RankingPanel.vue', import.meta.url),
      'utf-8',
    )

    expect(rankingSource).toContain('排行榜加载中...')
    expect(rankingSource).toContain('排行榜暂时无法加载')
    expect(rankingSource).toContain('暂无排行榜数据')
    expect(rankingSource).toContain("panelState === 'success'")
    expect(rankingSource).toContain("panelState === 'loading'")
    expect(rankingSource).toContain("panelState === 'error'")
    expect(rankingSource).toContain('ranking-panel__state')
    expect(rankingSource).not.toContain('ranking-panel-template__fallback-text')
    expect(rankingSource).toContain('state: { x: 76, y: 94, w: 208, h: 46 }')
    expect(rankingSource).toContain('entry.username')
    expect(rankingSource).toContain('entry.score')
  })

  it('only renders ranking rows in the success branch', () => {
    const rankingSource = readFileSync(
      new URL('../components/RankingPanel.vue', import.meta.url),
      'utf-8',
    )

    const successBranch = rankingSource.indexOf('<template v-if="panelState === \'success\'">')
    const rankingRow = rankingSource.indexOf('class="ranking-panel-template__row"')
    const stateBranch = rankingSource.indexOf('class="ranking-panel__state"')

    expect(successBranch).toBeGreaterThan(-1)
    expect(rankingRow).toBeGreaterThan(successBranch)
    expect(stateBranch).toBeGreaterThan(rankingRow)
  })

  it('loads on page mount and refreshes after entering WON status', () => {
    const homeSource = readFileSync(
      new URL('../views/HomeView.vue', import.meta.url),
      'utf-8',
    )

    expect(homeSource).toContain('void loadLeaderboard()')
    expect(homeSource).toContain("status === 'WON'")
    expect(homeSource).toContain("previousStatus !== 'WON'")
    expect(homeSource).toContain(':leaderboard="leaderboard"')
  })
})
