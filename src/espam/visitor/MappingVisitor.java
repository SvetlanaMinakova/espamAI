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


import espam.datamodel.mapping.Mapping;
import espam.datamodel.mapping.MFifo;
import espam.datamodel.mapping.MProcess;
import espam.datamodel.mapping.MProcessor;

import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// Mapping Visitor

/**
 *  This class is an abstract class for a visitor that is used to generate
 *  Mapping information.
 *
 * @author  Wei Zhong
 * @version  $Id: MappingVisitor.java,v 1.1 2007/12/07 22:07:24 stefanov Exp $
 */

public class MappingVisitor implements Visitor {

	///////////////////////////////////////////////////////////////////
	////                         public methods                     ///

	/**
	 *  Visit a Mapping component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(Mapping x) {
	}

	/**
	 *  Visit a MFifo component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MFifo x) {
	}

	/**
	 *  Visit a MProcess component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MProcess x) {
	}

	/**
	 *  Visit a MProcessor component.
	 *
	 * @param  x A Visitor Object.
	 */
	public void visitComponent(MProcessor x) {
	}

}