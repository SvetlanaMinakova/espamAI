
package espam.main;

import java.io.StringWriter;

/**
 * Write The Copyright Statement of the ESPAM tool
 *
 * @author Todor Stefanov
 * @version $Id: Copyright.java,v 1.1 2007/12/07 22:06:45 stefanov Exp $
 */
public class Copyright {
    public static void writeCopyright(StringWriter printStream) {
        printStream.write("\n");
        printStream.write(" The ESPAM Software Tool\n");
        
        printStream.write(" Copyright (c) 2004-2008 Leiden University (LERC group at LIACS)\n");
        printStream.write(" All rights reserved.\n");
        printStream.write("\n");
        printStream.write(" The use and distribution terms for this software are covered by the\n");
        printStream.write(" Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)\n");
        printStream.write(" which can be found in the file LICENSE at the root of this distribution.\n");
        printStream.write(" By using this software in any fashion, you are agreeing to be bound by\n");
        printStream.write(" the terms of this license.\n");
        printStream.write("\n");
        
        printStream.write("\n\n");
        printStream.write(" Principal Authors: \n");
        printStream.write("\n");
        printStream.write(" Todor Stefanov \n");
        printStream.write(" Hristo Nikolov \n");
        printStream.write(" Ed Deprettere\n");
        printStream.write("\n");
        printStream.write(" Contributors:\n");
        printStream.write("\n");
        printStream.write(" Kai Huang\n");
        printStream.write(" Ji Gu\n");
        printStream.write(" Wei Zhong\n");
        printStream.write(" Ying Tao\n");
        printStream.write("\n");
        printStream.write(" For more information email: {stefanov,nikolov,edd}@liacs.nl\n");
        printStream.write("\n");
    }
}
