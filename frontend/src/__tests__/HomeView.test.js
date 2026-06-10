import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { mockGameState, mockSaves, statusLabels } from '../mock/gameState'

describe('HomeView', () => {
  it('uses the auth and game dashboard containers', () => {
    const source = readFileSync(
      new URL('../views/HomeView.vue', import.meta.url),
      'utf-8',
    )
    const authSource = readFileSync(
      new URL('../components/AuthView.vue', import.meta.url),
      'utf-8',
    )

    expect(source).toContain('AuthView')
    expect(source).toContain('StartMenu')
    expect(source).toContain('GameDashboard')
    expect(source).toContain('currentView')
    expect(source).toContain('start-menu')
    expect(source).toContain('enterStartMenu')
    expect(authSource).toContain('viewMode')
    expect(authSource).toContain('authMode')
    expect(authSource).toContain('landing')
    expect(authSource).toContain('auth')
    expect(authSource).toContain('sesameLogo')
    expect(authSource).toContain('sesame-logo.png')
  })

  it('provides mock auth and game state', () => {
    expect(mockGameState.title).toBe('芝麻开门')
    expect(mockGameState.authIntro).toMatchObject({
      title: '芝麻开门',
      subtitle: '暗语开启石门，火光照亮秘窟。',
    })
    expect(mockGameState.authIntro.story).toContain('秘窟')
    expect(mockGameState.authIntro.sections.map((section) => section.title)).toEqual([
      '石门传闻',
      '探索目标',
      '行动规则',
    ])
    expect(mockGameState.authIntro.sections[0].paragraphs).toHaveLength(3)
    expect(mockGameState.authIntro.quote).toContain('古卷')
    expect(mockGameState.authIntro.victory.title).toBe('胜利条件')
    expect(mockGameState.authIntro.sections[2].points).toContain('移动房间会消耗体力')
    expect(mockGameState.chapter).toBe('第一章：石门回声')
    expect(mockGameState.player).toMatchObject({
      name: '寻宝者',
      gold: 90,
      stamina: 25,
      maxStamina: 30,
      currentWeight: 9,
      maxWeight: 20,
      location: '石门大厅',
    })
    expect(mockGameState.room.exits).toHaveLength(3)
    expect(mockGameState.room.visibleItems).toHaveLength(7)
    expect(mockGameState.inventory).toHaveLength(4)
    expect(mockGameState.logs).toHaveLength(4)
    expect(mockGameState.actions.directionActions).toHaveLength(4)
    expect(mockGameState.actions.itemActions).toHaveLength(4)
    expect(mockSaves).toHaveLength(3)
    expect(mockSaves[0]).toMatchObject({
      saveName: '月纹回廊前的整备',
      currentLevel: 2,
      currentWeight: 11,
      maxWeight: 24,
    })
    expect(statusLabels.SHOPPING).toBe('商店整备')
  })

  it('keeps key dashboard labels in component sources', () => {
    const componentSources = [
      '../components/AuthView.vue',
      '../components/StartMenu.vue',
      '../components/SaveListPanel.vue',
      '../components/GameDashboard.vue',
      '../components/GameHeader.vue',
      '../components/GameScene.vue',
      '../components/PlayerStatus.vue',
      '../components/InventoryPanel.vue',
      '../components/GameLog.vue',
      '../components/ActionPanel.vue',
    ]
      .map((componentPath) => readFileSync(
        new URL(componentPath, import.meta.url),
        'utf-8',
      ))
      .join('\n')

    expect(componentSources).toContain('登录')
    expect(componentSources).toContain('注册')
    expect(componentSources).toContain('登录 / 注册')
    expect(componentSources).toContain('游戏介绍')
    expect(componentSources).toContain('返回入口')
    expect(componentSources).toContain('探险入口')
    expect(componentSources).toContain('读取存档')
    expect(componentSources).toContain('退出登录')
    expect(mockGameState.authIntro.sections[1].title).toBe('探索目标')
    expect(mockGameState.authIntro.sections[2].title).toBe('行动规则')
    expect(componentSources).toContain('芝麻开门')
    expect(componentSources).toContain('进入秘窟')
    expect(componentSources).toContain('创建寻宝者')
    expect(componentSources).toContain('玩家状态')
    expect(componentSources).toContain('当前位置')
    expect(componentSources).toContain('背包物品')
    expect(componentSources).toContain('游戏日志')
    expect(mockGameState.room.name).toBe('石门大厅')
  })
})
