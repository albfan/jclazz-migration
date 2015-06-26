package ru.andrew.jclazz.apps.tester;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

public final class TestTool
{
    public static void suite(File suiteDir, Tester tester)
    {
        File[] files = suiteDir.listFiles(tester.getInputFileFilter());
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
            }
        });
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
            {
                suite(files[i], tester);
            }
            else
            {
                try
                {
                    boolean res = tester.test(files[i]);
                    if (!res)
                    {
                        System.out.println(files[i].getName() + " - FAILED (" + tester.getName() + ")");
                    }
                    else
                    {
                        System.out.println(files[i].getName() + " - PASS (" + tester.getName() + ")");
                    }
                }
                catch (Throwable th)
                {
                    System.out.println(files[i].getName() + " - FAILED (" + tester.getName() + ") "+th);
                }
            }
        }
    }
}
