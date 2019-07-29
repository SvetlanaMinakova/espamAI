package espam.datamodel.platform.processors;

//////////////////////////////////////////////////////////////////////////
//// Graphics Processing Unit (GPU)

import espam.datamodel.EspamException;
import espam.visitor.PlatformVisitor;

/**
 * This class describes GPU
 */
public class GPU extends Processor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a GPU with a name.
     */
    public GPU(String name) {
        super(name);
    }

    /**
     *  Constructor to create a GPU with a name and cores number.
     */
    public GPU(String name, Integer cores) {
        super(name);
        _cores = cores;
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }

    /**
     *  Clone this GPU
     *
     * @return  a new instance of the GPU.
     */
    public Object clone() {
      GPU newObj = (GPU) super.clone();
      newObj.setCores(_cores);
      return( newObj );
    }

    /**
     *  Return a description of the GPU.
     *
     * @return  a description of the GPU.
     */
    public String toString() {
        return "GPU: " + getName();
    }

    /**
     * Get GPU cores number
     * @return GPU cores number
     */
    public Integer getCores() {
        return _cores;
    }

    /**
     * Set GPU cores number
     * @param cores GPU cores number
     */
    public void setCores(Integer cores) {
        this._cores = cores;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                 ////
    private Integer _cores;
}
