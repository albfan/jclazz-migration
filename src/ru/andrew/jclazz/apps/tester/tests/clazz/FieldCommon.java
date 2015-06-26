package ru.andrew.jclazz.apps.tester.tests.clazz;

import java.math.*;
import java.util.*;

public class FieldCommon
{
    public String str1;
    private long l2;
    protected double d3;
    public final String str4 = "STR4";
    public static float f5;
    private volatile char c6;
    protected transient boolean b7;

    public final boolean n0 = true;
    public final byte n1 = 2;
    public final char n2 = 4;
    public final short n3 = 10;
    public final int n4 = 100;
    public final long n5 = 555L;
    public final float n6 = 33.0f;
    public final double n7 = 3.14;
    public final String n8 = "CONST";
    public final BigInteger n9 = BigInteger.valueOf(1000L);

    public final long n10;

    public List<Number> g1;
    public List<? extends Number> g2;
    public List<? super Number> g3;
    public List<?> g4;

    public FieldCommon()
    {
        n10 = 33L;
    }

    public FieldCommon(String str)
    {
        n10 = 23L;
    }
}
