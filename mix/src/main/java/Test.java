import java.lang.reflect.Field;

public class Test {
    public static void main(String[] args) throws Exception {
        // [1] 获取 enum
        Color red = Color.RED;
        System.out.println(red);

        // [2] 获取 enum
        Color blue = Enum.valueOf(Color.class, "BLUE");
        System.out.println(blue);

        // [3] 获取 enum 的属性值
        Class<Color> clazz = Color.class;
        int key = getFieldIntValue(Color.class, blue, "key");
        System.out.println(key);

        // [4] 使用值查找对应的 enum 对象
        for (Color color : clazz.getEnumConstants()) {
            if (color.getKey() == 2) {
                System.out.println(color);
                break;
            }
        }
    }

    // 获取 int 的属性值
    public static <T> int getFieldIntValue(Class<T> clazz, Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (int) field.get(obj);
    }

    public enum Color {
        RED(1, "红"),
        GREEN(2, "绿"),
        BLUE(3, "蓝");

        private final Integer key;
        private final String label;

        Color(Integer key, String label) {
            this.key = key;
            this.label = label;
        }

        public Integer getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }
    }
}
