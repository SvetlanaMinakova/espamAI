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
      
      package espam.parser.matlab.scheduler;
          
          /**
            *  ASTrightVariableList
            *
            * @author Todor Stefanov
            * @version  $Id: ASTrightVariableList.java,v 1.2 2002/05/28 12:37:08
            *      stefanov Exp $
            */
          
          public class ASTrightVariableList extends SimpleNode {
          
              /**
                *  Constructor for the ASTrightVariableList object
                *
                * @param  id Description of the Parameter
                */
              public ASTrightVariableList(int id) {
              super(id);
          }
              
              
              /**
                *  Constructor for the ASTrightVariableList object
                *
                * @param  p Description of the Parameter
                * @param  id Description of the Parameter
                */
              public ASTrightVariableList(Parser p, int id) {
              super(p, id);
          }
              
      }
