# VsTV 手机增强版

[![构建状态](https://github.com/saymealoud/vstv/actions/workflows/mobile-player.yml/badge.svg?branch=feat%2Fmobile-player)](https://github.com/saymealoud/vstv/actions/workflows/mobile-player.yml)
[![下载量](https://img.shields.io/github/downloads/saymealoud/vstv/total)](https://github.com/saymealoud/vstv/releases)
[![License](https://img.shields.io/github/license/saymealoud/vstv)](LICENSE)

由 **saymealoud** 维护的 VsTV 分支，重点改善安卓手机和平板的直播播放体验。

**[下载本仓库安装包](https://github.com/saymealoud/vstv/releases)** · [播放器源码](https://github.com/saymealoud/vstv/tree/feat/mobile-player) · [构建记录](https://github.com/saymealoud/vstv/actions/workflows/mobile-player.yml)

> 首页 `main` 分支用于说明；可构建的 Android 源码在 `feat/mobile-player` 分支。本分支基于公开的 **v2.2.4** 源码，不包含上游 2.7 / 2.8 的全部功能。

## 下载与安装

1. 打开 **[saymealoud/vstv Releases](https://github.com/saymealoud/vstv/releases)**，选择本仓库发布的版本。
2. 在该版本的 **Assets** 中下载 `vstv-mobile-test-arm.apk`，不要选择自动生成的 Source code 压缩包。
3. 在安卓设备上安装，桌面名称为 **VsTV 手机测试版**。
4. 首次打开后自行添加直播源。应用不内置或提供频道内容。

测试包包名为 `com.vesaa.mytv.mobiletest`，使用 debug 签名，可与官方 VsTV 并存。它不会覆盖官方版本，也不会自动导入官方版本的配置。

请以本仓库 Releases 的说明和附件为准；本页下载按钮、徽章和发行入口均属于 **saymealoud/vstv**。

## 手机播放操作

| 操作 | 功能 |
| --- | --- |
| 屏幕左侧上下滑 | 调整当前窗口亮度，显示百分比 |
| 屏幕右侧上下滑 | 调整媒体音量，可调至静音 |
| 单击画面 | 显示或隐藏控制栏，闲置 5 秒自动隐藏 |
| 上一台 / 下一台 / 选台 | 切换频道；上下滑动不再误切频道 |
| 左右滑动 | 切换线路 |
| 横 / 竖屏 | 手动切换屏幕方向 |
| 自动旋转 / 锁定旋转 | 跟随系统旋转设置或固定当前方向 |
| 锁屏 / 解锁屏幕 | 防止误触播放器控件 |
| 小窗 | 进入系统画中画，支持 Android 8+ 且设备需允许画中画 |
| 双击 / 长按 | 打开快捷菜单 / 直播源选择 |

手机、平板首次安装默认开启离开时自动小窗，可在界面设置关闭。分屏、设置或选台界面不自动进入小窗。电视仍保留横屏和遥控器操作。

## 当前测试版范围

- 已通过云端编译和手势单元测试；仍需真机验证不同品牌的旋转、音量及画中画行为。
- 测试包使用系统解码器，不包含 FFmpeg 扩展软解。部分必须软解的频道可能无法播放。
- Releases 提供手机测试 APK；其他架构或正式签名版本仅在有对应附件时可用。
- 原有直播源、节目单、多线路、回看、精选和分屏等能力以此分支实际实现为准。

## 开发与构建

```sh
git clone --branch feat/mobile-player https://github.com/saymealoud/vstv.git
cd vstv
bash ./gradlew :app:testOriginalArmDebugUnitTest :app:assembleOriginalArmDebug -PsystemDecodersOnly=true
```

环境：JDK 17、Android SDK 35。完整操作和验收说明见 [MOBILE_PLAYER.md](https://github.com/saymealoud/vstv/blob/feat/mobile-player/MOBILE_PLAYER.md)。

[Mobile player test APK](https://github.com/saymealoud/vstv/actions/workflows/mobile-player.yml) 工作流构建 ARM 测试包。正式 Release 工作流仅向当前仓库发布，需要自行配置签名；不会向原作者仓库发布。

## 来源与许可

本项目基于 VsTV 公开源码继续维护，保留原作者 vesaaa 以及此前贡献者的版权、许可与致谢，遵循 [MIT License](LICENSE)。来源说明不作为本分支安装包的下载入口。

应用仅作为播放器，不提供直播源或节目内容。请自行导入已获得合法授权的地址。
