package xtuer.util;

import xtuer.sp.procedure.Procedure;

public class ProcedurePrinter {
    public static void print(Procedure proc) {
        System.out.println("OriginalArgs:");
        TablePrinter.print(proc.getOriginalArgs(), "scale", "value", "length", "precision", "dataTypeValue2");
        System.out.println("InoutArgs:");
        TablePrinter.print(proc.getInOutInoutArgs(), "scale", "value", "length", "precision", "dataTypeValue2");
        System.out.println("InArgs:");
        TablePrinter.print(proc.getInArgs(), "scale", "value", "length", "precision", "dataTypeValue2");
        System.out.println("CallableSQL:");
        System.out.println(proc.getCallableSql());
        System.out.println("Signature:");
        System.out.println(proc.getSignature());
        System.out.println();
    }
}
