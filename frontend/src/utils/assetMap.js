import pageDungeonBg from '../assets/images/backgrounds/page-dungeon-bg.png'
import introScrollBg from '../assets/images/backgrounds/intro-scroll-bg.png'
import startMenuBg from '../assets/images/backgrounds/start-menu-bg.png'

import roomAncientCoffer from '../assets/images/backgrounds/room-ancient-coffer.png'
import roomBrokenBridge from '../assets/images/backgrounds/room-broken-bridge.png'
import roomBrokenVault from '../assets/images/backgrounds/room-broken-vault.png'
import roomBronzeMoonRoom from '../assets/images/backgrounds/room-bronze-moon-room.png'
import roomCorridorCamp from '../assets/images/backgrounds/room-corridor-camp.png'
import roomDeepWellAltar from '../assets/images/backgrounds/room-deep-well-altar.png'
import roomDrainPath from '../assets/images/backgrounds/room-drain-path.png'
import roomEchoHall from '../assets/images/backgrounds/room-echo-hall.png'
import roomEntrance from '../assets/images/backgrounds/room-entrance.png'
import roomFinalGate from '../assets/images/backgrounds/room-final-gate.png'
import roomGoldenThrone from '../assets/images/backgrounds/room-golden-throne.png'
import roomHiddenChestRoom from '../assets/images/backgrounds/room-hidden-chest-room.png'
import roomHiddenVault from '../assets/images/backgrounds/room-hidden-vault.png'
import roomMechanismGallery from '../assets/images/backgrounds/room-mechanism-gallery.png'
import roomMechanismPath from '../assets/images/backgrounds/room-mechanism-path.png'
import roomMerchantBones from '../assets/images/backgrounds/room-merchant-bones.png'
import roomMoonCorridor from '../assets/images/backgrounds/room-moon-corridor.png'
import roomMoonGate from '../assets/images/backgrounds/room-moon-gate.png'
import roomMoonSecretRoom from '../assets/images/backgrounds/room-moon-secret-room.png'
import roomOldAltar from '../assets/images/backgrounds/room-old-altar.png'
import roomSandPit from '../assets/images/backgrounds/room-sand-pit.png'
import roomStarAltar from '../assets/images/backgrounds/room-star-altar.png'
import roomStarSideHall from '../assets/images/backgrounds/room-star-side-hall.png'
import roomStoneCamp from '../assets/images/backgrounds/room-stone-camp.png'
import roomStoneCourt from '../assets/images/backgrounds/room-stone-court.png'
import roomStoneGate from '../assets/images/backgrounds/room-stone-gate.png'
import roomSupplyAlcove from '../assets/images/backgrounds/room-supply-alcove.png'
import roomThroneAntechamber from '../assets/images/backgrounds/room-throne-antechamber.png'

import iconBlackPearl from '../assets/images/items/icon-black-pearl.png'
import iconBronzeMoonToken from '../assets/images/items/icon-bronze-moon-token.png'
import iconCleanWater from '../assets/images/items/icon-clean-water.png'
import iconDryFood from '../assets/images/items/icon-dry-food.png'
import iconGoldCrown from '../assets/images/items/icon-gold-crown.png'
import iconKingScepter from '../assets/images/items/icon-king-scepter.png'
import iconLantern from '../assets/images/items/icon-lantern.png'
import iconOldMap from '../assets/images/items/icon-old-map.png'
import iconRope from '../assets/images/items/icon-rope.png'
import iconStaminaPotion from '../assets/images/items/icon-stamina-potion.png'
import iconStarCompass from '../assets/images/items/icon-star-compass.png'
import iconTypeEquipment from '../assets/images/items/icon-type-equipment.png'
import iconTypeKey from '../assets/images/items/icon-type-key.png'
import iconTypeSupply from '../assets/images/items/icon-type-supply.png'
import iconTypeTreasure from '../assets/images/items/icon-type-treasure.png'

import iconFinalExit from '../assets/images/status/icon-final-exit.png'
import iconLoadSuccess from '../assets/images/status/icon-load-success.png'
import iconNeedKey from '../assets/images/status/icon-need-key.png'
import iconPasswordCorrect from '../assets/images/status/icon-password-correct.png'
import iconPasswordWrong from '../assets/images/status/icon-password-wrong.png'
import iconSaveSuccess from '../assets/images/status/icon-save-success.png'
import iconStatusFailed from '../assets/images/status/icon-status-failed.png'
import iconStatusPlaying from '../assets/images/status/icon-status-playing.png'
import iconStatusShopping from '../assets/images/status/icon-status-shopping.png'
import iconStatusWon from '../assets/images/status/icon-status-won.png'
import iconWarningMoney from '../assets/images/status/icon-warning-money.png'
import iconWarningStamina from '../assets/images/status/icon-warning-stamina.png'
import iconWarningWeight from '../assets/images/status/icon-warning-weight.png'

import itemSlotRoomGoldNormal from '../assets/images/ui/item-slot-room-gold-normal.png'
import actionPanelFrame from '../assets/images/ui/frames/hud-panel-action-frame.png'
import logPanelFrame from '../assets/images/ui/frames/hud-log-panel-frame.png'
import narrationBarFrame from '../assets/images/ui/frames/hud-narration-bar-frame.png'
import playerStatusFrame from '../assets/images/ui/frames/hud-panel-status-frame.png'
import rankingPanelFrame from '../assets/images/ui/frames/hud-ranking-panel-frame.png'
import topbarFrame from '../assets/images/ui/frames/hud-topbar-frame.png'
import backpackEntryIcon from '../assets/images/ui/icons/icon-backpack-entry.png'
import modalFailedBg from '../assets/images/modals/modal-failed-bg.png'
import modalPasswordBg from '../assets/images/modals/modal-password-bg.png'
import modalShopBg from '../assets/images/modals/modal-shop-bg.png'
import modalWinBg from '../assets/images/modals/modal-win-bg.png'

const normalizeKey = (value) => String(value || '')
  .trim()
  .replace(/_/g, '-')
  .toLowerCase()

const normalizeType = (type) => {
  const rawType = String(type || '').trim()
  const upperType = rawType.toUpperCase()
  const aliases = {
    TREASURE: 'TREASURE',
    SUPPLY: 'SUPPLY',
    EQUIPMENT: 'EQUIPMENT',
    KEY: 'KEY',
    CLUE: 'KEY',
    UNKNOWN: 'fallback',
    宝物: 'TREASURE',
    补给: 'SUPPLY',
    装备: 'EQUIPMENT',
    线索: 'KEY',
    机关: 'KEY',
    暗语: 'KEY',
  }

  return aliases[rawType] || aliases[upperType] || 'fallback'
}

export const pageBackgrounds = {
  home: pageDungeonBg,
  'page-home': pageDungeonBg,
  auth: pageDungeonBg,
  intro: introScrollBg,
  'intro-scroll': introScrollBg,
  startMenu: startMenuBg,
  'start-menu': startMenuBg,
  gameDashboard: roomStoneGate,
  'game-dashboard': roomStoneGate,
}

export const roomBackgrounds = {
  entrance: roomEntrance,
  'stone-court': roomStoneCourt,
  stonegate: roomStoneGate,
  'stone-hall': roomStoneGate,
  'stone-room': roomStoneGate,
  'room-stone-gate': roomStoneGate,
  'old-altar': roomOldAltar,
  'broken-bridge': roomBrokenBridge,
  'bronze-moon-room': roomBronzeMoonRoom,
  'supply-alcove': roomSupplyAlcove,
  'mechanism-path': roomMechanismPath,
  'stone-gate': roomStoneGate,
  'stone-camp': roomStoneCamp,
  'moon-corridor': roomMoonCorridor,
  'moon-secret-room': roomMoonSecretRoom,
  'hidden-chest-room': roomHiddenChestRoom,
  'sand-pit': roomSandPit,
  'echo-hall': roomEchoHall,
  'merchant-bones': roomMerchantBones,
  'drain-path': roomDrainPath,
  'star-altar': roomStarAltar,
  'moon-gate': roomMoonGate,
  'corridor-camp': roomCorridorCamp,
  'throne-antechamber': roomThroneAntechamber,
  'star-side-hall': roomStarSideHall,
  'ancient-coffer': roomAncientCoffer,
  'golden-throne': roomGoldenThrone,
  'mechanism-gallery': roomMechanismGallery,
  'final-gate': roomFinalGate,
  'broken-vault': roomBrokenVault,
  'deep-well-altar': roomDeepWellAltar,
  'hidden-vault': roomHiddenVault,
}

export const itemTypeIcons = {
  TREASURE: iconTypeTreasure,
  SUPPLY: iconTypeSupply,
  EQUIPMENT: iconTypeEquipment,
  KEY: iconTypeKey,
  CLUE: iconTypeKey,
  UNKNOWN: iconTypeTreasure,
  fallback: iconTypeTreasure,
}

export const itemIcons = {
  'old-map': iconOldMap,
  'clean-water': iconCleanWater,
  'dry-food': iconDryFood,
  'stamina-potion': iconStaminaPotion,
  rope: iconRope,
  lantern: iconLantern,
  'bronze-moon-token': iconBronzeMoonToken,
  'star-compass': iconStarCompass,
  'gold-crown': iconGoldCrown,
  'king-scepter': iconKingScepter,
  'black-pearl': iconBlackPearl,
}

export const statusIcons = {
  IN_PROGRESS: iconStatusPlaying,
  SHOPPING: iconStatusShopping,
  WON: iconStatusWon,
  FAILED: iconStatusFailed,
  staminaWarning: iconWarningStamina,
  weightWarning: iconWarningWeight,
  moneyWarning: iconWarningMoney,
  passwordCorrect: iconPasswordCorrect,
  passwordWrong: iconPasswordWrong,
  needKey: iconNeedKey,
  finalExit: iconFinalExit,
  saveSuccess: iconSaveSuccess,
  loadSuccess: iconLoadSuccess,
}

export const uiAssets = {
  actionPanelFrame,
  backpackEntryIcon,
  roomItemSlot: itemSlotRoomGoldNormal,
  logPanelFrame,
  narrationBarFrame,
  playerStatusFrame,
  rankingPanelFrame,
  topbarFrame,
}

export const modalBackgrounds = {
  shop: modalShopBg,
  password: modalPasswordBg,
  win: modalWinBg,
  failed: modalFailedBg,
}

export function getPageBackground(pageKey) {
  return pageBackgrounds[pageKey] || pageBackgrounds.home
}

export function getRoomBackground(roomId) {
  return roomBackgrounds[normalizeKey(roomId)] || roomBackgrounds['stone-gate']
}

export function getItemTypeIcon(type) {
  return itemTypeIcons[normalizeType(type)] || itemTypeIcons.fallback
}

export function getItemIcon(item) {
  if (!item) return itemTypeIcons.fallback

  const itemKey = normalizeKey(item.id || item.key)
  return itemIcons[itemKey] || getItemTypeIcon(item.type)
}

export function getStatusIcon(status) {
  return statusIcons[status] || null
}

export function getUiAsset(assetKey) {
  return uiAssets[assetKey] || null
}

export function getModalBackground(modalKey) {
  return modalBackgrounds[modalKey] || null
}
