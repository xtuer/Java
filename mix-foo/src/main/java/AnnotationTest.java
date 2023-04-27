import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;

public class AnnotationTest {
    // 数组属性只有一个值时不需要 {}
    @Lock(1) // 或者 @Lock(value = 1)
    public void foo() {
    }

    @Lock(value = {1, 2})
    public void bar() {
    }

    // 打印方法的注解。
    public static void printMethodAnnotation(String methodName) throws Exception {
        Method method = AnnotationTest.class.getMethod(methodName);
        Annotation anno = method.getAnnotations()[0];
        Lock lock = (Lock) anno;
        System.out.printf("NS: %s\n", Arrays.toString(lock.value()));
    }

    public static void main(String[] args) throws Exception {
        printMethodAnnotation("foo"); // NS: [1]
        printMethodAnnotation("bar"); // NS: [1, 2]
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Lock {
    int[] value() default {};
}
