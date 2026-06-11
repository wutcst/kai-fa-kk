const DEFAULT_OBJECTIVE = '探索当前房间，寻找通往下一片遗迹区域的道路。'
const OBJECTIVE_PREFIX = '游戏目标：'

function getExitDirections(exits) {
  if (Array.isArray(exits)) {
    return exits
      .map((exit) => (typeof exit === 'string' ? exit : exit?.direction))
      .filter((direction) => typeof direction === 'string' && direction)
      .map((direction) => direction.toLowerCase())
  }

  if (exits && typeof exits === 'object') {
    return Object.keys(exits).map((direction) => direction.toLowerCase())
  }

  return []
}

function getCurrentObjective(logs) {
  if (!Array.isArray(logs)) {
    return DEFAULT_OBJECTIVE
  }

  const objectiveLog = logs.find((log) => (
    typeof log === 'string' && log.startsWith(OBJECTIVE_PREFIX)
  ))
  const objective = objectiveLog?.slice(OBJECTIVE_PREFIX.length).trim()

  return objective || DEFAULT_OBJECTIVE
}

/**
 * Preserves the backend GameState and adds fields used by frontend views.
 *
 * @param {unknown} rawState Raw GameState returned by the backend.
 * @returns {unknown} Adapted GameState.
 */
export function adaptGameState(rawState) {
  if (!rawState || typeof rawState !== 'object') {
    return rawState
  }

  const rawPlayer = rawState.player
  const rawRoom = rawState.currentRoom
  const player = rawPlayer && typeof rawPlayer === 'object'
    ? {
        ...rawPlayer,
        inventoryItems: Array.isArray(rawPlayer.inventory) ? rawPlayer.inventory : [],
      }
    : rawPlayer
  const currentRoom = rawRoom && typeof rawRoom === 'object'
    ? {
        ...rawRoom,
        exitDirections: getExitDirections(rawRoom.exits),
        roomItems: Array.isArray(rawRoom.items) ? rawRoom.items : [],
      }
    : rawRoom

  return {
    ...rawState,
    player,
    currentRoom,
    currentObjectives: getCurrentObjective(rawState.logs),
  }
}
