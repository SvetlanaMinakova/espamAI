package espam.datamodel.platform.processors;
/////////////// HWCE??? ///////////////////

import espam.visitor.PlatformVisitor;

/**
 * This class describes HWCE
 */
public class HWCE extends Processor{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a HWCE with a name.
     */
    public HWCE(String name) {
        super(name);
    }

    /** Accept a Visitor
     *  @param x A Visitor Object.
     *  @exception EspamException If an error occurs.
     */
    public void accept(PlatformVisitor x) {
        x.visitComponent(this);
    }

    /**
     *  Clone this HWCE
     *
     * @return  a new instance of the HWCE.
     */
    public Object clone() {
      HWCE newObj = (HWCE) super.clone();
      return( newObj );
    }

    /**
     *  Return a description of the HWCE.
     *
     * @return  a description of the HWCE.
     */
    public String toString() {
        return "HWCE: " + getName();
    }

}
