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
          
          import java.util.Iterator;
              import java.util.LinkedList;
                  
                  import espam.datamodel.parsetree.statement.Statement;
                      
                      /**
                        * @author  Todor Stefanov
                        * @version  $Id: ASTindexStatements.java,v 1.2 2002/06/19 21:14:22 kienhuis
                        *      Exp $
                        */
                      
                      public class ASTindexStatements extends SimpleNode {
                      /**
                        *  Constructor for the ASTindexStatements object
                        *
                        * @param  id Description of the Parameter
                        */
                          public ASTindexStatements(int id) {
                          super(id);
                      }
                          
                          
                          /**
                            *  Constructor for the ASTindexStatements object
                            *
                            * @param  p Description of the Parameter
                            * @param  id Description of the Parameter
                            */
                          public ASTindexStatements(Parser p, int id) {
                          super(p, id);
                      }
                          
                          
                          /**
                            *  Adds a feature to the IndexStatementFirst attribute of the
                            *  ASTindexStatements object
                            *
                            * @param  pn The feature to be added to the IndexStatementFirst
                            *      attribute
                            */
                          public void addIndexStatementFirst(Statement pn) {
                          _list.addFirst(pn);
                      }
                          
                          
                          /**
                            *  Adds a feature to the IndexStatementLast attribute of the
                            *  ASTindexStatements object
                            *
                            * @param  pn The feature to be added to the IndexStatementLast
                            *      attribute
                            */
                          public void addIndexStatementLast(Statement pn) {
                          _list.addLast(pn);
                      }
                          
                          
                          /**
                            *  Gets the indexStatements attribute of the ASTindexStatements object
                            *
                            * @return  The indexStatements value
                            */
                          public Iterator getIndexStatements() {
                          return _list.iterator();
                      }
                          
                          
                          private LinkedList _list = new LinkedList();
                              
                  }
