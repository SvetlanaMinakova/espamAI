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

/**
 * An implementation of interface CharStream, where the stream is assumed to
 * contain only ASCII characters (without unicode processing).
 *
 * @author Bart Kienhuis
 * @version $Id: ASCII_CharStream.java,v 1.1 2007/12/07 22:07:02 stefanov Exp $
 */

public final class ASCII_CharStream {

    /**
     * Constructor for the ASCII_CharStream object
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     * @param buffersize Description of the Parameter
     */
    public ASCII_CharStream(java.io.Reader dstream, int startline,
            int startcolumn, int buffersize) {
        if (inputStream != null) {
            throw new Error("\n   ERROR: Second call to the constructor of a static ASCII_CharStream.  You must\n" +
                    "       either use ReInit() or set the JavaCC option STATIC to false\n" +
                    "       during the generation of this class.");
        }
        inputStream = dstream;
        line = startline;
        column = startcolumn - 1;

        available = bufsize = buffersize;
        buffer = new char[buffersize];
        bufline = new int[buffersize];
        bufcolumn = new int[buffersize];
    }


    /**
     * Constructor for the ASCII_CharStream object
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     */
    public ASCII_CharStream(java.io.Reader dstream, int startline,
            int startcolumn) {
        this(dstream, startline, startcolumn, 4096);
    }


    /**
     * Constructor for the ASCII_CharStream object
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     * @param buffersize Description of the Parameter
     */
    public ASCII_CharStream(java.io.InputStream dstream, int startline,
            int startcolumn, int buffersize) {
        this(new java.io.InputStreamReader(dstream), startline, startcolumn, 4096);
    }


    /**
     * Constructor for the ASCII_CharStream object
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     */
    public ASCII_CharStream(java.io.InputStream dstream, int startline,
            int startcolumn) {
        this(dstream, startline, startcolumn, 4096);
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception java.io.IOException MyException If such and such occurs
     */
    public static final char BeginToken() throws java.io.IOException {
        tokenBegin = -1;
        char c = readChar();
        tokenBegin = bufpos;

        return c;
    }


    /**
     * Description of the Method
     */
    public static void Done() {
        buffer = null;
        bufline = null;
        bufcolumn = null;
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public static final String GetImage() {
        if (bufpos >= tokenBegin) {
            return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
        }
        else {
            return new String(buffer, tokenBegin, bufsize - tokenBegin) +
                    new String(buffer, 0, bufpos + 1);
        }
    }


    /**
     * Description of the Method
     *
     * @param len Description of the Parameter
     * @return Description of the Return Value
     */
    public static final char[] GetSuffix(int len) {
        char[] ret = new char[len];

        if ((bufpos + 1) >= len) {
            System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
        }
        else {
            System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,
                    len - bufpos - 1);
            System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
        }

        return ret;
    }


    /**
     * Description of the Method
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     * @param buffersize Description of the Parameter
     */
    public static void ReInit(java.io.Reader dstream, int startline,
            int startcolumn, int buffersize) {
        inputStream = dstream;
        line = startline;
        column = startcolumn - 1;

        if (buffer == null || buffersize != buffer.length) {
            available = bufsize = buffersize;
            buffer = new char[buffersize];
            bufline = new int[buffersize];
            bufcolumn = new int[buffersize];
        }
        prevCharIsLF = prevCharIsCR = false;
        tokenBegin = inBuf = maxNextCharInd = 0;
        bufpos = -1;
    }


    /**
     * Description of the Method
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     */
    public static void ReInit(java.io.Reader dstream, int startline,
            int startcolumn) {
        ReInit(dstream, startline, startcolumn, 4096);
    }


    /**
     * Description of the Method
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     * @param buffersize Description of the Parameter
     */
    public static void ReInit(java.io.InputStream dstream, int startline,
            int startcolumn, int buffersize) {
        ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, 4096);
    }


    /**
     * Description of the Method
     *
     * @param dstream Description of the Parameter
     * @param startline Description of the Parameter
     * @param startcolumn Description of the Parameter
     */
    public static void ReInit(java.io.InputStream dstream, int startline,
            int startcolumn) {
        ReInit(dstream, startline, startcolumn, 4096);
    }


    /**
     * Method to adjust line and column numbers for the start of a token.
     * <BR>
     *
     *
     * @param newLine Description of the Parameter
     * @param newCol Description of the Parameter
     */
    public static void adjustBeginLineColumn(int newLine, int newCol) {
        int start = tokenBegin;
        int len;

        if (bufpos >= tokenBegin) {
            len = bufpos - tokenBegin + inBuf + 1;
        }
        else {
            len = bufsize - tokenBegin + bufpos + 1 + inBuf;
        }

        int i = 0;

        int j = 0;

        int k = 0;
        int nextColDiff = 0;
        int columnDiff = 0;

        while (i < len &&
                bufline[j = start % bufsize] == bufline[k = ++start % bufsize]) {
            bufline[j] = newLine;
            nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];
            bufcolumn[j] = newCol + columnDiff;
            columnDiff = nextColDiff;
            i++;
        }

        if (i < len) {
            bufline[j] = newLine++;
            bufcolumn[j] = newCol + columnDiff;

            while (i++ < len) {
                if (bufline[j = start % bufsize] != bufline[++start % bufsize]) {
                    bufline[j] = newLine++;
                }
                else {
                    bufline[j] = newLine;
                }
            }
        }

        line = bufline[j];
        column = bufcolumn[j];
    }


    /**
     * Description of the Method
     *
     * @param amount Description of the Parameter
     */
    public static final void backup(int amount) {

        inBuf += amount;
        if ((bufpos -= amount) < 0) {
            bufpos += bufsize;
        }
    }


    /**
     * Gets the beginColumn attribute of the ASCII_CharStream class
     *
     * @return The beginColumn value
     */
    public static final int getBeginColumn() {
        return bufcolumn[tokenBegin];
    }


    /**
     * Gets the beginLine attribute of the ASCII_CharStream class
     *
     * @return The beginLine value
     */
    public static final int getBeginLine() {
        return bufline[tokenBegin];
    }


    /**
     * @return The column value
     * @deprecated
     * @see #getEndColumn
     */

    public static final int getColumn() {
        return bufcolumn[bufpos];
    }


    /**
     * Gets the endColumn attribute of the ASCII_CharStream class
     *
     * @return The endColumn value
     */
    public static final int getEndColumn() {
        return bufcolumn[bufpos];
    }


    /**
     * Gets the endLine attribute of the ASCII_CharStream class
     *
     * @return The endLine value
     */
    public static final int getEndLine() {
        return bufline[bufpos];
    }


    /**
     * @return The line value
     * @deprecated
     * @see #getEndLine
     */

    public static final int getLine() {
        return bufline[bufpos];
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     * @exception java.io.IOException MyException If such and such occurs
     */
    public static final char readChar() throws java.io.IOException {
        if (inBuf > 0) {
            --inBuf;
            return (char) ((char) 0xff & buffer[(bufpos == bufsize - 1) ? (bufpos = 0) : ++bufpos]);
        }

        if (++bufpos >= maxNextCharInd) {
            FillBuff();
        }

        char c = (char) ((char) 0xff & buffer[bufpos]);

        UpdateLineColumn(c);
        return (c);
    }


    /**
     * Description of the Field
     */
    public static int bufpos = -1;
    /**
     * Description of the Field
     */
    public static final boolean staticFlag = true;
    static int available;
    static int bufsize;
    static int tokenBegin;


    /**
     * Description of the Method
     *
     * @param wrapAround Description of the Parameter
     */
    private static final void ExpandBuff(boolean wrapAround) {
        char[] newbuffer = new char[bufsize + 2048];
        int newbufline[] = new int[bufsize + 2048];
        int newbufcolumn[] = new int[bufsize + 2048];

        try {
            if (wrapAround) {
                System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
                System.arraycopy(buffer, 0, newbuffer,
                        bufsize - tokenBegin, bufpos);
                buffer = newbuffer;

                System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
                System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
                bufline = newbufline;

                System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
                System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
                bufcolumn = newbufcolumn;

                maxNextCharInd = (bufpos += (bufsize - tokenBegin));
            }
            else {
                System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
                buffer = newbuffer;

                System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
                bufline = newbufline;

                System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
                bufcolumn = newbufcolumn;

                maxNextCharInd = (bufpos -= tokenBegin);
            }
        } catch (Throwable t) {
            throw new Error(t.getMessage());
        }

        bufsize += 2048;
        available = bufsize;
        tokenBegin = 0;
    }


    /**
     * Description of the Method
     *
     * @exception java.io.IOException MyException If such and such occurs
     */
    private static final void FillBuff() throws java.io.IOException {
        if (maxNextCharInd == available) {
            if (available == bufsize) {
                if (tokenBegin > 2048) {
                    bufpos = maxNextCharInd = 0;
                    available = tokenBegin;
                }
                else if (tokenBegin < 0) {
                    bufpos = maxNextCharInd = 0;
                }
                else {
                    ExpandBuff(false);
                }
            }
            else if (available > tokenBegin) {
                available = bufsize;
            }
            else if ((tokenBegin - available) < 2048) {
                ExpandBuff(true);
            }
            else {
                available = tokenBegin;
            }
        }

        int i;
        try {
            if ((i = inputStream.read(buffer, maxNextCharInd,
                    available - maxNextCharInd)) == -1) {
                inputStream.close();
                throw new java.io.IOException();
            }
            else {
                maxNextCharInd += i;
            }
            return;
        } catch (java.io.IOException e) {
            --bufpos;
            backup(0);
            if (tokenBegin == -1) {
                tokenBegin = bufpos;
            }
            throw e;
        }
    }


    /**
     * Description of the Method
     *
     * @param c Description of the Parameter
     */
    private static final void UpdateLineColumn(char c) {
        column++;

        if (prevCharIsLF) {
            prevCharIsLF = false;
            line += (column = 1);
        }
        else if (prevCharIsCR) {
            prevCharIsCR = false;
            if (c == '\n') {
                prevCharIsLF = true;
            }
            else {
                line += (column = 1);
            }
        }

        switch (c) {
            case '\r':
                prevCharIsCR = true;
                break;
            case '\n':
                prevCharIsLF = true;
                break;
            case '\t':
                column--;
                column += (8 - (column & 07));
                break;
            default:
                break;
        }

        bufline[bufpos] = line;
        bufcolumn[bufpos] = column;
    }


    private static int bufcolumn[];

    private static char[] buffer;
    private static int bufline[];

    private static int column = 0;
    private static int inBuf = 0;

    private static java.io.Reader inputStream;
    private static int line = 1;
    private static int maxNextCharInd = 0;

    private static boolean prevCharIsCR = false;
    private static boolean prevCharIsLF = false;

}
