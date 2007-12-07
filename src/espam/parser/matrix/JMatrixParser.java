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

/**
 * This class defines a Parser that construct a JMatrix from a String. The
 * String describes a Matrix in the way a matrix is written in Matlab.
 *
 * @author Bart Kienhuis
 * @version $Id: JMatrixParser.java,v 1.1 2007/12/07 22:07:03 stefanov Exp $
 */

public class JMatrixParser
/*
 * @bgen(jjtree)
 */
         implements JMatrixParserConstants, JMatrixParserTreeConstants {

    public JMatrixParser() {
        this(new ByteArrayInputStream("a hack!!".getBytes()));
        _byteStream = null;
    }


    public JMatrixParser(java.io.InputStream stream) {
        if (jj_initialized_once) {
            System.out.println("ERROR: Second call to constructor of static parser.  You must");
            System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
            System.out.println("       during parser generation.");
            throw new Error();
        }
        jj_initialized_once = true;
        jj_input_stream = new ASCII_CharStream(stream, 1, 1);
        token_source = new JMatrixParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
    }


    public JMatrixParser(java.io.Reader stream) {
        if (jj_initialized_once) {
            System.out.println("ERROR: Second call to constructor of static parser.  You must");
            System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
            System.out.println("       during parser generation.");
            throw new Error();
        }
        jj_initialized_once = true;
        jj_input_stream = new ASCII_CharStream(stream, 1, 1);
        token_source = new JMatrixParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
    }


    public JMatrixParser(JMatrixParserTokenManager tm) {
        if (jj_initialized_once) {
            System.out.println("ERROR: Second call to constructor of static parser.  You must");
            System.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
            System.out.println("       during parser generation.");
            throw new Error();
        }
        jj_initialized_once = true;
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
    }


    public void ReInit(JMatrixParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jjtree.reset();
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
    }


    public static void ReInit(java.io.InputStream stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jjtree.reset();
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
    }


    public static void ReInit(java.io.Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jjtree.reset();
        jj_gen = 0;
        for (int i = 0; i < 4; i++) {
            jj_la1[i] = -1;
        }
    }


    public static final void disable_tracing() {
    }


    public static final long element() throws ParseException {
        /*
         * @bgen(jjtree) element
         */
        SimpleNode jjtn000 = new SimpleNode(JJTELEMENT);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        Token i;
        Token s;
        int sign = 1;
        int value;
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case SIGN:
                    s = jj_consume_token(SIGN);
                    if ((s.image).equals("-")) {
                        sign = -1 * sign;
                    }
                    break;
                default:
                    jj_la1[3] = jj_gen;
                    ;
            }
            i = jj_consume_token(NATURAL);
            value = sign * (new Integer(i.image)).intValue();
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
             {
                if (true) {
                    return value;
                }
            }
        } finally {
            if (jjtc000) {
                jjtree.closeNodeScope(jjtn000, true);
            }
        }
        throw new Error("Missing return statement in function");
    }


    public static final void enable_tracing() {
    }


    public static final ParseException generateParseException() {
        jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[13];
        for (int i = 0; i < 13; i++) {
            la1tokens[i] = false;
        }
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 4; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 13; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.addElement(jj_expentry);
            }
        }
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = (int[]) jj_expentries.elementAt(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }


    /**
     * Convert a string representing a matrix in Matlab format in an
     * instance of JMatrix.
     *
     * @param matlabString string representing the matrix.
     * @return a Matrix
     * @exception ParseException MyException If such and such occurs
     */
    public static JMatrix getJMatrix(String matlabString)
             throws ParseException {
        _byteStream = new ByteArrayInputStream(matlabString.getBytes());
        ReInit(_byteStream);
        JMatrix m = null;
        m = matrix();
        return m;
    }


    public static final Token getNextToken() {
        if (token.next != null) {
            token = token.next;
        }
        else {
            token = token.next = token_source.getNextToken();
        }
        jj_ntk = -1;
        jj_gen++;
        return token;
    }


    /**
     * Convert a string representing a matrix in Matlab format in an
     * instance of JMatrix.
     *
     * @param matlabString string representing the matrix.
     * @return a Matrix
     * @exception ParseException MyException If such and such occurs
     */
    public static SignedMatrix getSignedMatrix(String matlabString)
             throws ParseException {
        _byteStream = new ByteArrayInputStream(matlabString.getBytes());
        ReInit(_byteStream);
        SignedMatrix m = null;
        m = new SignedMatrix(matrix());
        return m;
    }


    public static final Token getToken(int index) {
        Token t = token;
        for (int i = 0; i < index; i++) {
            if (t.next != null) {
                t = t.next;
            }
            else {
                t = t.next = token_source.getNextToken();
            }
        }
        return t;
    }


    public static final JMatrix matrix() throws ParseException {
        /*
         * @bgen(jjtree) matrix
         */
        SimpleNode jjtn000 = new SimpleNode(JJTMATRIX);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        JMatrix m;
        JMatrix row = null;
        try {
            jj_consume_token(9);
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case NATURAL:
                case SIGN:
                    row = row();
                    break;
                default:
                    jj_la1[0] = jj_gen;
                    ;
            }
            if (row != null) {
                m = (JMatrix) row.clone();
            }
            else {
                m = new JMatrix();
            }
            label_1:
            while (true) {
                switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                    case 10:
                        ;
                        break;
                    default:
                        jj_la1[1] = jj_gen;
                        break label_1;
                }
                jj_consume_token(10);
                row = row();
                m.insertRows(row, m.nbRows());
            }
            jj_consume_token(11);
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
             {
                if (true) {
                    return m;
                }
            }
        } catch (Throwable jjte000) {
            if (jjtc000) {
                jjtree.clearNodeScope(jjtn000);
                jjtc000 = false;
            }
            else {
                jjtree.popNode();
            }
            if (jjte000 instanceof RuntimeException) {
                 {
                    if (true) {
                        throw (RuntimeException) jjte000;
                    }
                }
            }
            if (jjte000 instanceof ParseException) {
                 {
                    if (true) {
                        throw (ParseException) jjte000;
                    }
                }
            }
             {
                if (true) {
                    throw (Error) jjte000;
                }
            }
        } finally {
            if (jjtc000) {
                jjtree.closeNodeScope(jjtn000, true);
            }
        }
        throw new Error("Missing return statement in function");
    }


    public static final JMatrix row() throws ParseException {
        /*
         * @bgen(jjtree) row
         */
        SimpleNode jjtn000 = new SimpleNode(JJTROW);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        Vector row = new Vector();
        JMatrix m;
        long element;
        try {
            element = element();
            row.add(new Long(element));
            label_2:
            while (true) {
                switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                    case 12:
                        ;
                        break;
                    default:
                        jj_la1[2] = jj_gen;
                        break label_2;
                }
                jj_consume_token(12);
                element = element();
                row.add(new Long(element));
            }
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            m = new JMatrix(1, row.size());
            for (int col = 0; col < row.size(); col++) {
                m.setElement(0, col, (long) ((Long) row.elementAt(col)).longValue());
            }
             {
                if (true) {
                    return m;
                }
            }
        } catch (Throwable jjte000) {
            if (jjtc000) {
                jjtree.clearNodeScope(jjtn000);
                jjtc000 = false;
            }
            else {
                jjtree.popNode();
            }
            if (jjte000 instanceof RuntimeException) {
                 {
                    if (true) {
                        throw (RuntimeException) jjte000;
                    }
                }
            }
            if (jjte000 instanceof ParseException) {
                 {
                    if (true) {
                        throw (ParseException) jjte000;
                    }
                }
            }
             {
                if (true) {
                    throw (Error) jjte000;
                }
            }
        } finally {
            if (jjtc000) {
                jjtree.closeNodeScope(jjtn000, true);
            }
        }
        throw new Error("Missing return statement in function");
    }


    public static Token token, jj_nt;
    public static JMatrixParserTokenManager token_source;
    /*
     * @bgen(jjtree)
     */
    protected static JJTJMatrixParserState jjtree = new JJTJMatrixParserState();
    static ASCII_CharStream jj_input_stream;


    private static final Token jj_consume_token(int kind) throws ParseException {
        Token oldToken;
        if ((oldToken = token).next != null) {
            token = token.next;
        }
        else {
            token = token.next = token_source.getNextToken();
        }
        jj_ntk = -1;
        if (token.kind == kind) {
            jj_gen++;
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }


    private static final int jj_ntk() {
        if ((jj_nt = token.next) == null) {
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        }
        else {
            return (jj_ntk = jj_nt.kind);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private //
    private static ByteArrayInputStream _byteStream;

    private static java.util.Vector jj_expentries = new java.util.Vector();
    private static int[] jj_expentry;
    private static int jj_gen;

    private static boolean jj_initialized_once = false;
    private static int jj_kind = -1;
    private static final int[] jj_la1 = new int[4];
    private static final int[] jj_la1_0 = {0xc0, 0x400, 0x1000, 0x80,};
    private static int jj_ntk;

}
