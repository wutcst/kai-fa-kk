const ROOM_NAME_MAP = Object.freeze({
  entrance: '秘窟入口',
  'stone-court': '石门外庭',
  'old-altar': '旧祭坛',
  'broken-bridge': '断裂石桥',
  'bronze-moon-room': '铜月石室',
  'supply-alcove': '补给壁龛',
  'mechanism-path': '机关侧道',
  'stone-gate': '石门关门',
  'stone-camp': '石门营地',
  'moon-corridor': '月纹回廊',
  'moon-secret-room': '月纹密室',
  'hidden-chest-room': '隐藏宝箱',
  'sand-pit': '流沙陷坑',
  'echo-hall': '回声石厅',
  'merchant-bones': '商旅遗骨',
  'drain-path': '暗渠小径',
  'star-altar': '星纹祭台',
  'moon-gate': '月纹关门',
  'corridor-camp': '回廊营地',
  'throne-antechamber': '王座前厅',
  'star-side-hall': '星纹侧殿',
  'ancient-coffer': '古代宝匣',
  'golden-throne': '沉金王座',
  'mechanism-gallery': '机关长廊',
  'final-gate': '最终石门',
  'broken-vault': '破碎金库',
  'deep-well-altar': '深井祭坛',
  'hidden-vault': '隐秘宝库',
})

export function getRoomName(roomId) {
  return ROOM_NAME_MAP[roomId] || roomId
}
