package xtuer.util;

import xtuer.funcproc.function.Function;

public class FunctionPrinter {
    public static void print(Function func) {
        System.out.println("OriginalArgs:");
        TablePrinter.print(func.getOriginalArgs(), "scale", "value", "length", "precision", "dataTypeValue");
        System.out.println("InoutArgs:");
        TablePrinter.print(func.getInOutInoutArgs(), "scale", "value", "length", "precision", "dataTypeValue");
        System.out.println("ReturnArgs:");
        TablePrinter.print(func.getReturnArgs(), "scale", "value", "length", "precision", "dataTypeValue");
        System.out.println("CallableSQL:");
        System.out.println(func.getCallableSql());
        System.out.println("Signature:");
        System.out.println(func.getSignature());
        System.out.println();
    }
}
