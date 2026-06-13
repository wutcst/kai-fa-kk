import {
  describe,
  expect,
  it,
} from 'vitest'
import {
  readFileSync,
} from 'node:fs'
import {
  adaptGameState,
} from '../api/gameStateAdapter'

const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf-8')

describe('password availability and backpack actions', () => {
  it('preserves canSubmitPassword from the backend state', () => {
    const state = adaptGameState({
      sessionId: 'session-1',
      status: 'IN_PROGRESS',
      player: {
        inventory: [],
      },
      currentRoom: {
        exits: {
          east: 'stone-court',
        },
        items: [],
      },
      logs: [],
      canSubmitPassword: true,
    })

    expect(state.canSubmitPassword).toBe(true)
    expect(state.currentRoom.exitDirections).toEqual(['east'])
  })

  it('defaults canSubmitPassword to false when omitted', () => {
    const state = adaptGameState({
      player: {
        inventory: [],
      },
      currentRoom: {
        exits: {},
        items: [],
      },
      logs: [],
    })

    expect(state.canSubmitPassword).toBe(false)
  })

  it('offers use actions only for directly usable inventory types', () => {
    const backpackSource = readSource('../components/BackpackModal.vue')

    expect(backpackSource).toMatch(/\bcanUseItem\b/)
    expect(backpackSource).toMatch(/type\s*===\s*['"]SUPPLY['"]/)
    expect(backpackSource).toMatch(/type\s*===\s*['"]EQUIPMENT['"]/)
    expect(backpackSource).toContain('hasDirectUseEffect(item)')
    expect(backpackSource).toContain('v-else-if="canUseItem(item)"')
    expect(backpackSource).toContain('无需主动使用')
    expect(backpackSource).not.toContain('无直接效果')
  })

  it('shows and opens password input only from canSubmitPassword', () => {
    const dashboardSource = readSource('../components/GameDashboard.vue')
    const actionSource = readSource('../components/ActionPanel.vue')

    expect(dashboardSource).toContain(
      'const canSubmitPassword = computed(() => Boolean(props.gameState?.canSubmitPassword))',
    )
    expect(dashboardSource).toContain('!canSubmitPassword.value')
    expect(dashboardSource).toContain(':can-submit-password="canSubmitPassword"')
    expect(actionSource).toContain('if (props.canSubmitPassword)')
    expect(actionSource).not.toContain("props.currentRoom.id === 'mechanism-gallery'")
  })

  it('groups help content into accurate beginner guidance sections', () => {
    const dashboardSource = readSource('../components/GameDashboard.vue')

    expect(dashboardSource).toContain('<h3>操作</h3>')
    expect(dashboardSource).toContain('<h3>下一步怎么走</h3>')
    expect(dashboardSource).toContain('<h3>地图</h3>')
    expect(dashboardSource).toContain('<h3>背包</h3>')
    expect(dashboardSource).toContain('<h3>暗语</h3>')
    expect(dashboardSource).toContain('先查看当前房间出口')
    expect(dashboardSource).toContain('残旧地图')
    expect(dashboardSource).toContain('点击“查看地图”')
    expect(dashboardSource).toContain('关键物品通常用于特殊机关')
    expect(dashboardSource).toContain('最终暗语需要在机关长廊输入')
    expect(dashboardSource).toContain('Q：显示或收起当前房间物品')
    expect(dashboardSource).toContain('P：输入暗语，仅在机关长廊可用')
    expect(dashboardSource).toContain('留意“芝麻纹”“门”“开门”等关键词')
    expect(dashboardSource).toContain('“被呼唤的对象”和“要执行的动作”')
    expect(dashboardSource).not.toContain('真正的暗语是“芝麻开门”')
    expect(dashboardSource).not.toContain('完整答案：芝麻开门')
    expect(dashboardSource).not.toContain('M：')
  })
})