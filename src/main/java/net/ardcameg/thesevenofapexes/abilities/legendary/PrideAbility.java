package net.ardcameg.thesevenofapexes.abilities.legendary;

public final class PrideAbility {
    private PrideAbility() {}

    /**
     * "傲慢"の数に基づいて、効果の最終的な倍率を計算して返す
     * @param prideCount "傲慢"の所持数
     * @return 計算された効果倍率 (例: 傲慢1個なら2倍)
     */
    public static int calculateEffectMultiplier(int prideCount) {
        // 傲慢がなければ、効果は1倍 (通常通り)
        if (prideCount <= 0) {
            return 1;
        }
        // 傲慢1個で効果2倍、2個で3倍...というように、(所持数 + 1) 倍にする
        return prideCount + 1;
    }
}