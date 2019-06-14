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
          
          import espam.utils.symbolic.expression.LinTerm;
              
              /**
                * @author Todor Stefanov
                * @version  $Id: ASTterm.java,v 1.1 2008/05/23 15:04:15 stefanov Exp $
                */
              
              public class ASTterm extends SimpleNode {
              
                  /**
                    *  Constructor for the ASTterm object
                    *
                    * @param  id Description of the Parameter
                    */
                  public ASTterm(int id) {
                  super(id);
              }
                  
                  
                  /**
                    *  Constructor for the ASTterm object
                    *
                    * @param  p Description of the Parameter
                    * @param  id Description of the Parameter
                    */
                  public ASTterm(Parser p, int id) {
                  super(p, id);
              }
                  
                  
                  /**
                    *  Gets the term attribute of the ASTterm object
                    *
                    * @return  The term value
                    */
                  public LinTerm getTerm() {
                  return _term;
              }
                  
                  
                  /**
                    *  Sets the term attribute of the ASTterm object
                    *
                    * @param  aTerm The new term value
                    */
                  public void setTerm(LinTerm aTerm) {
                  _term = aTerm;
              }
                  
                  
                  private LinTerm _term;
          }
