package dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyBuilder implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 调用 Object 的方法, proxy 为 MyInterface 的实例对象，即下面 main 方法中的 proxy
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }

        System.out.println("proxy invoke");

        return null;
    }

    // 生成代理对象，调用代理对象的方法都会调用 MyProxy 的 invoke 方法
    public MyInterface newProxy(Class<MyInterface> type) {
        return (MyInterface) Proxy.newProxyInstance(MyInterface.class.getClassLoader(), new Class[] { type }, this);
    }

    public static void main(String[] args) {
        ProxyBuilder pb = new ProxyBuilder();
        MyInterface proxy = pb.newProxy(MyInterface.class);
        proxy.foo();
    }
}
