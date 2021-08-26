package classindex;

import org.atteo.classindex.ClassIndex;

public class Client {
    public static void main(String[] args) {
        Iterable<Class<?>> clazzes = ClassIndex.getAnnotated(IndexedEnum.class);
        for (Class<?> clazz : clazzes) {
            System.out.println(clazz.getName());
        }
    }

    @IndexedEnum
    public enum State {
        INIT, RUNNING, OK, FAILED
    }
}
