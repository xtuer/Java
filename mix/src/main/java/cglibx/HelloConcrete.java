package cglibx;

public class HelloConcrete {
    public String sayHello(String str) {
        return "HelloConcrete: " + str;
    }

    protected String foo() {
        return "protected foo()";
    }

    public final String bar() {
        return "public final bar()";
    }
}
