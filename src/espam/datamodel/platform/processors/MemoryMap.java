
package espam.datamodel.platform.processors;

import java.util.Vector;

import espam.datamodel.platform.Port;

//////////////////////////////////////////////////////////////////////////
//// Memory Map

/**
 * This class describes a memory map of a processor.
 *
 * @author Todor Stefanov
 * @version  $Id: MemoryMap.java,v 1.1 2007/12/07 22:09:06 stefanov Exp $
 */

public class MemoryMap implements Cloneable {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     *  Constructor to create a memory map with a name and an empty
     *  portList and pageList.
     */
    public MemoryMap(String name) {
        _name = name;
        _pageList = new Vector();
    }
    
    /** Accept a Visitor
      *  @param x A Visitor Object.
      *  @exception EspamException If an error occurs.
      */
    //public void accept(Visitor x) throws EspamException { }
    
    /**
     *  Clone this memory map
     *
     * @return  a new instance of the memory map.
     */
    public Object clone() {
        try {
            MemoryMap newObj = (MemoryMap) super.clone();
            newObj.setName( _name );
            newObj.setPort( _port );
            newObj.setPageList( (Vector) _pageList.clone() );
            return (newObj);
        }
        catch( CloneNotSupportedException e ) {
            System.out.println("Error Clone not Supported");
        }
        return null;
    }
    
    /**
     *  Get the name of this memory map.
     *
     * @return  the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     *  Set the name of this memory map.
     *
     * @param  name The new name value
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     *  Get the port of this memory map.
     *
     * @return  the port
     */
    public Port getPort() {
        return _port;
    }
    
    /**
     *  Set the port of this memory map.
     *
     * @param  port The new port.
     */
    public void setPort(Port port) {
        _port = port;
    }
    
    /**
     *  Get the list of pages of this memory map.
     *
     * @return  the list of pages
     */
    public Vector getPageList() {
        return _pageList;
    }
    
    /**
     *  Set the list of pages of this memory map.
     *
     * @param  pageList The new list.
     */
    public void setPageList(Vector pageList) {
        _pageList = pageList;
    }
    
    /**
     *  Set the start addresses of of different memory segments of a memory map.
     *
     * @param  a segment.
     */
    public void setProgramMemorySegment( int programMemorySegment ) {
        _programMemorySegment = programMemorySegment;
    }
    
    public void setDataMemorySegment( int dataMemorySegment ) {
        _dataMemorySegment = dataMemorySegment;
    }
    
    public void setFifosReadSegment( int fifosReadSegment ) {
        _fifosReadSegment = fifosReadSegment;
    }
    
    public void setFifosWriteSegment( int fifosWriteSegment ) {
        _fifosWriteSegment = fifosWriteSegment;
    }
    
    public void setVirtualBufferSegment( int virtualBufferSegment ) {
        _virtualBufferSegment = virtualBufferSegment;
    }
    
    public void setPeripheralsSegment( int peripheralsSegment ) {
        _peripheralsSegment = peripheralsSegment;
    }
    
    /**
     *  Get the start addresses of of different memory segments of a memory map.
     *
     * @return  the segment
     */
    public int getProgramMemorySegment() {
        return _programMemorySegment;
    }
    
    public int getDataMemorySegment() {
        return _dataMemorySegment;
    }
    
    public int getFifosReadSegment() {
        return _fifosReadSegment;
    }
    
    public int getFifosWriteSegment() {
        return _fifosWriteSegment;
    }
    
    public int getVirtualBufferSegment() {
        return _virtualBufferSegment;
    }
    
    public int getPeripheralsSegment() {
        return _peripheralsSegment;
    }
    
    /**
     *  Return a description of the memory map.
     *
     * @return  a description of the memory map.
     */
    public String toString() {
        return "Memory Map: " + _name;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     *  Name of the Memory Map.
     */
    private String _name = null;
    
    /**
     *  List of the ports to which this memory map corresponds.
     */
    private Port _port = null;
    
    /**
     *  List of the pages (segments) of this memory map
     */
    private Vector _pageList = null;
    
    /**
     *  Start addresses of different memory segments of a memory map
     */
    private int _programMemorySegment = 0;
    private int _dataMemorySegment = 0;
    private int _fifosReadSegment = 0;
    private int _fifosWriteSegment = 0;
    private int _virtualBufferSegment = 0;
    private int _peripheralsSegment = 0;
}
