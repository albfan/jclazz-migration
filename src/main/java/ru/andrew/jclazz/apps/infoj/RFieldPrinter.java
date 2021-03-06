package ru.andrew.jclazz.apps.infoj;

import ru.andrew.jclazz.*;
import ru.andrew.jclazz.attributes.*;

import java.io.*;

public class RFieldPrinter
{
    private static final String INDENT = "    ";

    public void print(PrintWriter pw, FIELD_INFO f_info)
    {
        pw.println(f_info.getName() + " of " + f_info.getDescriptor().getFQN());
        pw.println("{");
        pw.println(INDENT + "Access Flags: " + f_info.getAccessFlags());
        pw.println(INDENT + "PUBLIC      : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_PUBLIC) > 0 ? "x" : ""));
        pw.println(INDENT + "PRIVATE     : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_PRIVATE) > 0 ? "x" : ""));
        pw.println(INDENT + "PROTECTED   : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_PROTECTED) > 0 ? "x" : ""));
        pw.println(INDENT + "STATIC      : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_STATIC) > 0 ? "x" : ""));
        pw.println(INDENT + "FINAL       : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_FINAL) > 0 ? "x" : ""));
        pw.println(INDENT + "VOLATILE    : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_VOLATILE) > 0 ? "x" : ""));
        pw.println(INDENT + "TRANSIENT   : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_TRANSIENT) > 0 ? "x" : ""));
        pw.println(INDENT + "SYNTHETIC   : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_SYNTHETIC) > 0 ? "x" : ""));
        pw.println(INDENT + "ENUM        : " + ((f_info.getAccessFlags() & FIELD_INFO.ACC_ENUM) > 0 ? "x" : ""));
        pw.println();

        pw.println(INDENT + "Attributes:");
        ATTRIBUTE_INFO[] attrs = f_info.getAttributes();
        for (int i = 0; i < attrs.length; i++)
        {
            pw.println(INDENT + attrs[i].toString());
        }
        pw.println("}");
    }
}
