package espam.datamodel.mapping.DNNMapping;
/** Common DNN mapping types
 * The DNN mapping type determines the mapping of DNN nodes onto a target platform:
 *
 *  SEQUENTIAL:
 *      CNN nodes are executed sequentially. Every CNN node, if possible, is offloaded on GPU for
 *      computations. Every GPU has one CPU (host) node, allocated for communication with GPU.
 *      This allocated CPU is used ONLY for communication with GPU, i.e., no no-GPU tasks are executed on this CPU.
 *      If node cannot be offloaded for computation on GPU, it is computed on CPUs, so that the node
 *      workload is spread on all CPUs, bedsides of the CPUs, allocated for communication with GPUs.
 *
 *  PIPELINE: (High throughput mode)
 *      CNN nodes are executed in parallel pipeline fashion. All available processors are
 *      utilized simultaneously.
 *
 *  CUSTOM: custom mapping provided
 *
 * */

public enum DNN_MAPPING_TYPE {
    SEQUENTIAL, PIPELINE
}
