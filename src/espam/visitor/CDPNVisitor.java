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

package espam.visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.pn.cdpn.CDInGate;
import espam.datamodel.pn.cdpn.CDOutGate;
import espam.datamodel.pn.cdpn.CDChannel;

import espam.main.UserInterface;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// ADGraph Visitor

/**
 *  This class is an abstract class for a visitor that is used to generate
 *  Compaan Dynamic Process Network (CDPN) description.
 *
 * @author  Todor Stefanov
 * @version  $Id: CDPNVisitor.java,v 1.1 2007/12/07 22:07:23 stefanov Exp $
 */

public class CDPNVisitor extends PNVisitor {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

	/**
	 *  Visit a CDProcessNetwork component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CDProcessNetwork x) {
	}

	/**
	 *  Visit a CDProcess component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CDProcess x) {
	}

	/**
	 *  Visit a CDInGate component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CDInGate x) {
	}

	/**
	 *  Visit a CDOutGate component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CDOutGate x) {
	}

	/**
	 *  Visit an CDChannel component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(CDChannel x) {
	}
}
