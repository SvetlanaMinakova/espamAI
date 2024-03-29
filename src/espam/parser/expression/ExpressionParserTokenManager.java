/* Generated By:JJTree&JavaCC: Do not edit this line. ExpressionParserTokenManager.java */
/*******************************************************************\
  * 
  This file is donated to ESPAM by Compaan Design BV (www.compaandesign.com) 
  Copyright (c) 2000 - 2005 Leiden University (LERC group at LIACS)
  Copyright (c) 2005 - 2007 CompaanDesign BV, The Netherlands
  All rights reserved.
  
  The use and distribution terms for this software are covered by the 
  Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
  which can be found in the file LICENSE at the root of this distribution.
  By using this software in any fashion, you are agreeing to be bound by 
  the terms of this license.
  
  You must not remove this notice, or any other, from this software.
  
  \*******************************************************************/

package espam.parser.expression;
import espam.utils.symbolic.expression.*;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Iterator;
import java.io.ByteArrayInputStream;

public class ExpressionParserTokenManager implements ExpressionParserConstants
{
    public  java.io.PrintStream debugStream = System.out;
    public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
    private final int jjStopAtPos(int pos, int kind)
    {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }
    private final int jjMoveStringLiteralDfa0_0()
    {
        switch(curChar)
        {
            case 9:
                jjmatchedKind = 34;
                return jjMoveNfa_0(5, 0);
            case 10:
                jjmatchedKind = 35;
                return jjMoveNfa_0(5, 0);
            case 13:
                jjmatchedKind = 36;
                return jjMoveNfa_0(5, 0);
            case 32:
                jjmatchedKind = 33;
                return jjMoveNfa_0(5, 0);
            case 33:
                return jjMoveStringLiteralDfa1_0(0x800000L);
            case 37:
                return jjMoveStringLiteralDfa1_0(0x10000c000L);
            case 38:
                jjmatchedKind = 21;
                return jjMoveNfa_0(5, 0);
            case 40:
                jjmatchedKind = 49;
                return jjMoveNfa_0(5, 0);
            case 41:
                jjmatchedKind = 51;
                return jjMoveNfa_0(5, 0);
            case 42:
                jjmatchedKind = 17;
                return jjMoveStringLiteralDfa1_0(0x10000L);
            case 43:
                jjmatchedKind = 19;
                return jjMoveNfa_0(5, 0);
            case 44:
                jjmatchedKind = 50;
                return jjMoveNfa_0(5, 0);
            case 45:
                jjmatchedKind = 20;
                return jjMoveNfa_0(5, 0);
            case 47:
                jjmatchedKind = 18;
                return jjMoveNfa_0(5, 0);
            case 58:
                jjmatchedKind = 31;
                return jjMoveNfa_0(5, 0);
            case 59:
                jjmatchedKind = 30;
                return jjMoveNfa_0(5, 0);
            case 60:
                jjmatchedKind = 27;
                return jjMoveStringLiteralDfa1_0(0x2000000L);
            case 61:
                jjmatchedKind = 22;
                return jjMoveNfa_0(5, 0);
            case 62:
                jjmatchedKind = 26;
                return jjMoveStringLiteralDfa1_0(0x1000000L);
            case 67:
                return jjMoveStringLiteralDfa1_0(0x100L);
            case 68:
                return jjMoveStringLiteralDfa1_0(0x20L);
            case 69:
                return jjMoveStringLiteralDfa1_0(0x206L);
            case 70:
                return jjMoveStringLiteralDfa1_0(0x88L);
            case 73:
                return jjMoveStringLiteralDfa1_0(0x1010L);
            case 77:
                return jjMoveStringLiteralDfa1_0(0xc40L);
            case 79:
                return jjMoveStringLiteralDfa1_0(0x2000L);
            case 91:
                jjmatchedKind = 28;
                return jjMoveNfa_0(5, 0);
            case 93:
                jjmatchedKind = 29;
                return jjMoveNfa_0(5, 0);
            case 99:
                return jjMoveStringLiteralDfa1_0(0x100L);
            case 100:
                return jjMoveStringLiteralDfa1_0(0x20L);
            case 101:
                return jjMoveStringLiteralDfa1_0(0x206L);
            case 102:
                return jjMoveStringLiteralDfa1_0(0x88L);
            case 105:
                return jjMoveStringLiteralDfa1_0(0x1010L);
            case 109:
                return jjMoveStringLiteralDfa1_0(0xc40L);
            case 111:
                return jjMoveStringLiteralDfa1_0(0x2000L);
            default :
                return jjMoveNfa_0(5, 0);
        }
    }
    private final int jjMoveStringLiteralDfa1_0(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 0);
        }
        switch(curChar)
        {
            case 42:
                if ((active0 & 0x10000L) != 0L)
            {
                jjmatchedKind = 16;
                jjmatchedPos = 1;
            }
                break;
            case 61:
                if ((active0 & 0x800000L) != 0L)
            {
                jjmatchedKind = 23;
                jjmatchedPos = 1;
            }
                else if ((active0 & 0x1000000L) != 0L)
                {
                    jjmatchedKind = 24;
                    jjmatchedPos = 1;
                }
                else if ((active0 & 0x2000000L) != 0L)
                {
                    jjmatchedKind = 25;
                    jjmatchedPos = 1;
                }
                break;
            case 65:
                return jjMoveStringLiteralDfa2_0(active0, 0x800L);
            case 69:
                return jjMoveStringLiteralDfa2_0(active0, 0x100L);
            case 70:
                if ((active0 & 0x10L) != 0L)
            {
                jjmatchedKind = 4;
                jjmatchedPos = 1;
            }
                return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
            case 73:
                return jjMoveStringLiteralDfa2_0(active0, 0x420L);
            case 76:
                return jjMoveStringLiteralDfa2_0(active0, 0x82L);
            case 78:
                return jjMoveStringLiteralDfa2_0(active0, 0x4L);
            case 79:
                return jjMoveStringLiteralDfa2_0(active0, 0x48L);
            case 80:
                return jjMoveStringLiteralDfa2_0(active0, 0xb000L);
            case 81:
                return jjMoveStringLiteralDfa2_0(active0, 0x200L);
            case 97:
                return jjMoveStringLiteralDfa2_0(active0, 0x800L);
            case 101:
                return jjMoveStringLiteralDfa2_0(active0, 0x100L);
            case 102:
                if ((active0 & 0x10L) != 0L)
            {
                jjmatchedKind = 4;
                jjmatchedPos = 1;
            }
                return jjMoveStringLiteralDfa2_0(active0, 0x4000L);
            case 105:
                return jjMoveStringLiteralDfa2_0(active0, 0x420L);
            case 108:
                return jjMoveStringLiteralDfa2_0(active0, 0x82L);
            case 109:
                return jjMoveStringLiteralDfa2_0(active0, 0x100000000L);
            case 110:
                return jjMoveStringLiteralDfa2_0(active0, 0x4L);
            case 111:
                return jjMoveStringLiteralDfa2_0(active0, 0x48L);
            case 112:
                return jjMoveStringLiteralDfa2_0(active0, 0xb000L);
            case 113:
                return jjMoveStringLiteralDfa2_0(active0, 0x200L);
            default :
                break;
        }
        return jjMoveNfa_0(5, 1);
    }
    private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 1);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 1);
        }
        switch(curChar)
        {
            case 65:
                return jjMoveStringLiteralDfa3_0(active0, 0x8000L);
            case 68:
                if ((active0 & 0x4L) != 0L)
            {
                jjmatchedKind = 2;
                jjmatchedPos = 2;
            }
                else if ((active0 & 0x40L) != 0L)
                {
                    jjmatchedKind = 6;
                    jjmatchedPos = 2;
                }
                else if ((active0 & 0x1000L) != 0L)
                {
                    jjmatchedKind = 12;
                    jjmatchedPos = 2;
                }
                else if ((active0 & 0x2000L) != 0L)
                {
                    jjmatchedKind = 13;
                    jjmatchedPos = 2;
                }
                break;
            case 73:
                return jjMoveStringLiteralDfa3_0(active0, 0x100L);
            case 78:
                if ((active0 & 0x400L) != 0L)
            {
                jjmatchedKind = 10;
                jjmatchedPos = 2;
            }
                break;
            case 79:
                return jjMoveStringLiteralDfa3_0(active0, 0x80L);
            case 82:
                if ((active0 & 0x8L) != 0L)
            {
                jjmatchedKind = 3;
                jjmatchedPos = 2;
            }
                break;
            case 83:
                return jjMoveStringLiteralDfa3_0(active0, 0x2L);
            case 85:
                return jjMoveStringLiteralDfa3_0(active0, 0x4200L);
            case 86:
                if ((active0 & 0x20L) != 0L)
            {
                jjmatchedKind = 5;
                jjmatchedPos = 2;
            }
                break;
            case 88:
                if ((active0 & 0x800L) != 0L)
            {
                jjmatchedKind = 11;
                jjmatchedPos = 2;
            }
                break;
            case 97:
                return jjMoveStringLiteralDfa3_0(active0, 0x100008000L);
            case 100:
                if ((active0 & 0x4L) != 0L)
            {
                jjmatchedKind = 2;
                jjmatchedPos = 2;
            }
                else if ((active0 & 0x40L) != 0L)
                {
                    jjmatchedKind = 6;
                    jjmatchedPos = 2;
                }
                else if ((active0 & 0x1000L) != 0L)
                {
                    jjmatchedKind = 12;
                    jjmatchedPos = 2;
                }
                else if ((active0 & 0x2000L) != 0L)
                {
                    jjmatchedKind = 13;
                    jjmatchedPos = 2;
                }
                break;
            case 105:
                return jjMoveStringLiteralDfa3_0(active0, 0x100L);
            case 110:
                if ((active0 & 0x400L) != 0L)
            {
                jjmatchedKind = 10;
                jjmatchedPos = 2;
            }
                break;
            case 111:
                return jjMoveStringLiteralDfa3_0(active0, 0x80L);
            case 114:
                if ((active0 & 0x8L) != 0L)
            {
                jjmatchedKind = 3;
                jjmatchedPos = 2;
            }
                break;
            case 115:
                return jjMoveStringLiteralDfa3_0(active0, 0x2L);
            case 117:
                return jjMoveStringLiteralDfa3_0(active0, 0x4200L);
            case 118:
                if ((active0 & 0x20L) != 0L)
            {
                jjmatchedKind = 5;
                jjmatchedPos = 2;
            }
                break;
            case 120:
                if ((active0 & 0x800L) != 0L)
            {
                jjmatchedKind = 11;
                jjmatchedPos = 2;
            }
                break;
            default :
                break;
        }
        return jjMoveNfa_0(5, 2);
    }
    private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 2);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 2);
        }
        switch(curChar)
        {
            case 65:
                return jjMoveStringLiteralDfa4_0(active0, 0x200L);
            case 69:
                if ((active0 & 0x2L) != 0L)
            {
                jjmatchedKind = 1;
                jjmatchedPos = 3;
            }
                break;
            case 76:
                if ((active0 & 0x100L) != 0L)
            {
                jjmatchedKind = 8;
                jjmatchedPos = 3;
            }
                break;
            case 78:
                return jjMoveStringLiteralDfa4_0(active0, 0x4000L);
            case 79:
                return jjMoveStringLiteralDfa4_0(active0, 0x80L);
            case 82:
                return jjMoveStringLiteralDfa4_0(active0, 0x8000L);
            case 97:
                return jjMoveStringLiteralDfa4_0(active0, 0x200L);
            case 101:
                if ((active0 & 0x2L) != 0L)
            {
                jjmatchedKind = 1;
                jjmatchedPos = 3;
            }
                break;
            case 108:
                if ((active0 & 0x100L) != 0L)
            {
                jjmatchedKind = 8;
                jjmatchedPos = 3;
            }
                break;
            case 110:
                return jjMoveStringLiteralDfa4_0(active0, 0x4000L);
            case 111:
                return jjMoveStringLiteralDfa4_0(active0, 0x80L);
            case 114:
                return jjMoveStringLiteralDfa4_0(active0, 0x8000L);
            case 116:
                return jjMoveStringLiteralDfa4_0(active0, 0x100000000L);
            default :
                break;
        }
        return jjMoveNfa_0(5, 3);
    }
    private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 3);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 3);
        }
        switch(curChar)
        {
            case 65:
                return jjMoveStringLiteralDfa5_0(active0, 0x8000L);
            case 67:
                return jjMoveStringLiteralDfa5_0(active0, 0x4000L);
            case 76:
                if ((active0 & 0x200L) != 0L)
            {
                jjmatchedKind = 9;
                jjmatchedPos = 4;
            }
                break;
            case 82:
                if ((active0 & 0x80L) != 0L)
            {
                jjmatchedKind = 7;
                jjmatchedPos = 4;
            }
                break;
            case 97:
                return jjMoveStringLiteralDfa5_0(active0, 0x8000L);
            case 99:
                return jjMoveStringLiteralDfa5_0(active0, 0x4000L);
            case 108:
                if ((active0 & 0x200L) != 0L)
            {
                jjmatchedKind = 9;
                jjmatchedPos = 4;
            }
                return jjMoveStringLiteralDfa5_0(active0, 0x100000000L);
            case 114:
                if ((active0 & 0x80L) != 0L)
            {
                jjmatchedKind = 7;
                jjmatchedPos = 4;
            }
                break;
            default :
                break;
        }
        return jjMoveNfa_0(5, 4);
    }
    private final int jjMoveStringLiteralDfa5_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 4);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 4);
        }
        switch(curChar)
        {
            case 77:
                return jjMoveStringLiteralDfa6_0(active0, 0x8000L);
            case 84:
                return jjMoveStringLiteralDfa6_0(active0, 0x4000L);
            case 97:
                return jjMoveStringLiteralDfa6_0(active0, 0x100000000L);
            case 109:
                return jjMoveStringLiteralDfa6_0(active0, 0x8000L);
            case 116:
                return jjMoveStringLiteralDfa6_0(active0, 0x4000L);
            default :
                break;
        }
        return jjMoveNfa_0(5, 5);
    }
    private final int jjMoveStringLiteralDfa6_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 5);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 5);
        }
        switch(curChar)
        {
            case 69:
                return jjMoveStringLiteralDfa7_0(active0, 0x8000L);
            case 73:
                return jjMoveStringLiteralDfa7_0(active0, 0x4000L);
            case 98:
                if ((active0 & 0x100000000L) != 0L)
            {
                jjmatchedKind = 32;
                jjmatchedPos = 6;
            }
                break;
            case 101:
                return jjMoveStringLiteralDfa7_0(active0, 0x8000L);
            case 105:
                return jjMoveStringLiteralDfa7_0(active0, 0x4000L);
            default :
                break;
        }
        return jjMoveNfa_0(5, 6);
    }
    private final int jjMoveStringLiteralDfa7_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 6);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 6);
        }
        switch(curChar)
        {
            case 79:
                return jjMoveStringLiteralDfa8_0(active0, 0x4000L);
            case 84:
                return jjMoveStringLiteralDfa8_0(active0, 0x8000L);
            case 111:
                return jjMoveStringLiteralDfa8_0(active0, 0x4000L);
            case 116:
                return jjMoveStringLiteralDfa8_0(active0, 0x8000L);
            default :
                break;
        }
        return jjMoveNfa_0(5, 7);
    }
    private final int jjMoveStringLiteralDfa8_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 7);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 7);
        }
        switch(curChar)
        {
            case 69:
                return jjMoveStringLiteralDfa9_0(active0, 0x8000L);
            case 78:
                if ((active0 & 0x4000L) != 0L)
            {
                jjmatchedKind = 14;
                jjmatchedPos = 8;
            }
                break;
            case 101:
                return jjMoveStringLiteralDfa9_0(active0, 0x8000L);
            case 110:
                if ((active0 & 0x4000L) != 0L)
            {
                jjmatchedKind = 14;
                jjmatchedPos = 8;
            }
                break;
            default :
                break;
        }
        return jjMoveNfa_0(5, 8);
    }
    private final int jjMoveStringLiteralDfa9_0(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return jjMoveNfa_0(5, 8);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return jjMoveNfa_0(5, 8);
        }
        switch(curChar)
        {
            case 82:
                if ((active0 & 0x8000L) != 0L)
            {
                jjmatchedKind = 15;
                jjmatchedPos = 9;
            }
                break;
            case 114:
                if ((active0 & 0x8000L) != 0L)
            {
                jjmatchedKind = 15;
                jjmatchedPos = 9;
            }
                break;
            default :
                break;
        }
        return jjMoveNfa_0(5, 9);
    }
    private final void jjCheckNAdd(int state)
    {
        if (jjrounds[state] != jjround)
        {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }
    private final void jjAddStates(int start, int end)
    {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }
    private final void jjCheckNAddTwoStates(int state1, int state2)
    {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }
    private final void jjCheckNAddStates(int start, int end)
    {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }
    private final void jjCheckNAddStates(int start)
    {
        jjCheckNAdd(jjnextStates[start]);
        jjCheckNAdd(jjnextStates[start + 1]);
    }
    static final long[] jjbitVec0 = {
        0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
    };
    private final int jjMoveNfa_0(int startState, int curPos)
    {
        int strKind = jjmatchedKind;
        int strPos = jjmatchedPos;
        int seenUpto;
        input_stream.backup(seenUpto = curPos + 1);
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { throw new Error("Internal Error"); }
        curPos = 0;
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 27;
        int i = 1;
        jjstateSet[0] = startState;
        int j, kind = 0x7fffffff;
        for (;;)
        {
            if (++jjround == 0x7fffffff)
                ReInitRounds();
            if (curChar < 64)
            {
                long l = 1L << curChar;
                MatchLoop: do
                {
                    switch(jjstateSet[--i])
                    {
                        case 5:
                            if ((0x3fe000000000000L & l) != 0L)
                        {
                            if (kind > 42)
                                kind = 42;
                            jjCheckNAddTwoStates(7, 8);
                        }
                            else if (curChar == 47)
                                jjAddStates(0, 1);
                            else if (curChar == 48)
                            {
                                if (kind > 42)
                                    kind = 42;
                                jjCheckNAddStates(2, 4);
                            }
                            else if (curChar == 37)
                                jjstateSet[jjnewStateCnt++] = 0;
                            break;
                        case 0:
                            if (curChar == 37)
                            jjCheckNAddStates(5, 7);
                            break;
                        case 1:
                            if ((0xffffffffffffdbffL & l) != 0L)
                            jjCheckNAddStates(5, 7);
                            break;
                        case 2:
                            if ((0x2400L & l) != 0L && kind > 38)
                            kind = 38;
                            break;
                        case 3:
                            if (curChar == 10 && kind > 38)
                            kind = 38;
                            break;
                        case 4:
                            if (curChar == 13)
                            jjstateSet[jjnewStateCnt++] = 3;
                            break;
                        case 6:
                            if ((0x3fe000000000000L & l) == 0L)
                            break;
                            if (kind > 42)
                                kind = 42;
                            jjCheckNAddTwoStates(7, 8);
                            break;
                        case 7:
                            if ((0x3ff000000000000L & l) == 0L)
                            break;
                            if (kind > 42)
                                kind = 42;
                            jjCheckNAddTwoStates(7, 8);
                            break;
                        case 10:
                            if ((0x3ff000000000000L & l) == 0L)
                            break;
                            if (kind > 46)
                                kind = 46;
                            jjstateSet[jjnewStateCnt++] = 10;
                            break;
                        case 11:
                            if (curChar != 48)
                            break;
                            if (kind > 42)
                                kind = 42;
                            jjCheckNAddStates(2, 4);
                            break;
                        case 13:
                            if ((0x3ff000000000000L & l) == 0L)
                            break;
                            if (kind > 42)
                                kind = 42;
                            jjCheckNAddTwoStates(13, 8);
                            break;
                        case 14:
                            if ((0xff000000000000L & l) == 0L)
                            break;
                            if (kind > 42)
                                kind = 42;
                            jjCheckNAddTwoStates(14, 8);
                            break;
                        case 15:
                            if (curChar == 47)
                            jjAddStates(0, 1);
                            break;
                        case 16:
                            if (curChar == 47)
                            jjCheckNAddStates(8, 10);
                            break;
                        case 17:
                            if ((0xffffffffffffdbffL & l) != 0L)
                            jjCheckNAddStates(8, 10);
                            break;
                        case 18:
                            if ((0x2400L & l) != 0L && kind > 37)
                            kind = 37;
                            break;
                        case 19:
                            if (curChar == 10 && kind > 37)
                            kind = 37;
                            break;
                        case 20:
                            if (curChar == 13)
                            jjstateSet[jjnewStateCnt++] = 19;
                            break;
                        case 21:
                            if (curChar == 42)
                            jjCheckNAddTwoStates(22, 23);
                            break;
                        case 22:
                            if ((0xfffffbffffffffffL & l) != 0L)
                            jjCheckNAddTwoStates(22, 23);
                            break;
                        case 23:
                            if (curChar == 42)
                            jjAddStates(11, 12);
                            break;
                        case 24:
                            if ((0xffff7fffffffffffL & l) != 0L)
                            jjCheckNAddTwoStates(25, 23);
                            break;
                        case 25:
                            if ((0xfffffbffffffffffL & l) != 0L)
                            jjCheckNAddTwoStates(25, 23);
                            break;
                        case 26:
                            if (curChar == 47 && kind > 39)
                            kind = 39;
                            break;
                        default : break;
                    }
                } while(i != startsAt);
            }
            else if (curChar < 128)
            {
                long l = 1L << (curChar & 077);
                MatchLoop: do
                {
                    switch(jjstateSet[--i])
                    {
                        case 5:
                        case 10:
                            if ((0x7fffffe87fffffeL & l) == 0L)
                            break;
                            if (kind > 46)
                                kind = 46;
                            jjCheckNAdd(10);
                            break;
                        case 1:
                            jjAddStates(5, 7);
                            break;
                        case 8:
                            if ((0x100000001000L & l) != 0L && kind > 42)
                            kind = 42;
                            break;
                        case 12:
                            if ((0x100000001000000L & l) != 0L)
                            jjCheckNAdd(13);
                            break;
                        case 13:
                            if ((0x7e0000007eL & l) == 0L)
                            break;
                            if (kind > 42)
                                kind = 42;
                            jjCheckNAddTwoStates(13, 8);
                            break;
                        case 17:
                            jjAddStates(8, 10);
                            break;
                        case 22:
                            jjCheckNAddTwoStates(22, 23);
                            break;
                        case 24:
                        case 25:
                            jjCheckNAddTwoStates(25, 23);
                            break;
                        default : break;
                    }
                } while(i != startsAt);
            }
            else
            {
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
                MatchLoop: do
                {
                    switch(jjstateSet[--i])
                    {
                        case 1:
                            if ((jjbitVec0[i2] & l2) != 0L)
                            jjAddStates(5, 7);
                            break;
                        case 17:
                            if ((jjbitVec0[i2] & l2) != 0L)
                            jjAddStates(8, 10);
                            break;
                        case 22:
                            if ((jjbitVec0[i2] & l2) != 0L)
                            jjCheckNAddTwoStates(22, 23);
                            break;
                        case 24:
                        case 25:
                            if ((jjbitVec0[i2] & l2) != 0L)
                            jjCheckNAddTwoStates(25, 23);
                            break;
                        default : break;
                    }
                } while(i != startsAt);
            }
            if (kind != 0x7fffffff)
            {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt) == (startsAt = 27 - (jjnewStateCnt = startsAt)))
                break;
            try { curChar = input_stream.readChar(); }
            catch(java.io.IOException e) { break; }
        }
        if (jjmatchedPos > strPos)
            return curPos;
        
        int toRet = Math.max(curPos, seenUpto);
        
        if (curPos < toRet)
            for (i = toRet - Math.min(curPos, seenUpto); i-- > 0; )
            try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) { throw new Error("Internal Error : Please send a bug report."); }
        
        if (jjmatchedPos < strPos)
        {
            jjmatchedKind = strKind;
            jjmatchedPos = strPos;
        }
        else if (jjmatchedPos == strPos && jjmatchedKind > strKind)
            jjmatchedKind = strKind;
        
        return toRet;
    }
    private final int jjMoveStringLiteralDfa0_1()
    {
        switch(curChar)
        {
            case 37:
                return jjMoveStringLiteralDfa1_1(0x10000000000L);
            default :
                return 1;
        }
    }
    private final int jjMoveStringLiteralDfa1_1(long active0)
    {
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return 1;
        }
        switch(curChar)
        {
            case 101:
                return jjMoveStringLiteralDfa2_1(active0, 0x10000000000L);
            default :
                return 2;
        }
    }
    private final int jjMoveStringLiteralDfa2_1(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return 2;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return 2;
        }
        switch(curChar)
        {
            case 110:
                return jjMoveStringLiteralDfa3_1(active0, 0x10000000000L);
            default :
                return 3;
        }
    }
    private final int jjMoveStringLiteralDfa3_1(long old0, long active0)
    {
        if (((active0 &= old0)) == 0L)
            return 3;
        try { curChar = input_stream.readChar(); }
        catch(java.io.IOException e) {
            return 3;
        }
        switch(curChar)
        {
            case 100:
                if ((active0 & 0x10000000000L) != 0L)
                return jjStopAtPos(3, 40);
                break;
            default :
                return 4;
        }
        return 4;
    }
    static final int[] jjnextStates = {
        16, 21, 12, 14, 8, 1, 2, 4, 17, 18, 20, 24, 26, 
    };
    public static final String[] jjstrLiteralImages = {
        "", null, null, null, null, null, null, null, null, null, null, null, null, 
        null, null, null, "\52\52", "\52", "\57", "\53", "\55", "\46", "\75", "\41\75", 
        "\76\75", "\74\75", "\76", "\74", "\133", "\135", "\73", "\72", 
        "\45\155\141\164\154\141\142", null, null, null, null, null, null, null, null, null, null, null, null, null, 
        null, null, null, "\50", "\54", "\51", };
    public static final String[] lexStateNames = {
        "DEFAULT", 
        "IN_MATLAB_COMMENT", 
    };
    public static final int[] jjnewLexState = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
        -1, -1, 
    };
    static final long[] jjtoToken = {
        0xe4501ffffffffL, 
    };
    static final long[] jjtoSkip = {
        0xfe00000000L, 
    };
    static final long[] jjtoMore = {
        0x20000000000L, 
    };
    protected SimpleCharStream input_stream;
    private final int[] jjrounds = new int[27];
    private final int[] jjstateSet = new int[54];
    protected char curChar;
    public ExpressionParserTokenManager(SimpleCharStream stream){
        if (SimpleCharStream.staticFlag)
            throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
        input_stream = stream;
    }
    public ExpressionParserTokenManager(SimpleCharStream stream, int lexState){
        this(stream);
        SwitchTo(lexState);
    }
    public void ReInit(SimpleCharStream stream)
    {
        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }
    private final void ReInitRounds()
    {
        int i;
        jjround = 0x80000001;
        for (i = 27; i-- > 0;)
            jjrounds[i] = 0x80000000;
    }
    public void ReInit(SimpleCharStream stream, int lexState)
    {
        ReInit(stream);
        SwitchTo(lexState);
    }
    public void SwitchTo(int lexState)
    {
        if (lexState >= 2 || lexState < 0)
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
        else
            curLexState = lexState;
    }
    
    protected Token jjFillToken()
    {
        Token t = Token.newToken(jjmatchedKind);
        t.kind = jjmatchedKind;
        String im = jjstrLiteralImages[jjmatchedKind];
        t.image = (im == null) ? input_stream.GetImage() : im;
        t.beginLine = input_stream.getBeginLine();
        t.beginColumn = input_stream.getBeginColumn();
        t.endLine = input_stream.getEndLine();
        t.endColumn = input_stream.getEndColumn();
        return t;
    }
    
    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;
    
    public Token getNextToken() 
    {
        int kind;
        Token specialToken = null;
        Token matchedToken;
        int curPos = 0;
        
        EOFLoop :
            for (;;)
        {   
            try   
            {     
                curChar = input_stream.BeginToken();
            }     
            catch(java.io.IOException e)
            {        
                jjmatchedKind = 0;
                matchedToken = jjFillToken();
                return matchedToken;
            }
            
            for (;;)
            {
                switch(curLexState)
                {
                    case 0:
                        jjmatchedKind = 0x7fffffff;
                        jjmatchedPos = 0;
                        curPos = jjMoveStringLiteralDfa0_0();
                        break;
                    case 1:
                        jjmatchedKind = 0x7fffffff;
                        jjmatchedPos = 0;
                        curPos = jjMoveStringLiteralDfa0_1();
                        if (jjmatchedPos == 0 && jjmatchedKind > 41)
                        {
                            jjmatchedKind = 41;
                        }
                        break;
                }
                if (jjmatchedKind != 0x7fffffff)
                {
                    if (jjmatchedPos + 1 < curPos)
                        input_stream.backup(curPos - jjmatchedPos - 1);
                    if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
                    {
                        matchedToken = jjFillToken();
                        if (jjnewLexState[jjmatchedKind] != -1)
                            curLexState = jjnewLexState[jjmatchedKind];
                        return matchedToken;
                    }
                    else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
                    {
                        if (jjnewLexState[jjmatchedKind] != -1)
                            curLexState = jjnewLexState[jjmatchedKind];
                        continue EOFLoop;
                    }
                    if (jjnewLexState[jjmatchedKind] != -1)
                        curLexState = jjnewLexState[jjmatchedKind];
                    curPos = 0;
                    jjmatchedKind = 0x7fffffff;
                    try {
                        curChar = input_stream.readChar();
                        continue;
                    }
                    catch (java.io.IOException e1) { }
                }
                int error_line = input_stream.getEndLine();
                int error_column = input_stream.getEndColumn();
                String error_after = null;
                boolean EOFSeen = false;
                try { input_stream.readChar(); input_stream.backup(1); }
                catch (java.io.IOException e1) {
                    EOFSeen = true;
                    error_after = curPos <= 1 ? "" : input_stream.GetImage();
                    if (curChar == '\n' || curChar == '\r') {
                        error_line++;
                        error_column = 0;
                    }
                    else
                        error_column++;
                }
                if (!EOFSeen) {
                    input_stream.backup(1);
                    error_after = curPos <= 1 ? "" : input_stream.GetImage();
                }
                throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
            }
        }
    }
    
}
