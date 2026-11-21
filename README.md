# Chain Keyboard — کیبورد چین

A Samsung-style Android keyboard (IME) with Persian + English layouts,
Microsoft Translator (never Google), clipboard history and screenshot paste.

کیبوردی شبیه کیبورد سامسونگ با چیدمان فارسی و انگلیسی، مترجم مایکروسافت
(نه گوگل)، تاریخچه کلیپ‌بورد و چسباندن اسکرین‌شات.

## Features / قابلیت‌ها

- **Samsung look, dark theme** — black background, dark-gray rounded keys,
  top icon toolbar (emoji / translate / clipboard / settings / more).
  ظاهر تیره شبیه کیبورد سامسونگ، کلیدهای خاکستری تیره گرد، نوار آیکون بالا.
- **Persian layout (exact)** — digits ۱–۰, rows چ ج ح خ ه ع غ ف ق ث ص ض /
  پ م ن ت ا ل ب ی س ش / گ ک و د ر ز ط, wide space bar labelled فارسی,
  globe key to switch languages, cursor-move key, `!#1` symbols page.
  چیدمان فارسی دقیقاً مثل اسکرین‌شات کیبورد سامسونگ.
- **English (US) layout (exact)** — qwerty rows, space bar labelled
  `English (US)`, `!#1` symbols page.
- **Emoji panel** — full system emojis, category tabs
  (smileys, animals, food, activities, travel, objects, symbols, flags),
  recents row and search. ایموجی‌های کامل سیستم با دسته‌بندی و جستجو.
- **Clipboard panel** — history of copied texts (max 50, persisted),
  tap to paste, long-press to pin, trash icon clears unpinned items.
  تاریخچه کلیپ‌بورد با سنجاق و پاک کردن.
- **Screenshot paste** — take a screenshot while the keyboard is open and
  a “Paste” pill (thumbnail + Paste) appears at the top, like Samsung’s.
  Tapping it inserts the image with `InputConnection.commitContent`
  (`image/png`); if the target app does not accept images, a short text
  note is inserted instead. Screenshots are also added to the clipboard
  panel. Needs the media permission (granted in Settings).
  با گرفتن اسکرین‌شات، دکمه «چسباندن» بالای کیبورد ظاهر می‌شود.
- **Microsoft Translator** — translate panel inside the keyboard
  (Persian ↔ English by default, more languages available).
  Default transport is the public Bing endpoint
  `https://www.bing.com/ttranslatev3` — no key required.
  Optionally enter your own **Azure Translator** key + region + endpoint
  in Settings to use Azure instead. The key is stored only on the device.
  مترجم مایکروسافت، نه گوگل؛ بدون کلید از سرویس بینگ استفاده می‌کند
  و کلید آژور اختیاری است.
- **Settings** — enable-keyboard guide, default language, clipboard /
  screenshot / vibration / sound toggles, Azure key fields.

## Install / نصب

1. Download `app-debug.apk` from the **Actions** tab
   (workflow “Build debug APK”, artifact `chain-keyboard-debug`)
   or build it yourself (below) and install the APK on the phone.
2. Open **Chain Keyboard** (the app icon) → **1. Open keyboard settings** →
   enable **Chain Keyboard** in the keyboard list.
3. Tap **2. Select Chain Keyboard** (or long-press the globe key in any
   app → choose Chain Keyboard).
4. In Settings, tap **Grant media permission** so screenshot paste works.
5. Optional: enter an Azure Translator key for the translator.

به فارسی: فایل APK را نصب کنید، برنامه «کیبورد چین» را باز کنید،
از بخش «فعال کردن کیبورد» آن را در لیست کیبوردها فعال کنید،
بعد با دکمه «انتخاب کیبورد» آن را به‌عنوان کیبورد پیش‌فرض انتخاب کنید
و دسترسی به عکس‌ها را بدهید تا چسباندن اسکرین‌شات کار کند.

> Tested target: Samsung Galaxy A34, Android 17.
> هدف تست: گوشی سامسونگ A34 با اندروید ۱۷.

## Build / ساخت

```sh
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Requirements: JDK 17, Android SDK (compileSdk 35, minSdk 26,
targetSdk 35). One library only — AndroidX RecyclerView (emoji and
clipboard grids). Translation uses plain `HttpURLConnection` and
`org.json` (built into Android); no network libraries, no paid deps.

## Project layout

```
app/src/main/
  AndroidManifest.xml
  java/dev/chain/keyboard/
    ChainKeyboardService.kt   InputMethodService, keys, panels, paste
    KeyboardLayouts.kt        Persian / English / symbols layouts
    EmojiData.kt              Emoji catalogue (Unicode, system-rendered)
    EmojiPanel.kt             Emoji panel (tabs, recents, search)
    ClipboardStore.kt         Persisted clipboard history (max 50)
    ClipboardPanel.kt         Clipboard grid UI (pin / clear)
    ScreenshotWatcher.kt      MediaStore observer for new screenshots
    MicrosoftTranslator.kt    Bing no-key + optional Azure transport
    TranslatePanel.kt         In-keyboard translate UI
    SettingsActivity.kt       Settings screen
    Prefs.kt                  SharedPreferences settings
  res/
    layout/keyboard_root.xml  Toolbar + Paste pill + panels + keys
    layout/activity_settings.xml
    drawable/                 Key / toolbar / pill backgrounds
    values/strings.xml        English strings
    values-fa/strings.xml     Persian strings
    xml/method.xml            Input-method subtypes (fa, en_US)
```

## Notes / نکته‌ها

- The cursor-move key (↔) opens a small arrow pad (◀ ▶ ▲ ▼ + Done)
  because a draggable cursor key cannot be done reliably with plain
  buttons. کلید حرکت مکان‌نما یک صفحه کوچک با چهار جهت باز می‌کند.
- Shift: single tap = one capital / Persian alt letter; double-tap =
  caps lock. Backspace: long-press repeats.
- Emoji are the phone’s own system emojis — the panel never needs an
  emoji download.
- No data leaves the phone except translation requests to Microsoft
  (Bing or Azure, whichever is configured) and nothing is uploaded
  for screenshots or clipboard.
```

## License

MIT — see `LICENSE` (add one before publishing if you want the repo public
under a licence).
