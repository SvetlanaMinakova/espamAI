darts (Dataflow Analysis for Hard-Real-Time Scheduling)
=======================================================

darts is a framework for hard-real-time schedulability analysis of 
streaming applications modeled as dataflow graphs. 
Given a set of graphs, where each graph corresponds to an application, 
it performs the following:

* Constructing a complete task-set of asynchronous periodic tasks 
for each application
* Computing the maximum throughput and minimum latency of each 
application under strictly periodic scheduling
* Deriving the minimum number of processors needed to schedule 
the set of applications on homogeneous systems by different 
scheduling algorithms (e.g., Partitioned EDF and optimal)
* Generating platform and mapping specifications which can be 
used as input to the Daedalus^RT design flow

Currently, darts supports only acyclic Cyclo-Static Dataflow (CSDF) graphs.

Task-set Construction
---------------------
Given an acyclic CSDF graph, it derives an asynchronous set of periodic tasks. 
The deadline setting is customizable using a deadline factor (see below). 
The task-set derivation is implemented in `ACSDFModel.py`. 
To see how to use the script, run it with `-h` to see the help message. 

Computing the throughput and latency
------------------------------------
The minimum latency and maxmimum throughput are automatically computed 
during the task-set derivation. 
The latency is shown next to `Graph maximum latency` field. 
The throughput is the reciprocal of the output actor(s) period(s).

Platform and Mapping Specifications
-----------------------------------
The platform and mapping specifications can be found under different 
scheduling algorithms. 
The supported mapping are listed in the `supported_mappings` variable 
in `PlatformGenerator.py` which determins the platform and mapping 
specifications. To see how to use the script, run it with `-h` to see 
the help message. 

`PlatformGenerator.py` produces several files. 
The most important two are: platform specifications (`platform.pla`) 
and mapping specifications (`mapping.map`)

Graph Formats
-------------

darts accepts as an input the graphs in StreamIt Modified Format 
(called GPH Format) produced by [pntools](https://daedalus.liacs.nl/pntools/).
The GPH format is serialized as JSON. See the files under 
test/benchmarks/gph to see examples of the GPH format.

In order to facilitate conversion between different formats, the user 
can use the `CSDFParser.py` to convert from .gph format into the the 
XML format required by [SDF For Free](http://www.es.ele.tue.nl/sdf3/). 
To see how to use it, run it with `-h`.

Bugs/Questions
---------------
In case of bugs/questions, please contact [Mohamed Bamakhrama](mohamed@liacs.nl)

LICENSE
-------
This tool is released under the BSD license shown in LICENSE.md

