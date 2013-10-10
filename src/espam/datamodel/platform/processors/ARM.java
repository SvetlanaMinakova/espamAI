package espam.datamodel.platform.processors;

import java.util.Vector;

import espam.visitor.PlatformVisitor;
import espam.datamodel.EspamException;

//////////////////////////////////////////////////////////////////////////
//// ARM Processor

/**
 * This class describes ARM processor.
 *
 * @author Mohamed Bamakhrama
 * @version  $Id: ARM.java,v 1.1 2007/12/07 22:09:05 stefanov Exp $
 */

public class ARM extends Processor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a ARM processor with a name.
     *
     */
    public ARM(String name) {
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
     *  Clone this ARM processor
     *
     * @return  a new instance of the ARM processor.
     */
    public Object clone() {
            ARM newObj = (ARM) super.clone();
            return( newObj );
    }

    /**
     *  Return a description of the PowerPC processor.
     *
     * @return  a description of the PowerPC processor.
     */
    public String toString() {
        return "ARM processor: " + getName();
    }
}
