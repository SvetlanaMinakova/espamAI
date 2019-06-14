#!/usr/bin/python

import sys

sys.path.append('../darts/')

from EDF import EDF
from ActorModel import ActorModel
from Processor import Processor

if __name__ == "__main__":

    a1 = ActorModel(None, 1, "a1", None, 6)
    a1.deadline = 6
    a1.period = 20
    a1.start_time = 0

    a2 = ActorModel(None, 2, "a2", None, 8)
    a2.deadline = 8
    a2.period = 60
    a2.start_time = 6

    a3 = ActorModel(None, 3, "a3", None, 12)
    a3.deadline = 12
    a3.period = 30
    a3.start_time = 0#26

#    a4 = ActorModel(None, 4, "a4", None, 20)
#    a4.deadline = 20
#    a4.period = 20
#    a4.start_time = 28

    processor = Processor(1.0)
    processor.add_actor(a1)
    processor.add_actor(a2)
#    processor.add_actor(a3)
#    processor.add_actor(a4)

    baruah = EDF(processor.get_actors(), False, True, True)
    if baruah.is_schedulable():
        print("Baruah Test: Schedulable")
    else:
        print("Baruah Test: Unschedulable")

    qpa = EDF(processor.get_actors(), False, True, False)
    if qpa.is_schedulable():
        print("QPA Test: Schedulable")
    else:
        print("QPA Test: Unschedulable")

    print("Utilization = %s" % processor.get_utilization())
    print("Density = %s" % processor.get_density())



