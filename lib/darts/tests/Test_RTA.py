#!/usr/bin/python

import sys
import copy
from operator import attrgetter

sys.path.append('../darts/')

from FPPS import FPPS
from ActorModel import ActorModel
from Processor import Processor

def test1():

    a1 = ActorModel(None, 1, "a1", None, 1)
    a1.deadline = 3
    a1.period = 4
    a1.start_time = 0

    a2 = ActorModel(None, 2, "a2", None, 1)
    a2.deadline = 4
    a2.period = 5
    a2.start_time = 0

    a3 = ActorModel(None, 3, "a3", None, 2)
    a3.deadline = 5
    a3.period = 6
    a3.start_time = 0

    a4 = ActorModel(None, 4, "a4", None, 1)
    a4.deadline = 10
    a4.period = 11
    a4.start_time = 0

#    a5 = ActorModel(None, 5, "a5", None, 8)
#    a5.deadline = 78
#    a5.period = 96
#    a5.start_time = 0

#    a6 = ActorModel(None, 6, "a6", None, 2)
#    a6.deadline = 16
#    a6.period = 12
#    a6.start_time = 0

#    a7 = ActorModel(None, 7, "a7", None, 10)
#    a7.deadline = 120
#    a7.period = 280
#    a7.start_time = 0

#    a8 = ActorModel(None, 8, "a8", None, 26)
#    a8.deadline = 160
#    a8.period = 660
#    a8.start_time = 0

    processor = Processor(1.0)
    processor.add_actor(a1)
    processor.add_actor(a2)
    processor.add_actor(a3)
    processor.add_actor(a4)
#    processor.add_actor(a5)
#    processor.add_actor(a6)
#    processor.add_actor(a7)
#    processor.add_actor(a8)

    fpps = FPPS(processor.get_actors())
    schedulable = fpps.is_schedulable()
    if schedulable:
        print("Schedulable")
    else:
        print("Unschedulable")
    print("Utilization = %s" % processor.get_utilization())
    print("Density = %s" % processor.get_density())

def test2():
    a1 = ActorModel(None, 1, "a1", None, 3)
    a1.deadline = 7
    a1.period = 7
    a1.start_time = 0

    a2 = ActorModel(None, 2, "a2", None, 2)
    a2.deadline = 12
    a2.period = 12
    a2.start_time = 0

    a3 = ActorModel(None, 3, "a3", None, 5)
    a3.deadline = 20
    a3.period = 20
    a3.start_time = 0

    processor = Processor(1.0)
    processor.add_actor(a1)
    processor.add_actor(a2)
    processor.add_actor(a3)

    fpps = FPPS(processor.get_actors())
    schedulable = fpps.is_schedulable()
    if schedulable:
        print("Schedulable")
    else:
        print("Unschedulable")
    print("Utilization = %s" % processor.get_utilization())
    print("Density = %s" % processor.get_density())

def test3():
    a1 = ActorModel(None, 1, "a1", None, 1)
    a1.deadline = 2
    a1.period = 3
    a1.start_time = 0

    a2 = ActorModel(None, 2, "a2", None, 2)
    a2.deadline = 3
    a2.period = 4
    a2.start_time = 2

    processor = Processor(1.0)
    processor.add_actor(a1)
    processor.add_actor(a2)

    fpps = FPPS(processor.get_actors())
    schedulable = fpps.is_schedulable()
    if schedulable:
        print("Schedulable")
    else:
        print("Unschedulable")
    print("Utilization = %s" % processor.get_utilization())
    print("Density = %s" % processor.get_density())

def test4():
    a1 = ActorModel(None, 1, "a1", None, 1)
    a1.deadline = 3
    a1.period = 5
    a1.start_time = 0

    a2 = ActorModel(None, 2, "a2", None, 2)
    a2.deadline = 4
    a2.period = 6
    a2.start_time = 11

    processor = Processor(1.0)
    processor.add_actor(a1)
    processor.add_actor(a2)

    fpps = FPPS(processor.get_actors())
    schedulable = fpps.is_schedulable()
    if schedulable:
        print("Schedulable")
    else:
        print("Unschedulable")
    print("Utilization = %s" % processor.get_utilization())
    print("Density = %s" % processor.get_density())

def test5():

    a1 = ActorModel(None, 1, "a1", None, 2)
    a1.deadline = 2
    a1.period = 10
    a1.start_time = 17

    a2 = ActorModel(None, 2, "a2", None, 1)
    a2.deadline = 2
    a2.period = 15
    a2.start_time = 0

    a3 = ActorModel(None, 3, "a3", None, 5)
    a3.deadline = 10
    a3.period = 22
    a3.start_time = 1

    a4 = ActorModel(None, 4, "a4", None, 5)
    a4.deadline = 20
    a4.period = 33
    a4.start_time = 6

    a5 = ActorModel(None, 5, "a5", None, 5)
    a5.deadline = 42
    a5.period = 42
    a5.start_time = 1

    a6 = ActorModel(None, 6, "a6", None, 7)
    a6.deadline = 47
    a6.period = 57
    a6.start_time = 19

    a7 = ActorModel(None, 7, "a7", None, 2)
    a7.deadline = 90
    a7.period = 90
    a7.start_time = 34

    a8 = ActorModel(None, 8, "a8", None, 3)
    a8.deadline = 120
    a8.period = 120
    a8.start_time = 36

    a9 = ActorModel(None, 9, "a9", None, 17)
    a9.deadline = 340
    a9.period = 345
    a9.start_time = 0

    a10 = ActorModel(None, 10, "a10", None, 2)
    a10.deadline = 700
    a10.period = 700
    a10.start_time = 0

    processor = Processor(1.0)
    processor.add_actor(a1)
#    processor.add_actor(a2)
    processor.add_actor(a3)
    processor.add_actor(a4)
    processor.add_actor(a5)
    processor.add_actor(a6)
    processor.add_actor(a7)
    processor.add_actor(a8)
    processor.add_actor(a9)
    processor.add_actor(a10)

    fpps = FPPS(processor.get_actors())
    schedulable = fpps.is_schedulable()
    if schedulable:
        print("Schedulable")
    else:
        print("Unschedulable")
    print("Utilization = %s" % processor.get_utilization())
    print("Density = %s" % processor.get_density())

def test6():
    print("test response time under mode change")
    
    a1 = ActorModel(None, 1, "a1", None, 10)
    a1.deadline = a1.period = 100
    a1.start_time = 0

    # a2 = ActorModel(None, 2, "a2", None, 1)
    # a2.deadline = 2
    # a2.period = 15
    # a2.start_time = 0

    a3 = ActorModel(None, 3, "a3", None, 30)
    a3.deadline = a3.period = 200
    a3.start_time = 0

    a4 = ActorModel(None, 4, "a4", None, 40)
    a4.deadline = a4.period = 280
    a4.start_time = 0

    a5 = ActorModel(None, 5, "a5", None, 50)
    a5.deadline = a5.period = 300
    a5.start_time = 0

    a6 = ActorModel(None, 6, "a6", None, 60)
    a6.deadline = a6.period = 350
    a6.start_time = 0

    processor = Processor(1.0)
    processor.add_param_actor(1, a1)
#    processor.add_actor(a2)
    processor.add_param_actor(1, a3)
    processor.add_param_actor(1, a4)
    processor.add_param_actor(1, a5)
    processor.add_param_actor(1, a6)
    print(len(processor))

    # processor.add_actor(a7)
    # processor.add_actor(a8)
    # processor.add_actor(a9)
    # processor.add_actor(a10)

    # schedulable = SchedulabilityAnalysis.FPPS_RTA(processor)
    # if schedulable:
    #     print("Schedulable")
    # else:
    #     print("Unschedulable")
    # print("Utilization = %s" % processor.get_utilization())
    # print("Density = %s" % processor.get_density())

def test7():
    a1 = ActorModel(None, 1, "a1", None, 20)
    a1.deadline = 50
    a1.period = 60
    a1.start_time = 0

    # a2 = ActorModel(None, 2, "a2", None, 1)
    # a2.deadline = 2
    # a2.period = 15
    # a2.start_time = 0

    a3 = ActorModel(None, 3, "a3", None, 20)
    a3.deadline = 70
    a3.period = 90
    a3.start_time = 0

    # a4 = ActorModel(None, 4, "a4", None, )
    # a4.deadline = 20
    # a4.period = 33
    # a4.start_time = 6

    a5 = ActorModel(None, 5, "a5", None, 30)
    a5.deadline = 140
    a5.period = 200
    a5.start_time = 0

    a6 = ActorModel(None, 6, "a6", None, 20)
    a6.deadline = 140
    a6.period = 200
    a6.start_time = 0

    processor = Processor(1.0)
    processor.add_actor(a1)
#    processor.add_actor(a2)
    processor.add_actor(a3)
    # processor.add_actor(a4)
    processor.add_actor(a5)
    processor.add_actor(a6)
    # processor.add_actor(a7)
    # processor.add_actor(a8)
    # processor.add_actor(a9)
    # processor.add_actor(a10)

    # schedulable = SchedulabilityAnalysis.FPPS_RTA(processor)
    # if schedulable:
    #     print("Schedulable")
    # else:
    #     print("Unschedulable")
    # print("Utilization = %s" % processor.get_utilization())
    # print("Density = %s" % processor.get_density())

    R_I = FPPS.compute_response_time(processor)
    print(R_I)

def test8():
    a1 = ActorModel(None, 1, "a1", None, 20)
    a1.deadline = 50
    a1.period = 60
    a1.start_time = 0

    a12 = copy.deepcopy(a1)
    # print(a12.actor_id)
    a12.wcet = 40
    a12.deadline = 90
    a12.period = 100
    a12.start_time = 0
    a12.offset = 0

    # a2 = ActorModel(None, 2, "a2", None, 1)
    # a2.deadline = 2
    # a2.period = 15
    # a2.start_time = 0

    a3 = ActorModel(None, 3, "a3", None, 20)
    a3.deadline = 70
    a3.period = 90
    a3.start_time = 0

    a32 = copy.deepcopy(a3)
    a32.wcet = 40
    a32.deadline = 80
    a32.period = 100
    a32.start_time = 0
    a32.offset = 0

    # a4 = ActorModel(None, 4, "a4", None, )
    # a4.deadline = 20
    # a4.period = 33
    # a4.start_time = 6

    # a5 = ActorModel(None, 5, "a5", None, 30)
    # a5.deadline = 140
    # a5.period = 200
    # a5.start_time = 0

    processor = Processor(1.0)
    processor.add_param_actor(1, a1)
    processor.add_param_actor(1, a3)
    processor.add_param_actor(2, a12)
    processor.add_param_actor(2, a32)
    R_lst = FPPS.compute_fpps_rta_mc(processor, 1, 2)
    print(R_lst)

if __name__ == "__main__":
    # Reference results:
    # [19, 8, 20, 20, 59, 90, 116, 197, 218]
    # [0, 2, 9, 14, 33, 54, 77, 180, 216]
    # Schedulable
    # Utilization = 0.919997242778
    # Density = 2.11806315434 
    # test5()

    # test6()

    # Example used in Pedro's thesis (P106)
    # old mode task
    # [20, 40, 90]
    # [0, 20, 60]
    # Schedulable
    # Utilization = 0.705555555556
    # Density = 0.9
    # test7()

    test8()





