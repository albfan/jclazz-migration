package ru.andrew.jclazz.apps.tester.tests.blocks;

public class LoopGeneric
{
    public boolean exit;

    public void test1()
    {
        while (exit)
        {
            System.out.println("EXIT");
        }
    }
}
