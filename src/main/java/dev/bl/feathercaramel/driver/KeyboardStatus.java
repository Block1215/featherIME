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
