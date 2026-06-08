export const mockGameState = {
  title: '芝麻开门',
  chapter: '第一章：石门回声',
  status: '探索中',
  objective: '在火光熄灭前辨认石门上的暗语线索，并规划下一步移动路线。',
  authIntro: {
    title: '芝麻开门',
    subtitle: '暗语开启石门，火光照亮秘窟。',
    briefingTitle: '进入秘窟前，阅读探险简报',
    briefingSubtitle: '带上补给，记住暗语，在火光熄灭前找到出口。',
    story: '一张泛黄古卷被压在石门前，卷面写着进入秘窟的最后提醒。',
    quote: '“欲入秘窟者，须以暗语唤醒石门；贪多者困于黑暗，谨慎者带宝而归。”',
    victory: {
      title: '胜利条件',
      content: '找到正确暗语，开启石门，并在资源耗尽前安全离开秘窟。',
    },
    sections: [
      {
        id: '01',
        title: '石门传闻',
        paragraphs: [
          '沙漠深处的秘窟被古老暗语封印。传说只有找到隐藏在洞室中的线索，才能唤醒沉睡的石门。',
          '你将扮演一名寻宝者，在有限的金钱、体力和负重条件下探索秘窟，收集宝物、补给与关键线索，并在危险降临前安全离开。',
          '秘窟中的每一次前进都伴随着代价：火把会逐渐熄灭，体力会不断下降，背包也会因宝物而变得沉重。你需要在贪婪与谨慎之间做出选择。',
        ],
      },
      {
        id: '02',
        title: '探索目标',
        content: '规划路线、管理资源、收集关键物品，并找到开启石门的暗语。',
      },
      {
        id: '03',
        title: '行动规则',
        points: [
          '移动房间会消耗体力',
          '拾取物品会增加负重',
          '补给可以恢复体力',
          '宝物会影响最终收益',
          '线索可以帮助推断暗语',
          '资源不足可能触发救援或失败',
        ],
      },
    ],
  },
  player: {
    name: '寻宝者',
    gold: 90,
    stamina: 25,
    maxStamina: 30,
    currentWeight: 8,
    maxWeight: 20,
    location: '石门大厅',
  },
  room: {
    id: 'stone_gate',
    name: '石门大厅',
    dangerLevel: '低危',
    description: '潮湿石墙上刻着断裂的古老文字，蓝色传送门在大厅尽头缓慢旋转。两侧火把摇曳，照亮一扇需要暗语开启的厚重石门。',
    exits: [
      {
        direction: '北',
        name: '古卷密室',
      },
      {
        direction: '东',
        name: '补给洞穴',
      },
      {
        direction: '南',
        name: '入口营地',
      },
    ],
    visibleItems: [
      {
        id: 'ancient_scroll',
        name: '破旧古卷',
        type: '线索',
      },
      {
        id: 'torch',
        name: '半截火把',
        type: '补给',
      },
      {
        id: 'blue_portal',
        name: '蓝色传送门',
        type: '机关',
      },
    ],
  },
  inventory: [
    {
      id: 'water_bag',
      name: '水囊',
      type: '补给',
      weight: 2,
      description: '可在体力不足时恢复少量体力。',
    },
    {
      id: 'bronze_key',
      name: '青铜钥匙',
      type: '线索',
      weight: 1,
      description: '钥匙柄上刻着与石门相似的纹路。',
    },
    {
      id: 'small_gem',
      name: '小宝石',
      type: '宝物',
      weight: 5,
      description: '带出秘窟后可用于最终结算。',
    },
  ],
  logs: [
    {
      id: 1,
      time: '09:00',
      content: '支付入场费 10，进入秘窟。',
    },
    {
      id: 2,
      time: '09:05',
      content: '移动到石门大厅，体力减少 5。',
    },
    {
      id: 3,
      time: '09:08',
      content: '发现石门需要正确暗语才能开启。',
    },
    {
      id: 4,
      time: '09:10',
      content: '蓝色传送门发出低鸣，北侧古卷密室似乎有新的线索。',
    },
  ],
  actions: {
    directionActions: [
      {
        id: 'north',
        label: '北',
        hint: '古卷密室',
      },
      {
        id: 'south',
        label: '南',
        hint: '入口营地',
      },
      {
        id: 'east',
        label: '东',
        hint: '补给洞穴',
      },
      {
        id: 'west',
        label: '西',
        hint: '封闭石墙',
      },
    ],
    itemActions: [
      {
        id: 'look',
        label: '查看',
      },
      {
        id: 'take',
        label: '拾取',
      },
      {
        id: 'use',
        label: '使用',
      },
      {
        id: 'drop',
        label: '丢弃',
      },
    ],
  },
}
