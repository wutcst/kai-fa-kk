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
    expect(rankingSource).toContain('entry.username')
    expect(rankingSource).toContain('entry.score')
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
