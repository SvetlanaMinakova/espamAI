package espam.visitor.xml.csdf;

import espam.datamodel.graph.Node;
import espam.datamodel.graph.csdf.SDFGraphType;
import espam.datamodel.graph.csdf.CSDFGraph;
import espam.datamodel.graph.csdf.CSDFNode;
import espam.operations.refinement.CSDFTimingRefiner;
import espam.operations.refinement.RefinedCSDFEvaluator;
import espam.visitor.CSDFGraphVisitor;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * XML graph properties visitor
 */
public class CSDFGraphPropertiesXMLVisitor extends CSDFGraphVisitor {

   /**
     * Constructor for the CSDFGraphXMLVisitor object
     * @param printStream the output Xml print stream
     */
    public CSDFGraphPropertiesXMLVisitor(PrintStream printStream) {

        _printStream = printStream;
    }

    /**
     * Set prefix - white-space shift from the beginning
     * @param prefix
     */
    public void setPrefix (String prefix){
        _prefix = prefix;
    }

    /**
     *  Visit a CSDFGraph component.
     * @param  x A Visitor Object.
     */
    public void visitComponent(CSDFGraph x) {
        _wcet = CSDFTimingRefiner.getInstance().getExecTimes(x);
        SDFGraphType graphType = x.getType();
        /**open graph properties*/
        _printStream.println(_prefix + "<" + graphType + "Properties>");
        prefixInc();
        /** Visit the list of nodes of this CSDFGraph*/

        Vector nodeList = x.getNodeList();
        if( nodeList != null ) {
            Iterator i = nodeList.iterator();
            while( i.hasNext() ) {
                CSDFNode node = (CSDFNode) i.next();
                node.accept(this);
            }
        }
        prefixDec();

        /**close graph properties*/
        _printStream.println(_prefix + "</" + graphType + "Properties>");

        }


    /**
     * Visit SDF graph node
     * @param  x A Visitor Object.
     */
    @Override
    public void visitComponent(CSDFNode x) {
        prefixInc();
        /**open node properties*/
        _printStream.println(_prefix + "<actorProperties actor = \"" + x.getUniqueName() + "\" >");
        _assignProcessor(x);
        _assignMemory(x);
        /**close node properties*/
        _printStream.println(_prefix + "</actorProperties>");
        prefixDec();
    }

    /**
     * Imitate mapping of each node of CSDF graph on an abstract processor
     * type of 'proc_0'
     * @param graph SDF graph
     */
    public void createOneToOneDummyMapping(CSDFGraph graph){
        _nodeProcessorsMapping = new HashMap<>();
        Vector nodeList = graph.getNodeList();
        int procId = 0;
        if( nodeList != null ) {
            Iterator i = nodeList.iterator();
            while( i.hasNext() ) {
                CSDFNode node = (CSDFNode) i.next();
                _nodeProcessorsMapping.put(node.getUniqueName(),"proc_0");
            }
        }

    }

    /**
     * Imitate mapping of each node of CSDF graph on an abstract processor
     * type of 'proc_0'
     * @param graph SDF graph
     */
    public void createToProcDummyMapping(CSDFGraph graph, int maxProc){
        _nodeProcessorsMapping = new HashMap<>();
        Vector nodeList = graph.getNodeList();
        int nodesNum = nodeList.size();
        int procStep = nodesNum/maxProc;
        int procId =0;

        if( nodeList != null ) {
            Iterator i = nodeList.iterator();
            while( i.hasNext() ) {
                CSDFNode node = (CSDFNode) i.next();

                _nodeProcessorsMapping.put(node.getUniqueName(),"proc_" + procId);

                procId++;
                if(procId>=procStep)
                    procId=0;
            }
        }

    }


    /**
     * TODO refactoring on 'default' property and exec_time calculation/assignment
     * Assigns a processor form _nodeProcessorsMapping to an CSDFGraph node,
     * if specified in a mapping. A processor element requires the type attribute.
     * The value of this attribute specifies the processor type
     * for which the properties contained inside the element are valued.
     * The processor element may also have the default attribute.
     * This attribute can have the value true or false. When absent, its
     * default value is false. This default attribute is used in case that more
     * then one processor element is contained inside a actorProperties element.
     * One of the processor elements may have then set the value of the default
     * attribute to true. The analysis algorithms will then use the properties
     * contained inside this element. The processor element must contain
     * an executionTime element.
     * @param node SDF graph node
     */
    private void _assignProcessor(CSDFNode node,int maxLen){
        String proc = _nodeProcessorsMapping.get(node.getUniqueName());
        if(proc==null)
            return;
        prefixInc();
        _printStream.println(_prefix + "<processor type='" + proc + "' default = 'true'>");
        prefixInc();
        Vector<Integer> wcet = _wcet.get(node);
        if(wcet==null)
            wcet = CSDFTimingRefiner.getInstance().getDefaultExecTime(maxLen);
        String execTime = vecToCommaSeparatedStr(wcet);


        _printStream.println(_prefix + "<executionTime time='" + execTime + "'/>");


        prefixDec();
        _printStream.println(_prefix + "</processor>");
        prefixDec();
    }

    /**
     * TODO refactoring on 'default' property and exec_time calculation/assignment
     * Assigns a processor form _nodeProcessorsMapping to an CSDFGraph node,
     * if specified in a mapping. A processor element requires the type attribute.
     * The value of this attribute specifies the processor type
     * for which the properties contained inside the element are valued.
     * The processor element may also have the default attribute.
     * This attribute can have the value true or false. When absent, its
     * default value is false. This default attribute is used in case that more
     * then one processor element is contained inside a actorProperties element.
     * One of the processor elements may have then set the value of the default
     * attribute to true. The analysis algorithms will then use the properties
     * contained inside this element. The processor element must contain
     * an executionTime element.
     * @param node SDF graph node
     */
    private void _assignProcessor(CSDFNode node){
        String proc = _nodeProcessorsMapping.get(node.getUniqueName());
        if(proc==null)
            return;
           prefixInc();
           _printStream.println(_prefix + "<processor type='" + proc + "' default = 'true'>");
           prefixInc();
           Vector<Integer> wcet = _wcet.get(node);
           if(wcet==null)
               wcet = CSDFTimingRefiner.getInstance().getDefaultExecTime(node.getLength());
           String execTime = vecToCommaSeparatedStr(wcet);


           _printStream.println(_prefix + "<executionTime time='" + execTime + "'/>");


           prefixDec();
           _printStream.println(_prefix + "</processor>");
           prefixDec();
    }

    /**
     * TODO finish implementation
     * Assign memory to an CSDF node:
     * he memory element is a container for all memory related properties.
     * Currently, the only supported property is specified in the stateSize element.
     * The stateSize element has one required attribute. This attribute, max, specifies the maximum size (in bits)
     * of the state of the specified actor on the specified processor type during the firing of the actor.
     * TODO stateSize calculation, refined by CodeSize??
     * @param node SDF node
     */
    private void _assignMemory(CSDFNode node){
        prefixInc();
        _printStream.println(_prefix + "<memory>");
        prefixInc();
        int   stateSize = 0;
        try{ stateSize = RefinedCSDFEvaluator.getInstance().evalInternalMemory(node); }
        catch (Exception e){}
        _printStream.println(_prefix + "<stateSize max='" + stateSize + "'/>");
        prefixDec();
        _printStream.println(_prefix + "</memory>");
        prefixDec();
    }

    /**
     * Set mapping of the SDF graph nodes on processors
     * @param _nodeProcessorsMapping mapping of the SDF graph nodes on processors
     */
    public void setNodeProcessorsMapping(HashMap<String, String> _nodeProcessorsMapping) {
        this._nodeProcessorsMapping = _nodeProcessorsMapping;
    }

    /**
     * Get mapping of the SDF graph nodes on processors
     * @return mapping of the SDF graph nodes on processors
     */
    public HashMap<String, String> getNodeProcessorsMapping() {
        return _nodeProcessorsMapping;
    }

    /**
     * Serializes vector of integers as a string of values,
     * separated by comma
     * @param vec vector of integers to be serialized
     * @return vector of integers, serialized as a string of values,
     * separated by comma
     */
    private String vecToCommaSeparatedStr(Vector<Integer> vec){
        StringBuilder result = new StringBuilder();

        if(vec!=null){
            int curElemId = 0;
            int commaBorderId = vec.size()-1;

            for(Integer val: vec){
                result.append(val);
                if(curElemId<commaBorderId)
                    result.append(",");
                curElemId++;
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ///
    /**Mapping of the SDFG nodes on processors <Node_name,Processor_name>*/
    private HashMap<String,String> _nodeProcessorsMapping = new HashMap<>();

    /** worst-case execution times. By default operation wcet = 1*/
    private HashMap<CSDFNode,Vector<Integer>> _wcet;
}
