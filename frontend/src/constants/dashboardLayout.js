export const DASHBOARD_BASE = {
  width: 1920,
  height: 1080,
}

export const HUD_POSITIONS = {
  topbar: { x: 490, y: 16, w: 920, h: 90 },
  playerStatus: { x: 42, y: 60, w: 320, h: 395 },
  mapButton: { x: 374, y: 70, w: 90, h: 42 },
  actionPanel: { x: 42, y: 468, w: 320, h: 500 },
  ranking: { x: 1524, y: 60, w: 320, h: 295 },
  log: { x: 1524, y: 388, w: 320, h: 580 },
  roomItems: { x: 600, y: 350, w: 720, h: 340 },
  narration: { x: 500, y: 890, w: 920, h: 100 },
}

export const rectStyle = (rect) => ({
  position: 'absolute',
  left: `${rect.x}px`,
  top: `${rect.y}px`,
  right: 'auto',
  bottom: 'auto',
  width: `${rect.w}px`,
  height: `${rect.h}px`,
  transform: 'none',
})
