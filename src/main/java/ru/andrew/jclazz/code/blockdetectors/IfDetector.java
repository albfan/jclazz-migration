package ru.andrew.jclazz.code.blockdetectors;

import ru.andrew.jclazz.code.codeitems.*;
import ru.andrew.jclazz.code.codeitems.blocks.*;
import ru.andrew.jclazz.code.codeitems.ops.*;

import java.util.*;

public class IfDetector implements Detector
{
    public void analyze(Block block)
    {
        block.reset();
        while (block.hasMoreOperations())
        {
            CodeItem citem = block.next();
            if (!(citem instanceof If)) continue;
            If ifCond = (If) citem;

            if (!ifCond.isForwardBranch()) continue;

            CodeItem priorTarget = block.getOperationPriorTo(ifCond.getTargetOperation());

            if (priorTarget != null && priorTarget instanceof If)
            {
                createIfOR(block, ifCond, (If) priorTarget);
                continue;
            }
            if ((priorTarget == null) || (!(priorTarget instanceof GoTo)))
            {
                createIf(block, ifCond, priorTarget);
                continue;
            }

            GoTo priorTargetGoTo = (GoTo) priorTarget;

            if (priorTargetGoTo.isForwardBranch())
            {
                createIf(block, ifCond, priorTargetGoTo);
                continue;
            }
            else if (isIfContinue(block, priorTargetGoTo))
            {
                createIfContinue(block, ifCond, priorTargetGoTo);
                continue;
            }
        }
    }

    private void createIfOR(Block block, If ifCond, If priorTarget)
    {
        CodeItem priorOp = block.getOperationPriorTo(priorTarget.getTargetOperation());
        if (isCompoundIf(block, priorTarget))
        {
            List compoundConditions = block.createSubBlock(0, priorTarget.getStartByte() + 1, null);
            ((IfBlock) block).addAndConditions(compoundConditions, false);
            return;
        }

        IfBlock ifBlock = new IfBlock(block, ifCond);
        block.createSubBlock(priorTarget.getStartByte() + 1, priorTarget.getTargetOperation(), ifBlock);
        ifBlock.addAndConditions(block.createSubBlock(ifCond.getStartByte() + 1, priorTarget.getStartByte() + 1, null), true);
        block.removeOperation(ifCond.getStartByte());

        if (priorOp != null && priorOp instanceof GoTo)
        {
            detectElse(block, ifBlock, priorTarget, (GoTo) priorOp);
        }
    }

    private void createIf(Block block, If ifCond, CodeItem priorTarget)
    {
        if (isCompoundIf(block, ifCond))
        {
            List compoundConditions = block.createSubBlock(0, ifCond.getStartByte() + 1, null);
            ((IfBlock) block).addAndConditions(compoundConditions, false);
            return;
        }

        IfBlock ifBlock = new IfBlock(block, ifCond);
        block.createSubBlock(ifCond.getStartByte() + 1, ifCond.getTargetOperation(), ifBlock);
        block.removeOperation(ifCond.getStartByte());
        
        if (priorTarget != null && priorTarget instanceof GoTo)
        {
            detectElse(block, ifBlock, ifCond, (GoTo) priorTarget);
        }
    }

    private boolean isCompoundIf(Block block, If ifCond)
    {
        if (ifCond.getTargetOperation() <= block.getLastOperation().getStartByte())
        {
            return false;
        }
        if (!(block instanceof IfBlock))
        {
            return false;
        }

        CodeItem next = block.getParent().getOperationAfter(block.getStartByte());
        return (next instanceof Else) && (next.getStartByte() == ifCond.getTargetOperation());
    }

    private boolean isIfContinue(Block block, GoTo priorTarget)
    {
        // priorTarget - backward goto, but this is not loop
        // Case: if - continue for while (...) {...} block
        Block loop = block;
        while (loop != null)
        {
            if ((loop instanceof Loop) && (((Loop) loop).getBeginPc() == priorTarget.getTargetOperation()))
            {
                // This is "if - continue" case
                return true;
            }
            loop = loop.getParent();
        }
        return false;
    }

    private void createIfContinue(Block block, If ifCond, GoTo priorTarget)
    {
        if (isCompoundIf(block, ifCond))
        {
            List compoundConditions = block.createSubBlock(0, ifCond.getStartByte() + 1, null);
            ((IfBlock) block).addAndConditions(compoundConditions, false);
            return;
        }

        priorTarget.setContinue(true);

        IfBlock ifBlock = new IfBlock(block, ifCond);
        block.createSubBlock(ifCond.getStartByte() + 1, ifCond.getTargetOperation(), ifBlock);
        block.removeOperation(ifCond.getStartByte());
    }

    private void detectElse(Block block, IfBlock ifBlock, If ifCond, GoTo ifPriorTarget)
    {
        // Generic Else block
        if (block.getLastOperation().getStartByte() >= ifPriorTarget.getTargetOperation())
        {
            Else elseBlock = new Else(block);
            block.createSubBlock(ifCond.getTargetOperation(), ifPriorTarget.getTargetOperation(), elseBlock);
            ifBlock.setElseBlock(elseBlock);
            elseBlock.postCreate();
            return;
        }

        long ifPriorTarget_pc = ifPriorTarget.getTargetOperation();

        // Trying to find target operation
        Block ff_block = block;
        CodeItem ff_oper;
        do
        {
            ff_oper = ff_block.getOperationByStartByte(ifPriorTarget_pc);
            if (ff_oper != null) break;
            ff_block = ff_block.getParent();
        }
        while (ff_block != null);
        if (ff_oper == null) return;

        CodeItem prevFFOper = ff_block.getOperationPriorTo(ifPriorTarget_pc);
        if (prevFFOper instanceof Loop)
        {
            ifPriorTarget.setBreak(true); // Set goto to break
            return;
        }
        else if (prevFFOper instanceof Else || prevFFOper instanceof IfBlock)   // Else block
        {
            Else elseBlock = new Else(block);
            block.createSubBlock(ifCond.getTargetOperation(), ifPriorTarget_pc, elseBlock);
            ifBlock.setElseBlock(elseBlock);
            elseBlock.postCreate();
            return;
        }
        else if (prevFFOper instanceof Switch)
        {
            Else elseBlock = new Else(block);
            block.createSubBlock(ifCond.getTargetOperation(), ifPriorTarget_pc, elseBlock);
            ifBlock.setElseBlock(elseBlock);
            elseBlock.postCreate();
            return;
        }

        if (ff_block instanceof Loop)    // this is continue in for loop
        {
            if (((Loop) ff_block).isPrecondition())
            {
                ((Loop) ff_block).setIncrementalPartStartOperation(ff_oper.getStartByte());
            }
            ifPriorTarget.setContinue(true);
        }

        // Add return in previous "if" if adding new "if" 
    }
    
}
