package dev.bl.feathercaramel.util;

/**
 * カーソル矩形を表す record。
 * x, y はスクリーン座標 (GUI スケール後ピクセル)。
 */
public record Rect(float x, float y, float width, float height) {
    public static final Rect EMPTY = new Rect(0, 0, 0, 0);

    public float[] copy() {
        return new float[]{ x, y, width, height };
    }
}
