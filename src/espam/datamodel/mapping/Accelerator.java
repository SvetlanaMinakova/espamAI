package espam.datamodel.mapping;

/** class describe an Accelerator*/
public class  Accelerator extends MProcessor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Constructor to create a Accelerator with a name.
     */
    public Accelerator(String name) {
        super(name);
    }


    /**
     *  Clone this Accelerator
     *
     * @return  a new instance of the Accelerator.
     */
    public Object clone() {
        Accelerator newObj = (Accelerator) super.clone();
        return( newObj );
    }

    /**
     *  Return a description of the Accelerator.
     *
     * @return  a description of the Accelerator.
     */
    public String toString() {
        return "Accelerator: " + getName();
    }

    /**
     * Set host processor, launching kernels on accelerator
     * @param host host processor, launching kernels on accelerator
     */
    public void setHost(MProcessor host) {
        this._host = host;
    }

    /**
     * Get host processor, launching kernels on accelerator
     * @return host processor, launching kernels on accelerator
     */
    public MProcessor getHost() {
        return _host;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                 ////
    protected MProcessor _host;

}
