package office.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyProxy implements InvocationHandler {
    public Object bind(String interfaceName) throws ClassNotFoundException {
        Class<?> interfaceClass = Class.forName(interfaceName);
        return Proxy.newProxyInstance(MyProxy.class.getClassLoader(), new Class[] {interfaceClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return 3;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Mapper mapper = (Mapper) new MyProxy().bind("proxy.Mapper");
        System.out.println(mapper.add(1, 2));
    }
}
