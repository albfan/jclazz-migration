package ru.andrew.jclazz.apps.tester;

import java.io.*;

public final class FileComparator
{
    public static boolean compare(String path1, String path2)
    {
        FileInputStream fis1 = null;
        FileInputStream fis2 = null;
        byte b1[] = new byte[1024 * 1024];
        byte b2[] = new byte[1024 * 1024];
        try
        {
            fis1 = new NoNewLineFileInputStream(path1);
            fis2 = new NoNewLineFileInputStream(path2);

            do
            {
                int res1 = fis1.read(b1);
                int res2 = fis2.read(b2);
                if (res1 == -1 && res2 == -1) break;
                if ((res1 == -1 && res2 != -1) || (res1 != -1 && res2 == -1))
                {
                    return false;
                }
                if (res1 != res2)
                {
                    return false;
                }

                for (int i = 0; i < res1; i++)
                {
                    if (b1[i] != b2[i]) {
                        return false;
                    }
                }
            }
            while (true);
            return true;
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (fis1 != null) fis1.close();
                if (fis2 != null) fis2.close();
            }
            catch (IOException e)
            {
                System.out.println("ERROR");
            }
        }
    }
}

