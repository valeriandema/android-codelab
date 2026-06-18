# SAP Codelab — Location-Based Reminders

An Android app that posts a status-bar notification when you physically reach a location you attached
to a memo. Originally a coding-challenge base, it has been built out into a small but
production-shaped app: a multi-module Clean Architecture, single-activity Jetpack Compose UI, an MVI
presentation layer, Hilt dependency injection, Room persistence, and background geofencing via Google
Play Services.

No API key or account is required. Map tiles come from [OpenStreetMap (osmdroid)](https://github.com/osmdroid/osmdroid),
so you can clone, open in Android Studio, and run.

---

## The feature

- When creating a memo, the user picks a location by tapping a point on a map.
- On save, a **200 m geofence** is registered for that memo.
- When the device enters the geofence — **even if the app is in the background or not running** — a
  notification appears showing the memo title and the first 140 characters of its text.
- The geofence is held by Google Play Services, so it survives the app being killed; a boot receiver
  re-registers geofences after a reboot (Play Services clears them on restart).
- Once a reminder fires, the memo is marked done and its geofence is removed.

---

## Architecture

The project is split into four Gradle modules with dependencies pointing **inward** toward a
framework-free domain (Clean Architecture / dependency inversion):

```
:app        Android entry point — Compose UI (MVI), Hilt host, notifications
   │            depends on :domain, :data, :geofence
   ├── :geofence   Play Services geofencing + boot re-registration   → :domain
   ├── :data       Room persistence, repository implementation, mappers → :domain
   └── :domain     Pure Kotlin/JVM: models, use cases, repository & notifier ports
```

| Module      | Type            | Responsibility                                                                                 |
|-------------|-----------------|------------------------------------------------------------------------------------------------|
| `:domain`   | Pure JVM/Kotlin | `Memo` model, `SaveMemoUseCase` / `MarkMemoDoneUseCase`, and the `IMemoRepository` & `MemoReminder` **ports**. No Android dependencies. |
| `:data`     | Android library | Room database (`MemoEntity`, `MemoDao`), `MemoRepositoryImpl`, and entity↔domain mappers.       |
| `:geofence` | Android library | `GeofenceManager` (register/remove), `GeofenceBroadcastReceiver` (transition handling), `BootReceiver`. |
| `:app`      | Android app     | Single activity, Compose navigation, MVI ViewModels/screens, osmdroid map picker, `Notifier`.   |

### Dependency inversion in practice

The domain declares interfaces that outer layers implement, so business logic never depends on
Android or persistence:

- `IMemoRepository` is declared in `:domain` and implemented by `MemoRepositoryImpl` in `:data`
  (bound via Hilt `@Binds`).
- `MemoReminder` is declared in `:domain` and implemented by `Notifier` in `:app`. This lets the
  `:geofence` module trigger a notification without depending on the UI layer.

### Presentation: MVI

Screens follow a unidirectional **Model-View-Intent** pattern built on a small base class
(`mvi/Mvi.kt`):

- **State** — one immutable `UiState` per screen; the Composable is a pure function of it.
- **Intent** — the only way a view talks to its ViewModel (`onIntent`).
- **Effect** — one-shot side effects (navigation, messages) delivered via a buffered `Channel`,
  collected lifecycle-aware so they are not replayed on configuration change.

`MviViewModel<I, S, E>` owns a `StateFlow<S>` and an effects `Flow<E>`; subclasses reduce state with
`setState { … }` and emit with `sendEffect(…)`. Each screen has a `*Contract.kt` (state/intent/effect),
a `*ViewModel.kt`, and a `*Screen.kt`.

### Navigation: single activity + Compose

`MainActivity` is the only activity. `CodelabNavHost` defines all destinations:

- `home` — list of memos with an open/all filter
- `create` — create a memo
- `detail/{memoId}` — view a memo
- `map?lat=&lng=` — osmdroid location picker

The map picker returns the chosen coordinates to the create screen via the previous back-stack
entry's `SavedStateHandle`. Notification taps deep-link into `detail/{memoId}` through an intent
extra handled in `MainActivity`.

### Dependency injection

Hilt wires everything at the `SingletonComponent` level. Coroutine dispatchers and the
application-wide `CoroutineScope` are injected behind `@IoDispatcher` / `@ApplicationScope`
qualifiers (declared in `:domain`) so they can be swapped in tests.

### Build logic

Versions are centralized in a Gradle **version catalog** (`gradle/codelab.versions.toml`), and shared
build configuration lives in **convention plugins** under `buildSrc/` (`codelab.android.application`,
`codelab.android.library`, `codelab.jvm.library`, `codelab.hilt`, `codelab.android.room`). Adding a
module means applying the relevant convention plugin rather than copy-pasting config.

---

## Tech stack

- **Kotlin** 2.2 · **AGP** 8.12 · **compileSdk** 36 · **minSdk** 27
- **Jetpack Compose** (Material 3, Navigation Compose), single-activity
- **Hilt** for DI · **Room** for persistence · **Coroutines/Flow**
- **osmdroid** for the OpenStreetMap location picker
- **Google Play Services Location** for geofencing
- **Tests:** JUnit4, MockK, Turbine, kotlinx-coroutines-test (ViewModels, use cases, repository, mappers)

---

## Building & running

1. Open the project in Android Studio (tested with Narwhal Feature Drop; newer versions generally work).
2. The build uses a **JDK 21 toolchain** (`gradle/gradle-daemon-jvm.properties`). The
   `foojay-resolver` plugin downloads a matching JDK if none is installed; alternatively point
   `JAVA_HOME` at Android Studio's bundled JBR.
3. The only machine-specific file is `local.properties` (the Android SDK path), which Android Studio
   generates automatically.
4. Run the `app` configuration.

> If you have the NDK plugin installed, disable it for this project to avoid build errors.

### Trying it on an emulator

Geofence transitions on a real device can take a few minutes (Android throttles background location).
On an emulator the trigger is immediate: create a memo with a location, then open
**Extended controls → Location** and set coordinates within 200 m of the picked point (or run
`adb emu geo fix <lng> <lat>`). The reminder notification appears in the status bar.

### Permissions

The app requests, at runtime when needed:

- `ACCESS_FINE_LOCATION` — register and monitor geofences
- `ACCESS_BACKGROUND_LOCATION` (API 29+) — fire reminders when the app is closed
- `POST_NOTIFICATIONS` (API 33+) — show the reminder notification

---

## Testing

```bash
./gradlew test          # JVM unit tests across :app, :domain, :data
```

Coverage focuses on logic that benefits from it: use cases and validation (`:domain`), the repository
and mappers (`:data`), and the MVI ViewModels (`:app`), using fakes for the repository/DAO and Turbine
for asserting on state/effect flows.

---

## Original challenge brief

This repository began as the base for the "Location Based Notifications" coding challenge:

> When creating a new memo, the user provides a location by selecting a point on a map. The memo is
> saved. Once the user physically reaches that location (within 200 m of the selected point), a
> notification is displayed in the status bar containing the title and the first 140 characters of the
> note text, plus an icon. The feature must work when the app is in the background or not running.
