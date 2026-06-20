import { beforeEach, describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'

vi.mock('../api/httpClient', () => ({
  default: {
    request: vi.fn(),
  },
}))

import httpClient from '../api/httpClient'
import {
  buyShopItem,
  continueAdventure,
  getShopCatalog,
  sellShopItem,
} from '../api/gameApi'

describe('shop integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uses the real shop endpoints and request params', async () => {
    const gameState = { sessionId: 'session-1', status: 'SHOPPING' }
    httpClient.request
      .mockResolvedValueOnce({ data: [{ itemId: 'clean-water', price: 10 }] })
      .mockResolvedValue({ data: gameState })

    await expect(getShopCatalog('session-1')).resolves.toHaveLength(1)
    await buyShopItem('session-1', 'clean-water')
    await sellShopItem('session-1', 'silver-cup')
    await continueAdventure('session-1')

    expect(httpClient.request).toHaveBeenNthCalledWith(1, {
      method: 'get',
      url: '/game/shop/catalog',
      data: undefined,
      params: { sessionId: 'session-1' },
    })
    expect(httpClient.request).toHaveBeenNthCalledWith(2, {
      method: 'post',
      url: '/game/shop/buy',
      data: null,
      params: { sessionId: 'session-1', itemId: 'clean-water' },
    })
    expect(httpClient.request).toHaveBeenNthCalledWith(3, {
      method: 'post',
      url: '/game/shop/sell',
      data: null,
      params: { sessionId: 'session-1', itemId: 'silver-cup' },
    })
    expect(httpClient.request).toHaveBeenNthCalledWith(4, {
      method: 'post',
      url: '/game/shop/continue',
      data: null,
      params: { sessionId: 'session-1' },
    })
  })

  it('switches the dashboard from exploration controls to ShopPanel', () => {
    const dashboardSource = readFileSync(
      new URL('../components/GameDashboard.vue', import.meta.url),
      'utf-8',
    )

    expect(dashboardSource).toContain("props.gameState?.status === 'SHOPPING'")
    expect(dashboardSource).toContain('<ShopPanel')
    expect(dashboardSource).toContain('v-if="isShopping"')
    expect(dashboardSource).toContain('<ActionPanel')
    expect(dashboardSource).toContain('v-if="isPlaying"')
  })

  it('loads the catalog on shopping entry and updates state after continuing', () => {
    const homeSource = readFileSync(
      new URL('../views/HomeView.vue', import.meta.url),
      'utf-8',
    )

    expect(homeSource).toContain("status === 'SHOPPING'")
    expect(homeSource).toContain('void loadShopCatalog()')
    expect(homeSource).toContain('getShopCatalog(sessionId.value)')
    expect(homeSource).toContain('return runShopAction(continueAdventure)')
    expect(homeSource).toContain('enterLoadedGame(result)')
  })

  it('shows loading, empty, error, purchase, sale, and continue states', () => {
    const shopSource = readFileSync(
      new URL('../components/ShopPanel.vue', import.meta.url),
      'utf-8',
    )

    expect(shopSource).toContain('商店商品加载中...')
    expect(shopSource).toContain('暂无可购买商品')
    expect(shopSource).toContain('商店暂时无法加载')
    expect(shopSource).toContain('购买物资')
    expect(shopSource).toContain('出售宝物')
    expect(shopSource).toContain('继续探险')
  })
})
