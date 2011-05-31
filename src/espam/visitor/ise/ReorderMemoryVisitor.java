/*******************************************************************\

The ESPAM Software Tool 
Copyright (c) 2004-2009 Leiden University (LERC group at LIACS).
All rights reserved.

The use and distribution terms for this software are covered by the 
Common Public License 1.0 (http://opensource.org/licenses/cpl1.0.txt)
which can be found in the file LICENSE at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by 
the terms of this license.

You must not remove this notice, or any other, from this software.

\*******************************************************************/

package espam.visitor.ise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.HashMap;
import java.lang.Math;

import espam.datamodel.EspamException;
import espam.datamodel.LinearizationType;
import espam.datamodel.mapping.*;
import espam.datamodel.domain.IndexVector;
import espam.datamodel.domain.Polytope;
import espam.datamodel.graph.adg.*;
import espam.datamodel.platform.memories.*;
import espam.datamodel.platform.Platform;
import espam.datamodel.platform.Resource;
import espam.datamodel.platform.Port;
import espam.datamodel.platform.ports.*;
import espam.datamodel.platform.hwnodecompaan.*;
import espam.datamodel.pn.cdpn.*;
import espam.datamodel.pn.*;

import espam.main.UserInterface;

import espam.operations.transformations.Polytope2IndexBoundVector;

import espam.visitor.PlatformVisitor;
import espam.visitor.expression.*;
import espam.visitor.xps.Copier;

import espam.utils.symbolic.expression.*;
import espam.utils.symbolic.matrix.JMatrix;




//////////////////////////////////////////////////////////////////////////
//// Reorder Memory Visitor

/**
 * This class generates a reorder memory in VHDL for a given channel.
 *
 * @author Sven van Haastregt
 * @version $Id: ReorderMemoryVisitor.java,v 1.2 2011/05/31 10:05:08 svhaastr Exp $
 */

public class ReorderMemoryVisitor extends PlatformVisitor {

  // /////////////////////////////////////////////////////////////////
  // // public methods ///

  /**
   * Constructor for the ReorderMemoryVisitor object
   * @param mapping
   *            The mapping of the corresponding platform which contains crucial mapping information.
   */
  public ReorderMemoryVisitor( Mapping mapping, String codeDir ) {
    _ui = UserInterface.getInstance();
    _mapping = mapping;
    _adg = _mapping.getADG();
    _codeDir = codeDir;
  }

  /**
   * Generates a reorder memory for a particular "fifo" channel.
   *
   * @param x
   *            The "FIFO" that needs to be rendered.
   */
  public void visitComponent(Fifo x) {
    try {
      LinearizationType commModel = _mapping.getCDChannel(x).getCommunicationModel();
      if (commModel == LinearizationType.GenericOutOfOrder) {
        System.err.println("WARNING: Out of order not yet supported in ISE visitor.");
      }
      else {
        System.err.println("WARNING: Reorder visitor called, but channel is not GenericOutOfOrder.");
      }
      _coreName = "reorder_" + x.getName().substring(5); // remove leading FIFO_ part from name
      _moduleDir = _coreName;
      File dir = new File(_codeDir + "/" + _moduleDir);
      dir.mkdirs();

      CDChannel cdchan = _mapping.getCDChannel(x);
      assert(cdchan.getAdgEdgeList().size() == 1);    // Multiple ADGEdges for same channel untested/unhandled
      _adgEdge = (ADGEdge) cdchan.getAdgEdgeList().get(0);

      _fifo = x;
      _srcNode = (ADGNode)(_adgEdge.getFromPort().getNode());
      _dstNode = (ADGNode)(_adgEdge.getToPort().getNode());
      _writeTopLevel();
      _writeWriteAddrGen();
      _writeReadAddrGen();

    } catch (Exception e) {
      System.out.println(" In ISE Network Visitor: exception " + "occured: " + e.getMessage());
      e.printStackTrace();
    }

  }








  // /////////////////////////////////////////////////////////////////
  // // private methods ///

  /**
   * Write node domain info.
   */
  private void _writeDomainData(PrintStream outPS, ADGNode node) {
    IndexVector indexList = node.getDomain().getLinearBound().firstElement().getIndexVector();
    outPS.println("  -- Constants");
    outPS.println("  constant c_COUNTERS     : natural := " + (indexList.getIterationVector().size()) + ";");

    // get counter_width, default is 10
    String s = "";
    int maxCounterWidth = 0;
    int num = indexList.getIterationVector().size() - 1;
    Vector<Expression> boundExp = Polytope2IndexBoundVector.getExpression(node.getDomain().getLinearBound().get(0));
    
    for (int iterNum = 0; iterNum < indexList.getIterationVector().size(); iterNum++){
      String indexName = indexList.getIterationVector().get(iterNum);
      Expression expr_lb = boundExp.get(2*iterNum);
      Expression expr_ub = boundExp.get(2*iterNum + 1);
      int ub = CompaanHWNodeIseVisitor._findUpperBound(indexName, expr_lb, expr_ub);
      int counterWidth = (int) ((Math.log(ub)/Math.log(2))+2);  // Adding 2 to ensure correctness (sign bit)
      s = num + "=>" + counterWidth + ", " + s;

      num--;

      if(counterWidth > maxCounterWidth) {
        maxCounterWidth = counterWidth;
      }
    }
    outPS.println("  constant c_CNTR_QUANT   : natural := " + maxCounterWidth + ";");
    outPS.println("  constant c_CNTR_WIDTHS  : t_counter_width := ( " + s + "others=>10 );");
    outPS.println("");
    outPS.println("  signal sl_lower_bnd, sl_upper_bnd : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
    outPS.println("  signal sl_ITERATORS               : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
    outPS.println("  signal sl_REG_CNTRS               : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");

    Iterator j = indexList.getIterationVector().iterator();
    while(j.hasNext()){
      s = (String) j.next();
      outPS.println("  signal sl_low_" + s +", sl_high_" + s +"      : integer;");
      outPS.println("  signal sl_loop_" + s + ", sl_loop_" + s + "_rg  : integer;");
    }
  }


  /**
   * Write loop bounds.
   */
  private void _writeLoopBounds(PrintStream outPS, ADGNode node) {
    IndexVector indexList = node.getDomain().getLinearBound().firstElement().getIndexVector();
    Vector<Expression> boundExp = Polytope2IndexBoundVector.getExpression(node.getDomain().getLinearBound().get(0));
    Iterator exprIter;
    VhdlExpressionVisitor ExpVisit = new VhdlExpressionVisitor();
    exprIter = boundExp.iterator();
    
    Iterator j = indexList.getIterationVector().iterator();
    while (j.hasNext() && exprIter.hasNext()) {
      String s = (String) j.next();
      Expression lbExp = (Expression) exprIter.next();
      Expression ubExp = (Expression) exprIter.next();

      String lowerBound   = ExpVisit.visit(lbExp, indexList, 0);
      String lowerBoundRg = ExpVisit.visit(lbExp, indexList, 1);
      String upperBound   = ExpVisit.visit(ubExp, indexList, 1);

      outPS.println("  sl_low_" + s + "     <= " + lowerBound + " when RST='0' else " + lowerBoundRg +";");
      outPS.println("  sl_high_" + s + "    <= " + upperBound + ";");
    }
    outPS.println("");

    int loopNum = indexList.getIterationVector().size();
    j = indexList.getIterationVector().iterator();
    while (j.hasNext()) {
      String s = (String) j.next();
      outPS.println("  sl_loop_" + s + "    <= to_integer(signed( sl_ITERATORS(c_CNTR_WIDTHS(" + (loopNum - 1)+ ")+" + (loopNum - 1) + "*c_CNTR_QUANT-1 downto " + (loopNum - 1) + "*c_CNTR_QUANT)));");
      outPS.println("  sl_loop_" + s + "_rg <= to_integer(signed( sl_REG_CNTRS(c_CNTR_WIDTHS(" + (loopNum - 1) + ")+" + (loopNum - 1) + "*c_CNTR_QUANT-1 downto " + (loopNum - 1) + "*c_CNTR_QUANT)));");
      loopNum--;
    }
    outPS.println("");

    loopNum = indexList.getIterationVector().size();
    j = indexList.getIterationVector().iterator();
    while (j.hasNext()) {
      String s = (String) j.next();
      outPS.println("  sl_lower_bnd(" + loopNum + "*c_CNTR_QUANT-1 downto " + (loopNum -1) + "*c_CNTR_QUANT) <= std_logic_vector(to_signed(sl_low_" + s + ",c_CNTR_QUANT));");
      outPS.println("  sl_upper_bnd(" + loopNum + "*c_CNTR_QUANT-1 downto " + (loopNum -1) + "*c_CNTR_QUANT) <= std_logic_vector(to_signed(sl_high_" + s + ",c_CNTR_QUANT));");
      loopNum--;
    }
  }


  /**
   * Write addressing function.
   */
  private void _writeAddressFunction(PrintStream outPS, ADGNode node) {
    IndexVector indexList = node.getDomain().getLinearBound().firstElement().getIndexVector();
    Vector<Expression> boundExp = Polytope2IndexBoundVector.getExpression(node.getDomain().getLinearBound().get(0));
    String addrFunc = "";
    int cumulBoundingBox = 1;
    for (int iterNum = 0; iterNum < indexList.getIterationVector().size(); iterNum++){
      String indexName = indexList.getIterationVector().get(iterNum);
      Expression expr_lb = boundExp.get(2*iterNum);
      Expression expr_ub = boundExp.get(2*iterNum + 1);
      int ub = CompaanHWNodeIseVisitor._findUpperBound(indexName, expr_lb, expr_ub);
      int dimCard = ub+1; // dimension cardinality; assumes lb=0
      outPS.println("  -- " + indexName + "  0--" + ub + "  " + dimCard);
      if (iterNum == 0) {
        addrFunc += "sl_loop_" + indexName + "_rg";
      }
      else {
        addrFunc += " + sl_loop_" + indexName + "_rg*" + cumulBoundingBox;
      }
      cumulBoundingBox *= dimCard;
    }
    outPS.println("  s_writeAddr <= " + addrFunc + ";");
  }


  /**
   * Write the write address generator file.
   */
  private void _writeWriteAddrGen() throws FileNotFoundException {
    PrintStream outPS = _openFile(_moduleDir + "/" + _coreName + "_wag.vhd");
    outPS.println("-- Write address generator for channel " + _fifo.getName());
    outPS.println("-- Generated by ESPAM.");
    outPS.println("-- Sven van Haastregt, LIACS, Leiden University.");
    outPS.println("");
    outPS.println("library IEEE;");
    outPS.println("use IEEE.std_logic_1164.all;");
    outPS.println("use IEEE.numeric_std.all;");
    outPS.println("");
    outPS.println("library work;");
    outPS.println("use work.hw_node_pack.all;");
    outPS.println("");
    outPS.println("entity " + _coreName + "_wag is");
    outPS.println("  generic (");
    outPS.println("    C_AWIDTH : integer := 16");
    outPS.println("  );");
    outPS.println("  port (");
    outPS.println("    clk       : in  std_logic;");
    outPS.println("    rst       : in  std_logic;");
    outPS.println("    nextAddr  : in  std_logic;");
    outPS.println("    writeAddr : out std_logic_vector(C_AWIDTH-1 downto 0)");
    outPS.println("  );");
    outPS.println("end entity " + _coreName + "_wag;");
    outPS.println("");
    outPS.println("");
    outPS.println("architecture behaviour of " + _coreName + "_wag is");
    outPS.println("");
    outPS.println("  component GEN_COUNTER is");
    outPS.println("    generic (");
    outPS.println("      N_CNTRS      : natural := 1;");
    outPS.println("      QUANT        : natural := 32;");
    outPS.println("      CNTR_WIDTH   : t_counter_width := ( 0=>10, 1=>10, 2=>9, others=>10 )");
    outPS.println("    );");
    outPS.println("    port (");
    outPS.println("      RST          : in  std_logic;");
    outPS.println("      CLK          : in  std_logic;");
    outPS.println("      ENABLE       : in  std_logic;");
    outPS.println("      LOWER_BND_IN : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
    outPS.println("      UPPER_BND_IN : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
    outPS.println("      ITERATORS    : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
    outPS.println("      REG_CNTRS    : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
    outPS.println("      DONE         : out std_logic");
    outPS.println("    );");
    outPS.println("  end component;");
    outPS.println("");
    _writeDomainData(outPS, _srcNode);
    outPS.println("  signal s_writeAddr : integer;");
    outPS.println("");
    outPS.println("begin");
    outPS.println("");
    outPS.println("  wag_cntr : GEN_COUNTER");
    outPS.println("  generic map(");
    outPS.println("    N_CNTRS       => c_COUNTERS,");
    outPS.println("    QUANT         => c_CNTR_QUANT,");
    outPS.println("    CNTR_WIDTH    => c_CNTR_WIDTHS");
    outPS.println("  )");
    outPS.println("  port map(");
    outPS.println("    RST           => rst,");
    outPS.println("    CLK           => clk,");
    outPS.println("    ENABLE        => nextAddr,");
    outPS.println("    LOWER_BND_IN  => sl_lower_bnd,");
    outPS.println("    UPPER_BND_IN  => sl_upper_bnd,");
    outPS.println("    ITERATORS     => sl_ITERATORS,");
    outPS.println("    REG_CNTRS     => sl_REG_CNTRS,");
    outPS.println("    DONE          => open");
    outPS.println("  );");
    outPS.println("");
    _writeLoopBounds(outPS, _srcNode);
    outPS.println("");
    _writeAddressFunction(outPS, _srcNode);
    outPS.println("");
    outPS.println("  writeAddr <= std_logic_vector(to_unsigned(s_writeAddr, C_AWIDTH));");
    outPS.println("");
    outPS.println("end architecture behaviour;");
    outPS.println("");
  }


  /**
   * Write the read address generator file.
   */
  private void _writeReadAddrGen() throws FileNotFoundException {
    PrintStream outPS = _openFile(_moduleDir + "/" + _coreName + "_rag.vhd");
    outPS.println("-- Write address generator for channel " + _fifo.getName());
  }


  /**
   * Write the toplevel file.
   */
  private void _writeTopLevel() throws FileNotFoundException {
    PrintStream topPS = _openFile(_moduleDir + "/" + _coreName + ".vhd");
    topPS.println("-- Reordering memory for channel " + _fifo.getName());
    topPS.println("-- Generated by ESPAM.");
    topPS.println("-- Sven van Haastregt, LIACS, Leiden University.");
    topPS.println("");
    topPS.println("library IEEE;");
    topPS.println("use IEEE.std_logic_1164.all;");
    topPS.println("");
    topPS.println("entity " + _coreName + " is");
    topPS.println("  generic (");
    topPS.println("    C_EXT_RESET_HIGH    : integer := 1;");
    topPS.println("    C_DWIDTH            : integer := 32;");
    topPS.println("    C_AWIDTH            : integer := 4");
    topPS.println("  );");
    topPS.println("  port (");
    topPS.println("    -- Clock and reset signals");
    topPS.println("    Ext_Clk : in  std_logic;");
    topPS.println("    Ext_Rst : in  std_logic;");
    topPS.println("");
    topPS.println("    -- FSL master signals");
    topPS.println("    FSL_M_Clk     : in  std_logic;");
    topPS.println("    FSL_M_Data    : in  std_logic_vector(0 to C_DWIDTH-1);");
    topPS.println("    FSL_M_Control : in  std_logic;");
    topPS.println("    FSL_M_Write   : in  std_logic;");
    topPS.println("    FSL_M_Full    : out std_logic;");
    topPS.println("");
    topPS.println("    -- FSL slave signals");
    topPS.println("    FSL_S_Clk     : in  std_logic;");
    topPS.println("    FSL_S_Data    : out std_logic_vector(0 to C_DWIDTH-1);");
    topPS.println("    FSL_S_Control : out std_logic;");
    topPS.println("    FSL_S_Read    : in  std_logic;");
    topPS.println("    FSL_S_Exists  : out std_logic");
    topPS.println("  );");
    topPS.println("end entity " + _coreName + ";");
    topPS.println("");
    topPS.println("");
    topPS.println("architecture behaviour of " + _coreName +" is");
    topPS.println("");
    topPS.println("  signal s_rst : std_logic;");
    topPS.println("  signal s_clk : std_logic;");
    topPS.println("");
    topPS.println("begin");
    topPS.println("");
    topPS.println("  s_clk <= Ext_Clk;");
    topPS.println("  s_rst <= Ext_Rst when C_EXT_RESET_HIGH=1 else not Ext_Rst;");
    topPS.println("");

    topPS.println("-- TODO: generate body, this is a playground for now");
    JMatrix m = _adgEdge.getMapping();

    topPS.println("");
    topPS.println("end architecture behaviour;");
  }

  

  /**
   * Open a file to write
   * 
   * @param fileName
   *            the fullpath file name
   */
  private PrintStream _openFile(String fileName) throws FileNotFoundException {
    PrintStream ps = null;
    String fn = "";

    System.out.println(" -- OPEN FILE: " + fileName);

    fn = _codeDir + "/" + fileName;
    if (fileName.equals(""))
      ps = new PrintStream(System.out);
    else
      ps = new PrintStream(new FileOutputStream(fn));

    return ps;
  }


  private boolean _isSource(ADGNode node) {
    return (node.getInPorts().size() == 0);
  }
  
  private boolean _isSink(ADGNode node) {
    return (node.getOutPorts().size() == 0);
  }
  
  
  
  // /////////////////////////////////////////////////////////////////
  // // private variables ///
  private String _coreName;

  private String _moduleName;

  private String _moduleDir;

  private UserInterface _ui = null;

  private String _codeDir;

  private String _projectName;
  
  private Mapping _mapping;

  private ADGraph _adg;
  
  private ADGEdge _adgEdge; //corresponding ADG edge

  private ADGNode _srcNode;   // Writer ADG Node
  private ADGNode _dstNode;   // Read ADG Node

  private Fifo _fifo;
  
  private Vector _inArgList ;   //in arguments of the ADG function
  
  private Vector _adgInPorts ;      //in ports of the ADG node
  
  private Vector _outArgList ;      //out arguments of the ADG function
  
  private Vector _adgOutPorts ;     //out ports of the ADG node

  ////////////////////////////////////
  // Experimental & hardcoded options:
  ////////////////////////////////////

}
