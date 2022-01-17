import lombok.Getter;
import lombok.Setter;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Test {
    public static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = Unsafe.class.getDeclaredField("theUnsafe");
        declaredField.setAccessible(true);
        return (Unsafe) declaredField.get(null);
    }

    public static void main(String[] args) throws Exception {
        CasDemo cd = new CasDemo(1);
        System.out.println(cd.compareAndSwap(2, 2));
        System.out.println(cd.getValue());

        System.out.println(cd.compareAndSwap(1, 2));
        System.out.println(cd.getValue());
    }
}

@Getter
@Setter
class CasDemo {
    private int value;

    public CasDemo(int value) {
        this.value = value;
    }

    public boolean compareAndSwap(int expected, int newValue) throws Exception {
        Unsafe unsafe = Test.getUnsafe();
        long offset = unsafe.objectFieldOffset(CasDemo.class.getDeclaredField("value"));
        return unsafe.compareAndSwapInt(this, offset, expected, newValue);
    }
}
