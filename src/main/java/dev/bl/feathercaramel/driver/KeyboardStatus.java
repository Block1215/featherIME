/*
 * featherIME - IME support for Minecraft, compatible with Feather Client.
 * Based on caramelChat by LemonCaramel <https://github.com/LemonCaramel/caramelChat>.
 *
 * Copyright (C) 2023 LemonCaramel
 * Copyright (C) 2024-2026 @Bl (modifications)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.bl.feathercaramel.driver;

/**
 * キーボードレイアウト・IME状態のスナップショット。
 */
public record KeyboardStatus(Language language, boolean useNative) {

    @Override
    public Language language() {
        return useNative() ? language : Language.ENGLISH;
    }

    public String display() { return language().display; }
    public float  offset()  { return language().offset; }

    public enum Language {
        ENGLISH("ENG", 0.5f),
        KOREAN("한", 0.0f),
        JAPANESE("あ", 0.5f),
        CHINESE_SIMPLIFIED("中", 0.5f),
        CHINESE_TRADITIONAL("中", 0.5f),
        OTHER("Native", 0.5f);

        public final String display;
        public final float  offset;

        Language(String d, float o) { display = d; offset = o; }
    }
}
