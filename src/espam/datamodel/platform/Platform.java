
package espam.datamodel.platform;

import espam.datamodel.platform.communication.AXICrossbar;
import java.util.Vector;
import java.util.Iterator;
import java.io.IOException;

import espam.datamodel.platform.host_interfaces.ADMXRCII;
import espam.datamodel.platform.host_interfaces.ADMXPL;
import espam.datamodel.platform.host_interfaces.XUPV5LX110T;
import espam.datamodel.platform.host_interfaces.ML505;
import espam.datamodel.platform.host_interfaces.ML605;
import espam.datamodel.platform.host_interfaces.ZedBoard;


import espam.datamodel.platform.processors.*;
import espam.visitor.PlatformVisitor;

//////////////////////////////////////////////////////////////////////////
//// Platform

/**
 * This class is a basic platform.
 *
 * @author Todor Stefanov
 * @version  $Id: Platform.java,v 1.2 2012/07/02 12:34:03 tzhai Exp $
 */

public class Platform extends Resource {
    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////
    public enum InterconnectionTYPE {AXI, PLB}
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a Platform with a name,
     *  empty resource list and empty link list
     */
    public Platform(String name) {
        super(name);
        _resourceList = new Vector();
        _linkList = new Vector();
    }
    
    
    public String getBoardName() {
		
		// Doing lazy initialization because we don't know when Resource that contains board is present
		if(_boardName == null){
			Iterator j = this.getResourceList().iterator();
			while (j.hasNext()) {
				Resource resource = (Resource)j.next();
				if( resource instanceof ADMXRCII ) {
					_boardName = "ADM-XRC-II";
				} else if( resource instanceof ADMXPL ) {
					_boardName = "ADM-XPL";
				} else if( resource instanceof XUPV5LX110T ) {
					_boardName = "XUPV5-LX110T";
				} else if( resource instanceof ML505 ) {
					_boardName = "ML505";
				} else if( resource instanceof ML605 ) {
					_boardName = "ML605";
				} else if( resource instanceof ZedBoard ) {
					_boardName = "ZedBoard";
				}
			}  
        }
        
        return _boardName;
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception MatParserException If an error occurs.
      */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }
    
    /**
     *  Clone this Platform
     *
     * @return  a new instance of the Platform.
     */
    public Object clone() {
        Platform newObj = (Platform) super.clone();
        newObj.setResourceList( (Vector) _resourceList.clone() );
        newObj.setLinkList( (Vector) _linkList.clone() );
        return( newObj );
    }
    
    /**
     *  Get the resource list of a Platform.
     *
     * @return  the resource list
     */
    public Vector getResourceList() {
        return _resourceList;
    }
    
    /**
     *  Set the resource list of a Platform.
     *
     * @param  resourceList The new list
     */
    public void setResourceList( Vector resourceList) {
        _resourceList = resourceList;
    }
    
    /**
     *  Get the link list of a Platform
     *
     * @return  the link list
     */
    public Vector getLinkList() {
        return _linkList;
    }
    
    /**
     *  Set the link list of a Platform
     *
     * @param  linkList The new list
     */
    public void setLinkList(Vector linkList) {
        _linkList = linkList;
    }
    
    /**
     *  Return a description of the Platform.
     *
     * @return  a description of the Platform.
     */
    public String toString() {
        return "Platform: " + getName();
    }
    
    /**
     *  Return a resource which has a specific name. Return null if
     *  resource cannot be found.
     *
     * @param  name the name of the resource to search for.
     * @return  the resource with the specific name.
     */
    public Resource getResource(String name) {
        Iterator i;
        i = _resourceList.iterator();
        while (i.hasNext()) {
            Resource resource = (Resource) i.next();
            if (resource.getName().equals(name)) {
                return resource;
            }
        }
        return null;
    }
    
    /**
     * Determine the type of interconection:
     *  TYPE_AXI
     *
     * @param  name the name of the resource to search for.
     * @return  the resource with the specific name.
     */
    public boolean getInterconnectionType(){
        assert _resourceList != null;
        
        Iterator i = _resourceList.iterator();
        while( i.hasNext() ) {
            Resource resource = (Resource) i.next();
            if( resource instanceof AXICrossbar ) {
                return true;
            }
        }
        return false;
    }

    /**TODO: update if other CPUs appear
     * Get list of cpu cores in the platform
     * @return list of cpu cores in the mapping
     */
    public Vector<Processor> getCPUList(){
        Vector<Processor> cpuList = new Vector<>();
        for (Object resObj: getResourceList()) {
            if(resObj instanceof Processor) {
                if(resObj instanceof ARM || resObj instanceof MicroBlaze){
                    Processor cpu = (Processor)resObj;
                    cpuList.add(cpu);
                }
            }
        }
        return cpuList;
    }

    /**
     * Get list of gpu cores in the platform
     * @return list of gpu cores in the mapping
     */
    public Vector<Processor> getGPUList(){
        Vector<Processor> gpuList = new Vector<>();
        for (Object resObj: getResourceList()) {
            if(resObj instanceof GPU){
                Processor gpu = (Processor)resObj;
                gpuList.add(gpu);
            }
        }
        return gpuList;
    }

    /**TODO: update if other FPGAs appear
     * Get list of fpgas in the platform
     * @return list of fpgas in the mapping
     */
    public Vector<Processor> getFPGAList(){
        Vector<Processor> fpgaList = new Vector<>();
        for (Object resObj: getResourceList()) {
            if(resObj instanceof HWCE){
                Processor fpga = (Processor)resObj;
                fpgaList.add(fpga);
            }
        }
        return fpgaList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  List of the resources of a Platform.
     */
    private Vector _resourceList = null;
    
    private String _boardName = null;
    
    /**
     *  List of the links of a Platform.
     */
    private Vector _linkList = null;
}
