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

package espam.parser.expression;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Iterator;
import espam.utils.symbolic.expression.*;

/**
 * @author Bart Kienhuis
 * @version $Id: ExpressionParser.java,v 1.1 2007/12/07 22:06:56 stefanov Exp $
 */

public class ExpressionParser
/*
 * @bgen(jjtree)
 */
         implements ExpressionParserConstants, ExpressionParserTreeConstants {
    public ExpressionParser() {
        this(new ByteArrayInputStream("a hack!!".getBytes()));
        _byteStream = null;
    }


    public ExpressionParser(java.io.InputStream stream) {
        jj_input_stream = new ASCII_CharStream(stream, 1, 1);
        token_source = new ExpressionParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }


    public ExpressionParser(java.io.Reader stream) {
        jj_input_stream = new ASCII_CharStream(stream, 1, 1);
        token_source = new ExpressionParserTokenManager(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }


    public ExpressionParser(ExpressionParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jj_gen = 0;
        for (int i = 0; i < 11; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }


    public final void Identifier() throws ParseException {
        /*
         * @bgen(jjtree) Identifier
         */
        ASTIdentifier jjtn000 = new ASTIdentifier(this, JJTIDENTIFIER);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        Token t;
        try {
            t = jj_consume_token(IDENTIFIER);
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            jjtn000.setName(t.toString());
        } finally {
            if (jjtc000) {
                jjtree.closeNodeScope(jjtn000, true);
            }
        }
    }


    public final void Integer() throws ParseException {
        /*
         * @bgen(jjtree) Integer
         */
        ASTInteger jjtn000 = new ASTInteger(this, JJTINTEGER);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        Token t;
        try {
            t = jj_consume_token(INTEGER_LITERAL);
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            jjtn000.setValue(t.toString());
        } finally {
            if (jjtc000) {
                jjtree.closeNodeScope(jjtn000, true);
            }
        }
    }


    public void ReInit(java.io.InputStream stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jjtree.reset();
        jj_gen = 0;
        for (int i = 0; i < 11; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }


    public void ReInit(java.io.Reader stream) {
        jj_input_stream.ReInit(stream, 1, 1);
        token_source.ReInit(jj_input_stream);
        token = new Token();
        jj_ntk = -1;
        jjtree.reset();
        jj_gen = 0;
        for (int i = 0; i < 11; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }


    public void ReInit(ExpressionParserTokenManager tm) {
        token_source = tm;
        token = new Token();
        jj_ntk = -1;
        jjtree.reset();
        jj_gen = 0;
        for (int i = 0; i < 11; i++) {
            jj_la1[i] = -1;
        }
        for (int i = 0; i < jj_2_rtns.length; i++) {
            jj_2_rtns[i] = new JJCalls();
        }
    }


    public final Expression complexExpression() throws ParseException {
        /*
         * @bgen(jjtree) complexExpression
         */
        ASTcomplexExpression jjtn000 = new ASTcomplexExpression(this, JJTCOMPLEXEXPRESSION);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        int signValue;
        int numberOfChildern;
        SimpleNode node;
        String nodeName;
        LinTerm trm;
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case ADD:
                case SUB:
                    sign();
                    break;
                default:
                    jj_la1[0] = jj_gen;
                    ;
            }
            termOrOperator();
            label_1:
            while (true) {
                if (jj_2_1(2)) {
                    ;
                }
                else {
                    break label_1;
                }
                linearOperator();
                termOrOperator();
            }
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            numberOfChildern = jjtn000.jjtGetNumChildren();
            signValue = 1;
            ArrayList termList = new ArrayList();
            for (int i = 0; i < numberOfChildern; i++) {
                node = (SimpleNode) jjtn000.jjtGetChild(i);
                nodeName = node.toString();
                // System.out.println(" -- Node: " + nodeName);
                if (nodeName == "sign") {
                    signValue = ((ASTsign) node).getValue();
                }
                if (nodeName == "linearOperator") {
                    signValue = ((ASTlinearOperator) node).getValue();
                }
                if (nodeName == "termOrOperator") {
                    trm = ((ASTtermOrOperator) node).getTerm();
                    trm.setSign(signValue);
                    termList.add(trm);
                }
            }
             {
                if (true) {
                    return new Expression(termList);
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


    public final void disable_tracing() {
    }


    public final void enable_tracing() {
    }


    public final void fraction() throws ParseException {
        /*
         * @bgen(jjtree) fraction
         */
        ASTfraction jjtn000 = new ASTfraction(this, JJTFRACTION);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        int num;
        int den;
        try {
            Integer();
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case FRACTION:
                    jj_consume_token(FRACTION);
                    Integer();
                    break;
                default:
                    jj_la1[10] = jj_gen;
                    ;
            }
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            int numberOfChildern = jjtn000.jjtGetNumChildren();
            if (numberOfChildern == 1) {
                num = ((ASTInteger) jjtn000.jjtGetChild(0)).getValue();
                den = 1;
            }
            else {
                num = ((ASTInteger) jjtn000.jjtGetChild(0)).getValue();
                den = ((ASTInteger) jjtn000.jjtGetChild(1)).getValue();
            }
            jjtn000.setFraction(num, den);
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
    }


    public final ParseException generateParseException() {
        jj_expentries.removeAllElements();
        boolean[] la1tokens = new boolean[52];
        for (int i = 0; i < 52; i++) {
            la1tokens[i] = false;
        }
        if (jj_kind >= 0) {
            la1tokens[jj_kind] = true;
            jj_kind = -1;
        }
        for (int i = 0; i < 11; i++) {
            if (jj_la1[i] == jj_gen) {
                for (int j = 0; j < 32; j++) {
                    if ((jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                }
            }
        }
        for (int i = 0; i < 52; i++) {
            if (la1tokens[i]) {
                jj_expentry = new int[1];
                jj_expentry[0] = i;
                jj_expentries.addElement(jj_expentry);
            }
        }
        jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        int[][] exptokseq = new int[jj_expentries.size()][];
        for (int i = 0; i < jj_expentries.size(); i++) {
            exptokseq[i] = (int[]) jj_expentries.elementAt(i);
        }
        return new ParseException(token, exptokseq, tokenImage);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Expression getExpression(String expression)
             throws ParseException {
        Expression linearExpression = null;
        // Make a byte stream from the string
        _byteStream = new ByteArrayInputStream(expression.getBytes());
        // Re initialize the parser
        this.ReInit(_byteStream);
        linearExpression = this.complexExpression();
        return linearExpression;
    }


    public final Token getNextToken() {
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


    public final Token getToken(int index) {
        Token t = lookingAhead ? jj_scanpos : token;
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


    public final void linearOperator() throws ParseException {
        /*
         * @bgen(jjtree) linearOperator
         */
        ASTlinearOperator jjtn000 = new ASTlinearOperator(this, JJTLINEAROPERATOR);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case ADD:
                    jj_consume_token(ADD);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    jjtn000.setValue(1);
                    break;
                case SUB:
                    jj_consume_token(SUB);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    jjtn000.setValue(-1);
                    break;
                default:
                    jj_la1[3] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        } finally {
            if (jjtc000) {
                jjtree.closeNodeScope(jjtn000, true);
            }
        }
    }


    public final void sign() throws ParseException {
        /*
         * @bgen(jjtree) sign
         */
        ASTsign jjtn000 = new ASTsign(this, JJTSIGN);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case ADD:
                    jj_consume_token(ADD);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    jjtn000.setValue(1);
                    break;
                case SUB:
                    jj_consume_token(SUB);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    jjtn000.setValue(-1);
                    break;
                default:
                    jj_la1[2] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        } finally {
            if (jjtc000) {
                jjtree.closeNodeScope(jjtn000, true);
            }
        }
    }


    public final void simpleExpression() throws ParseException {
        /*
         * @bgen(jjtree) simpleExpression
         */
        ASTsimpleExpression jjtn000 = new ASTsimpleExpression(this, JJTSIMPLEEXPRESSION);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        int signValue;
        int numberOfChildern;
        SimpleNode node;
        String nodeName;
        LinTerm trm;
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case ADD:
                case SUB:
                    sign();
                    break;
                default:
                    jj_la1[1] = jj_gen;
                    ;
            }
            term();
            label_2:
            while (true) {
                if (jj_2_2(2)) {
                    ;
                }
                else {
                    break label_2;
                }
                linearOperator();
                term();
            }
            jjtree.closeNodeScope(jjtn000, true);
            jjtc000 = false;
            numberOfChildern = jjtn000.jjtGetNumChildren();
            signValue = 1;
            ArrayList termList = new ArrayList();
            for (int i = 0; i < numberOfChildern; i++) {
                node = (SimpleNode) jjtn000.jjtGetChild(i);
                nodeName = node.toString();
                if (nodeName == "sign") {
                    signValue = ((ASTsign) node).getValue();
                }
                if (nodeName == "linearOperator") {
                    signValue = ((ASTlinearOperator) node).getValue();
                }
                if (nodeName == "term") {
                    trm = ((ASTterm) node).getTerm();
                    trm.setSign(signValue);
                    termList.add(trm);
                }
            }
            jjtn000.setLinearExp(termList);
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
    }


    public final void specialOperator() throws ParseException {
        /*
         * @bgen(jjtree) specialOperator
         */
        ASTspecialOperator jjtn000 = new ASTspecialOperator(this, JJTSPECIALOPERATOR);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        LinTerm term = null;
        Expression exp = null;
        Expression exp1 = null;
        Expression exp2 = null;
        int div;
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case DIV:
                    jj_consume_token(DIV);
                    jj_consume_token(49);
                    simpleExpression();
                    jj_consume_token(50);
                    Integer();
                    jj_consume_token(51);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    exp = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(0)).getLinearExp();
                    div = ((ASTInteger) jjtn000.jjtGetChild(1)).getValue();
                    term = new DivTerm(exp, div);
                    jjtn000.setTerm(term);
                    break;
                case MOD:
                    jj_consume_token(MOD);
                    jj_consume_token(49);
                    simpleExpression();
                    jj_consume_token(50);
                    Integer();
                    jj_consume_token(51);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    exp = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(0)).getLinearExp();
                    div = ((ASTInteger) jjtn000.jjtGetChild(1)).getValue();
                    term = new ModTerm(exp, div);
                    jjtn000.setTerm(term);
                    break;
                case FLOOR:
                    jj_consume_token(FLOOR);
                    jj_consume_token(49);
                    simpleExpression();
                    jj_consume_token(51);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    exp = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(0)).getLinearExp();
                    term = new FloorTerm(exp);
                    jjtn000.setTerm(term);
                    break;
                case CEIL:
                    jj_consume_token(CEIL);
                    jj_consume_token(49);
                    simpleExpression();
                    jj_consume_token(51);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    exp = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(0)).getLinearExp();
                    term = new CeilTerm(exp);
                    jjtn000.setTerm(term);
                    break;
                case MAX:
                    jj_consume_token(MAX);
                    jj_consume_token(49);
                    simpleExpression();
                    jj_consume_token(50);
                    simpleExpression();
                    jj_consume_token(51);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    exp1 = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(0)).getLinearExp();
                    exp2 = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(1)).getLinearExp();
                    term = new MaximumTerm(exp1, exp2);
                    jjtn000.setTerm(term);
                    break;
                case MIN:
                    jj_consume_token(MIN);
                    jj_consume_token(49);
                    simpleExpression();
                    jj_consume_token(50);
                    simpleExpression();
                    jj_consume_token(51);
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    exp1 = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(0)).getLinearExp();
                    exp2 = ((ASTsimpleExpression)
                            jjtn000.jjtGetChild(1)).getLinearExp();
                    term = new MinimumTerm(exp1, exp2);
                    jjtn000.setTerm(term);
                    break;
                default:
                    jj_la1[6] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
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
    }


    public final void specialTerm() throws ParseException {
        /*
         * @bgen(jjtree) specialTerm
         */
        ASTspecialTerm jjtn000 = new ASTspecialTerm(this, JJTSPECIALTERM);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        int num;
        int den;
        LinTerm term;
        int numberOfChildren;
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case INTEGER_LITERAL:
                    fraction();
                    jj_consume_token(MUL);
                    specialOperator();
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    numberOfChildren = jjtn000.jjtGetNumChildren();
                    num = ((ASTfraction) jjtn000.jjtGetChild(0)).getNumerator();
                    den = ((ASTfraction) jjtn000.jjtGetChild(0)).getDenominator();
                    term = ((ASTspecialOperator) jjtn000.jjtGetChild(1)).getTerm();
                    term.setNumerator(num);
                    term.setDenominator(den);

                    jjtn000.setTerm(term);
                    break;
                case DIV:
                case MOD:
                case FLOOR:
                case CEIL:
                case MIN:
                case MAX:
                    specialOperator();
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    term = ((ASTspecialOperator) jjtn000.jjtGetChild(0)).getTerm();
                    jjtn000.setTerm(term);
                    break;
                default:
                    jj_la1[5] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
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
    }


    public final void term() throws ParseException {
        /*
         * @bgen(jjtree) term
         */
        ASTterm jjtn000 = new ASTterm(this, JJTTERM);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        int num;
        int den;
        String name;
        int numberOfChildern;
        try {
            switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                case INTEGER_LITERAL:
                    fraction();
                    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                        case MUL:
                            jj_consume_token(MUL);
                            Identifier();
                            break;
                        default:
                            jj_la1[7] = jj_gen;
                            ;
                    }
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    numberOfChildern = jjtn000.jjtGetNumChildren();
                    num = ((ASTfraction) jjtn000.jjtGetChild(0)).getNumerator();
                    den = ((ASTfraction) jjtn000.jjtGetChild(0)).getDenominator();
                    name = "";
                    if (numberOfChildern == 2) {
                        name = ((ASTIdentifier) jjtn000.jjtGetChild(1)).getName();
                    }
                    jjtn000.setTerm(new LinTerm(num, den, name));
                    break;
                case IDENTIFIER:
                    Identifier();
                    switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                        case MUL:
                            jj_consume_token(MUL);
                            fraction();
                            break;
                        default:
                            jj_la1[8] = jj_gen;
                            ;
                    }
                    jjtree.closeNodeScope(jjtn000, true);
                    jjtc000 = false;
                    numberOfChildern = jjtn000.jjtGetNumChildren();
                    name = ((ASTIdentifier) jjtn000.jjtGetChild(0)).getName();
                    num = 1;
                    den = 1;
                    if (numberOfChildern == 2) {
                        num = ((ASTfraction) jjtn000.jjtGetChild(1)).getNumerator();
                        den = ((ASTfraction) jjtn000.jjtGetChild(1)).getDenominator();
                    }
                    jjtn000.setTerm(new LinTerm(num, den, name));
                    break;
                default:
                    jj_la1[9] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
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
    }


    public final void termOrOperator() throws ParseException {
        /*
         * @bgen(jjtree) termOrOperator
         */
        ASTtermOrOperator jjtn000 = new ASTtermOrOperator(this, JJTTERMOROPERATOR);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);
        try {
            if (jj_2_3(3)) {
                specialTerm();
                jjtree.closeNodeScope(jjtn000, true);
                jjtc000 = false;
                jjtn000.setTerm(((ASTspecialTerm) jjtn000.jjtGetChild(0)).getTerm());
            }
            else {
                switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
                    case INTEGER_LITERAL:
                    case IDENTIFIER:
                        term();
                        jjtree.closeNodeScope(jjtn000, true);
                        jjtc000 = false;
                        jjtn000.setTerm(((ASTterm) jjtn000.jjtGetChild(0)).getTerm());
                        break;
                    default:
                        jj_la1[4] = jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
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
    }


    public boolean lookingAhead = false;
    public Token token, jj_nt;

    public ExpressionParserTokenManager token_source;
    /*
     * @bgen(jjtree)
     */
    protected JJTExpressionParserState jjtree = new JJTExpressionParserState();


    static final class JJCalls {
        int arg;
        Token first;
        int gen;
        JJCalls next;
    }


    ASCII_CharStream jj_input_stream;


    private final boolean jj_2_1(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        boolean retval = !jj_3_1();
        jj_save(0, xla);
        return retval;
    }


    private final boolean jj_2_2(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        boolean retval = !jj_3_2();
        jj_save(1, xla);
        return retval;
    }


    private final boolean jj_2_3(int xla) {
        jj_la = xla;
        jj_lastpos = jj_scanpos = token;
        boolean retval = !jj_3_3();
        jj_save(2, xla);
        return retval;
    }


    private final boolean jj_3R_10() {
        if (jj_3R_14()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_11() {
        if (jj_3R_15()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_12() {
        if (jj_3R_14()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_scan_token(MUL)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_16()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_13() {
        if (jj_3R_16()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_14() {
        if (jj_3R_17()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_18()) {
            jj_scanpos = xsp;
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_15() {
        if (jj_scan_token(IDENTIFIER)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_16() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_19()) {
            jj_scanpos = xsp;
            if (jj_3R_20()) {
                jj_scanpos = xsp;
                if (jj_3R_21()) {
                    jj_scanpos = xsp;
                    if (jj_3R_22()) {
                        jj_scanpos = xsp;
                        if (jj_3R_23()) {
                            jj_scanpos = xsp;
                            if (jj_3R_24()) {
                                return true;
                            }
                            if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                                return false;
                            }
                        }
                        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                            return false;
                        }
                    }
                    else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                        return false;
                    }
                }
                else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                    return false;
                }
            }
            else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                return false;
            }
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_17() {
        if (jj_scan_token(INTEGER_LITERAL)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_18() {
        if (jj_scan_token(FRACTION)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_17()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_19() {
        if (jj_scan_token(DIV)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_scan_token(49)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_25()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_20() {
        if (jj_scan_token(MOD)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_scan_token(49)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_25()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_21() {
        if (jj_scan_token(FLOOR)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_scan_token(49)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_25()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_22() {
        if (jj_scan_token(CEIL)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_scan_token(49)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_25()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_23() {
        if (jj_scan_token(MAX)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_scan_token(49)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_25()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_24() {
        if (jj_scan_token(MIN)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_scan_token(49)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_25()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_25() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_26()) {
            jj_scanpos = xsp;
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_5()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_26() {
        if (jj_3R_27()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_27() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_28()) {
            jj_scanpos = xsp;
            if (jj_3R_29()) {
                return true;
            }
            if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                return false;
            }
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_28() {
        if (jj_scan_token(ADD)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_29() {
        if (jj_scan_token(SUB)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_3() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_7()) {
            jj_scanpos = xsp;
            if (jj_3R_8()) {
                return true;
            }
            if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                return false;
            }
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_4() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3_3()) {
            jj_scanpos = xsp;
            if (jj_3R_9()) {
                return true;
            }
            if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                return false;
            }
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_5() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_10()) {
            jj_scanpos = xsp;
            if (jj_3R_11()) {
                return true;
            }
            if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                return false;
            }
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_6() {
        Token xsp;
        xsp = jj_scanpos;
        if (jj_3R_12()) {
            jj_scanpos = xsp;
            if (jj_3R_13()) {
                return true;
            }
            if (jj_la == 0 && jj_scanpos == jj_lastpos) {
                return false;
            }
        }
        else if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_7() {
        if (jj_scan_token(ADD)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_8() {
        if (jj_scan_token(SUB)) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3R_9() {
        if (jj_3R_5()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3_1() {
        if (jj_3R_3()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_4()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3_2() {
        if (jj_3R_3()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        if (jj_3R_5()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private final boolean jj_3_3() {
        if (jj_3R_6()) {
            return true;
        }
        if (jj_la == 0 && jj_scanpos == jj_lastpos) {
            return false;
        }
        return false;
    }


    private void jj_add_error_token(int kind, int pos) {
        if (pos >= 100) {
            return;
        }
        if (pos == jj_endpos + 1) {
            jj_lasttokens[jj_endpos++] = kind;
        }
        else if (jj_endpos != 0) {
            jj_expentry = new int[jj_endpos];
            for (int i = 0; i < jj_endpos; i++) {
                jj_expentry[i] = jj_lasttokens[i];
            }
            boolean exists = false;
            for (java.util.Enumeration e = jj_expentries.elements(); e.hasMoreElements(); ) {
                int[] oldentry = (int[]) (e.nextElement());
                if (oldentry.length == jj_expentry.length) {
                    exists = true;
                    for (int i = 0; i < jj_expentry.length; i++) {
                        if (oldentry[i] != jj_expentry[i]) {
                            exists = false;
                            break;
                        }
                    }
                    if (exists) {
                        break;
                    }
                }
            }
            if (!exists) {
                jj_expentries.addElement(jj_expentry);
            }
            if (pos != 0) {
                jj_lasttokens[(jj_endpos = pos) - 1] = kind;
            }
        }
    }


    private final Token jj_consume_token(int kind) throws ParseException {
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
            if (++jj_gc > 100) {
                jj_gc = 0;
                for (int i = 0; i < jj_2_rtns.length; i++) {
                    JJCalls c = jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < jj_gen) {
                            c.first = null;
                        }
                        c = c.next;
                    }
                }
            }
            return token;
        }
        token = oldToken;
        jj_kind = kind;
        throw generateParseException();
    }


    private final int jj_ntk() {
        if ((jj_nt = token.next) == null) {
            return (jj_ntk = (token.next = token_source.getNextToken()).kind);
        }
        else {
            return (jj_ntk = jj_nt.kind);
        }
    }


    private final void jj_rescan_token() {
        jj_rescan = true;
        for (int i = 0; i < 3; i++) {
            JJCalls p = jj_2_rtns[i];
            do {
                if (p.gen > jj_gen) {
                    jj_la = p.arg;
                    jj_lastpos = jj_scanpos = p.first;
                    switch (i) {
                        case 0:
                            jj_3_1();
                            break;
                        case 1:
                            jj_3_2();
                            break;
                        case 2:
                            jj_3_3();
                            break;
                    }
                }
                p = p.next;
            } while (p != null);
        }
        jj_rescan = false;
    }


    private final void jj_save(int index, int xla) {
        JJCalls p = jj_2_rtns[index];
        while (p.gen > jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen = jj_gen + xla - jj_la;
        p.first = token;
        p.arg = xla;
    }


    private final boolean jj_scan_token(int kind) {
        if (jj_scanpos == jj_lastpos) {
            jj_la--;
            if (jj_scanpos.next == null) {
                jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
            }
            else {
                jj_lastpos = jj_scanpos = jj_scanpos.next;
            }
        }
        else {
            jj_scanpos = jj_scanpos.next;
        }
        if (jj_rescan) {
            int i = 0;
            Token tok = token;
            while (tok != null && tok != jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) {
                jj_add_error_token(kind, i);
            }
        }
        return (jj_scanpos.kind != kind);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private //
    private ByteArrayInputStream _byteStream;
    private final JJCalls[] jj_2_rtns = new JJCalls[3];
    private int jj_endpos;

    private java.util.Vector jj_expentries = new java.util.Vector();
    private int[] jj_expentry;
    private int jj_gc = 0;
    private int jj_gen;
    private int jj_kind = -1;
    private int jj_la;
    private final int[] jj_la1 = new int[11];
    private final int[] jj_la1_0 = {0x180000, 0x180000, 0x180000, 0x180000, 0x0, 0xde0, 0xde0, 0x20000, 0x20000, 0x0, 0x40000,};
    private final int[] jj_la1_1 = {0x0, 0x0, 0x0, 0x0, 0x4400, 0x400, 0x0, 0x0, 0x0, 0x4400, 0x0,};
    private int[] jj_lasttokens = new int[100];
    private int jj_ntk;
    private boolean jj_rescan = false;
    private Token jj_scanpos, jj_lastpos;
    private boolean jj_semLA;

}
