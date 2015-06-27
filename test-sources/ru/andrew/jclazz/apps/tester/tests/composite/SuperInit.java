package ru.andrew.jclazz.apps.tester.tests.composite;

import java.io.*;

public class SuperInit extends File
{
    public SuperInit(String pathname)
    {
        super(pathname);
    }

    public SuperInit(String pathname, int i)
    {
        this(pathname);
        System.out.println(i);
    }
}
