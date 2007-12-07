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

public class TokenMgrError extends Error {

    /*
     * Constructors of various flavors follow.
     */
    public TokenMgrError() { }


    public TokenMgrError(String message, int reason) {
        super(message);
        errorCode = reason;
    }


    public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar, int reason) {
        this(LexicalError(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar), reason);
    }


    /**
     * You can also modify the body of this method to customize your error
     * messages. For example, cases like LOOP_DETECTED and
     * INVALID_LEXICAL_STATE are not of end-users concern, so you can return
     * something like : "Internal Error : Please file a bug report .... "
     * from this method for such cases in the release version of your
     * parser.
     *
     * @return The message value
     */
    public String getMessage() {
        return super.getMessage();
    }


    /**
     * Returns a detailed message for the Error when it is thrown by the
     * token manager to indicate a lexical error. Parameters : EOFSeen :
     * indicates if EOF caused the lexicl error curLexState : lexical state
     * in which this error occured errorLine : line number when the error
     * occured errorColumn : column number when the error occured errorAfter
     * : prefix that was seen before this error occured curchar : the
     * offending character Note: You can customize the lexical error message
     * by modifying this method.
     *
     * @param EOFSeen Description of the Parameter
     * @param lexState Description of the Parameter
     * @param errorLine Description of the Parameter
     * @param errorColumn Description of the Parameter
     * @param errorAfter Description of the Parameter
     * @param curChar Description of the Parameter
     * @return Description of the Return Value
     */
    protected static String LexicalError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar) {
        return ("Lexical error at line " +
                errorLine + ", column " +
                errorColumn + ".  Encountered: " +
                (EOFSeen ? "<EOF> " : ("\"" + addEscapes(String.valueOf(curChar)) + "\"") + " (" + (int) curChar + "), ") +
                "after : \"" + addEscapes(errorAfter) + "\"");
    }


    /**
     * Replaces unprintable characters by their espaced (or unicode escaped)
     * equivalents in the given string
     *
     * @param str The feature to be added to the Escapes attribute
     * @return Description of the Return Value
     */
    protected static final String addEscapes(String str) {
        StringBuffer retval = new StringBuffer();
        char ch;
        for (int i = 0; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case 0:
                    continue;
                case '\b':
                    retval.append("\\b");
                    continue;
                case '\t':
                    retval.append("\\t");
                    continue;
                case '\n':
                    retval.append("\\n");
                    continue;
                case '\f':
                    retval.append("\\f");
                    continue;
                case '\r':
                    retval.append("\\r");
                    continue;
                case '\"':
                    retval.append("\\\"");
                    continue;
                case '\'':
                    retval.append("\\\'");
                    continue;
                case '\\':
                    retval.append("\\\\");
                    continue;
                default:
                    if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                        String s = "0000" + Integer.toString(ch, 16);
                        retval.append("\\u" + s.substring(s.length() - 4, s.length()));
                    }
                    else {
                        retval.append(ch);
                    }
                    continue;
            }
        }
        return retval.toString();
    }


    /**
     * Tried to change to an invalid lexical state.
     */
    static final int INVALID_LEXICAL_STATE = 2;
    /*
     * Ordinals for various reasons why an Error of this type can be thrown.
     */
    /**
     * Lexical error occured.
     */
    static final int LEXICAL_ERROR = 0;

    /**
     * Detected (and bailed out of) an infinite loop in the token manager.
     */
    static final int LOOP_DETECTED = 3;

    /**
     * An attempt wass made to create a second instance of a static token
     * manager.
     */
    static final int STATIC_LEXER_ERROR = 1;

    /**
     * Indicates the reason why the exception is thrown. It will have one of
     * the above 4 values.
     */
    int errorCode;
}
