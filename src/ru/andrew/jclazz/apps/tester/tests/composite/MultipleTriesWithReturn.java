package ru.andrew.jclazz.apps.tester.tests.composite;

public class MultipleTriesWithReturn
{
    public void test()
    {
        try
        {
            System.out.println("TRY-1");
        }
        catch (Exception exc)
        {
            try
            {
                System.out.println("INNER TRY");
                return;
            }
            catch (Exception e)
            {
                System.out.println("E");
            }
        }

        try
        {
            System.out.println("TRY-2");
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
