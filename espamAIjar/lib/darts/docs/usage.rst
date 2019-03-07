============
Usage
============
In this chapter, we explain how to use darts to perform analysis on CSDF graphs.

Creating Graphs
---------------
darts supports creating graphs in the following ways:

    * Provide the graph in StreamIt extended graph format (.gph).
      We adapted this format from the `StreamIt project
      <http://groups.csail.mit.edu/cag/streamit/>`__. The original format as
      defined by MIT did not support CSDF graphs. So, we extended it to support
      CSDF graphs. The format is defined using the following Backus-Naur Form
      (BNF) grammer

        .. include:: gph.bnf


      An example of a valid .gph file descring an SDF graph is shown below:

        .. include:: pipeline_example.gph

    * The second option is to provide the graph in 
      `SDF For Free <http://www.es.ele.tue.nl/sdf3/>`__ format. SDF For Free
      uses XML format to define the graphs.

    * The third option is to construct the graph manually through a Python
      script. **TODO**: Add an example
    
    * The fourth option is to construct the graph by first specifying 
      a static affine-nested loop program and then using pntools.
    
    
