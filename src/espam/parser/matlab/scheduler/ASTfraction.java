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
            *  ASTfaction
            *
            * @author  Todor Stefanov
            * @version  $Id: ASTfraction.java,v 1.1 2008/05/23 15:04:18 stefanov Exp $
            */
          
          public class ASTfraction extends SimpleNode {
          
              /**
                *  Constructor for the ASTfraction object
                *
                * @param  id Description of the Parameter
                */
              public ASTfraction(int id) {
              super(id);
          }
              
              
              /**
                *  Constructor for the ASTfraction object
                *
                * @param  p Description of the Parameter
                * @param  id Description of the Parameter
                */
              public ASTfraction(Parser p, int id) {
              super(p, id);
          }
              
              
              /**
                *  Gets the denominator attribute of the ASTfraction object
                *
                * @return  The denominator value
                */
              public int getDenominator() {
              return _den;
          }
              
              
              /**
                *  Gets the numerator attribute of the ASTfraction object
                *
                * @return  The numerator value
                */
              public int getNumerator() {
              return _num;
          }
              
              
              /**
                *  Sets the fraction attribute of the ASTfraction object
                *
                * @param  num The new fraction value
                * @param  den The new fraction value
                */
              public void setFraction(int num, int den) {
              _num = num;
                  _den = den;
          }
              
              
              private int _den;
                  
                  private int _num;
                      
      }
