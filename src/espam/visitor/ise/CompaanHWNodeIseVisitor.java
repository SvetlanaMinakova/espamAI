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

import espam.main.UserInterface;

import espam.operations.transformations.*;
import espam.operations.codegeneration.CodeGenerationException;
import espam.operations.codegeneration.Polytope2IfStatements;

import espam.visitor.PlatformVisitor;
import espam.visitor.expression.*;
import espam.visitor.xps.Copier;

import espam.utils.symbolic.expression.*;



//////////////////////////////////////////////////////////////////////////
//// Platform Hardware Node Visitor for ISE

/**
 * This class implements a visitor that is used to generate pcores for ISE.
 * It is almost identical to its XPS equivalent, but there are some crucial
 * differences: all instantiated VHDL module names are unique, e.g., each
 * eval_logic_rd unit has a suffix identifying the node it belongs to.
 *
 * @author Ying Tao, Todor Stefanov, Hristo Nikolov, Sven van Haastregt
 * @version $Id: CompaanHWNodeIseVisitor.java,v 1.4 2011/06/23 13:17:19 svhaastr Exp $
 */

public class CompaanHWNodeIseVisitor extends PlatformVisitor {

	// /////////////////////////////////////////////////////////////////
	// // public methods ///

	/**
	 * Constructor for the CompaanHWNodeIseVisitor object
	 * @param mapping
	 *            The mapping of the corresponding platform which contains crucial mapping information.
	 */
	public CompaanHWNodeIseVisitor( Mapping mapping ) throws FileNotFoundException, EspamException {

		_ui = UserInterface.getInstance();
		_mapping = mapping;
		_adg = _mapping.getADG();

		if (_ui.getOutputFileName() == "") {
			_codeDir = _ui.getBasePath() + "/" + _ui.getFileName();
		} else {
			_codeDir = _ui.getBasePath() + "/" + _ui.getOutputFileName();
		}

	}

	/**
	 * Print all the files for a generated pcore in the correct format
	 *
	 * @param x
	 *            The platform that needs to be rendered.
	 */
	public void visitComponent(Platform x) {
		try {
			Iterator i;
			i = x.getResourceList().iterator();
			while (i.hasNext()) {

				Resource resource = (Resource) i.next();
				if (resource instanceof CompaanHWNode) {

					CompaanHWNode node = (CompaanHWNode) resource;
					_HWNode = node;
					_coreName = node.getName();

					Iterator j = _mapping.getProcessorList().iterator();
					while(j.hasNext()){
						MProcessor mp = (MProcessor) j.next();
						if(mp.getResource() instanceof CompaanHWNode){
							if(mp.getResource().getName().equals(_coreName)== true){
								MProcess p = (MProcess)mp.getProcessList().get(0);
								_adgNode = p.getNode();
								_inArgList = _adgNode.getFunction().getInArgumentList();
								//_adgInPorts = _adgNode.getInPorts();
								_outArgList = _adgNode.getFunction().getOutArgumentList();
								//_adgOutPorts = _adgNode.getOutPorts();
								_indexList = _adgNode.getDomain().getLinearBound().firstElement().getIndexVector();

                //////
                // TODO: also move this to a separate function
                // Take and sort input ports
                _adgInPorts = new Vector();
                Iterator ai = _inArgList.iterator();
                while (ai.hasNext()) {
                  ADGVariable var = (ADGVariable) ai.next();
                  Iterator pi = _adgNode.getInPorts().iterator();
                  while (pi.hasNext()) {
                    ADGInPort port = (ADGInPort) pi.next();
                    if (var.getName().equals(port.getBindVariables().get(0).getName())) {
                      _adgInPorts.add(port);
                    }
                  }
                }
                _adgOutPorts = getOutPortList(_adgNode);

                //////

								//get the hashmap of all parameters and their upperbounds
								Iterator paramIter;
								paramIter = _indexList.getParameterVector().iterator();
								while(paramIter.hasNext()){
									ADGParameter param = (ADGParameter) paramIter.next();
									Vector<Integer> bounds = new Vector<Integer>();
									bounds.addElement(new Integer(param.getLowerBound()));
									bounds.addElement(new Integer(param.getUpperBound()));
									_parameters.put(param.getName(), bounds);
								}
							}
						}
					}


          if (_adgNode != null && !_isSource(_adgNode) && !_isSink(_adgNode)) {
            // create the subdirectories
            _moduleName = _coreName + "_v1_00_a";
            _moduleDir = "pcores/" + _moduleName;
            _hdlFile = _coreName + ".vhd";

            _currentCodeDir = _codeDir + "/" + _moduleDir;
            File dir = new File(_currentCodeDir);
            dir.mkdirs();
            dir = new File(_currentCodeDir + "/" + _hdlDir);
            dir.mkdirs();

            if (_optimizeCounters == true)
              _analyzeCounters();

            _writeHdlFuncFile();
            _writeHdlExUnitFile();
            _writeHdlEvalLogRdFile();
            _writeHdlEvalLogWrFile();
            _writeHdlHWNodeFile();
            // HWNodePack,RdMux,WrMux,CtrlUnit,GenCounter,Param are copied from the vhdl library
          }
				}
			}
		} catch (Exception e) {
			System.out.println(" In Compaan Hardware Node Visitor: exception " + "occured: "
					+ e.getMessage());
			e.printStackTrace();
		}

	}


  /**
   * Returns an ordered list of all output ports of the ADGNode.
   */
  public Vector<ADGOutPort> getOutPortList(ADGNode node) {
    // Take the relevant output ports
    Vector<ADGOutPort> ret = new Vector<ADGOutPort>();
    Iterator ai = node.getFunction().getOutArgumentList().iterator();
    while (ai.hasNext()) {
      ADGVariable var = (ADGVariable) ai.next();
      Iterator pi = node.getOutPorts().iterator();
      while (pi.hasNext()) {
        ADGOutPort port = (ADGOutPort) pi.next();
        if (var.getName().equals(port.getBindVariables().get(0).getName())) {
          ret.add(port);
        }
      }
    }
    
    _numReusePorts = 0;
    // Add the reuse output ports to the end of the outport list
    Iterator pi = node.getOutPorts().iterator();
    while (pi.hasNext()) {
      ADGOutPort port = (ADGOutPort) pi.next();
      assert(port.getBindVariables().size() == 1);
      if (_isReusePort(port)) {
        // It's a reuse port
        ret.add(port);
        _numReusePorts++;
      }
    }

    return ret;
  }

	// /////////////////////////////////////////////////////////////////
	// // private methods ///

	/**
	 * Analyzes how many counters are really needed for this node.
	 */
	private void _analyzeCounters() {
		//get the upper/lower bound expressions
		Vector <Expression> boundExp = Polytope2IndexBoundVector.getExpression(_adgNode.getDomain().getLinearBound().firstElement());
	
		Iterator exprIter;
		VhdlExpressionVisitor ExpVisit = new VhdlExpressionVisitor();
		exprIter = boundExp.iterator();
		Iterator j = _indexList.getIterationVector().iterator();
		while(j.hasNext()&& exprIter.hasNext()){
			String s = (String) j.next();
			
			Expression lbExp = (Expression) exprIter.next();
			Expression ubExp = (Expression) exprIter.next();

      System.out.print("Analyzing " + s + ":  ");
      if (lbExp.isNumber() && ubExp.isNumber()) {
        int lb = lbExp.evaluate(null, null);
        int ub = ubExp.evaluate(null, null);
        System.out.print(lb + " .. " + ub + "   " + (ub-lb+1));
        if (ub-lb+1 == 0) {
          System.out.print("   0 iterations!");
        }
        else if (ub-lb+1 == 1) {
          System.out.print("   1 iteration!");
          _skipList.add(s);
          //j.remove();
        }
        else if (((ub-lb+1)&(ub-lb))==0) {
          System.out.print("   power of two!");
        }
      }
      else {
        System.out.print("no constant bounds");
      }
      System.out.println("");
    }


    /////////////////////////////////////////////////////////////////////////
/*    System.out.println("After modifications:");
		exprIter = boundExp.iterator();
		j = _indexList.getIterationVector().iterator();
		while(j.hasNext()&& exprIter.hasNext()){
			String s = (String) j.next();
			
			Expression lbExp = (Expression) exprIter.next();
			Expression ubExp = (Expression) exprIter.next();
      System.out.print("Analyzing " + s + ":  ");
      if (lbExp.isNumber() && ubExp.isNumber()) {
        int lb = lbExp.evaluate(null, null);
        int ub = ubExp.evaluate(null, null);
        System.out.print(lb + " .. " + ub + "   " + (ub-lb+1));
        if (ub-lb+1 == 0) {
          System.out.print("   0 iterations!");
        }
        else if (ub-lb+1 == 1) {
          System.out.print("   1 iteration!");
        }
        else if (((ub-lb+1)&(ub-lb))==0) {
          System.out.print("   power of two!");
        }
      }
      else {
        System.out.print("no constant bounds");
      }
      System.out.println("");

		}*/
  }



	/**
	 * write the vhdl file for the function.
	 * (need to be modified, getting the function interface from the xml specification and ESPAM library)
	 * (currently only an empty function with the same interface as the arguments of the ADG function, need to be manually replaced)
	 */
	private void _writeHdlFuncFile()throws FileNotFoundException {
		PrintStream hdlPS = _openFile(_hdlDir + "/" + _adgNode.getFunction().getName() + ".vhd");

		hdlPS.println("-- File automatically generated by ESPAM");
		hdlPS.println("");

		hdlPS.println("library ieee;");
		hdlPS.println("use ieee.std_logic_1164.all;");
		hdlPS.println("use ieee.numeric_std.all;");
		hdlPS.println("");
		hdlPS.println("");
		hdlPS.println("entity " + _adgNode.getFunction().getName() + " is");
		hdlPS.println("   port (");
		hdlPS.println("      RST   : in  std_logic;");
		hdlPS.println("      CLK   : in  std_logic;");
		hdlPS.println("");


		Iterator i = _inArgList.iterator();
		ADGVariable in_arg, out_arg;

		while(i.hasNext()){
			in_arg = (ADGVariable) i.next();
			//currently default vector length 32.
			hdlPS.println("      " + in_arg.getName() + "  : in  std_logic_vector(31 downto 0);");
		}
		hdlPS.println("");

		i = _outArgList.iterator();
		while(i.hasNext()){
			out_arg = (ADGVariable) i.next();
			hdlPS.println("      " + out_arg.getName() + " : out std_logic_vector(31 downto 0);");
		}
		
		hdlPS.println("");
		hdlPS.println("      EN    : in  std_logic");
		hdlPS.println("   );");
		hdlPS.println("end " + _adgNode.getFunction().getName() + ";");
		hdlPS.println("");


		hdlPS.println("architecture RTL of " + _adgNode.getFunction().getName() + " is");
		hdlPS.println("begin");
		hdlPS.println("");
		hdlPS.println("end RTL;");
	}
	
	
	
	/**
	 * write the execution_unit .vhd file which is a wrapper for the function VHDL file.
	 * (need to be modified, getting the function interface from the xml specification) 
	 * (currently only a wrapper with the same interface as the arguments of the ADG function, need to be manually modified)
	 */
	private void _writeHdlExUnitFile() throws FileNotFoundException {

		PrintStream hdlPS = _openFile(_hdlDir + "/" + "execution_unit.vhd");

		hdlPS.println("-- File automatically generated by ESPAM");
		hdlPS.println("");

		hdlPS.println("library ieee;");
		hdlPS.println("use ieee.std_logic_1164.all;");
		hdlPS.println("use ieee.std_logic_unsigned.all;");
		hdlPS.println("");
		
		hdlPS.println("entity EXECUTION_UNIT_" + _coreName + " is");
		hdlPS.println("   generic (");
		hdlPS.println("      N_INPORTS  : natural := 1;");
		hdlPS.println("      N_OUTPORTS : natural := 1;");
		hdlPS.println("      IP_RESET   : natural := 1;");
		hdlPS.println("      QUANT      : natural := 32");
		hdlPS.println("   );");

		hdlPS.println("   port (");
		hdlPS.println("      RST        : in  std_logic;");
		hdlPS.println("      CLK        : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("      IN_PORTS   : in  std_logic_vector(N_INPORTS*QUANT-1 downto 0);");
		hdlPS.println("      OUT_PORTS  : out std_logic_vector(N_OUTPORTS*QUANT-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("      ENABLE     : in  std_logic;");
		hdlPS.println("      IP_WRITE   : out std_logic;");
		hdlPS.println("      IP_READ    : out std_logic");
		hdlPS.println("   );");
		hdlPS.println("end EXECUTION_UNIT_" + _coreName + ";");
		
		
		hdlPS.println("");
		hdlPS.println("architecture RTL of EXECUTION_UNIT_" + _coreName + " is");
		hdlPS.println("");
		hdlPS.println("   component " + _adgNode.getFunction().getName() + " is");
		hdlPS.println("      port (");
		hdlPS.println("         RST   : in std_logic;");
		hdlPS.println("         CLK   : in std_logic;");
		hdlPS.println("");
		
		Iterator i = _inArgList.iterator();
		ADGVariable in_arg, out_arg;
		
		while(i.hasNext()){
			in_arg = (ADGVariable) i.next();
			//currently default signal width 32
			hdlPS.println("         " + in_arg.getName() + "  : in  std_logic_vector(31 downto 0);");
		}
		hdlPS.println("");
		
		i = _outArgList.iterator();
		while(i.hasNext()){
			out_arg = (ADGVariable) i.next();
			hdlPS.println("         " + out_arg.getName() + " : out std_logic_vector(31 downto 0);");
		}
		
		hdlPS.println("");
		hdlPS.println("         EN    : in  std_logic");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");		
		hdlPS.println("   signal sl_RST : std_logic;");
		hdlPS.println("");
		hdlPS.println("begin");
		hdlPS.println("");
		hdlPS.println("   sl_RST <= RST when IP_RESET=1 else not RST;");
		hdlPS.println("");
		hdlPS.println("   IP_READ  <= '1';");
		hdlPS.println("   IP_WRITE <= '1';");
		hdlPS.println("");
		hdlPS.println("   FUNC : " + _adgNode.getFunction().getName());
		hdlPS.println("   port map (");
		
		i = _inArgList.iterator();
		int index = 0;
		while(i.hasNext()){
			in_arg = (ADGVariable) i.next();
			if (index == 0){
				hdlPS.println("      " + in_arg.getName() + " => IN_PORTS(QUANT-1 downto 0),");
			}
			else {
				hdlPS.println("      " + in_arg.getName() + " => IN_PORTS(" + (index + 1) + "*QUANT-1 downto " + index + "*QUANT),");
			}
			
			index++;
		}
		hdlPS.println("");
		
		i = _outArgList.iterator();
		index = 0;
		while(i.hasNext()){
			out_arg = (ADGVariable) i.next();
			if (index == 0){
				hdlPS.println("      " + out_arg.getName() + " => OUT_PORTS(QUANT-1 downto 0),");
			}
			else {
				hdlPS.println("      " + out_arg.getName() + " => OUT_PORTS(" + (index + 1) + "*QUANT-1 downto " + index + "*QUANT),");
			}
			
			index++;
		}
		
		hdlPS.println("");			
		hdlPS.println("      CLK => CLK,");
		hdlPS.println("      RST => sl_RST,");
		hdlPS.println("      EN  => ENABLE");
		hdlPS.println("   );");
		hdlPS.println("");
		hdlPS.println("end RTL;");
	}



	/**
	 * write the eval_logic_rd and eval_logic_wr body.
	 * @throws CodeGenerationException
	 */
	private void _writeHdlEvalLogic(PrintStream hdlPS, Vector ports, Vector args) throws FileNotFoundException, CodeGenerationException {
		Vector paramNames = _indexList.getParameterVectorNames();
		
		int i;
		// parameter signals
		String param = "";
		if(paramNames.size() != 0){
			param = "signal sl_" + paramNames.get(0).toString();
			for(i = 1; i < paramNames.size(); i++){
				param = param + ", sl_" + paramNames.get(i).toString();
			}
		}

		Iterator j;
		j = _indexList.getIterationVector().iterator();
		// signals corresponding to index's upper/lower bounds.
		while(j.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;
			hdlPS.println("   signal sl_low_" + s +", sl_high_" + s +" : integer;");
		}
		
		j = _indexList.getIterationVector().iterator();
		// first one is the most outer loop
		while(j.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;
			hdlPS.println("   signal sl_loop_" + s + ", sl_loop_" + s + "_rg : integer;");
		}
		
		j = _indexList.getIterationVector().iterator();
		// first one is the most outer loop
		while(j.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;
			hdlPS.println("   signal sl_" + s + ", sl_" + s + "_rg : integer;");
		}
		
    // Print increment strobe declarations for first N-1 counters
		j = _indexList.getIterationVector().iterator();
		while(j.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;
      if (j.hasNext()) {
  			hdlPS.println("   signal sl_" + s + "incr : std_logic;");
      }
		}

		if(paramNames.size() != 0){
			hdlPS.println("   " + param + " : integer;");
		}
		
		int ctrlNum = 0;
		Iterator ADGInIter;
		ADGInIter = ports.iterator();
		while (ADGInIter.hasNext()){
			ADGPort adg_in_port = (ADGPort) ADGInIter.next();
			
			Iterator linearBound = adg_in_port.getDomain().getLinearBound().iterator();
			while(linearBound.hasNext()){
				Polytope polytope = (Polytope) linearBound.next();
				
				try{
					Polytope ndPolytope = _adgNode.getDomain().getLinearBound().get(0);
					Polytope sPolytope = Polytope2IfStatements.simplifyPDinND( polytope, ndPolytope);
					
					if (sPolytope.getConstraints()==null){						
					}
					else{
						Vector <Expression> vectorExpr = Polytope2Expression.getExpression(sPolytope);
						ctrlNum = ctrlNum + vectorExpr.size();
					}
					
				}catch( Exception e ) {
					e.printStackTrace();
					throw new CodeGenerationException(
						"simplifying domain of ADG port "
							+ adg_in_port.getName()
							+ ": "
							+ e.getMessage());
				}
			}
		}
		
		String ctrlSig = "   signal e0";
		for(i = 1; i < ctrlNum; i++){
			ctrlSig = ctrlSig + " ,e" + i;
		}
		
		hdlPS.println(ctrlSig + " : boolean;");	
		hdlPS.println("");

    if (_regEval) {
      hdlPS.println("   signal sl_done_0 : std_logic;");
      hdlPS.println("");
    }

		hdlPS.println("begin");
		hdlPS.println("");	
		
		// first parameter's value is got from the left most signals, which is also input first.(shift left register)
		int paramNum = paramNames.size();
		Iterator k;
		k = paramNames.iterator();
		while(k.hasNext()){
			String s = (String) k.next();
      if (_skipList.contains(s))
        continue;
			hdlPS.println("   sl_" + s + " <= CONV_INTEGER( PARAMETERS(" + paramNum + "*PAR_WIDTH-1 downto " + (paramNum - 1) + "*PAR_WIDTH) );");
			paramNum--;
		}		
		hdlPS.println("");
		
    String incrAssignments = "";
    String loopIterAssignments = "";
    String loopIterRgAssignments = "";
		// !!!!!!CNTR_WIDTH(0) corresponds to inner loop, to be consistant with gen_counter
		int loopNum = _indexList.getIterationVector().size() - _skipList.size();
		j = _indexList.getIterationVector().iterator();
		while(j.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;
			hdlPS.println("   sl_" + s + "    <= CONV_INTEGER( ITERATORS(CNTR_WIDTH(" + (loopNum - 1)+ ")+" + (loopNum - 1) + "*QUANT-1 downto " + (loopNum - 1) + "*QUANT) );");
			hdlPS.println("   sl_" + s + "_rg <= CONV_INTEGER( REG_CNTRS(CNTR_WIDTH(" + (loopNum - 1) + ")+" + (loopNum - 1) + "*QUANT-1 downto " + (loopNum - 1) + "*QUANT) );");
      if (j.hasNext()) {
        int counterNum = Integer.parseInt(s.substring(1));
        String nextlevelIter = s.substring(0,1) + (counterNum+1);
        incrAssignments += "   sl_" + s + "incr <= '1' when sl_" + nextlevelIter + "_rg=sl_high_" + nextlevelIter + " ";
        if (counterNum < _indexList.getIterationVector().size()-2) {
          incrAssignments += "and sl_" + nextlevelIter + "incr='1'";
        }
        incrAssignments += "else '0';\n";
        loopIterAssignments += "   sl_loop_" + s + " <= sl_" + s + " when RST='0' and sl_" + s + "incr='1' else sl_" + s + "_rg;\n";
      }
      loopIterRgAssignments += "   sl_loop_" + s + "_rg <= sl_" + s + "_rg;\n";
			loopNum--;
		}
		
		hdlPS.println("");
		hdlPS.println("   -- Individual counter increment signals");
    hdlPS.println(incrAssignments);
    hdlPS.println("   -- Iterators used in bound expressions");	
		hdlPS.println(loopIterAssignments);
    hdlPS.println("   -- Registered iterators");
		hdlPS.println(loopIterRgAssignments);
		
		//get the upper/lower bound expressions
		Vector <Expression> boundExp = Polytope2IndexBoundVector.getExpression(_adgNode.getDomain().getLinearBound().firstElement());
	
		Iterator exprIter;
		VhdlExpressionVisitor ExpVisit = new VhdlExpressionVisitor();
		exprIter = boundExp.iterator();
		j = _indexList.getIterationVector().iterator();
		while(j.hasNext()&& exprIter.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;
			
			Expression lbExp = (Expression) exprIter.next();
			Expression ubExp = (Expression) exprIter.next();

			String lowerBound   = ExpVisit.visit(lbExp, _indexList, 0);
			String lowerBoundRg = ExpVisit.visit(lbExp, _indexList, 1);
			String upperBound   = ExpVisit.visit(ubExp, _indexList, 1);

			//hdlPS.println("   sl_low_" + s + "  <= " + lowerBound + " when RST='0' else " + lowerBoundRg +";");
			hdlPS.println("   sl_low_" + s + " <= " + lowerBound + ";");
			hdlPS.println("   sl_high_" + s + " <= " + upperBound + ";");
		}

		hdlPS.println("");
		
		// !!!!!!most inner loop at right most position(QUANT-1 downto 0), to be consistant with gen_counter
		loopNum = _indexList.getIterationVector().size() - _skipList.size();
		exprIter = boundExp.iterator();
		j = _indexList.getIterationVector().iterator();
		while(j.hasNext()&& exprIter.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;
			
			hdlPS.println("   LOWER_BND_OUT(" + loopNum + "*QUANT-1 downto " + (loopNum -1) + "*QUANT) <= CONV_STD_LOGIC_VECTOR(sl_low_" + s + ",QUANT);");
			loopNum--;
		}

		hdlPS.println("");

		loopNum = _indexList.getIterationVector().size() - _skipList.size();
		exprIter = boundExp.iterator();
		j = _indexList.getIterationVector().iterator();
		while(j.hasNext()&& exprIter.hasNext()){
			String s = (String) j.next();
      if (_skipList.contains(s))
        continue;

			hdlPS.println("   UPPER_BND_OUT(" + loopNum + "*QUANT-1 downto " + (loopNum -1) + "*QUANT) <= CONV_STD_LOGIC_VECTOR(sl_high_" + s + ",QUANT);");
			loopNum--;
		}

		hdlPS.println("");

		// set the control signals
		if(ports.size() == 0){
      if (_regEval) {
        System.out.println("WARNING: adding registers to eval_logic w/ empty port list has not been tested!");
      }
			
		}
		else{
			// first get the ordered ADG in ports according to the bounded in arguments order
      Vector<ADGPort> orderedADGInPorts = ports;  // We sorted them in the constructor
			/*Vector<ADGPort> orderedADGInPorts = new Vector<ADGPort>();
			Iterator inArgIter = args.iterator();
			while(inArgIter.hasNext()){
				ADGVariable in_arg = (ADGVariable) inArgIter.next();
				String arg_name = in_arg.getName();

				j = ports.iterator();
				while (j.hasNext()) {
					ADGPort adg_port = (ADGPort) j.next();
					if (adg_port.getBindVariables().get(0).getName().equals(arg_name) == true){
						orderedADGInPorts.addElement(adg_port);
					}
				}
			}*/

			int index = 0;
			int exprIndex = 0;

			HashMap <Expression, String> hashExpr = new HashMap <Expression, String>();
			ADGInIter = orderedADGInPorts.iterator();
      String ctrlAssign = "";
			while (ADGInIter.hasNext()){
				ADGPort adg_in_port = (ADGPort) ADGInIter.next();
				String ctrlPerPort = "";
				
				Iterator linearBound = adg_in_port.getDomain().getLinearBound().iterator();
				while(linearBound.hasNext()){
					Polytope polytope = (Polytope) linearBound.next();
					
					try{
						Polytope ndPolytope = _adgNode.getDomain().getLinearBound().get(0);
						Polytope sPolytope = Polytope2IfStatements.simplifyPDinND( polytope, ndPolytope);
						
						if (sPolytope.getConstraints()==null){
							ctrlPerPort = ""; 
						}
						else{
							Vector <Expression> vectorExpr = Polytope2Expression.getExpression(sPolytope);
							Iterator expIter = vectorExpr.iterator();
							while(expIter.hasNext()){
								Expression e = (Expression) expIter.next();
								//System.out.println("expression for read log " + e.toString());
								
								//if not found in hashmap, add it, expression as key
								if(! hashExpr.containsKey(e)){
									String value = "e" + exprIndex ;
									hashExpr.put(e, value);
									
									String s = ExpVisit.visit(e, sPolytope.getIndexVector(), 1);
									
									if(e.getEqualityType()== Expression.GEQ){
										hdlPS.println("   " + value + " <= " + s + ">=0;");
									}
									else if(e.getEqualityType() == Expression.EQU){
										hdlPS.println("   " + value + " <= " + s + "=0;");
									}
									else{
										hdlPS.println("   " + value + " <= " + s + "<=0;");
									}


									ctrlPerPort = ctrlPerPort + "b2std(" + value + ") and ";
									
									exprIndex++;
								}						
								//if found in hashmap, use the current value of the expression
								else{
									String value = (String) hashExpr.get(e);
									
									ctrlPerPort = ctrlPerPort + "b2std(" + value + ") and ";
								}
							}
						}
							
					}catch( Exception e ) {
						e.printStackTrace();
						throw new CodeGenerationException(
							"simplifying domain of ADG port "
								+ adg_in_port.getName()
								+ ": "
								+ e.getMessage());
					}
				}
				
        String ctrlIndent = _regEval ? "      " : "";
				//hdlPS.println("   CONTROL(" + index + ") <= " + ctrlPerPort + "'1';");
				//hdlPS.println("");
        //ctrlAssign += ctrlIndent + "   CONTROL(" + index + ") <= " + ctrlPerPort + "'1';\n";
        ctrlAssign += ctrlIndent + "   CONTROL(" + index + ") <= " + ctrlPerPort + "'1';  --" + adg_in_port.getName() + "\n";
				
				index++;
			}

      // Print control assignments:
      hdlPS.println("");
      if (_regEval) {
        hdlPS.println("   process(clk)");
        hdlPS.println("   begin");
        hdlPS.println("     if (RST = '1') then");
        hdlPS.println("       sl_done_0 <= '0';");
        hdlPS.println("       CONTROL <= (others => '0');");
        hdlPS.println("     elsif (rising_edge(CLK)) then");
        hdlPS.println("       if (ENABLE = '1') then");
        hdlPS.println("");
      }
      hdlPS.println(ctrlAssign);
      if (_regEval) {
        hdlPS.println("         sl_done_0 <= CNTR_DONE;");
        hdlPS.println("        end if;");
        hdlPS.println("      end if;");
        hdlPS.println("   end process;");
        hdlPS.println("");
        hdlPS.println("   EVAL_DONE <= sl_done_0;");
      }
      hdlPS.println("");
		}
  }




	/**
	 * write the eval_logic_rd .vhd file
	 * @throws CodeGenerationException
	 */
	private void _writeHdlEvalLogRdFile()throws FileNotFoundException, CodeGenerationException {
		PrintStream hdlPS = _openFile(_hdlDir + "/" + "eval_logic_rd.vhd");

		hdlPS.println("-- File automatically generated by ESPAM");
		hdlPS.println("");

		hdlPS.println("library ieee;");
		hdlPS.println("use ieee.std_logic_1164.all;");
		hdlPS.println("use ieee.std_logic_signed.all;");
		hdlPS.println("use ieee.std_logic_arith.all;");	
		hdlPS.println("library work;");
		hdlPS.println("use work.hw_node_pack.all;");
		hdlPS.println("");

		hdlPS.println("entity EVAL_LOGIC_RD_" + _coreName + " is");
		hdlPS.println("   generic (");
		hdlPS.println("      N_IN_PORTS    : natural := 1;");
		hdlPS.println("      N_CNTRS       : natural := 1;");
		hdlPS.println("      QUANT         : natural := 32;");
		hdlPS.println("      CNTR_WIDTH    : t_counter_width := ( 0=>10, 1=>10, 2=>9, others=>10 );");
		hdlPS.println("      N_PAR         : natural;");
		hdlPS.println("      PAR_WIDTH     : natural");
		hdlPS.println("   );");

		hdlPS.println("   port (");
		hdlPS.println("      RST           : in  std_logic;");
		hdlPS.println("      CLK           : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("      PARAMETERS    : in  std_logic_vector(N_PAR*PAR_WIDTH-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("      LOWER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("      UPPER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("      ITERATORS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("      REG_CNTRS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("");	  
    if (_regEval) {
      hdlPS.println("      EVAL_DONE     : out std_logic;");
      hdlPS.println("      CNTR_DONE     : in std_logic;");
      hdlPS.println("      ENABLE        : in std_logic;");
      hdlPS.println("");	  
    }
		hdlPS.println("      CONTROL       : out std_logic_vector(N_IN_PORTS-1 downto 0)");
		hdlPS.println("   );");
		hdlPS.println("end EVAL_LOGIC_RD_" + _coreName + ";");
		hdlPS.println("");
		
		hdlPS.println("architecture RTL of EVAL_LOGIC_RD_" + _coreName + " is");	
		hdlPS.println("");
		
    _writeHdlEvalLogic(hdlPS, _adgInPorts, _inArgList);
		
		hdlPS.println("end RTL;");
	}
	
	/**
	 * write the eval_logic_wr .vhd file
	 * @throws CodeGenerationException 
	 */
	private void _writeHdlEvalLogWrFile()throws FileNotFoundException, CodeGenerationException {
		PrintStream hdlPS = _openFile(_hdlDir + "/" + "eval_logic_wr.vhd");
		
		hdlPS.println("-- File automatically generated by ESPAM");
		hdlPS.println("");

		hdlPS.println("library ieee;");
		hdlPS.println("use ieee.std_logic_1164.all;");
		hdlPS.println("use ieee.std_logic_signed.all;");	
		hdlPS.println("use ieee.std_logic_arith.all;");		
		hdlPS.println("library work;");
		hdlPS.println("use work.hw_node_pack.all;");
		hdlPS.println("");
		
		hdlPS.println("entity EVAL_LOGIC_WR_" + _coreName + " is");
		hdlPS.println("   generic (");
		hdlPS.println("      N_OUT_PORTS   : natural := 1;");
		hdlPS.println("      N_CNTRS       : natural := 1;");
		hdlPS.println("      QUANT         : natural := 32;");
		hdlPS.println("      CNTR_WIDTH    : t_counter_width := ( 0=>10, 1=>10, 2=>9, others=>10 );");
		hdlPS.println("      N_PAR         : natural;");
		hdlPS.println("      PAR_WIDTH     : natural");
		hdlPS.println("   );");

		hdlPS.println("   port (");
		hdlPS.println("      RST           : in  std_logic;");
		hdlPS.println("      CLK           : in  std_logic;");
		hdlPS.println("");  
		hdlPS.println("      PARAMETERS    : in  std_logic_vector(N_PAR*PAR_WIDTH-1 downto 0);");
		hdlPS.println("");	  
		hdlPS.println("      LOWER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("      UPPER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("      ITERATORS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("      REG_CNTRS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("");	  
    if (_regEval) {
      hdlPS.println("      EVAL_DONE     : out std_logic;");
      hdlPS.println("      CNTR_DONE     : in std_logic;");
      hdlPS.println("      ENABLE        : in std_logic;");
      hdlPS.println("");	  
    }
		hdlPS.println("      CONTROL       : out std_logic_vector(N_OUT_PORTS-1 downto 0)");
		hdlPS.println("   );");
		hdlPS.println("end EVAL_LOGIC_WR_" + _coreName + ";");
		hdlPS.println("");
		
		hdlPS.println("architecture RTL of EVAL_LOGIC_WR_" + _coreName + " is	");

    _writeHdlEvalLogic(hdlPS, _adgOutPorts, _outArgList);

		hdlPS.println("end RTL;");
	}

	
	/**
	 * write the _corename .vhd file, which is the top-level VHDL file for the HardwareNode IP core
	 */
	private void _writeHdlHWNodeFile()throws FileNotFoundException {
		PrintStream hdlPS = _openFile(_hdlDir + "/" + _hdlFile);

		hdlPS.println("-- File automatically generated by ESPAM");
		hdlPS.println("");
		
		hdlPS.println("library ieee;");
		hdlPS.println("use ieee.std_logic_1164.all;");
		hdlPS.println("library work;");
		hdlPS.println("use work.hw_node_pack.all;");
		hdlPS.println("");

		hdlPS.println("entity " + _coreName + " is");
		hdlPS.println("   generic (");
		hdlPS.println("      RESET_HIGH : natural := 1;");
		hdlPS.println("      PAR_WIDTH  : natural := 16;");
		hdlPS.println("      QUANT      : natural := 32");
		hdlPS.println("   );");

		hdlPS.println("   port (");
		hdlPS.println("      -- Dataflow input interfaces");
		
		
		//first order the ports according to the arguments' order
		
		//the in ports of the node are corresponding to in ports of the ADGNode

		Vector <ADGInPort> binding_in_ports;
		
		Iterator i;
		Iterator j;
		
		ADGVariable in_arg;
		ADGInPort adg_in_port;
		String in_arg_name;
		
		if(_adgInPorts.size() == 0){
			
		}
		else{
			i = _inArgList.iterator();
			while(i.hasNext()){
				in_arg = (ADGVariable) i.next();
				
				// get the vector of the in ports of the node relating to the specific in_argument of the function
				binding_in_ports = new Vector<ADGInPort>();
				in_arg_name = in_arg.getName();
							
				j = _adgInPorts.iterator();
				while (j.hasNext()) {
					adg_in_port = (ADGInPort) j.next();
					if (adg_in_port.getBindVariables().get(0).getName().equals(in_arg_name)== true){
						binding_in_ports.addElement(adg_in_port);
					}
				}
				
				Iterator k = binding_in_ports.iterator();
				while (k.hasNext())	{
					ADGInPort in = (ADGInPort) k.next();
					hdlPS.println("      " + in.getName() + "_Rd    : out std_logic;");
					hdlPS.println("      " + in.getName() + "_Din   : in  std_logic_vector(QUANT-1 downto 0);");
					hdlPS.println("      " + in.getName() + "_Exist : in  std_logic;");
					hdlPS.println("      " + in.getName() + "_CLK   : out std_logic;");
					hdlPS.println("      " + in.getName() + "_CTRL  : in  std_logic;");
					hdlPS.println("");
				}
			}
		}

		hdlPS.println("      -- Dataflow output interfaces");
		
		//the out ports of the node are corresponding to out ports of the ADGNode
		Vector <ADGOutPort> binding_out_ports;
		
		ADGVariable out_arg;	
		ADGOutPort adg_out_port;
		String out_arg_name;
/*	
		if(_adgOutPorts.size() == 0){
			
		}
		else{
			i = _outArgList.iterator();
			while(i.hasNext()){
				out_arg = (ADGVariable) i.next();
				
				////get the vector of the out ports of the node relating to the specific out_argument of the function
				binding_out_ports = new Vector<ADGOutPort>();
				out_arg_name = out_arg.getName();

				j = _adgOutPorts.iterator();
				while (j.hasNext()) {				
					adg_out_port = (ADGOutPort) j.next();
          System.out.println("Port: " + adg_out_port.getName() + "  " + adg_out_port.getBindVariables().get(0).getName());
					//if (adg_out_port.getBindVariables().get(0).getName().equals(out_arg_name)== true){
						binding_out_ports.addElement(adg_out_port);
					//}
				}

				Iterator k = binding_out_ports.iterator();
				while (k.hasNext())	{
					ADGOutPort out = (ADGOutPort) k.next();
					hdlPS.println("      " + out.getName() + "_Wr   : out std_logic;");
					hdlPS.println("      " + out.getName() + "_Dout : out std_logic_vector(QUANT-1 downto 0);");
					hdlPS.println("      " + out.getName() + "_Full : in  std_logic;");
					hdlPS.println("      " + out.getName() + "_CLK  : out std_logic;");
					hdlPS.println("      " + out.getName() + "_CTRL : out std_logic;");
					hdlPS.println("");
				}
			}
		}
*/
    i = _adgOutPorts.iterator();
    while (i.hasNext())	{
      ADGOutPort out = (ADGOutPort) i.next();
      hdlPS.println("      " + out.getName() + "_Wr   : out std_logic;");
      hdlPS.println("      " + out.getName() + "_Dout : out std_logic_vector(QUANT-1 downto 0);");
      hdlPS.println("      " + out.getName() + "_Full : in  std_logic;");
      hdlPS.println("      " + out.getName() + "_CLK  : out std_logic;");
      hdlPS.println("      " + out.getName() + "_CTRL : out std_logic;");
      hdlPS.println("");
    }

		hdlPS.println("      PARAM_DT : in  std_logic_vector(PAR_WIDTH-1 downto 0);");
		hdlPS.println("      PARAM_LD : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("      RST      : in  std_logic;");
		hdlPS.println("      CLK      : in  std_logic;");
		hdlPS.println("      STOP     : out std_logic");
		hdlPS.println("   );");
		hdlPS.println("end " + _coreName + ";");
		hdlPS.println("");
		
		hdlPS.println("architecture RTL of " + _coreName + " is");
		hdlPS.println("");   
		hdlPS.println("   component READ_MUX is");
		hdlPS.println("      generic (");
		hdlPS.println("         N_PORTS    : natural := 1;");
		hdlPS.println("         PORT_WIDTH : natural := 32");
		hdlPS.println("      );");

		hdlPS.println("      port(");
		hdlPS.println("         IN_PORTS   : in  std_logic_vector(N_PORTS*PORT_WIDTH-1 downto 0);");
		hdlPS.println("         EXISTS     : in  std_logic_vector(N_PORTS-1 downto 0);");
		hdlPS.println("         READS      : out std_logic_vector(N_PORTS-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("         OUT_PORT   : out std_logic_vector(PORT_WIDTH-1 downto 0);");
		hdlPS.println("         EXIST      : out std_logic;");
		hdlPS.println("         READ       : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         CONTROL    : in  std_logic_vector(N_PORTS-1 downto 0)");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println(""); 

		hdlPS.println("   component EVAL_LOGIC_RD_" + _coreName + " is");
		hdlPS.println("      generic (");
		hdlPS.println("         N_IN_PORTS    : natural := 1;");
		hdlPS.println("         N_CNTRS       : natural := 1;");
		hdlPS.println("         QUANT         : natural := 32;");
		hdlPS.println("         CNTR_WIDTH    : t_counter_width := ( 0=>10, 1=>10, 2=>9, others=>10 );");
		hdlPS.println("         N_PAR         : natural;");
		hdlPS.println("         PAR_WIDTH     : natural");
		hdlPS.println("      );");

		hdlPS.println("      port (");
		hdlPS.println("         RST           : in  std_logic;");
		hdlPS.println("         CLK           : in  std_logic;");
		hdlPS.println("");  
		hdlPS.println("         PARAMETERS    : in  std_logic_vector(N_PAR*PAR_WIDTH-1 downto 0);");
		hdlPS.println("");	  
		hdlPS.println("         LOWER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         UPPER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         ITERATORS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         REG_CNTRS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("         EVAL_DONE     : out std_logic;");
      hdlPS.println("         CNTR_DONE     : in std_logic;");
      hdlPS.println("         ENABLE        : in std_logic;");
      hdlPS.println("");
    }
		hdlPS.println("         CONTROL       : out std_logic_vector(N_IN_PORTS-1 downto 0)");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");
		
		hdlPS.println("   component EVAL_LOGIC_WR_" + _coreName + " is");
		hdlPS.println("      generic (");
		hdlPS.println("         N_OUT_PORTS   : natural := 1;");
		hdlPS.println("         N_CNTRS       : natural := 1;");
		hdlPS.println("         QUANT         : natural := 32;");
		hdlPS.println("         CNTR_WIDTH    : t_counter_width := ( 0=>10, 1=>10, 2=>9, others=>10 );");
		hdlPS.println("         N_PAR         : natural;");
		hdlPS.println("         PAR_WIDTH     : natural");
		hdlPS.println("      );");

		hdlPS.println("      port (");
		hdlPS.println("         RST           : in  std_logic;");
		hdlPS.println("         CLK           : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         PARAMETERS    : in  std_logic_vector(N_PAR*PAR_WIDTH-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("         LOWER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         UPPER_BND_OUT : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         ITERATORS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         REG_CNTRS     : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("         EVAL_DONE     : out std_logic;");
      hdlPS.println("         CNTR_DONE     : in std_logic;");
      hdlPS.println("         ENABLE        : in std_logic;");
      hdlPS.println("");
    }
		hdlPS.println("         CONTROL       : out std_logic_vector(N_OUT_PORTS-1 downto 0)");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");

		hdlPS.println("   component GEN_COUNTER is");
		hdlPS.println("      generic (");
		hdlPS.println("         N_CNTRS      : natural := 1;");
		hdlPS.println("         QUANT        : natural := 32;");
		hdlPS.println("         CNTR_WIDTH   : t_counter_width := ( 0=>10, 1=>10, 2=>9, others=>10 )");
		hdlPS.println("      );");

		hdlPS.println("      port (");
		hdlPS.println("         RST          : in  std_logic;");
		hdlPS.println("         CLK          : in  std_logic;");
		hdlPS.println("");	  
		hdlPS.println("         ENABLE       : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         LOWER_BND_IN : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         UPPER_BND_IN : in  std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         ITERATORS    : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         REG_CNTRS    : out std_logic_vector(N_CNTRS*QUANT-1 downto 0);");
		hdlPS.println("         DONE         : out std_logic");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");
		
		hdlPS.println("   component WRITE_DEMUX is");
		hdlPS.println("      generic (");
		hdlPS.println("         N_PORTS : natural := 1");
		hdlPS.println("      );");
		
		hdlPS.println("      port(");
		hdlPS.println("         WRITES  : out std_logic_vector(N_PORTS-1 downto 0);");
		hdlPS.println("         WRITE   : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         FULLS   : in  std_logic_vector(N_PORTS-1 downto 0);");
		hdlPS.println("         FULL    : out std_logic;");
		hdlPS.println("");
		hdlPS.println("         CONTROL : in  std_logic_vector(N_PORTS-1 downto 0)");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");  
		
		hdlPS.println("   component EXECUTION_UNIT_" + _coreName + " is");
		hdlPS.println("      generic (");
		hdlPS.println("         N_INPORTS  : natural := 1;");
		hdlPS.println("         N_OUTPORTS : natural := 1;");
		hdlPS.println("         IP_RESET   : natural := 1;");
		hdlPS.println("         QUANT      : natural := 32");
		hdlPS.println("      );");

		hdlPS.println("      port (");
		hdlPS.println("         RST        : in  std_logic;");
		hdlPS.println("         CLK        : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         IN_PORTS   : in  std_logic_vector(N_INPORTS*QUANT-1 downto 0);");
		hdlPS.println("         OUT_PORTS  : out std_logic_vector(N_OUTPORTS*QUANT-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("         ENABLE     : in  std_logic;");
		hdlPS.println("         IP_WRITE   : out std_logic;");
		hdlPS.println("         IP_READ    : out std_logic");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");
		
		hdlPS.println("   component CONTROLLER is");
		hdlPS.println("      generic (");
		hdlPS.println("         N_STAGES  : natural := 1;");
		hdlPS.println("         BLOCKING  : natural := 0");
		hdlPS.println("      );");

		hdlPS.println("      port (");
		hdlPS.println("         READ      : out std_logic;");
		hdlPS.println("         EXIST     : in  std_logic;");
		hdlPS.println("         WRITE     : out std_logic;");
		hdlPS.println("         FULL      : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         ENABLE_EX : out std_logic;");
		hdlPS.println("         IP_READ   : in  std_logic;");
		hdlPS.println("         IP_WRITE  : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         DONE_WR   : in  std_logic;");
		hdlPS.println("         DONE_RD   : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         CLK       : in  std_logic;");
		hdlPS.println("         RST       : in  std_logic");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");  
		
		hdlPS.println("   component PARAMETERS is");
		hdlPS.println("      generic (");
		hdlPS.println("         PAR_WIDTH  : natural;");
		hdlPS.println("         N_PAR      : natural;");
		hdlPS.println("         PAR_VALUES : t_par_values");
		hdlPS.println("      );");

		hdlPS.println("      port (");
		hdlPS.println("         RST        : in  std_logic;");
		hdlPS.println("         CLK        : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         PARAM_DT   : in  std_logic_vector(PAR_WIDTH-1 downto 0);");
		hdlPS.println("         PARAM_LD   : in  std_logic;");
		hdlPS.println("");
		hdlPS.println("         PARAMETERS : out std_logic_vector(N_PAR*PAR_WIDTH-1 downto 0)");
		hdlPS.println("      );");
		hdlPS.println("   end component;");
		hdlPS.println("");


		int in_func_var = _inArgList.size();
		int out_func_var = _outArgList.size();
		hdlPS.println("   -- Setting the parameters of the HW Node");
		hdlPS.println("   constant c_IN_PORTS     : natural := " + _adgInPorts.size() + "; -- number of input ports of a HW node");
		hdlPS.println("   constant c_OUT_PORTS    : natural := " + _adgOutPorts.size() + "; -- number of output ports of a HW node");
		hdlPS.println("   constant c_IN_FUNC_VAR  : natural := " + in_func_var + "; -- number of input ports of a HW IP");
		hdlPS.println("   constant c_OUT_FUNC_VAR : natural := " + out_func_var + "; -- number of output ports of a HW IP");
		hdlPS.println("   constant c_PARAMETERS   : natural := " + _indexList.getParameterVector().size() + "; -- number of global parameters");

		hdlPS.print("   constant c_PAR_VALUES   : t_par_values := (");

		Vector params = _adg.getParameterList();
		int cntr=0;
		Iterator p = params.iterator();
		while( p.hasNext() ) {
		    ADGParameter par = (ADGParameter) p.next();
                    hdlPS.print(cntr + "=>" + par.getValue() + ", ");
		    cntr++;
		}
		hdlPS.println("others=>0 ); -- each number represents the default value of a parameter");


		hdlPS.println("   constant c_COUNTERS     : natural := " + (_indexList.getIterationVector().size()-_skipList.size()) + "; -- number of iterators");
		
		// get counter_width
		String s = "";
		int num = _indexList.getIterationVector().size() - _skipList.size() - 1;
		Vector <Expression> boundExp = Polytope2IndexBoundVector.getExpression(_adgNode.getDomain().getLinearBound().get(0));
		
    ExpressionAnalyzer ea = new ExpressionAnalyzer(_indexList);
		for (int iterNum = 0; iterNum < _indexList.getIterationVector().size(); iterNum++){
			String indexName = _indexList.getIterationVector().get(iterNum);
      if (_skipList.contains(indexName))
        continue;
			Expression expr_lb = boundExp.get(2*iterNum);
			Expression expr_ub = boundExp.get(2*iterNum + 1);
			
			int counterWidth = ea.computeCounterWidth(indexName, expr_lb, expr_ub);
			s = num + "=>" + counterWidth + ", " + s;
			
			num--;
			
			if(counterWidth > _maxCounterWidth) {
				_maxCounterWidth = counterWidth;
			}
		}
		hdlPS.println("   constant c_CNTR_QUANT   : natural := " + _maxCounterWidth + ";");
		hdlPS.println("   constant c_CNTR_WIDTHS  : t_counter_width := ( " + s + "others=>10 );");

		hdlPS.println("   constant c_STAGES       : natural := 1; -- number of pipeline stages or delay");
		hdlPS.println("   constant c_BLOCKING     : natural := 0; -- block (or not) the pipeline if there is no input data");
		hdlPS.println("   constant c_IP_RESET     : natural := 1; -- active level of the HW IP reset signal");
		hdlPS.println("");

		hdlPS.println("   -- The signals list");
		hdlPS.println("   signal sl_read     : std_logic;");
		hdlPS.println("   signal sl_exist    : std_logic;");

		//# of exist signals is corresponding to # of rd_mux
		for(int num_sl_exist = 0; num_sl_exist < in_func_var; num_sl_exist++){
			hdlPS.println("   signal sl_exist_" + num_sl_exist + "  : std_logic;");
		}
		
		hdlPS.println("   signal sl_EnableEx : std_logic;");
		hdlPS.println("   signal sl_IP_Read  : std_logic;");
		hdlPS.println("   signal sl_IP_Write : std_logic;");
		hdlPS.println("   signal sl_write    : std_logic;");
		hdlPS.println("   signal sl_full     : std_logic;");
		hdlPS.println("   signal sl_done_wr  : std_logic;");
		hdlPS.println("   signal sl_done_rd  : std_logic;");
		hdlPS.println("");
		hdlPS.println("   signal sl_IN_PORTS     : std_logic_vector(c_IN_PORTS*QUANT-1 downto 0);");
		hdlPS.println("   signal sl_in_ports_ex  : std_logic_vector(c_IN_FUNC_VAR*QUANT-1 downto 0);");
		hdlPS.println("   signal sl_out_ports_ex : std_logic_vector(c_OUT_FUNC_VAR*QUANT-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("   signal sl_control_rd   : std_logic_vector(c_IN_PORTS-1 downto 0);");
		hdlPS.println("   signal sl_EXISTS       : std_logic_vector(c_IN_PORTS-1 downto 0);");
		hdlPS.println("   signal sl_READS        : std_logic_vector(c_IN_PORTS-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("   signal sl_control_wr   : std_logic_vector(c_OUT_PORTS-1 downto 0);");
		hdlPS.println("   signal sl_WRITES       : std_logic_vector(c_OUT_PORTS-1 downto 0);");
		hdlPS.println("   signal sl_FULLS        : std_logic_vector(c_OUT_PORTS-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("   signal sl_control_reuse : std_logic_vector(c_OUT_PORTS-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("   signal sl_parameters   : std_logic_vector(c_PARAMETERS*PAR_WIDTH-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("   signal sl_LOW_BND_RD, sl_UP_BND_RD      : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
		hdlPS.println("   signal sl_LOW_BND_WR, sl_UP_BND_WR      : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
		hdlPS.println("   signal sl_ITERATORS_RD, sl_ITERATORS_WR : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
		hdlPS.println("   signal sl_REG_CNTRS_RD, sl_REG_CNTRS_WR : std_logic_vector(c_COUNTERS*c_CNTR_QUANT-1 downto 0);");
		hdlPS.println("");
		hdlPS.println("   signal sl_RST : std_logic;");
    if (_regEval) {
      hdlPS.println("   signal sl_RST_1 : std_logic;");
      hdlPS.println("   signal sl_initstrobe : std_logic;");
      hdlPS.println("   signal sl_cntr_rd_en, sl_cntr_rd_done : std_logic;");
      hdlPS.println("   signal sl_cntr_wr_en, sl_cntr_wr_done : std_logic;");
    }
		hdlPS.println("");
		hdlPS.println("begin");
		hdlPS.println("");
		hdlPS.println("   sl_RST <= RST when RESET_HIGH=1 else not RST;");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("   -- RST is delayed 1 CLK cycle (used in eval_rd/rw and controller units)");
      hdlPS.println("   process(clk)");
      hdlPS.println("   begin");
      hdlPS.println("      if rising_edge(clk) then");
      hdlPS.println("         sl_RST_1 <= sl_RST;");
      hdlPS.println("      end if;");
      hdlPS.println("   end process;");
		hdlPS.println("");
		hdlPS.println("   sl_initstrobe <= not sl_RST and sl_RST_1;");
		hdlPS.println("   sl_cntr_rd_en <= sl_read or sl_initstrobe;");
		hdlPS.println("   sl_cntr_wr_en <= sl_write or sl_initstrobe;");
		hdlPS.println("");
    }
		hdlPS.println("--======================================================================================--");
		hdlPS.println("");
		
		if(_adgInPorts.size() == 0){
			hdlPS.println("   sl_exist <= '1';");
			hdlPS.println("");
		}
		else{
			int index_rd_mux = 0;
			int total_in_ports = 0;
			
			//one in_argument corresponding to one read_mux
			i = _inArgList.iterator();
			while(i.hasNext()){
				in_arg = (ADGVariable) i.next();
				
				////get the vector of the in ports of the node relating to the specific in_argument of the function
				binding_in_ports = new Vector<ADGInPort>();
				in_arg_name = in_arg.getName();
							
				j = _adgInPorts.iterator();
				while (j.hasNext()) {
					adg_in_port = (ADGInPort) j.next();
					if (adg_in_port.getBindVariables().get(0).getName().equals(in_arg_name)== true){
						binding_in_ports.addElement(adg_in_port);
					}
				}
				
				hdlPS.println("   RD_MUX_" + index_rd_mux + " : READ_MUX");
				hdlPS.println("   generic map (");
				hdlPS.println("      N_PORTS    => "+ binding_in_ports.size() + ",");
				hdlPS.println("      PORT_WIDTH => QUANT");
				hdlPS.println("   )");

				hdlPS.println("   port map (");
				
				//the situation of only one rd_mux is different
				if (total_in_ports == 0){
					hdlPS.println("      IN_PORTS   => sl_IN_PORTS(" + binding_in_ports.size() + "*QUANT-1 downto 0),");
					hdlPS.println("      EXISTS     => sl_EXISTS(" + (binding_in_ports.size()-1) + " downto 0),");
					hdlPS.println("      READS      => sl_READS(" + (binding_in_ports.size()-1) + " downto 0),");
					hdlPS.println("");
					hdlPS.println("      OUT_PORT   => sl_in_ports_ex(QUANT-1 downto 0),");
					hdlPS.println("      EXIST      => sl_exist_0,");
					hdlPS.println("      READ       => sl_read,");
					hdlPS.println("");

					hdlPS.println("      CONTROL    => sl_control_rd(" + (binding_in_ports.size() - 1) + " downto 0)");
					hdlPS.println("   );");
					hdlPS.println("");
				}
				else{
					hdlPS.println("      IN_PORTS   => sl_IN_PORTS(" + (binding_in_ports.size()+ total_in_ports) + "*QUANT-1 downto " + total_in_ports + "*QUANT),");
					hdlPS.println("      EXISTS     => sl_EXISTS(" + (binding_in_ports.size() + total_in_ports -1) + " downto " + total_in_ports + "),");
					hdlPS.println("      READS      => sl_READS(" + (binding_in_ports.size() + total_in_ports -1) + " downto " + total_in_ports + "),");
					hdlPS.println("");
					hdlPS.println("      OUT_PORT   => sl_in_ports_ex(" + (index_rd_mux + 1) + "*QUANT-1 downto " + index_rd_mux + "*QUANT),");
					hdlPS.println("      EXIST      => sl_exist_" + index_rd_mux + ",");
					hdlPS.println("      READ       => sl_read,");
					hdlPS.println("");

					hdlPS.println("      CONTROL    => sl_control_rd(" + (binding_in_ports.size() + total_in_ports -1) + " downto " + total_in_ports + ")");
					hdlPS.println("   );");
					hdlPS.println("");
				}
				
				Iterator k = binding_in_ports.iterator();
				int temp_index = total_in_ports;
				while (k.hasNext())	{
					ADGInPort in = (ADGInPort) k.next();
					hdlPS.println("   " + in.getName() + "_Rd <= sl_READS(" + temp_index +");");
					temp_index++;
				}
				
				hdlPS.println("");
				
				index_rd_mux++;
				total_in_ports = total_in_ports + binding_in_ports.size();
			}
			
			
			String sl_in_ports = ((ADGInPort)_adgInPorts.get(0)).getName() + "_Din;";
			String sl_exists = ((ADGInPort)_adgInPorts.get(0)).getName() + "_Exist;";
			String sl_exist = "sl_exist_0;";
			
			for (int n = 1; n < _adgInPorts.size(); n++){
				sl_in_ports = ((ADGInPort)_adgInPorts.get(n)).getName() + "_Din & " + sl_in_ports;
				sl_exists = ((ADGInPort)_adgInPorts.get(n)).getName() + "_Exist & " + sl_exists;
			}
			
			for (int m = 1; m < _inArgList.size(); m++){
				sl_exist = "sl_exist_" + m + " and " + sl_exist;
			}
				
			
			hdlPS.println("   sl_IN_PORTS <= " + sl_in_ports);
			if(_adgInPorts.size() == 1){
				hdlPS.println("   sl_EXISTS(0)   <= " + sl_exists);
			}
			else{
				hdlPS.println("   sl_EXISTS   <= " + sl_exists);
			}
			
			hdlPS.println("   sl_exist    <= " + sl_exist);
			hdlPS.println("");	
		}

		hdlPS.println("   EVAL_RD : EVAL_LOGIC_RD_" + _coreName + "");
		hdlPS.println("   generic map (");
		hdlPS.println("      N_IN_PORTS    => c_IN_PORTS,");
		hdlPS.println("      N_CNTRS       => c_COUNTERS,");
		hdlPS.println("      QUANT         => c_CNTR_QUANT,");
		hdlPS.println("      CNTR_WIDTH    => c_CNTR_WIDTHS,");
		hdlPS.println("      N_PAR         => c_PARAMETERS,");
		hdlPS.println("      PAR_WIDTH     => PAR_WIDTH");
		hdlPS.println("   )");

		hdlPS.println("   port map(");
		hdlPS.println("      RST           => sl_RST,");
		hdlPS.println("      CLK           => CLK,");
		hdlPS.println("");
		hdlPS.println("      PARAMETERS    => sl_parameters,");
		hdlPS.println("");
		hdlPS.println("      LOWER_BND_OUT => sl_LOW_BND_RD,");
		hdlPS.println("      UPPER_BND_OUT => sl_UP_BND_RD,");
		hdlPS.println("      ITERATORS     => sl_ITERATORS_RD,");
		hdlPS.println("      REG_CNTRS     => sl_REG_CNTRS_RD,");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("      EVAL_DONE     => sl_done_rd,");
      hdlPS.println("      CNTR_DONE     => sl_cntr_rd_done,");
      hdlPS.println("      ENABLE        => sl_cntr_rd_en,");
      hdlPS.println("");
    }
		hdlPS.println("      CONTROL       => sl_control_rd");
		hdlPS.println("   );");
		hdlPS.println("");
		
		hdlPS.println("   ITER_RD : GEN_COUNTER");
		hdlPS.println("   generic map(");
		hdlPS.println("      N_CNTRS       => c_COUNTERS,");
		hdlPS.println("      QUANT         => c_CNTR_QUANT,");
		hdlPS.println("      CNTR_WIDTH    => c_CNTR_WIDTHS");
		hdlPS.println("   )");

		hdlPS.println("   port map(");
		hdlPS.println("      RST           => sl_RST,");
		hdlPS.println("      CLK           => CLK,");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("      ENABLE        => sl_cntr_rd_en,");
    }
    else {
      hdlPS.println("      ENABLE        => sl_read,");
    }
		hdlPS.println("");
		hdlPS.println("      LOWER_BND_IN  => sl_LOW_BND_RD,");
		hdlPS.println("      UPPER_BND_IN  => sl_UP_BND_RD,");
		hdlPS.println("      ITERATORS     => sl_ITERATORS_RD,");
		hdlPS.println("      REG_CNTRS     => sl_REG_CNTRS_RD,");
		hdlPS.println("");
    if (_regEval) {
		  hdlPS.println("      DONE          => sl_cntr_rd_done");
    }
    else {
		  hdlPS.println("      DONE          => sl_done_rd");
    }
		hdlPS.println("   );");

		hdlPS.println("");
		hdlPS.println("--======================================================================================--");
		hdlPS.println("");

		hdlPS.println("   DEMUX : WRITE_DEMUX");
		hdlPS.println("   generic map (");
		hdlPS.println("      N_PORTS => c_OUT_PORTS");
		hdlPS.println("   )");

		hdlPS.println("   port map (");
		hdlPS.println("      WRITES  => sl_WRITES,");
		hdlPS.println("      WRITE   => sl_write,");
		hdlPS.println("");
		hdlPS.println("      FULLS   => sl_fulls,");
		hdlPS.println("      FULL    => sl_full,");
		hdlPS.println("");
		hdlPS.println("      CONTROL => sl_control_wr");
		hdlPS.println("   );");
		hdlPS.println("");
		
		if(_adgOutPorts.size() == 0){
			hdlPS.println("   sl_full    <= '0';");
			hdlPS.println("");	
		}
		else{
			i = _adgOutPorts.iterator();
			int index = 0;
			
			while (i.hasNext()){
				adg_out_port = (ADGOutPort) i.next();
        if (!_isReusePort(adg_out_port)) {
          hdlPS.println("   " + adg_out_port.getName() + "_Wr <= sl_WRITES(" + index +");");
        }
        else {
          hdlPS.println("   " + adg_out_port.getName() + "_Wr <= sl_control_reuse(" + index + ") and not " + adg_out_port.getName() + "_Full and sl_read;");
        }
				index++;
			}

			hdlPS.println("");

			i = _outArgList.iterator();
			index = 0;
			while(i.hasNext()){
				out_arg = (ADGVariable) i.next();
				out_arg_name = out_arg.getName();

				j = _adgOutPorts.iterator();
				while (j.hasNext()) {
					adg_out_port = (ADGOutPort) j.next();
					if (adg_out_port.getBindVariables().get(0).getName().equals(out_arg_name)== true){
						if(index == 0){
							hdlPS.println("   " + adg_out_port.getName() + "_Dout <= sl_out_ports_ex(QUANT-1 downto 0);");
						}
						else{
							hdlPS.println("   " + adg_out_port.getName() + "_Dout <= sl_out_ports_ex(" + (index + 1) + "*QUANT-1 downto " + index + "*QUANT);");
						}
					}
				}
				index++;
			}
      // Handle reuse ports
      if (_numReusePorts > 0) {
        j = _adgOutPorts.iterator();
        while (j.hasNext()) {
          adg_out_port = (ADGOutPort) j.next();
          if (_isReusePort(adg_out_port)) {
            ADGVariable var = adg_out_port.getBindVariables().get(0);
            int argno = -1;
            Iterator jj = _inArgList.iterator();
            while (jj.hasNext()) {
              ADGVariable vvar = (ADGVariable) jj.next();
              if (vvar.getName().equals(var.getName())) {
                argno = _inArgList.indexOf(vvar);
              }
            }
            assert(argno>=0); // Otherwise corresponding input arg was not found, which means something is wrong
            hdlPS.println("   " + adg_out_port.getName() + "_Dout <= sl_in_ports_ex(" + (argno+1) + "*QUANT-1 downto " + argno + "*QUANT);");
          }
        }
      }
			
			String sl_fulls = ((ADGOutPort)_adgOutPorts.get(0)).getName() + "_Full;";
			
			
			for (int n = 1; n < _adgOutPorts.size(); n++){
				sl_fulls = ((ADGOutPort)_adgOutPorts.get(n)).getName() + "_Full & " + sl_fulls;
			}

			hdlPS.println("");	
			if(_adgOutPorts.size() == 1){
				hdlPS.println("   sl_fulls(0) <= " + sl_fulls);
			}
			else{
				hdlPS.println("   sl_fulls <= " + sl_fulls);
			}
			
			hdlPS.println("");	
			
		}
		
		hdlPS.println("   EVAL_WR : EVAL_LOGIC_WR_" + _coreName + "");
		hdlPS.println("   generic map (");
		hdlPS.println("      N_OUT_PORTS   => c_OUT_PORTS,");
		hdlPS.println("      N_CNTRS       => c_COUNTERS,");
		hdlPS.println("      QUANT         => c_CNTR_QUANT,");
		hdlPS.println("      CNTR_WIDTH    => c_CNTR_WIDTHS,");
		hdlPS.println("      N_PAR         => c_PARAMETERS,");
		hdlPS.println("      PAR_WIDTH     => PAR_WIDTH");
		hdlPS.println("   )");

		hdlPS.println("   port map (");
		hdlPS.println("      RST           => sl_RST,");
		hdlPS.println("      CLK           => CLK,");
		hdlPS.println("");
		hdlPS.println("      PARAMETERS    => sl_parameters,");
		hdlPS.println("");
		hdlPS.println("      LOWER_BND_OUT => sl_LOW_BND_WR,");
		hdlPS.println("      UPPER_BND_OUT => sl_UP_BND_WR,");
		hdlPS.println("      ITERATORS     => sl_ITERATORS_WR,");
		hdlPS.println("      REG_CNTRS     => sl_REG_CNTRS_WR,");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("      EVAL_DONE     => sl_done_wr,");
      hdlPS.println("      CNTR_DONE     => sl_cntr_wr_done,");
      hdlPS.println("      ENABLE        => sl_cntr_wr_en,");
      hdlPS.println("");
    }
		hdlPS.println("      CONTROL       => sl_control_wr");
		hdlPS.println("   );");
		hdlPS.println("");

    if (_numReusePorts > 0) {
      hdlPS.println("   -- Additional write evaluation logic for reuse ports");
      hdlPS.println("   EVAL_REUSE : EVAL_LOGIC_WR_" + _coreName + "");
      hdlPS.println("   generic map (");
      hdlPS.println("      N_OUT_PORTS   => c_OUT_PORTS,");
      hdlPS.println("      N_CNTRS       => c_COUNTERS,");
      hdlPS.println("      QUANT         => c_CNTR_QUANT,");
      hdlPS.println("      CNTR_WIDTH    => c_CNTR_WIDTHS,");
      hdlPS.println("      N_PAR         => c_PARAMETERS,");
      hdlPS.println("      PAR_WIDTH     => PAR_WIDTH");
      hdlPS.println("   )");

      hdlPS.println("   port map (");
      hdlPS.println("      RST           => sl_RST,");
      hdlPS.println("      CLK           => CLK,");
      hdlPS.println("");
      hdlPS.println("      PARAMETERS    => sl_parameters,");
      hdlPS.println("");
      hdlPS.println("      LOWER_BND_OUT => sl_LOW_BND_RD,");
      hdlPS.println("      UPPER_BND_OUT => sl_UP_BND_RD,");
      hdlPS.println("      ITERATORS     => sl_ITERATORS_RD,");
      hdlPS.println("      REG_CNTRS     => sl_REG_CNTRS_RD,");
      hdlPS.println("");
      if (_regEval) {
        System.err.println("Please check if reuse and registered eval still work correctly!");
        assert(false);
        hdlPS.println("      EVAL_DONE     => sl_done_reuse,"); // TODO: check this signal
        hdlPS.println("      CNTR_DONE     => sl_cntr_rd_done,");
        hdlPS.println("      ENABLE        => sl_cntr_rd_en,");
        hdlPS.println("");
      }
      hdlPS.println("      CONTROL       => sl_control_reuse");
      hdlPS.println("   );");
      hdlPS.println("");
    }

		hdlPS.println("   ITER_WR : GEN_COUNTER");
		hdlPS.println("   generic map (");
		hdlPS.println("      N_CNTRS       => c_COUNTERS,");
		hdlPS.println("      QUANT         => c_CNTR_QUANT,");
		hdlPS.println("      CNTR_WIDTH    => c_CNTR_WIDTHS");
		hdlPS.println("   )");

		hdlPS.println("   port map (");
		hdlPS.println("      RST           => sl_RST,");
		hdlPS.println("      CLK           => CLK,");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("      ENABLE        => sl_cntr_wr_en,");
    }
    else {
      hdlPS.println("      ENABLE        => sl_write,");
    }
		hdlPS.println("");
		hdlPS.println("      LOWER_BND_IN  => sl_LOW_BND_WR,");
		hdlPS.println("      UPPER_BND_IN  => sl_UP_BND_WR,");
		hdlPS.println("      ITERATORS     => sl_ITERATORS_WR,");
		hdlPS.println("      REG_CNTRS     => sl_REG_CNTRS_WR,");
		hdlPS.println("");
    if (_regEval) {
      hdlPS.println("      DONE          => sl_cntr_wr_done");
    }
    else {
      hdlPS.println("      DONE          => sl_done_wr");
    }
		hdlPS.println("   );");

		hdlPS.println("");
		hdlPS.println("--======================================================================================--");
		hdlPS.println("");

		hdlPS.println("   EX : EXECUTION_UNIT_" + _coreName + "");
		hdlPS.println("   generic map (");
		hdlPS.println("      N_INPORTS  => c_IN_FUNC_VAR,");
		hdlPS.println("      N_OUTPORTS => c_OUT_FUNC_VAR,");
		hdlPS.println("      IP_RESET   => c_IP_RESET,");
		hdlPS.println("      QUANT      => QUANT");
		hdlPS.println("   )");

		hdlPS.println("   port map (");
		hdlPS.println("      RST        => sl_RST,");
		hdlPS.println("      CLK        => CLK,");
		hdlPS.println("");
		hdlPS.println("      IN_PORTS   => sl_in_ports_ex,");
		hdlPS.println("      OUT_PORTS  => sl_out_ports_ex,");
		hdlPS.println("");
		hdlPS.println("      ENABLE     => sl_EnableEx,");
		hdlPS.println("      IP_WRITE   => sl_IP_Write,");
		hdlPS.println("      IP_READ    => sl_IP_Read");
		hdlPS.println("   );");
		hdlPS.println("");
		
		hdlPS.println("   CTRL : CONTROLLER");
		hdlPS.println("   generic map (");
		hdlPS.println("      N_STAGES   => c_STAGES,");
		hdlPS.println("      BLOCKING   => c_BLOCKING");
		hdlPS.println("   )");

		hdlPS.println("   port map (");
    if (_regEval) {
      hdlPS.println("      RST        => sl_RST_1,");
    }
    else {
      hdlPS.println("      RST        => sl_RST,");
    }
		hdlPS.println("      CLK        => CLK,");
		hdlPS.println("");
		hdlPS.println("      READ       => sl_read,");
		hdlPS.println("      EXIST      => sl_exist,");
		hdlPS.println("");
		hdlPS.println("      ENABLE_EX  => sl_EnableEx,");
		hdlPS.println("      IP_READ    => sl_IP_Read,");
		hdlPS.println("      IP_WRITE   => sl_IP_Write,");
		hdlPS.println("");
		hdlPS.println("      WRITE      => sl_write,");
		hdlPS.println("      FULL       => sl_full,");
		hdlPS.println("");
		hdlPS.println("      DONE_WR    => sl_done_wr,");
		hdlPS.println("      DONE_RD    => sl_done_rd");
		hdlPS.println("   );");
		hdlPS.println(""); 
		
		hdlPS.println("   PAR_LOAD : PARAMETERS");
		hdlPS.println("   generic map (");
		hdlPS.println("      PAR_WIDTH  => PAR_WIDTH,");
		hdlPS.println("      N_PAR      => c_PARAMETERS,");
		hdlPS.println("      PAR_VALUES => c_PAR_VALUES");
		hdlPS.println("   )");

		hdlPS.println("   port map(");
		hdlPS.println("      RST        => sl_RST,");
		hdlPS.println("      CLK        => CLK,");
		hdlPS.println("");
		hdlPS.println("      PARAM_DT   => PARAM_DT,");
		hdlPS.println("      PARAM_LD   => PARAM_LD,");
		hdlPS.println("");
		hdlPS.println("      PARAMETERS => sl_parameters");
		hdlPS.println("   );");
		hdlPS.println("");  
		
		hdlPS.println("   STOP <= sl_done_wr;");
		hdlPS.println("");
		hdlPS.println("end RTL;");
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

		fn = _currentCodeDir + "/" + fileName;
		if (fileName.equals(""))
			ps = new PrintStream(System.out);
		else
			ps = new PrintStream(new FileOutputStream(fn));

		return ps;
	}
	
	
	
	//find the upper bounds for indexes, for deciding the counter bit width
	protected static int _findUpperBound(String index,
			Expression expr_lb, Expression expr_ub) {

		int lb = 0;
		int ub = 0;
		HashMap <String, Integer> ttParam_ub = new HashMap <String,Integer>();
		HashMap <String, Integer> ttParam_lb = new HashMap <String,Integer>();;
	//	boolean allAddition = true;
		
	//	ttParam.putAll(_parameters);
	//	ttParam.putAll(_boundsLinks);

//		System.out.println("Upper bound:" + expr);
		
		
		Iterator i = _parameters.keySet().iterator();
		while(i.hasNext()){
			String param_name = (String)i.next();
			
			//System.out.println("param name is " + param_name);
			
			Iterator expr_it = expr_ub.iterator();
			LinTerm j;
			while (expr_it.hasNext()) {
				j = (LinTerm) expr_it.next();
				
				//System.out.println("terms for upperbound " + j.toString());
				
				if( j.getName().equals(param_name) && j.getSign() == 1){
					ttParam_ub.put(param_name, _parameters.get(param_name).get(1));
					//continue;
					//System.out.println("param name is " + param_name + "is positive");
					//System.out.println(ttParam_ub.get(param_name).intValue());
				}
				else if(j.getName().equals(param_name) && j.getSign() == -1){
					ttParam_ub.put(param_name, _parameters.get(param_name).get(0));
					//continue;
					//System.out.println("param name is " + param_name + "is negetive");
				}
			}
			
		}
		
		Iterator j = _boundsLinks.keySet().iterator();
		while(j.hasNext()){
			String index_name = (String)j.next();
			Iterator expr_it = expr_ub.iterator();
			LinTerm term;
			while (expr_it.hasNext()) {
				term = (LinTerm) expr_it.next();
				if( term.getName().equals(index_name) && term.getSign() == 1){
					ttParam_ub.put(index_name, _boundsLinks.get(index_name).get(1));
					//continue;
				}
				else if(term.getName().equals(index_name) && term.getSign() == -1){
					ttParam_ub.put(index_name, _boundsLinks.get(index_name).get(0));
					//continue;
				}
			}

		}

	/*	Iterator i = expr.iterator();
		LinTerm j;
		while (i.hasNext()) {
			j = (LinTerm) i.next();
			//when the term is minus, we shouldn't use upperbound
			//we assume currently lower bound can't be less than 0, so we just simply remove this term.
			if( j.getSign() == -1){
				j.remove();
			}
		}*/

		Vector<Integer> point = new Vector<Integer>();
		point.addAll(ttParam_ub.values());

		Vector<String> indice = new Vector<String>();
		indice.addAll(ttParam_ub.keySet());

		//System.out.println("indice_ub " + indice.toString());
		//System.out.println("point_ub " + point.toString());

		ub = expr_ub.evaluate(point, indice);
		//System.out.println("upper bound " + index + " is : " + ub);
		
		//when upper bound is 0, it cannot have log2 operation
		if (ub == 0){
			ub = 1;
		}
		
////////////////////////////////////////////////////////////////////////////////////////////////////		
	//	System.out.println("upperbound expression is " + expr_ub.toString());
	//	System.out.println("lowerbound expression is " + expr_lb.toString());
		
		i = _parameters.keySet().iterator();
		while(i.hasNext()){
			String param_name = (String)i.next();
			
			//System.out.println("param name is " + param_name);
			
			Iterator expr_it = expr_lb.iterator();
			LinTerm term;
			while (expr_it.hasNext()) {
				term = (LinTerm) expr_it.next();
				
				//System.out.println("lowerbound term " + term.toString());
				
				if( term.getName().equals(param_name) && term.getSign() == -1){
					ttParam_lb.put(param_name, _parameters.get(param_name).get(1));
					//continue;
				}
				else if(term.getName().equals(param_name) && term.getSign() == 1){
					ttParam_lb.put(param_name, _parameters.get(param_name).get(0));
					//System.out.println("param name is " + param_name + " is positive");
					//System.out.println(ttParam_lb.get(param_name).intValue());
					//continue;
				}
			}
			
		}
		
		j = _boundsLinks.keySet().iterator();
		while(j.hasNext()){
			String index_name = (String)j.next();
			Iterator expr_it = expr_lb.iterator();
			LinTerm term;
			while (expr_it.hasNext()) {
				term = (LinTerm) expr_it.next();
				if( term.getName().equals(index_name) && term.getSign() == -1){
					ttParam_lb.put(index_name, _boundsLinks.get(index_name).get(1));
					//continue;
				}
				else if(term.getName().equals(index_name) && term.getSign() == 1){
					ttParam_lb.put(index_name, _boundsLinks.get(index_name).get(0));
					//continue;
				}
			}
			
		}
		
		
		Vector<Integer> point_lb = new Vector<Integer>();
		point_lb.addAll(ttParam_lb.values());

		Vector<String> indice_lb = new Vector<String>();
		indice_lb.addAll(ttParam_lb.keySet());
		
		lb = expr_lb.evaluate(point_lb, indice_lb);
		
		Vector<Integer> lb_ub = new Vector <Integer>();
		lb_ub.addElement(new Integer(lb));
		lb_ub.addElement(new Integer(ub));
		
		//System.out.println("lower bound and upperbound for index " + index + " are " + lb + " and " + ub);
		
		_boundsLinks.put(index, lb_ub);
		return ub;
	}

  private boolean _isSource(ADGNode node) {
    return (node.getInPorts().size() == 0);
  }
  
  private boolean _isSink(ADGNode node) {
    return (node.getOutPorts().size() == 0);
  }

  private boolean _isReusePort(ADGOutPort port) {
    return (port.getBindVariables().get(0).getName().indexOf("in") >= 0);
  }
  

	// /////////////////////////////////////////////////////////////////
	// // private variables ///
	private String _coreName;

	// dir name
	private String _moduleName;

	private String _moduleDir;

	private static String _dataDir = "data";

	private static String _devlDir = "devl";

	private static String _hdlDir = "hdl/vhdl";

	private String _hdlFile;

	private UserInterface _ui = null;

	private String _codeDir;
	
	private String _currentCodeDir;
	
	private Mapping _mapping;

	private CompaanHWNode _HWNode;
	
	private ADGraph _adg;
	
	private ADGNode _adgNode; //corresponding ADG node
	
	private Vector _inArgList ;		//in arguments of the ADG function
	
	private Vector _adgInPorts ;      //in ports of the ADG node
	
	private Vector _outArgList ;      //out arguments of the ADG function
	
	private Vector _adgOutPorts ;     //out ports of the ADG node

  private int _numReusePorts = 0;   // Number of reuse ports
	
	private  IndexVector _indexList ;  //index list of the ADG node

  private Vector<String> _skipList = new Vector<String>();  // List of iterators that are skipped in counter generation

	protected static HashMap <String, Vector<Integer>> _parameters = new HashMap <String, Vector<Integer>>();
	
	protected static HashMap <String, Vector<Integer>> _boundsLinks = new HashMap<String, Vector<Integer>>(); //hash map with index/param names as keys, and lower and upper bounds vector as values.
	
	private int _maxCounterWidth = 0;


  ////////////////////////////////////
  // Experimental & hardcoded options:
  ////////////////////////////////////

  // Put a register between expression and control signal in eval_logic modules
  // This may increase the maximum achievable clock frequency
  private boolean _regEval = false;

  // Try to optimize counters (such as pruning single-iteration counters)
  private boolean _optimizeCounters = false;
}
