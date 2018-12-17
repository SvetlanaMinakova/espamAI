package espam.datamodel.graph.cnn.neurons;

/** shows, that class process pads*/
/**
   * Pads are values, added to the beginning and ending along each axis.
   * in format [x1_begin, x2_begin...x1_end, x2_end,...],
   * where xi_begin the number of pixels added at the beginning of axis `i` and xi_end,
   * the number of pixels added at the end of axis `i`.
   * Pads should contain values >=0
   */
public interface PadsProcessor { }
