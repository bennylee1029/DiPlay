# Custom CarPlay day/night mode

This fork adds a CarPlay-only display setting. Existing licenses, attribution, and
third-party notices remain in effect. It does not change the Android/Samsung theme.

## Use

Open DiPlay → Settings → Display and performance → CarPlay day/night mode.
Choose an option and tap Save, then return to CarPlay. No reconnect is required.
The adjacent Ambient light threshold dialog accepts one whole lux value from 1 to
200000, defaulting to 30 lux. Save persists the value; Cancel keeps the old value.
Reset default fills in 30 lux; tap Save to confirm. The threshold applies only in
Automatic mode. The adjacent transition delay accepts 0–60 whole seconds (default 2);
0 switches immediately. Both directions use the saved delay.
From CarPlay, the existing three-finger downward swipe opens DiPlay settings.

- Follow Android system (default): uses Android's current day/night configuration.
- Automatic (ambient light): uses `Sensor.TYPE_LIGHT` while the CarPlay Activity is resumed.
- Always day: sends `false` to CarPlay.
- Always night: sends `true` to CarPlay.

Automatic mode starts with the current CarPlay state (Android state on a fresh launch).
Below the configured threshold continuously for the configured delay selects night.
At or above it continuously for the configured delay selects day. There is no intermediate
lux range. Crossing back before the configured delay cancels the pending transition;
invalid readings also cancel it. A new crossing starts a fresh observation interval.
A delayed callback supports on-change sensors that emit nothing while light is stable.

Pausing or destroying the Activity unregisters the listener and cancels its timer.
Resuming starts a fresh observation interval, retaining the previous day/night state.
Without a light sensor, or when registration fails, automatic mode follows Android.
When CarPlay is in the background, ambient observation pauses and its last state remains.
Settings save through the existing SharedPreferences store and survive app restarts.

## Custom resolution and preparation screen

Resolution accepts any integer percentage from 30 to 100, including 55 or 65.
Applying it reconnects an active CarPlay session through the existing flow.
Pixel dimensions remain even, and physical dimensions are preserved.
Existing resolution settings provide the initial value until a custom value is saved.

The preparation screen interpolates sizes across 240–480 dp of safe viewport height,
slightly enlarging short-screen content while preserving regular-screen sizes. It adapts to short landscape viewports, respects system-bar and
display-cutout insets, and scrolls when large fonts or long text exceed the available height.

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
the sensor, interrupt the configured observation window, switch all four modes, pause/resume, and
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
