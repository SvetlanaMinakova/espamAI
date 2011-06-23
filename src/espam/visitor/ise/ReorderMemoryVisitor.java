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
import espam.datamodel.domain.LBS;
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
 * @version $Id: ReorderMemoryVisitor.java,v 1.8 2011/06/23 13:17:19 svhaastr Exp $
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
      if (commModel != LinearizationType.GenericOutOfOrder) {
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
      System.out.println("Exception in ISE Network Visitor: " + e.getMessage());
      e.printStackTrace();
    }

  }








  // /////////////////////////////////////////////////////////////////
  // // private methods ///

  /**
   * Computes the required memory size.
   */
  private int _computeMemsize(LBS domain) {
    int size = 1;
    // For now, just count the number of points in the bounding box and consider that as the size
    int boxes[][] = _computeBoundingBoxes(domain.getLinearBound());
    for (int i = 0; i < boxes.length; i++) {
      size *= boxes[i][2];
    }
    return size;
  }


  /**
   * Writes node domain signal declarations.
   */
  private void _writeDomainData(PrintStream outPS, LBS domain) {
    IndexVector indexList = domain.getLinearBound().firstElement().getIndexVector();
    outPS.println("  -- Constants");
    outPS.println("  constant c_COUNTERS     : natural := " + (indexList.getIterationVector().size()) + ";");

    String s = "";
    String iterDecls = "";

    int maxCounterWidth = 0;
    int num = indexList.getIterationVector().size() - 1;
    Vector<Expression> boundExp = Polytope2IndexBoundVector.getExpression(domain.getLinearBound().get(0));
    int bounds[][] = _computeBoundingBoxes(domain.getLinearBound());

    ExpressionAnalyzer ea = new ExpressionAnalyzer(indexList);
    for (int iterNum = 0; iterNum < indexList.getIterationVector().size(); iterNum++){
      String indexName = indexList.getIterationVector().get(iterNum);
			Expression expr_lb = boundExp.get(2*iterNum);
			Expression expr_ub = boundExp.get(2*iterNum + 1);
      int lb = bounds[iterNum][0];
      int ub = bounds[iterNum][1];
      int counterWidth = ea.computeCounterWidth(indexName, expr_lb, expr_ub);
      s = num + "=>" + counterWidth + ", " + s;
      num--;

      if(counterWidth > maxCounterWidth) {
        maxCounterWidth = counterWidth;
      }

      iterDecls += "  signal sl_low_" + indexName + ", sl_high_" + indexName + "      : integer;\n";
      iterDecls += "  signal sl_loop_" + indexName + ", sl_loop_" + indexName + "_rg  : integer range " + lb + " to " + ub + ";\n";
      iterDecls += "  signal sl_" + indexName + ", sl_" + indexName + "_rg            : integer range " + lb + " to " + ub + ";\n";
      iterDecls += "  signal sl_m_" + indexName + "                    : integer range " + lb + " to " + ub + ";\n";
      iterDecls += "  signal sl_" + indexName + "incr                  : std_logic;\n";
    }
    outPS.println("  constant c_CNTR_QUANT   : natural := " + maxCounterWidth + ";");
    outPS.println("  constant c_CNTR_WIDTHS  : t_counter_width := ( " + s + "others=>10 );");
    outPS.println("");
    outPS.println("  signal sl_lower_bnd, sl_upper_bnd : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
    outPS.println("  signal sl_ITERATORS               : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
    outPS.println("  signal sl_REG_CNTRS               : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
    outPS.println(iterDecls);
  }


  /**
   * Write loop bounds.
   */
  private void _writeLoopControl(PrintStream outPS, LBS domain) {
    IndexVector indexList = domain.getLinearBound().firstElement().getIndexVector();
    Vector<Expression> boundExp = Polytope2IndexBoundVector.getExpression(domain.getLinearBound().get(0));
    Iterator exprIter;
    VhdlExpressionVisitor ExpVisit = new VhdlExpressionVisitor();
    exprIter = boundExp.iterator();
    
    String incrAssignments = "";
    String loopIterAssignments = "";
    String loopIterRgAssignments = "";
    Iterator j;
    int loopNum = indexList.getIterationVector().size();
    j = indexList.getIterationVector().iterator();
    while (j.hasNext()) {
      String s = (String) j.next();
      outPS.println("  sl_" + s + "    <= to_integer(signed( sl_ITERATORS(c_CNTR_WIDTHS(" + (loopNum - 1)+ ")+" + (loopNum - 1) + "*c_CNTR_QUANT-1 downto " + (loopNum - 1) + "*c_CNTR_QUANT)));");
      outPS.println("  sl_" + s + "_rg <= to_integer(signed( sl_REG_CNTRS(c_CNTR_WIDTHS(" + (loopNum - 1) + ")+" + (loopNum - 1) + "*c_CNTR_QUANT-1 downto " + (loopNum - 1) + "*c_CNTR_QUANT)));");
      if (j.hasNext()) {
        int counterNum = Integer.parseInt(s.substring(1));
        String nextlevelIter = s.substring(0,1) + (counterNum+1);
        incrAssignments += "  sl_" + s + "incr <= '1' when sl_" + nextlevelIter + "_rg=sl_high_" + nextlevelIter + " ";
        if (counterNum < indexList.getIterationVector().size()-2) {
          incrAssignments += "and sl_" + nextlevelIter + "incr='1'";
        }
        incrAssignments += "else '0';\n";
        loopIterAssignments += "  sl_loop_" + s + " <= sl_" + s + " when RST='0' and sl_" + s + "incr='1' else sl_" + s + "_rg;\n";
      }
      loopIterRgAssignments += "  sl_loop_" + s + "_rg <= sl_" + s + "_rg;\n";
      loopNum--;
    }
    outPS.println("");

		outPS.println("  -- Individual counter increment signals");
    outPS.println(incrAssignments);
    outPS.println("  -- Iterators used in bound expressions");	
		outPS.println(loopIterAssignments);
    outPS.println("  -- Registered iterators");
		outPS.println(loopIterRgAssignments);

    j = indexList.getIterationVector().iterator();
    while (j.hasNext() && exprIter.hasNext()) {
      String s = (String) j.next();
      Expression lbExp = (Expression) exprIter.next();
      Expression ubExp = (Expression) exprIter.next();

      String lowerBound   = ExpVisit.visit(lbExp, indexList, 0);
      //String lowerBoundRg = ExpVisit.visit(lbExp, indexList, 1);
      String upperBound   = ExpVisit.visit(ubExp, indexList, 1);

      //outPS.println("  sl_low_" + s + "     <= " + lowerBound + " when RST='0' else " + lowerBoundRg +";");
			outPS.println("  sl_low_" + s + "  <= " + lowerBound + ";");
      outPS.println("  sl_high_" + s + " <= " + upperBound + ";");
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
   * Computes bounding boxes for domain.
   * @Return An array which is structured as follows:
   *         [dim][0]  lowerbound
   *         [dim][1]  upperbound
   *         [dim][2]  max. required size for dimension
   */
  private int[][] _computeBoundingBoxes(Vector<Polytope> bound) {
    IndexVector indexList = bound.firstElement().getIndexVector();
    Vector<Expression> boundExp = Polytope2IndexBoundVector.getExpression(bound.get(0));
    //System.out.println(bound.get(0).toString() + "\n--");
    //System.out.println(boundExp);
    int nDims = indexList.getIterationVector().size();
    int ret[][] = new int[nDims][3];
    ExpressionAnalyzer ea = new ExpressionAnalyzer(indexList);

    for (int i = 0; i < nDims; i++) {
      String indexName = indexList.getIterationVector().get(i);
			Expression lbExp = boundExp.get(2*i);
			Expression ubExp = boundExp.get(2*i + 1);
      //System.out.println(lbExp.toString() + "    " + ubExp.toString());

      int bnds[] = ea.findBounds(indexName, lbExp, ubExp);
      ret[i][0] = bnds[0];
      ret[i][1] = bnds[1];
      ret[i][2] = ret[i][1] - ret[i][0] + 1;
    }

    return ret;
  }


  /**
   * Writes the mapping for write addrgen (mode=0) or read addrgen (mode=1).
   */
  private void _writeIteratorMapping(PrintStream outPS, IndexVector indexList, boolean mode) {
    int nDims = indexList.getIterationVector().size();
    JMatrix m = _adgEdge.getMapping();

    outPS.println("  -- Mapping");
    for (int i = 0; i < nDims; i++) {
      String indexName = indexList.getIterationVector().get(i);
      String rhs = "";
      if (mode) {
        for (int j = 0; j < m.nbColumns()-1; j++) {
          long coeff = m.getElement(i,j);
          rhs += (j > 0 && coeff >= 0) ? " + " : " ";
          rhs += coeff + "*sl_loop_" + indexList.getIterationVector().get(j) + "_rg";
        }
        long offset = m.getElement(i,m.nbColumns()-1);
        rhs += (offset>=0) ? " + "+offset : " "+offset;
      }
      else {
        rhs = "sl_loop_" + indexName + "_rg";
      }
      outPS.println("  sl_m_" + indexName + " <= " + rhs + ";");
    }
    outPS.println("");
  }


  /**
   * Writes addressing function.
   * @param isRead Determines whether the edge mapping has to be applied or not.
   */
  private void _writeAddressFunction(PrintStream outPS, LBS domain, boolean isRead) {
    IndexVector indexList = domain.getLinearBound().firstElement().getIndexVector();
    Vector<Expression> boundExp = Polytope2IndexBoundVector.getExpression(domain.getLinearBound().get(0));
    String addrFunc = "";
    int cumulBoundingBox = 1;
    int nDims = indexList.getIterationVector().size();
    int boxes[][] = _computeBoundingBoxes(domain.getLinearBound());

    _writeIteratorMapping(outPS, indexList, isRead);

    outPS.println("  -- Address function");
    for (int iterNum = 0; iterNum < nDims; iterNum++) {
      String indexName = indexList.getIterationVector().get(iterNum);
      outPS.println("  -- " + indexName + "  " + boxes[iterNum][0] + "--" + boxes[iterNum][1] + "  " + boxes[iterNum][2]);
      if (iterNum == 0) {
        addrFunc += "sl_m_" + indexName;
      }
      else {
        addrFunc = "(" + addrFunc + "*" + boxes[iterNum][2] + "+sl_m_" + indexName + ")";
      }
    }
    outPS.println("  s_address <= " + addrFunc + ";");
  }


  /**
   * Writes common parts of address generator files.
   */
  private void _writeAddrGenCommon(PrintStream outPS, LBS domain, String entityName) {
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
    outPS.println("entity " + entityName + " is");
    outPS.println("  generic (");
    outPS.println("    C_AWIDTH : integer := 8");
    outPS.println("  );");
    outPS.println("  port (");
    outPS.println("    clk       : in  std_logic;");
    outPS.println("    rst       : in  std_logic;");
    outPS.println("    nextAddr  : in  std_logic;");
    outPS.println("    address   : out std_logic_vector(C_AWIDTH-1 downto 0)");
    outPS.println("  );");
    outPS.println("end entity " + entityName + ";");
    outPS.println("");
    outPS.println("");
    outPS.println("architecture behaviour of " + entityName + " is");
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
    _writeDomainData(outPS, domain);
    outPS.println("  signal s_address : integer;");
    outPS.println("");
    outPS.println("begin");
    outPS.println("");
    outPS.println("  ag_cntr : GEN_COUNTER");
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
    _writeLoopControl(outPS, domain);
    outPS.println("");
  }


  /**
   * Write the write address generator file.
   */
  private void _writeWriteAddrGen() throws FileNotFoundException {
    PrintStream outPS = _openFile(_moduleDir + "/" + _coreName + "_WAG.vhd");
    outPS.println("-- Write address generator for channel " + _fifo.getName());
    _writeAddrGenCommon(outPS, _adgEdge.getFromPort().getDomain(), _coreName + "_WAG");
    _writeAddressFunction(outPS, _adgEdge.getFromPort().getDomain(), false);
    outPS.println("");
    outPS.println("  address <= std_logic_vector(to_unsigned(s_address, C_AWIDTH));");
    outPS.println("");
    outPS.println("end architecture behaviour;");
    outPS.println("");
  }


  /**
   * Write the read address generator file.
   */
  private void _writeReadAddrGen() throws FileNotFoundException {
    PrintStream outPS = _openFile(_moduleDir + "/" + _coreName + "_RAG.vhd");
    outPS.println("-- Read address generator for channel " + _fifo.getName());
    _writeAddrGenCommon(outPS, _adgEdge.getToPort().getDomain(), _coreName + "_RAG");
    _writeAddressFunction(outPS, _adgEdge.getFromPort().getDomain(), true);
    outPS.println("");
    outPS.println("  address <= std_logic_vector(to_unsigned(s_address, C_AWIDTH));");
    outPS.println("");
    outPS.println("end architecture behaviour;");
    outPS.println("");
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
    topPS.println("library fsl_v20_v2_11_a;");
    topPS.println("use fsl_v20_v2_11_a.sync_bram;");
    topPS.println("");
    topPS.println("entity " + _coreName + " is");
    topPS.println("  generic (");
    topPS.println("    C_EXT_RESET_HIGH    : integer := 1;");
    topPS.println("    C_DWIDTH            : integer := 32");
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
    topPS.println("  component " + _coreName + "_RAG is");
    topPS.println("    generic (");
    topPS.println("      C_AWIDTH : integer := 8");
    topPS.println("    );");
    topPS.println("    port (");
    topPS.println("      clk       : in  std_logic;");
    topPS.println("      rst       : in  std_logic;");
    topPS.println("      nextAddr  : in  std_logic;");
    topPS.println("      address   : out std_logic_vector(C_AWIDTH-1 downto 0)");
    topPS.println("    );");
    topPS.println("  end component;");
    topPS.println("");
    topPS.println("  component " + _coreName + "_WAG is");
    topPS.println("    generic (");
    topPS.println("      C_AWIDTH : integer := 8");
    topPS.println("    );");
    topPS.println("    port (");
    topPS.println("      clk       : in  std_logic;");
    topPS.println("      rst       : in  std_logic;");
    topPS.println("      nextAddr  : in  std_logic;");
    topPS.println("      address   : out std_logic_vector(C_AWIDTH-1 downto 0)");
    topPS.println("    );");
    topPS.println("  end component;");
    topPS.println("");
    topPS.println("  component Sync_BRAM is");
    topPS.println("    generic (");
    topPS.println("      C_DWIDTH : integer := 32;");
    topPS.println("      C_AWIDTH : integer := 16");
    topPS.println("    );");
    topPS.println("    port (");
    topPS.println("      clk     : in  std_logic;");
    topPS.println("      -- Write port");
    topPS.println("      we      : in  std_logic;");
    topPS.println("      a       : in  std_logic_vector(C_AWIDTH-1 downto 0);");
    topPS.println("      di      : in  std_logic_vector(C_DWIDTH-1 downto 0);");
    topPS.println("      spo     : out std_logic_vector(C_DWIDTH-1 downto 0);");
    topPS.println("      -- Read port");
    topPS.println("      dpra_en : in  std_logic;");
    topPS.println("      dpra    : in  std_logic_vector(C_AWIDTH-1 downto 0);");
    topPS.println("      dpo     : out std_logic_vector(C_DWIDTH-1 downto 0)");
    topPS.println("    );");
    topPS.println("  end component;");
    topPS.println("");
    int opdSize = _computeMemsize(_adgEdge.getFromPort().getDomain());
    int ipdSize = _computeMemsize(_adgEdge.getToPort().getDomain());
    assert(opdSize >= ipdSize);   // we map all addresses back to OPD; probably we don't have to care about the IPD at all
    topPS.println("  constant C_AWIDTH : natural := " + (int)Math.ceil(Math.log(opdSize+1)/Math.log(2)) + ";");
    topPS.println("");
    topPS.println("  signal read_addr       : std_logic_vector(C_AWIDTH-1 downto 0);");
    topPS.println("  signal read_addr_1     : std_logic_vector(C_AWIDTH-1 downto 0);");
    topPS.println("  signal read_addr_bram  : std_logic_vector(C_AWIDTH-1 downto 0);");
    topPS.println("  signal write_addr      : std_logic_vector(C_AWIDTH-1 downto 0);");
    topPS.println("  signal dout            : std_logic_vector(C_DWIDTH downto 0);");
    topPS.println("  signal din             : std_logic_vector(C_DWIDTH downto 0);");
    topPS.println("  signal read_en         : std_logic;");
    topPS.println("  signal write_en        : std_logic;");
    topPS.println("  signal exists          : std_logic;");
    topPS.println("  signal s_initstrobe    : std_logic;");
    topPS.println("  signal s_rst, s_rst_1  : std_logic;");
    topPS.println("  signal s_clk           : std_logic;");
    topPS.println("");
    topPS.println("begin");
    topPS.println("");
    topPS.println("  s_clk <= Ext_Clk;");
    topPS.println("  s_rst <= Ext_Rst when C_EXT_RESET_HIGH=1 else not Ext_Rst;");
    topPS.println("");

    topPS.println("  -- Read address generator");
    topPS.println("  raddrgen_1 : " + _coreName + "_RAG");
    topPS.println("    generic map (");
    topPS.println("      C_AWIDTH => C_AWIDTH");
    topPS.println("    )");
    topPS.println("    port map (");
    topPS.println("      clk => s_clk,");
    topPS.println("      rst => s_rst,");
    topPS.println("      nextAddr => read_en,");
    topPS.println("      address => read_addr");
    topPS.println("    );");
    topPS.println("");
    topPS.println("  -- Write address generator");
    topPS.println("  waddrgen_1 : " + _coreName + "_WAG");
    topPS.println("    generic map (");
    topPS.println("      C_AWIDTH => C_AWIDTH");
    topPS.println("    )");
    topPS.println("    port map (");
    topPS.println("      clk => s_clk,");
    topPS.println("      rst => s_rst,");
    topPS.println("      nextAddr => write_en,");
    topPS.println("      address => write_addr");
    topPS.println("    );");
    topPS.println("");

    topPS.println("  -- The actual memory");
    topPS.println("  Sync_BRAM_I1 : Sync_BRAM");
    topPS.println("    generic map (");
    topPS.println("      C_DWIDTH => C_DWIDTH+1,");
    topPS.println("      C_AWIDTH => C_AWIDTH");
    topPS.println("    )");
    topPS.println("    port map (");
    topPS.println("      clk => s_clk,");
    topPS.println("      -- Write port");
    topPS.println("      we  => write_en,");
    topPS.println("      a   => write_addr,");
    topPS.println("      di  => din,");
    topPS.println("      spo => open,");
    topPS.println("      -- Read port");
    topPS.println("      dpra_en => '1',");
    topPS.println("      dpra    => read_addr_bram,");
    topPS.println("      dpo     => dout");
    topPS.println("    );");
    topPS.println("");

    topPS.println("  read_addr_bram <= read_addr when FSL_S_Read = '1' else read_addr_1;");
    topPS.println("");
    topPS.println("  write_en <= FSL_M_Write;");
    topPS.println("  read_en <= FSL_S_Read or s_initstrobe;");
    topPS.println("");
    topPS.println("  FSL_M_Full <= s_rst_1;");
    topPS.println("  din <= '1' & FSL_M_Data;");
    topPS.println("");
    topPS.println("  exists <= '1' when dout(C_DWIDTH) = '1' else '0';");
    topPS.println("  FSL_S_Exists <= exists;");
    topPS.println("  FSL_S_Data <= dout(C_DWIDTH-1 downto 0);");
    topPS.println("");
    topPS.println("  process (s_clk) begin");
    topPS.println("    if (rising_edge(s_clk)) then");
    topPS.println("      s_rst_1 <= s_rst;");
    topPS.println("    end if;");
    topPS.println("  end process;");
    topPS.println("  s_initstrobe <= not s_rst and s_rst_1;");
    topPS.println("");
    topPS.println("  process (s_rst, s_clk) begin");
    topPS.println("    if (s_rst = '1') then");
    topPS.println("      read_addr_1 <= (others => '0');");
    topPS.println("    elsif (rising_edge(s_clk)) then");
    topPS.println("      if (read_en = '1') then");
    topPS.println("        read_addr_1 <= read_addr;");
    topPS.println("      end if;");
    topPS.println("    end if;");
    topPS.println("  end process;");

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


  
  
  // /////////////////////////////////////////////////////////////////
  // // private variables ///
  private String _coreName;

  private String _moduleName;

  private String _moduleDir;

  private UserInterface _ui = null;

  private String _codeDir;

  private Mapping _mapping;

  private ADGraph _adg;
  
  private ADGEdge _adgEdge; //corresponding ADG edge

  private ADGNode _srcNode;   // Writer ADG Node
  private ADGNode _dstNode;   // Read ADG Node

  private Fifo _fifo;
  
  ////////////////////////////////////
  // Experimental & hardcoded options:
  ////////////////////////////////////

}
