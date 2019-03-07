#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Performs the allocation of actors onto processors'

import sys
import copy
from operator import attrgetter

from Processor import Processor
from EDF import EDF
from FPPS import FPPS


FFD_APPROX_RATIO = 11/9
"""FFD Approximation Ratio"""

def contention(partition, pg):
    """Compute the contention for a given mapping

    Args:
        *partition*: The mapping of the actors onto processors.
        *pg*: A :class:`PlatformGenerator.PlatformGenerator` object containing 
        the graphs to be mapped onto the platform.
    """
    channels = pg.get_mapped_channels()
    gamma =[[0 for i in range(len(partition))] for j in range(len(partition))]
    for c in channels:
        gamma[c[0]][c[1]] = 1
    max_f = 0
    for r in gamma:
        s = sum(r)
        if s > max_f:
            max_f = s
    if max_f > 1:
        return max_f
    else:
        return 0

def update_and_verify(partition, pg):

    if partition != None and sum(map(len, partition)) == pg.get_num_of_actors():
        pg.update_graphs(partition)
        # Check schedulability again
        all_schedulable = True
        for processor in partition:
            if pg.get_sched_algo() == "FPPS":
                sched = FPPS(processor, not pg.is_constrained_deadline(),
                                False, False)
            elif pg.get_sched_algo() == "EDF":
                sched = EDF(processor, not pg.is_constrained_deadline(),
                                True, False)
            schedulable = sched.is_schedulable()
            if not schedulable:
                all_schedulable = False
                break
        if all_schedulable:
            return partition
    return None

def ffd(pg):
    """Performs First-Fit Decreasing allocation of the actors in the given 
    :class:`PlatformGenerator.PlatformGenerator` object. 

    Args:
        A :class:`PlatformGenerator.PlatformGenerator` object containing 
        the graphs to be mapped onto the platform.

    Returns:
        A partition describing the mapping of actors to processors.
        The partition is a list of :class:`Processor.Processor` objects. 
        The number of the entries in the list is the minimum number of 
        processors required by the allocation algorithm.

    """
    m = pg.get_min_num_of_processors()
    # sort actors according to utilization in descending order
    actors = pg.get_actors()
    sorted_actors = sorted(actors, key = attrgetter('utilization'), reverse = True)
    partition = None
    while partition == None:
        partition = [Processor(1.0) for i in range(m)]
        for actor in sorted_actors:
            allocated = False
            for processor in partition:
                if processor.can_fit(actor):
                    allocated = True
                    taskset = copy.copy(processor.get_actors())
                    taskset.add(actor)
                else:
                    allocated = False
                    continue

                if pg.get_sched_algo() == "FPPS":
                    sched = FPPS(taskset, not pg.is_constrained_deadline())
                elif pg.get_sched_algo() == "EDF":
                    sched = EDF(taskset, not pg.is_constrained_deadline())
                else:
                    sys.stderr.write("Unsupported scheduling algorithm!\n")
                    assert False

                schedulable = sched.is_schedulable()
                if schedulable:
                    processor.add_actor(actor)
                    allocated = True
                    break
                else:
                    allocated = False
            if not allocated:
                partition = None
                break
        if partition != None:
            break
        m = m + 1
    print(partition)
    return partition


def wf(pg):
    """Performs Worst-Fit allocation of the actors in the given 
    :class:`PlatformGenerator.PlatformGenerator` object. 

    Args:
        A :class:`PlatformGenerator.PlatformGenerator` object containing 
        the graphs to be mapped onto the platform.

    Returns:
        A partition describing the mapping of actors to processors.
        The partition is a list of :class:`Processor.Processor` objects. 
        The number of the entries in the list is the minimum number of 
        processors required by the allocation algorithm.

    """
    m = pg.get_min_num_of_processors()
    partition = None
    while partition == None:
        partition = [Processor(1.0) for i in range(m)]
        for actor in pg.get_actors():
            allocated = False
            # Sort the partitions according to utilization in ascending order
            sorted_partition = sorted(partition, key = attrgetter('utilization'), reverse = True)
            for i in range(0, len(sorted_partition)):
                
                if sorted_partition[i].can_fit(actor):
                    taskset = copy.copy(sorted_partition[i].get_actors())
                    taskset.add(actor)
                    allocated = True
                else:
                    allocated = False
                    continue

                if pg.get_sched_algo() == "FPPS":
                    sched = FPPS(taskset, not pg.is_constrained_deadline())
                elif pg.get_sched_algo() == "EDF":
                    sched = EDF(taskset, not pg.is_constrained_deadline())
                else:
                    sys.stderr.write("Unsupported scheduling algorithm!\n")
                    assert False

                schedulable = sched.is_schedulable()
                if schedulable:
                    sorted_partition[i].add_actor(actor)
                    allocated = True
                    break
                else:
                    allocated = False
            if not allocated:
                partition = None
                break
        if partition != None:
            break
        m = m + 1
    print(partition)
    return partition


def ff(pg):
    """Performs First-Fit allocation of the actors in the given 
    :class:`PlatformGenerator.PlatformGenerator` object. 

    Args:
        A :class:`PlatformGenerator.PlatformGenerator` object containing 
        the graphs to be mapped onto the platform.

    Returns:
        A partition describing the mapping of actors to processors.
        The partition is a list of :class:`Processor.Processor` objects. 
        The number of the entries in the list is the minimum number of 
        processors required by the allocation algorithm.

    """
    m = pg.get_min_num_of_processors()
    partition = None
    while partition == None:
        partition = [Processor(1.0) for i in range(m)]
        for actor in pg.get_actors():
            allocated = False
            for processor in partition:

                if processor.can_fit(actor):
                    allocated = True
                    taskset = copy.copy(processor.get_actors())
                    taskset.add(actor)
                else:
                    allocated = False
                    continue

                if pg.get_sched_algo() == "FPPS":
                    sched = FPPS(taskset, not pg.is_constrained_deadline())
                elif pg.get_sched_algo() == "EDF":
                    sched = EDF(taskset, not pg.is_constrained_deadline())
                else:
                    sys.stderr.write("Unsupported scheduling algorithm!\n")
                    assert False

                schedulable = sched.is_schedulable()
                if schedulable:
                    processor.add_actor(actor)
                    allocated = True
                    break
                else:
                    allocated = False
            if not allocated:
                partition = None
                break
        if partition != None:
            break
        m = m + 1
    print(partition)
    return partition


