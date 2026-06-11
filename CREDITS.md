# Credits & License Attribution

**featherIME**
Copyright (C) 2024-2026 @Bl

featherIME is a **derivative work**. You MUST comply with **both** licenses below.
Full license texts are in the [`LICENSES/`](LICENSES/) directory.

---

## 1. caramelChat — LGPL-3.0-or-later

- Copyright (C) 2023 LemonCaramel
- Source: https://github.com/LemonCaramel/caramelChat
- License: GNU Lesser General Public License v3.0 or later
  (`LGPL-3.0-or-later`) — see [`LICENSES/LGPL-3.0-or-later.txt`](LICENSES/LGPL-3.0-or-later.txt)
  and [`LICENSES/GPL-3.0-or-later.txt`](LICENSES/GPL-3.0-or-later.txt)

Most of the Java source code in this mod (the `wrapper/`, `controller/`,
`driver/`, `util/` packages and the IME-related mixins) is ported/adapted
from caramelChat. The original package `moe.caramel.chat` was relocated to
`dev.bl.feathercaramel`.

### Summary of modifications from caramelChat
- Relocated package namespace (`moe.caramel.chat` -> `dev.bl.feathercaramel`)
- Removed Architectury; pure Fabric implementation
- Made all mixins non-required (`require = 0`) for Feather Client startup
- Pre-edit underline rendering moved out of required mixins
- Added `dev.bl.feathercaramel.driver.arch.win.Imm32` (JNA bindings to
  `imm32.dll`) for [ENG]/[あ] IME conversion-mode detection and control
- Cursor-position handling fixes in `AbstractIMEWrapper`
- Sign / Book edit screen IME handling via `WrapperTextFieldHelper`
- Creative search box refresh-on-commit (`MixinCreativeModeInventoryScreen`)

---

## 2. CocoaInput-lib (CocoaInput Native) — MMPL_J 1.0.1

- Maintainer: Korea-Minecraft-Forum
- Source: https://github.com/Korea-Minecraft-Forum/CocoaInput-lib
- Original CocoaInput by Axeryok: https://github.com/Axeryok/CocoaInput
- Wayland part ("caramelChat Library") by LemonCaramel
- License: Minecraft Mod Public License Japanese Translation (MMPL_J)
  Version 1.0.1 — see [`LICENSES/MMPL_J-1.0.1.txt`](LICENSES/MMPL_J-1.0.1.txt)
  (identical to the LICENSE file of the CocoaInput-lib repository)

All four bundled native libraries are built from this repository:
`libwincocoainput.dll` (Windows), `libdarwincocoainput.dylib` (macOS),
`libx11cocoainput.so` (Linux/X11), and `libcaramelchatwl.so` (Linux/Wayland).

---

## Provenance of the bundled native libraries (corresponding source)

The four native libraries bundled in this jar are **unmodified copies of the
binaries distributed with caramelChat** (https://github.com/LemonCaramel/caramelChat),
which are built from CocoaInput-lib. featherIME did not rebuild them.
Their corresponding source code is available at:

| Library                      | License       | Source code |
|------------------------------|---------------|-------------|
| `libwincocoainput.dll`       | MMPL_J 1.0.1  | https://github.com/Korea-Minecraft-Forum/CocoaInput-lib (`src/win`) |
| `libdarwincocoainput.dylib`  | MMPL_J 1.0.1  | https://github.com/Korea-Minecraft-Forum/CocoaInput-lib (`src/darwin`) |
| `libx11cocoainput.so`        | MMPL_J 1.0.1  | https://github.com/Korea-Minecraft-Forum/CocoaInput-lib (`src/x11`) |
| `libcaramelchatwl.so`        | MMPL_J 1.0.1  | https://github.com/Korea-Minecraft-Forum/CocoaInput-lib (`src/wayland`) |

The featherIME source repository also contains copies of these C sources
(`src/win`, `src/x11`, `src/darwin`, `src/wayland`, taken from CocoaInput-lib,
plus added JNI bridge files). These are experimental and are **not** what the
bundled binaries were built from.

---

## License of this combined work

The Java code of featherIME is licensed under **LGPL-3.0-or-later**.
The bundled native libraries are licensed under **MMPL_J 1.0.1**.
As permitted by LGPL-3.0 section 4 (Combined Works), these components are
distributed together in a single jar.

THERE IS NO WARRANTY, to the extent permitted by applicable law.
