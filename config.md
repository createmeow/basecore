# BaseCore 配置说明

配置文件位于 `config/basecore.json`，首次启动游戏时自动生成。修改后**需重启游戏生效**（配置在启动时读取，无热重载命令）。

文件中未出现的字段会自动按默认值补全并回写，因此可以只保留需要修改的条目。

---

## 目录

- [顶层配置](#顶层配置)
- [upgrades — 升级成本](#upgrades--升级成本)
- [balance — 平衡性设置](#balance--平衡性设置)
  - [基础与防御炮](#基础与防御炮)
  - [怪物攻核玩法](#怪物攻核玩法)
  - [苏式基地计算机](#苏式基地计算机)
  - [Rs 能源系统](#rs-能源系统)
- [integration — 联动设置](#integration--联动设置)
  - [冷汗（Cold Sweat）联动](#冷汗cold-sweat联动)
  - [机械动力（Create）联动](#机械动力create联动)

---

## 顶层配置

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `gameplay` | 字符串 | `"modular"` | 游戏模式。<br>`modular`：模块式玩法，右键基地核心打开模块安装界面，模块直接放入核心槽位。<br>`component`：组件（零件）玩法，模块改为用「零件」在升级界面购买/退回，玩家死亡会掉落零件，村民交易改为支付零件。 |
| `part_death_drop_percent` | 整数 | `10` | 组件模式下玩家死亡时掉落身上零件的百分比（0~100）。掉落的零件以「零件包」物品形式掉出，其余保留。仅在 `gameplay` 为 `component` 时生效。 |

---

## upgrades — 升级成本

组件（`component`）模式下，在升级界面购买各模块所需的**零件数量**。模块式玩法不使用本段。

### 基地核心模块

| 配置项 | 对应模块 | 默认值 |
|--------|----------|--------|
| `range_cost` | 范围提升模块（每级 +6 格范围） | 64 |
| `defense_cost` | 防御增强模块（每个 +250 生命值） | 128 |
| `secure_cost` | 安全模块（防破坏，最多 1 个） | 96 |
| `auto_repair_cost` | 自动修复模块（每 15 秒回血） | 160 |
| `thorns_cost` | 荆棘模块（攻击者受到反伤） | 80 |
| `exp_def_cost` | 爆炸保护模块（免爆炸伤害，最多 1 个） | 120 |
| `counter_recon_cost` | 反侦察模块（识破范围内伪装） | 100 |
| `strength_cost` | 伤害提升模块（范围内力量效果） | 72 |
| `jump_boost_cost` | 跳跃提升模块 | 40 |
| `regeneration_cost` | 生命回复模块 | 144 |
| `resistance_cost` | 抗性提升模块 | 96 |
| `dig_speed_cost` | 挖掘速度提升模块 | 56 |
| `movement_speed_cost` | 速度模块 | 48 |
| `dig_slowdown_cost` | 挖掘疲劳模块（对入侵玩家施加） | 64 |
| `weakness_cost` | 虚弱模块（对入侵玩家施加） | 64 |
| `movement_slowdown_cost` | 缓慢模块（对入侵玩家施加） | 64 |

### 防御炮专属模块

| 配置项 | 对应模块 | 默认值 |
|--------|----------|--------|
| `defend_range_cost` | 防御炮范围提升 | 48 |
| `defend_defense_cost` | 防御炮防御增强 | 64 |
| `defend_strength_cost` | 防御炮伤害提升 | 72 |
| `defend_regeneration_cost` | 防御炮生命回复 | 120 |
| `defend_thorns_cost` | 防御炮荆棘 | 80 |
| `defend_auto_repair_cost` | 防御炮自动修复 | 120 |

---

## balance — 平衡性设置

### 基础与防御炮

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `basecore_range` | 整数 | `10` | 基地核心的基础作用范围（格）。范围模块每级再 +6。 |
| `defend_range` | 整数 | `15` | 防御炮的基础攻击范围（格）。 |
| `defend_health` | 整数 | `50` | 防御炮的基础生命值。 |
| `defend_damage` | 小数 | `4.0` | 防御炮的基础伤害。 |
| `defend_attack_interval` | 整数 | `80` | 防御炮攻击间隔，单位 tick（20 tick = 1 秒）。默认 80 = 每 4 秒攻击一次。 |
| `placement_seconds` | 整数 | `30` | 机械（防御炮等）部署/升级所需时长（秒）。 |

### 怪物攻核玩法

以下条目仅在 `monster_attack_core` 开启时生效，默认整体关闭，不影响原有玩法。

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `monster_attack_core` | 布尔 | `false` | 怪物攻击基地核心（占点玩法）总开关。开启后范围内的怪物会仇恨并攻击核心，核心生命值归零即损坏。 |
| `monster_core_damage` | 小数 | `2.0` | 怪物每积累满一轮裂纹对核心造成的伤害。 |
| `monster_crack_seconds` | 整数 | `2` | 一轮裂纹积累的总时长（秒），期间核心表面逐渐出现裂纹。 |
| `monster_attack_max_count` | 整数 | `10` | 同时攻击核心的最大怪物数量。每只怪物独立积累裂纹、独立扣血。 |
| `monster_sense_range` | 整数 | `32` | 怪物可感知核心的距离（格），范围内的怪物会仇恨并走向核心。 |
| `monster_attack_list_mode` | 字符串 | `"all"` | 名单模式：`all`（所有怪物都会攻核）/ `whitelist`（仅名单内的）/ `blacklist`（名单外的）。 |
| `monster_attack_list` | 字符串数组 | `[]` | 怪物实体 ID 名单，如 `["minecraft:zombie", "minecraft:skeleton"]`。仅在 `whitelist` / `blacklist` 模式下生效。 |

### 苏式基地计算机

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `soviet_computer_enabled` | 布尔 | `true` | 苏式基地计算机玩法总开关。关闭后无法放置，已放置的结构停止运行。 |
| `soviet_damage_min_seconds` | 整数 | `180` | 随机损坏的最短间隔（秒）。 |
| `soviet_damage_max_seconds` | 整数 | `600` | 随机损坏的最长间隔（秒）。损坏后需向机器槽位放入电子管修复。 |

### Rs 能源系统

基地核心与苏式计算机运行需要消耗 Rs 能源，用红石（10 Rs）/红石块（90 Rs）补充。能源物品的 Rs 数值可通过数据包覆盖：`data/<namespace>/data_maps/item/energy_value.json`。

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `energy_enabled` | 布尔 | `true` | 能源系统总开关。关闭后基地不再消耗能源、也不接受红石补充（视为永久供能）。 |
| `energy_capacity` | 整数 | `900` | 能源条容量（Rs）。默认 900 Rs = 基地核心 30 分钟 / 苏式计算机 15 分钟。 |
| `basecore_energy_seconds_per_rs` | 整数 | `2` | 普通基地核心每消耗 1 Rs 可运行的秒数。 |
| `soviet_energy_seconds_per_rs` | 整数 | `1` | 苏式基地计算机每消耗 1 Rs 可运行的秒数。 |
| `energy_refuel_seconds` | 整数 | `10` | 自动尝试补充能源的间隔（秒）。优先消耗升级界面的专用能源槽，其次扫描内部容器；剩余容量小于物品提供的 Rs 时不消耗（避免浪费）。 |

---

## integration — 联动设置

未安装对应模组时，相关联动自动失效，开关无意义。

### 冷汗（Cold Sweat）联动

低温会让基地核心的液晶屏失效直至彻底停机。苏式基地计算机使用三进制电路，不受温度影响。

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enabled` | 布尔 | `true` | 冷汗联动开关。关闭后基地核心不受环境温度影响。 |
| `gui_disable_temperature` | 小数 | `-5.0` | 环境温度低于该值（摄氏度）时无法打开基地核心 GUI（液晶屏失效）。 |
| `shutdown_temperature` | 小数 | `-15.0` | 环境温度低于该值（摄氏度）时基地核心彻底停止工作（模块效果、防御全部停止）。 |

### 机械动力（Create）联动

开启后基地核心/防御炮需要应力驱动才能工作，并新增「不间断应力供应器」方块。

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enabled` | 布尔 | `false` | 机械动力联动开关。默认关闭（应力方案尚未定稿）。 |
| `basecore_min_rpm` | 整数 | `64` | 基地核心正常工作所需的最低转速（RPM）。 |
| `basecore_stress` | 整数 | `128` | 单台基地核心占用的应力（SU）。 |
| `defend_min_rpm` | 整数 | `64` | 防御炮正常工作所需的最低转速（RPM）。 |
| `defend_stress` | 整数 | `128` | 单台防御炮占用的应力（SU）。 |
| `supply_air_capacity` | 整数 | `1800` | 不间断应力供应器单次可存储的 air 总量。 |
| `supply_rpm` | 整数 | `32` | 不间断应力供应器的输出转速（RPM）。 |
| `supply_stress` | 整数 | `128` | 不间断应力供应器的输出应力（SU）。 |
| `supply_air_interval_ticks` | 整数 | `40` | 供应器每消耗 1 air 所需的 tick 数（默认 40 tick = 2 秒）。 |
| `supply_link_range` | 整数 | `6` | 供应器与基地核心联动时的搜索半径（在基地范围之外额外增加的格数）。 |

---

## 完整默认配置示例

```json
{
  "gameplay": "modular",
  "part_death_drop_percent": 10,
  "upgrades": {
    "range_cost": 64,
    "defense_cost": 128,
    "secure_cost": 96,
    "auto_repair_cost": 160,
    "thorns_cost": 80,
    "exp_def_cost": 120,
    "counter_recon_cost": 100,
    "strength_cost": 72,
    "jump_boost_cost": 40,
    "regeneration_cost": 144,
    "resistance_cost": 96,
    "dig_speed_cost": 56,
    "movement_speed_cost": 48,
    "dig_slowdown_cost": 64,
    "weakness_cost": 64,
    "movement_slowdown_cost": 64,
    "defend_range_cost": 48,
    "defend_defense_cost": 64,
    "defend_strength_cost": 72,
    "defend_regeneration_cost": 120,
    "defend_thorns_cost": 80,
    "defend_auto_repair_cost": 120
  },
  "balance": {
    "basecore_range": 10,
    "defend_range": 15,
    "defend_health": 50,
    "defend_damage": 4.0,
    "defend_attack_interval": 80,
    "placement_seconds": 30,
    "monster_attack_core": false,
    "monster_core_damage": 2.0,
    "monster_crack_seconds": 2,
    "monster_attack_max_count": 10,
    "monster_sense_range": 32,
    "monster_attack_list_mode": "all",
    "monster_attack_list": [],
    "soviet_damage_min_seconds": 180,
    "soviet_damage_max_seconds": 600,
    "energy_capacity": 900,
    "basecore_energy_seconds_per_rs": 2,
    "soviet_energy_seconds_per_rs": 1,
    "energy_refuel_seconds": 10,
    "energy_enabled": true,
    "soviet_computer_enabled": true
  },
  "integration": {
    "cold_sweat": {
      "enabled": true,
      "gui_disable_temperature": -5.0,
      "shutdown_temperature": -15.0
    },
    "create": {
      "enabled": false,
      "basecore_min_rpm": 64,
      "basecore_stress": 128,
      "defend_min_rpm": 64,
      "defend_stress": 128,
      "supply_air_capacity": 1800,
      "supply_rpm": 32,
      "supply_stress": 128,
      "supply_air_interval_ticks": 40,
      "supply_link_range": 6
    }
  }
}
```
