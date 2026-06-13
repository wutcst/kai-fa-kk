import {
  describe,
  expect,
  it,
} from 'vitest'
import {
  readFileSync,
} from 'node:fs'
import {
  hintedRoomIds,
  roomMapLabels,
  roomMapPositions,
  visibleMapRoomIds,
} from '../constants/gameMap'

const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf-8')

describe('old map backpack entry', () => {
  it('shows a dedicated map action only for old-map', () => {
    const backpackSource = readSource('../components/BackpackModal.vue')

    expect(backpackSource).toMatch(/\bisOldMap\b/)
    expect(backpackSource).toMatch(/['"]old-map['"]/)
    expect(backpackSource).toContain('v-if="isOldMap(item)"')
    expect(backpackSource).toContain('查看地图')
    expect(backpackSource).toMatch(/\$emit\(['"]view-map['"]\)/)
    expect(backpackSource).toMatch(/\bcanUseItem\b/)
    expect(backpackSource).toMatch(/normalizeItemType\(item\.type\)\s*===\s*['"]KEY['"]/)
    expect(backpackSource).not.toContain("emit('use-item', 'old-map')")
  })

  it('opens the map modal from the dashboard without using an item API', () => {
    const dashboardSource = readSource('../components/GameDashboard.vue')
    const actionSource = readSource('../components/ActionPanel.vue')

    expect(dashboardSource).toContain('@view-map="openMapModal"')
    expect(dashboardSource).toContain('<GameMapModal')
    expect(dashboardSource).toContain(':open="isMapModalOpen"')
    expect(dashboardSource).toContain(':current-room-id="gameState?.player?.currentRoomId')
    expect(dashboardSource).toContain('v-if="isPlaying && hasOldMap"')
    expect(dashboardSource).toContain('class="game-dashboard-map-button"')
    expect(dashboardSource).toContain(':style="rectStyle(HUD_POSITIONS.mapButton)"')
    expect(dashboardSource).toContain('@click="openMapModal"')
    expect(actionSource).not.toContain("'open-map'")
    expect(dashboardSource).not.toContain("emit('use-item', 'old-map')")
  })
})

describe('game map modal', () => {
  it('renders only when open and supports close actions', () => {
    const modalSource = readSource('../components/GameMapModal.vue')

    expect(modalSource).toContain('v-if="open"')
    expect(modalSource).toContain('old-map.png')
    expect(modalSource).toContain('game-map-current-marker')
    expect(modalSource).not.toContain('game-map-special-badge')
    expect(modalSource).toContain('@click.self="$emit(\'close\')"')
    expect(modalSource).toContain('@click="$emit(\'close\')"')
  })

  it('keeps marker coordinates for representative rooms', () => {
    expect(roomMapPositions.entrance).toEqual({
      x: 7.5,
      y: 27,
    })
    expect(roomMapPositions['mechanism-gallery']).toEqual({
      x: 85,
      y: 70,
    })
    expect(roomMapPositions['final-gate']).toEqual({
      x: 98,
      y: 60,
    })
    expect(roomMapPositions['moon-secret-room']).toEqual({
      x: 22,
      y: 58,
    })
  })

  it('keeps the required directional relationships', () => {
    expect(roomMapPositions['old-altar'].y).toBeLessThan(roomMapPositions['stone-court'].y)
    expect(roomMapPositions['broken-bridge'].x).toBeGreaterThan(roomMapPositions['stone-court'].x)
    expect(roomMapPositions['final-gate'].x).toBeGreaterThan(roomMapPositions['mechanism-gallery'].x)
    expect(roomMapPositions['hidden-vault'].y).toBeGreaterThan(roomMapPositions['deep-well-altar'].y)
  })

  it('does not expose hinted side rooms as normal labels', () => {
    expect(hintedRoomIds).toEqual(expect.arrayContaining([
      'moon-secret-room',
      'hidden-chest-room',
      'star-side-hall',
      'ancient-coffer',
    ]))
    hintedRoomIds.forEach((roomId) => {
      expect(visibleMapRoomIds).not.toContain(roomId)
    })
    expect(roomMapLabels['stone-gate']).toBe('石门关门')
    expect(roomMapLabels['mechanism-path']).toBe('机关侧道')
    expect(roomMapLabels['final-gate']).toBe('最终石门')
  })
})