# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 协作模式

本项目采用 ask/chat 模式为主。所有文件操作（创建、编辑、删除）需先获得我的明确许可后再执行。

## Build Commands

```bash
# 构建（打包 shadowJar）
./gradlew build

# 仅编译
./gradlew compileJava

# 清理构建产物
./gradlew clean
```

构建产物位于 `build/libs/DiscBox-<version>.jar`。

## 项目概述

这是一个 **Spigot 1.21 服务端插件**（非 Mod），允许 Minecraft 玩家在服务器上播放 NBS 格式音乐。本质是一个调用 Spigot API 的普通 Java 项目，不需要反混淆/重映射/MDK。

### 技术栈

| 层面 | 技术 |
|------|------|
| 构建 | Gradle + Kotlin DSL + Shadow |
| JDK | 21 |
| API | Spigot 1.21.1 |
| 音乐引擎 | NoteBlockAPI 1.6.2（JitPack） |
| 数据库 | YAML 文件存储 / 可选 MySQL |
| 统计 | bStats（已 shade） |
| 占位符 | PlaceholderAPI |
| 多语言 | 16 种语言 YAML 文件（自动加载） |

## 架构概览

```
src/main/java/fr/skytasul/music/
├── JukeBox.java              # 主类 (extends JavaPlugin)，生命周期、配置、事件
├── JukeBoxDatas.java          # 玩家数据管理（YAML 序列化 / SQLite/MySQL）
├── JukeBoxInventory.java      # GUI 系统（Bukkit Inventory 事件驱动）
├── PlayerData.java            # 每个玩家的播放状态、Smart Random、BossBar
├── CommandMusic.java          # /music 玩家命令
├── CommandAdmin.java          # /adminmusic 管理员命令
└── utils/
    ├── CustomSongPlayer.java  # 封装 NoteBlockAPI 的播放器
    ├── Database.java          # 数据库连接抽象（MySQL）
    ├── JukeBoxRadio.java      # 电台模式
    ├── Lang.java              # 多语言系统（反射加载静态字段）
    ├── Particles.java         # 粒子效果工具
    ├── Placeholders.java      # PlaceholderAPI 扩展
    └── Playlists.java         # 播放列表模式枚举
```

### 核心流程

1. **启动** → `JukeBox.onEnable()` → 加载配置/语言 → 扫描 `songs/` 目录下的 NBS 文件 → 初始化玩家数据
2. **播放** → 玩家点歌 → `PlayerData.playSong()` / `playDirectoryPlaylist()` → `CustomSongPlayer`（封装 RadioSongPlayer）→ NoteBlockAPI 播放
3. **Smart Random** → 从目录中按权重选择未播放过的歌曲，30 分钟内不重复，支持 Repeat All
4. **数据持久化** → YAML 或 MySQL 存储玩家设置/收藏列表
5. **BossBar** → 播放时显示进度条，每 0.5 秒更新，支持 /music bar 开关

### 播放列表层级

```
songs/                           # 歌曲根目录
├── PlaylistA/                   # 每个子目录 = 一个播放列表
│   ├── song1.nbs
│   └── song2.nbs
└── PlaylistB/
    └── song3.nbs

default_playlist.yml             # 新玩家默认收藏列表（按内部名称引用）
```

播放列表按目录名排序，在 GUI 中分页显示（每页 45 个播放列表）。

## 已知问题 / Todo

- **线程安全**: `PlayerData` 事件处理中存在一些潜在的并发问题（如 `onSongEnd` 与 `stopPlaying` 的竞态）
- **内存**: `recentlyPlayedTracker`（Map）随播放列表增多而增长，目前没有上限清理
- **NoteBlockAPI 限制**: `CustomSongPlayer` 在 PlayerQuit 后不会自动清理，可能导致泄漏
