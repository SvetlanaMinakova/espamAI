/*******************************************************************\

The ESPAM Software Tool
Copyright (c) 2004-2008 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.xps.cdpn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.Runtime;
import java.io.File;

import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;

import espam.datamodel.mapping.Mapping;
import espam.visitor.xps.Copier;

import espam.main.UserInterface;
import espam.visitor.CDPNVisitor;

//////////////////////////////////////////////////////////////////////////
////XpsNetworkVisitor

/**
 *  This class .................
 *
 * @author  Wei Zhong, Hristo Nikolov, Todor Stefanov
 * @version  $Id: XpsNetworkVisitor.java,v 1.3 2012/05/02 15:16:08 mohamed Exp $
 */

public class XpsNetworkVisitor extends CDPNVisitor {

    /**
     *  Constructor for the XpsNetworkVisitor object
     *
     * @param  printStream Description of the Parameter
     */
    public XpsNetworkVisitor( Mapping mapping ) {
    	_mapping = mapping;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                     ///

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcessNetwork x ) {

    	try {
            //copy the library
    	    UserInterface _ui = UserInterface.getInstance();
    	    String _codeDir;
    	    if (_ui.getOutputFileName() == "") {
    		_codeDir = _ui.getBasePath() + File.separatorChar + _ui.getFileName() + File.separatorChar;
    	    } else {
    		_codeDir = _ui.getBasePath() + File.separatorChar + _ui.getOutputFileName() + File.separatorChar;
    	    }
            File f = new File(_ui.getXpsLibPath());
            File t = new File(_codeDir);
            Copier.copy(f, t, 1, true);

        	//Create the software
            XpsProcessVisitor pt = new XpsProcessVisitor( _mapping );
            x.accept( pt );

    	} catch (Exception e) {
            System.out.println(" In Xps Network Visitor: exception " +
 			       "occured: " + e.getMessage());
             e.printStackTrace();
 	    }

    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDChannel x ) {

    }

    /**
     * @param  x Description of the Parameter
     */
    public void visitComponent( CDProcess x ) {

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Mapping _mapping;

}

