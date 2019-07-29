
package espam.datamodel.mapping;

import java.util.Iterator;
import java.util.Vector;

import espam.datamodel.graph.adg.ADGraph;
import espam.datamodel.graph.adg.ADGNode;
import espam.datamodel.platform.Platform;
import espam.datamodel.pn.Gate;
import espam.datamodel.pn.cdpn.CDProcessNetwork;
import espam.datamodel.pn.cdpn.CDChannel;
import espam.datamodel.pn.cdpn.CDProcess;
import espam.datamodel.platform.memories.Fifo;
import espam.visitor.MappingVisitor;

// import espam.datamodel.mapping.MProcess;


//////////////////////////////////////////////////////////////////////////
//// Mapping

/**
 * This class contains mapping information that shows how a process network
 * is mapped onto a platform.
 *
 * @author Todor Stefanov
 * @version  $Id: Mapping.java,v 1.3 2012/05/17 14:32:40 tzhai Exp $
 */

public class Mapping implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a mapping with a name
     *  and an empty processor list
     */
    public Mapping(String name) {
        _name = name;
        _processorList = new Vector();
        _fifoList = new Vector();
        _adg = new ADGraph("");
        _cdpn = new CDProcessNetwork("");
        _platform = new Platform("");
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      */
    public void accept(MappingVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this Mapping
     *
     * @return  a new instance of the Mapping.
     */
    public Object clone() {
        try {
            Mapping newObj = (Mapping) super.clone();
            newObj.setName(_name);
            newObj.setProcessorList( (Vector) _processorList.clone() );
            newObj.setFifoList( (Vector) _fifoList.clone() );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    
    /**
     *  Get the name of this mapping.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this mapping.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the processor list of Mapping.
     *
     * @return  the processor list
     */
    public Vector getProcessorList() {
        return _processorList;
    }
    
    /**
     *  Set the processor list of Mapping.
     *
     * @param  processorList The new list
     */
    public void setProcessorList( Vector processorList) {
        _processorList = processorList;
    }
    
    /**
     *  Get the fifo list of Mapping.
     *
     * @return  the fifo list
     */
    public Vector getFifoList() {
        return _fifoList;
    }
    
    /**
     *  Set the fifo list of Mapping.
     *
     * @param  fifoList The new list
     */
    public void setFifoList( Vector fifoList) {
        _fifoList = fifoList;
    }
    
    /**
     *  Get the ADG of Mapping.
     *
     * @return  the ADG
     */
    public ADGraph getADG() {
        return _adg;
    }
    
    /**
     *  Set the ADG of Mapping.
     *
     * @param  adg The new ADG
     */
    public void setADG( ADGraph adg) {
        _adg = adg;
    }
    
    /**
     *  Get the CDPN of Mapping.
     *
     * @return  the CDPN
     */
    public CDProcessNetwork getCDPN() {
        return _cdpn;
    }
    
    /**
     *  Set the CDPN of Mapping.
     *
     * @param  cdpn The new CDPN
     */
    public void setCDPN( CDProcessNetwork cdpn) {
        _cdpn = cdpn;
    }
    
    /**
     *  Get the Platform of Mapping.
     *
     * @return  the Platform
     */
    public Platform getPlatform() {
        return _platform;
    }
    
    /**
     *  Set the Platform of Mapping.
     *
     * @param  platform The new Platform
     */
    public void setPlatform( Platform platform) {
        _platform = platform;
    }
    
    
    /**
     *  Return a description of the Mapping.
     *
     * @return  a description of the Mapping.
     */
    public String toString() {
        return "Mapping: " + _name;
    }
    
    /**
     *  Return a processor which has a specific name. Return null if
     *  processor cannot be found.
     *
     * @param  name the name of the processor to search for.
     * @return  the processor with the specific name.
     */
    public MProcessor getProcessor(String name) {
        Iterator i;
        i = _processorList.iterator();
        while (i.hasNext()) {
            MProcessor processor = (MProcessor) i.next();
            if (processor.getName().contains(name)|| name.contains(processor.getName()))
                return processor;
        }
        return null;
    }
    
    /**
     *  Return a MFifo which mapping with a specific CDChannel. Return null if MFifo cannot
     *  be found.
     *
     * @param  name the CDChannel to search for.
     * @return  the MFifo which mapping with a specific CDChannel.
     */
    public MFifo getMFifo(CDChannel cdChannel) {
        String cdChannelName = cdChannel.getName();
        Iterator i;
        i = getFifoList().iterator();
        while (i.hasNext()) {
            MFifo mFifo = (MFifo) i.next();
            CDChannel mcdChannel = mFifo.getChannel(); 
            if (mcdChannel != null && mcdChannel.getName().equals(cdChannelName)) {
                return mFifo;
            }
        }
        return null;
    }
    
    /**
     *  Return a CDChannel which mapping with a specific Fifo. Return null if CDChannel cannot
     *  be found.
     *
     * @param  name the Fifo to search for.
     * @return  the CDChannel which mapping with a specific Fifo.
     */
    public CDChannel getCDChannel(Fifo fifo) {
        String fifoName = fifo.getName();
        Iterator i;
        i = getFifoList().iterator();
        while (i.hasNext()) {
            MFifo mFifo = (MFifo) i.next();
            Fifo mapFifo = mFifo.getFifo();
            if (mapFifo.getName().equals(fifoName)) {
                CDChannel cdChannel = mFifo.getChannel();
                return cdChannel;
            }
        }
        return null;
    }
    
    /**
     *  Return a MProcessor which mapping with a specific CDProcess. Return null if MProcessor cannot
     *  be found.
     *
     * @param  name the CDProcess to search for.
     * @return  the MProcessor which mapping with a specific CDProcess.
     */
    public MProcessor getMProcessor(CDProcess cdProcess) {
        Iterator i;
        i = cdProcess.getAdgNodeList().iterator();
        ADGNode adgNode = (ADGNode) i.next();
        String adgNodeName = adgNode.getName();
        
        Iterator j;
        i = getProcessorList().iterator();
        while (i.hasNext()) {
            MProcessor mProcessor = (MProcessor) i.next();
            j = mProcessor.getProcessList().iterator();
            while (j.hasNext()) {
                MProcess mProcess = (MProcess) j.next();
                String madgNodeName = mProcess.getNode().getName();
                if (madgNodeName.equals(adgNodeName)) {
                    return mProcessor;
                }
            }
        }
        return null;
    }
    
    /**
     *  Return a CDProcess which is mapped onto the given mProcessor. Return null if
     *  process cannot be found.
     *
     * @param  mProcess.
     * @return  the CDprocess.
     */
    public CDProcess getCDProcess(MProcessor mProcessor) {
        CDProcess rt;
        
        Iterator processIt = mProcessor.getProcessList().iterator();
        while (processIt.hasNext()) {
            MProcess mProcess = (MProcess) processIt.next();
            ADGNode adgNode = mProcess.getNode();
            rt = _cdpn.getProcess(adgNode);
            return rt;
        }
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of a Mapping.
     */
    private String _name = null;
    
    /**
     *  List of the processors of Platform.
     */
    private Vector _processorList = null;
    
    /**
     *  List of MFifos. MFifo points to a Fifo and CDChannel mapped on it
     */
    private Vector _fifoList = null;
    
    /**
     *  Pointer to the ADG involved in a mapping.
     */
    private ADGraph _adg = null;
    
    /**
     *  Pointer to the CDPN involved in a mapping.
     */
    private CDProcessNetwork _cdpn = null;
    
    /**
     *  Pointer to the Platform involved in a mapping.
     */
    private Platform _platform = null;
    
}
