package ru.andrew.jclazz.attributes.verification;

import ru.andrew.jclazz.*;

import java.io.*;

public class SameLocals1StackItemFrame extends StackMapFrame
{
    private VerificationTypeInfo vti;

    public SameLocals1StackItemFrame(int frame_type)
    {
        super(frame_type);
    }

    public void load(ClazzInputStream cis, Clazz clazz) throws IOException, ClazzException
    {
        vti = new VerificationTypeInfo();
        vti.load(cis, clazz);
    }

    public int getOffsetDelta()
    {
        return frame_type - 64;
    }

    public VerificationTypeInfo getVerificationTypeInfo()
    {
        return vti;
    }

    public String toString()
    {
        return prefix(this) + "stack item = " + vti.toString();
    }
}
