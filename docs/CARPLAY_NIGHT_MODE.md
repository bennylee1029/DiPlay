# Custom CarPlay day/night mode

This fork adds a CarPlay-only display setting. Existing licenses, attribution, and
third-party notices remain in effect. It does not change the Android/Samsung theme.

## Use

Open DiPlay → Settings → Display and performance → CarPlay day/night mode.
Choose an option and tap Save, then return to CarPlay. No reconnect is required.
From CarPlay, the existing three-finger downward swipe opens DiPlay settings.

- Follow Android system (default): uses Android's current day/night configuration.
- Automatic (ambient light): uses `Sensor.TYPE_LIGHT` while the CarPlay Activity is resumed.
- Always day: sends `false` to CarPlay.
- Always night: sends `true` to CarPlay.

Automatic mode starts with the current CarPlay state (Android state on a fresh launch).
While day is active, below 20 lux continuously for five seconds switches to night.
While night is active, above 100 lux continuously for five seconds switches to day.
Exactly 20 and 100 lux, and values between them, preserve the current state and cancel
any pending transition. Invalid readings also cancel pending transitions.
A delayed callback supports on-change sensors that emit nothing while light is stable.

Pausing or destroying the Activity unregisters the listener and cancels its timer.
Resuming starts a fresh observation interval, retaining the previous day/night state.
Without a light sensor, or when registration fails, automatic mode follows Android.
When CarPlay is in the background, ambient observation pauses and its last state remains.
Settings save through the existing SharedPreferences store and survive app restarts.

## Implementation and validation

`CarPlayNightMode.kt` contains the Android-independent controller and stable preference keys.
`AndroidAmbientLight.kt` adapts the Android sensor and main-looper timer.
`CarPlayHostActivity` owns lifecycle and continues to call `AirPlaySession.setNightMode`.
The existing event channel and `pendingNightMode` retry path are unchanged.
`DiPlayActivity` uses its existing choice dialog, with reconnect disabled for this setting.

Run the controller/persistence tests, Android lint, and identity-free debug build:

```sh
./gradlew :common:testDebugUnitTest :mobile:lintDebug :mobile:assembleDebug
```

No private accessory identity or release signing key is needed for source tests.
A source-only debug APK is not a provisioned standalone CarPlay receiver.
Hardware checks still need an Android light sensor and a connected iPhone: cover/uncover
the sensor, interrupt the five-second window, switch all four modes, pause/resume, and
reconnect while a selected mode is pending. Verify Android's own theme stays unchanged.

## Keep up with upstream

`origin` is `bennylee1029/DiPlay`; `upstream` is `shihabal3amri/DiPlay`.
Keep `main` free of custom commits. For a published custom branch, merge upstream:

```sh
git fetch upstream
git switch main
git merge --ff-only upstream/main
git push origin main
git switch ambient-light
git merge upstream/main
# Resolve conflicts and rerun the checks above.
git push origin ambient-light
```

For a personal unpublished branch, rebasing onto `upstream/main` is also possible.
Before rewriting a published branch, create a backup and coordinate with other users;
use `--force-with-lease`, never an unconditional force push.
