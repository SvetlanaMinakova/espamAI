#!/usr/bin/python

import sys

sys.path.append('../darts/')

from EDF import EDF
from ActorModel import ActorModel
from Processor import Processor

if __name__ == "__main__":

    a1 = ActorModel(None, 1, "a1", None, 6000)
    a1.deadline = 18000
    a1.period = 31000
    a1.start_time = 0

    a2 = ActorModel(None, 2, "a2", None, 2000)
    a2.deadline = 9000
    a2.period = 9800
    a2.start_time = 0

    a3 = ActorModel(None, 3, "a3", None, 1000)
    a3.deadline = 12000
    a3.period = 17000
    a3.start_time = 0

    a4 = ActorModel(None, 4, "a4", None, 90)
    a4.deadline = 3000
    a4.period = 4200
    a4.start_time = 0

    a5 = ActorModel(None, 5, "a5", None, 8)
    a5.deadline = 78
    a5.period = 96
    a5.start_time = 0

    a6 = ActorModel(None, 6, "a6", None, 2)
    a6.deadline = 16
    a6.period = 12
    a6.start_time = 0

    a7 = ActorModel(None, 7, "a7", None, 10)
    a7.deadline = 120
    a7.period = 280
    a7.start_time = 0

    a8 = ActorModel(None, 8, "a8", None, 26)
    a8.deadline = 160
    a8.period = 660
    a8.start_time = 0

    processor = Processor(1.0)
    processor.add_actor(a1)
    processor.add_actor(a2)
    processor.add_actor(a3)
    processor.add_actor(a4)
    processor.add_actor(a5)
    processor.add_actor(a6)
    processor.add_actor(a7)
    processor.add_actor(a8)

    qpa = EDF(processor.get_actors(), False)
    if qpa.is_schedulable():
        print("QPA Test: Schedulable")
    else:
        print("QPA Test: Unschedulable")
    print("Utilization = %s" % processor.get_utilization())
    print("Density = %s" % processor.get_density())

