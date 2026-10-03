# DiPlay 自訂版本

更新日期：2026-10-03。本文件以目前交付的 release 為準，取代初版 debug 說明。

## 正式版與安裝

- APK：`DiPlay-0.2.10-custom-release.apk`（由本機交付，未發布 GitHub Release）
- 版本：`0.2.10-ambient-custom`（versionCode 29）
- 套件：`com.shihab.diplay.custom`
- SHA-256：`8c181fffe6b01cbb82fa158e10d12eb1b5a2406bb3072ac589a2daccb59adc67`

使用者已在 Samsung Galaxy S22 Ultra 上安裝本次 release，並回報一切正常。
這是使用自己簽章的獨立正式版，與作者版、debug 版並存，不會覆蓋它們。
首次安裝需要重新設定偏好；使用時請關閉其他 DiPlay 版本。

## 設定操作

開啟 DiPlay → 設定 → 顯示與效能。

| 設定 | 選項或範圍 | 生效方式 |
| --- | --- | --- |
| CarPlay 日夜模式 | 跟隨 Android、自動環境光、固定日間、固定夜間 | 儲存後返回 CarPlay，不需重新連線 |
| 環境光切換門檻 | 1–200000 lux 的整數，預設 30 lux | 只適用自動模式，返回 CarPlay 後重新觀察 |
| 日夜切換延遲 | 0–60 秒的整數，預設 2 秒；0 表示立即切換 | 返回 CarPlay 後重新觀察 |
| 解析度 | 30–100% 的整數，可輸入 55%、65% 等數值；預設 100% | 連線中套用並重新連線；未連線時下次生效 |

自動模式低於門檻為夜間，等於或高於門檻為日間，沒有中間維持區間。
光線必須持續位於新狀態一側達到設定時間才切換；中斷或無效讀值取消計時。
使用 `Sensor.TYPE_LIGHT`，依 Activity lifecycle 註冊及解除；背景期間保留狀態。
沒有光感測器或註冊失敗時跟隨 Android。
僅控制傳給 CarPlay 的 night mode，不修改 Samsung／Android 全系統主題。

解析度寬、高按百分比縮放並維持偶數像素對齊，舊設定會作為新設定初始值。
UI 大小仍同時受到 iPhone 版面規則及既有 CarPlay 大小設定影響。

準備畫面依可用高度調整圖示、字級與間距，保留系統列與螢幕缺口的安全邊距。
橫向低高度螢幕使用緊湊排版；內容超過可用高度時縮放置中，不可上下捲動，避免上下裁切。直向維持原有尺寸與置中效果。

## 專案與提交狀態

- 本機專案：`C:\Users\benny\.vscode\Code\DiPlay`
- Fork：[bennylee1029/DiPlay](https://github.com/bennylee1029/DiPlay)
- 分支：`ambient-light`
- `origin`：`https://github.com/bennylee1029/DiPlay.git`
- `upstream`：`https://github.com/shihabal3amri/DiPlay.git`
- 最後已提交、已推送版本：`99e0bfff7dbbac1387d8077dcbe6bfa5690fc5bc`（加入 50% 選項）。
- 四種日夜模式較早提交：`988dccb4fee7d5f4abe3b1becd25e571ee0e4047`。

單一 lux 門檻、自訂延遲、自訂解析度、排版修正及 release 設定隨本文件一同提交至 `ambient-light`。
上列提交編號是先前版本；本次修改以本文件所在的 Git 提交為準。
`main` 保留作 upstream 同步分支。未修改原作者 repository，也未向原作者開 PR。
本次交付本機 APK，未發布 GitHub Release。既有 LICENSE、AGPL UI 標示及 notices 保留。

## 主要修改檔案

以下路徑相對於本機專案：

| 檔案 | 用途 |
| --- | --- |
| `common/src/main/java/com/shilapi/xcertplay/CarPlayNightMode.kt` | 四種模式、獨立環境光控制器及可調延遲 |
| `common/src/main/java/com/shilapi/xcertplay/AndroidAmbientLight.kt` | Android 光感測器與計時介接 |
| `common/src/main/java/com/shilapi/xcertplay/AmbientLightThreshold.kt` | 單一 lux 門檻及範圍 |
| `common/src/main/java/com/shilapi/xcertplay/CarPlayHostActivity.kt` | lifecycle、設定套用、準備畫面及解析度整合 |
| `common/src/main/java/com/shilapi/xcertplay/AirPlayPersistence.kt` | SharedPreferences 保存及舊設定相容 |
| `common/src/main/java/com/shilapi/xcertplay/DiPlayActivity.kt` | 日夜、lux、延遲及百分比設定介面 |
| `common/src/main/res/values*/strings_night_mode.xml`、`strings_ambient_threshold.xml`、`strings_custom_settings.xml` | 設定文字與說明 |
| `shared/src/main/java/com/shilapi/xcertplay/airplay/CarPlayDisplayScale.kt` | 任意整數百分比縮放及偶數像素對齊 |
| `common/src/test/java/com/shilapi/xcertplay/CarPlayNightModeControllerTest.kt`、`CarPlayNightModePersistenceTest.kt`、`AmbientLightThresholdTest.kt` | 控制器、儲存、延遲、門檻及 lifecycle 測試 |
| `shared/src/test/java/com/shilapi/xcertplay/airplay/CarPlayDisplayScaleTest.kt` | 自訂縮放測試 |
| `mobile/build.gradle.kts` | 自訂 release 套件、簽章設定及明確指定的認證資料 |

沿用 `AirPlaySession.setNightMode(Boolean)`、event channel 及 `pendingNightMode` 機制。

## 驗證與認證資料

- 原始碼：550 項測試通過，0 失敗、0 錯誤、0 跳過。
- Debug 建置及應用層級 lint 通過。
- Release 建置成功，release lint 0 錯誤。
- 正式 APK 簽章有效，已確認不可 debug。
- 使用者先確認新版 debug 正常，再確認本次 release 正常。

認證資料取自作者公開的 `v0.2.10` APK，兩個檔案與已確認正常的 debug APK 逐位元組一致。
保存在 `.private/runtime-auth/offline-mfi/`，透過 `DIPLAY_AUTH_ASSETS_DIR` 明確加入 APK。
這些檔案由 Git 忽略，未提交或上傳。
初期不含認證資料的 source-only debug 已不作為目前安裝建議。

自己的 RSA 3072-bit 正式簽章資料保存在本機：

- 金鑰：`.private/signing/diplay-custom-release.p12`
- 密碼：`.private/signing/password.dpapi`，由 Windows 使用者保護。

請保留這些檔案。後續正式版更新須使用同一金鑰，並增加 versionCode。
DPAPI 密碼檔不可假設能直接移到其他電腦或 Windows 帳號解密；移機前需處理可恢復的金鑰備份。

## 未來同步 upstream

如有新的本機修改，先保存並提交；私有認證與簽章檔案不可加入提交。
確認工作目錄乾淨後，再同步：

```powershell
Set-Location 'C:\Users\benny\.vscode\Code\DiPlay'
git fetch upstream
git switch main
git merge --ff-only upstream/main
git push origin main
git switch ambient-light
git merge upstream/main
# 解決衝突，重跑測試、lint 與建置後才推送。
git push origin ambient-light
```

環境光控制器獨立於 Activity；也可在提交本機修改後，以 cherry-pick 移植至新的分支。
