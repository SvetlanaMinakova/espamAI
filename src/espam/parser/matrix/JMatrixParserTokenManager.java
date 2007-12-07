/*******************************************************************\

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

package espam.parser.matrix;

import java.io.ByteArrayInputStream;
import java.util.Vector;
import espam.utils.symbolic.matrix.JMatrix;
import espam.utils.symbolic.matrix.SignedMatrix;

public class JMatrixParserTokenManager implements JMatrixParserConstants {
    public JMatrixParserTokenManager(ASCII_CharStream stream) {
        if (input_stream != null) {
            throw new TokenMgrError("ERROR: Second call to constructor of static lexer. You must use ReInit() to initialize the static variables.", TokenMgrError.STATIC_LEXER_ERROR);
        }
        input_stream = stream;
    }


    public JMatrixParserTokenManager(ASCII_CharStream stream, int lexState) {
        this(stream);
        SwitchTo(lexState);
    }


    public static void ReInit(ASCII_CharStream stream) {
        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }


    public static void ReInit(ASCII_CharStream stream, int lexState) {
        ReInit(stream);
        SwitchTo(lexState);
    }


    public static void SwitchTo(int lexState) {
        if (lexState >= 1 || lexState < 0) {
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
        }
        else {
            curLexState = lexState;
        }
    }


    public static final Token getNextToken() {
        int kind;
        Token specialToken = null;
        Token matchedToken;
        int curPos = 0;

        EOFLoop:
        for (; ; ) {
            try {
                curChar = input_stream.BeginToken();
            } catch (java.io.IOException e) {
                jjmatchedKind = 0;
                matchedToken = jjFillToken();
                matchedToken.specialToken = specialToken;
                return matchedToken;
            }

            try {
                input_stream.backup(0);
                while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L) {
                    curChar = input_stream.BeginToken();
                }
            } catch (java.io.IOException e1) {
                continue EOFLoop;
            }
            jjmatchedKind = 0x7fffffff;
            jjmatchedPos = 0;
            curPos = jjMoveStringLiteralDfa0_0();
            if (jjmatchedKind != 0x7fffffff) {
                if (jjmatchedPos + 1 < curPos) {
                    input_stream.backup(curPos - jjmatchedPos - 1);
                }
                if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                    matchedToken = jjFillToken();
                    matchedToken.specialToken = specialToken;
                    return matchedToken;
                }
                else {
                    if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                        matchedToken = jjFillToken();
                        if (specialToken == null) {
                            specialToken = matchedToken;
                        }
                        else {
                            matchedToken.specialToken = specialToken;
                            specialToken = (specialToken.next = matchedToken);
                        }
                    }
                    continue EOFLoop;
                }
            }
            int error_line = input_stream.getEndLine();
            int error_column = input_stream.getEndColumn();
            String error_after = null;
            boolean EOFSeen = false;
            try {
                input_stream.readChar();
                input_stream.backup(1);
            } catch (java.io.IOException e1) {
                EOFSeen = true;
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
                if (curChar == '\n' || curChar == '\r') {
                    error_line++;
                    error_column = 0;
                }
                else {
                    error_column++;
                }
            }
            if (!EOFSeen) {
                input_stream.backup(1);
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
            }
            throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
        }
    }


    public static final String[] jjstrLiteralImages = {
            "", null, null, null, null, null, null, null, null, "\133", "\73", "\135",
            "\54",};
    public static final String[] lexStateNames = {
            "DEFAULT",
            };
    protected static char curChar;

    static int curLexState = 0;
    static int defaultLexState = 0;
    static final long[] jjbitVec0 = {
            0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
            };
    static int jjmatchedKind;
    static int jjmatchedPos;
    static int jjnewStateCnt;
    static final int[] jjnextStates = {
            7, 8,
            };
    static int jjround;
    static final long[] jjtoSkip = {
            0x11eL,
            };
    static final long[] jjtoSpecial = {
            0x100L,
            };
    static final long[] jjtoToken = {
            0x1ee1L,
            };


    private static final void ReInitRounds() {
        int i;
        jjround = 0x80000001;
        for (i = 10; i-- > 0; ) {
            jjrounds[i] = 0x80000000;
        }
    }


    private static final void jjAddStates(int start, int end) {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }


    private static final void jjCheckNAdd(int state) {
        if (jjrounds[state] != jjround) {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }


    private static final void jjCheckNAddStates(int start, int end) {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }


    private static final void jjCheckNAddStates(int start) {
        jjCheckNAdd(jjnextStates[start]);
        jjCheckNAdd(jjnextStates[start + 1]);
    }


    private static final void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }


    private static final Token jjFillToken() {
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


    private static final int jjMoveNfa_0(int startState, int curPos) {
        int[] nextStates;
        int startsAt = 0;
        jjnewStateCnt = 10;
        int i = 1;
        jjstateSet[0] = startState;
        int j;
        int kind = 0x7fffffff;
        for (; ; ) {
            if (++jjround == 0x7fffffff) {
                ReInitRounds();
            }
            if (curChar < 64) {
                long l = 1L << curChar;
                MatchLoop:
                do {
                    switch (jjstateSet[--i]) {
                        case 2:
                            if ((0x3ff000000000000L & l) != 0L) {
                                if (kind > 6) {
                                    kind = 6;
                                }
                                jjCheckNAdd(3);
                            }
                            else if ((0x280000000000L & l) != 0L) {
                                if (kind > 7) {
                                    kind = 7;
                                }
                            }
                            else if (curChar == 47) {
                                jjstateSet[jjnewStateCnt++] = 6;
                            }
                            break;
                        case 1:
                            if ((0x3ff008000000000L & l) == 0L) {
                                break;
                            }
                            if (kind > 5) {
                                kind = 5;
                            }
                            jjstateSet[jjnewStateCnt++] = 1;
                            break;
                        case 3:
                            if ((0x3ff000000000000L & l) == 0L) {
                                break;
                            }
                            if (kind > 6) {
                                kind = 6;
                            }
                            jjCheckNAddTwoStates(4, 3);
                            break;
                        case 4:
                            if ((0x3ff000000000000L & l) == 0L) {
                                break;
                            }
                            if (kind > 6) {
                                kind = 6;
                            }
                            jjCheckNAdd(4);
                            break;
                        case 5:
                            if ((0x280000000000L & l) != 0L && kind > 7) {
                                kind = 7;
                            }
                            break;
                        case 6:
                            if (curChar == 47) {
                                jjCheckNAddTwoStates(7, 8);
                            }
                            break;
                        case 7:
                            if ((0xffffffffffffdbffL & l) != 0L) {
                                jjCheckNAddTwoStates(7, 8);
                            }
                            break;
                        case 8:
                            if ((0x2400L & l) != 0L) {
                                kind = 8;
                            }
                            break;
                        case 9:
                            if (curChar == 47) {
                                jjstateSet[jjnewStateCnt++] = 6;
                            }
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }
            else if (curChar < 128) {
                long l = 1L << (curChar & 077);
                MatchLoop:
                do {
                    switch (jjstateSet[--i]) {
                        case 2:
                        case 0:
                            if ((0x7fffffe87fffffeL & l) == 0L) {
                                break;
                            }
                            if (kind > 5) {
                                kind = 5;
                            }
                            jjCheckNAddTwoStates(0, 1);
                            break;
                        case 1:
                            if ((0x7fffffe87fffffeL & l) == 0L) {
                                break;
                            }
                            if (kind > 5) {
                                kind = 5;
                            }
                            jjCheckNAdd(1);
                            break;
                        case 7:
                            jjAddStates(0, 1);
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }
            else {
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
                MatchLoop:
                do {
                    switch (jjstateSet[--i]) {
                        case 7:
                            if ((jjbitVec0[i2] & l2) != 0L) {
                                jjAddStates(0, 1);
                            }
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt) == (startsAt = 10 - (jjnewStateCnt = startsAt))) {
                return curPos;
            }
            try {
                curChar = input_stream.readChar();
            } catch (java.io.IOException e) {
                return curPos;
            }
        }
    }


    private static final int jjMoveStringLiteralDfa0_0() {
        switch (curChar) {
            case 44:
                return jjStopAtPos(0, 12);
            case 59:
                return jjStopAtPos(0, 10);
            case 91:
                return jjStopAtPos(0, 9);
            case 93:
                return jjStopAtPos(0, 11);
            default:
                return jjMoveNfa_0(2, 0);
        }
    }


    private static final int jjStartNfaWithStates_0(int pos, int kind, int state) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            return pos + 1;
        }
        return jjMoveNfa_0(state, pos + 1);
    }


    private static final int jjStartNfa_0(int pos, long active0) {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }


    private static final int jjStopAtPos(int pos, int kind) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }


    private static final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
            default:
                return -1;
        }
    }


    private static ASCII_CharStream input_stream;
    private static final int[] jjrounds = new int[10];
    private static final int[] jjstateSet = new int[20];

}
