package andriod.os;

/**
 * @author newChar
 * date 2025/6/19
 * @since 当前版本，（以及描述）
 * @since 迭代版本，（以及描述）
 */
public class Build {

    public static final boolean IS_DEBUGGABLE = false;

    private static int getInt(String property) {
        return SystemProperties.getInt(property, -999);
    }

}
