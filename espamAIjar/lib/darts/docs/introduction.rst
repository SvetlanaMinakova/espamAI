============
Introduction
============
darts (stands for Dataflow Analysis for Real-Time Scheduling) is a tool-set 
to perform scheduling analysis on applications modeled as acyclic 
Synchronous Data-Flow ([LM1987]_) and Cyclo-Static Dataflow (CSDF [BELP1996]_) graphs.


Features
--------
darts accepts as input a set of CSDF graphs and computes the following:

    * The period, deadline, and start time of each actor
    * The buffer size of each communication channel
    * The minimum number of processor needed to schedule the actors 
      using a given allocationa algorithm and a given scheduling algorithm.
As a result, the set of CSDF graphs is amenable to any real-time scheduling algorithm
that can be applied to asynchronous tasksets.

Currently darts supports the following scheduling algorithms:

    1. Earliest Deadline First (EDF)
    2. Rate Monotonic (RM)

darts supports the following schedulability tests: **TODO

darts supports the following allocation algorithms: **TODO

Besides the main functionality as scheduling analysis tool, darts allows the
user to also perform the following operations on CSDF graphs:

    * Compute the repetition vector of the graph
    * Unfold SDF graphs to produce equivalent CSDF graphs.
    The unfolded graphs can be then exported into textual format,
    or analyzed to determine their scheduling parameters.

.. [LM1987] Edward A. Lee and David G. Messerschmitt, Synchronous data flow, 
    Proceedings of the IEEE , vol. 75, no. 9, pp. 1235--1245, September 1987.
    DOI: `10.1109/PROC.1987.13876 <http://dx.doi.org/10.1109/PROC.1987.13876>`__

Limitations
-----------
Currently, darts has the following limitations:

    * Truly cyclic graphs are not supported. However, note that graphs
      with self-cycles only are supported, since the self-cycles limit
      auto-concurrency only.
    * No support for converting CSDF <-> SDF
    * No support for computing the self-timed schedule


Known issues
-----------
At the moment, darts has the following issues:
    
    * Computing least common multiple involves high running time. A possible solution
      is offload this part in C.
