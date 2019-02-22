#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Produces off-lines schedules'

import sys
import heapq
import os.path
import math
from operator import attrgetter

import Utilities


def dump_schedule_to_grasp(path, filename, processor, schedule):
    of = open(os.path.join(path, filename), "w")
    
    # determine scaling factor if the schedule is 'long' (for fast GRASP visualization)
    max_t = schedule[-1][0]
    graph_scaling = math.pow( 10, (math.floor(math.log10(max_t)-4)) )
    if graph_scaling<10:
        graph_scaling = 1
    
    # create header of the grasp file
    n_actors = len(processor.get_actors())
    i = 1
    for actor in processor.get_actors():
        of.write("newTask task_" + str(actor.get_global_id()) + \
                 " -priority " + str(actor.get_priority()) + \
                 " -name \"" + actor.get_name() + "\" -color grey" + \
                 str((i*100)//n_actors) + "\n")
        i+=1
    of.write("\n")
    
    # dump all schedule events
    job_id = {i : 0 for i in processor.get_actors()}
    for entry in schedule:
        if entry[1]=="ARR":
            of.write("plot " + str(entry[0]//graph_scaling) + \
                     " jobArrived job"+str(entry[2].get_global_id())+"."\
                     +str(job_id[entry[2]])+ \
                     " task_"+str(entry[2].get_global_id()) + "\n" )
        elif entry[1]=="RUN":
            of.write("plot " + str(entry[0]//graph_scaling) + \
                     " jobResumed job"+str(entry[2].get_global_id())+"."\
                     +str(job_id[entry[2]]) + "\n" )
        elif entry[1]=="PRE":
            of.write("plot " + str(entry[0]//graph_scaling) + \
                     " jobPreempted job"+str(entry[2].get_global_id())+"."\
                     +str(job_id[entry[2]]) + "\n" )
        elif entry[1]=="FIN":
            of.write("plot " + str(entry[0]//graph_scaling) + \
                     " jobCompleted job"+str(entry[2].get_global_id())+"."\
                     +str(job_id[entry[2]]) + "\n" )
            job_id[entry[2]]+=1
            
    of.flush()    
    of.close()


def dump_schedule_to_txt(path, filename, schedule):
    of = open(os.path.join(path, filename), "w")
    of.flush()
    of.write("\n\n")
    of.write("Time\t\tStatus\t\tTask\n")
    t = 0
    while len(schedule) > 0:
        if t == schedule[0][0]:
            while len(schedule) > 0 and t == schedule[0][0]:
                e = schedule.pop(0)
                of.write(str(e[0]) + "\t\t" + e[1] + "\t\t" + e[2].get_name() + "\n")
        #else:
        #    of.write(str(t) + "\n")
        t += 1
    of.flush()    
    of.close()

class EDF:
    """
    Builds a static schedule for a set of actors using the Earliest Deadline First
    Scheduling policy.
    """
    def __init__(self, output_dir, processor, processor_id=1):
        """
        @param processor is a Processor object containing tasks.
        By this point, we assume that SchedulabilityAnalysis has been used
        to check the feasibility and all the actors in processor are schedulable.
        """
        assert processor != None and len(processor) > 0
        assert os.path.isdir(output_dir)
        self.output_dir = output_dir
        self.ready_queue = []
        self.waiting_queue = []
        self.last_actor = None
        self.processor = processor
        self.processor_id = processor_id
        self.schedule_filename = "processor_%s.schedule" % str(self.processor_id)

    def clear(self):
        self.ready_queue = []
        self.waiting_queue = []
        self.last_actor = None
    
    def compute_start_end_times(self):
        period_vec = []
        start_time_vec = []
        for actor in self.processor.get_actors():
            period_vec.append(actor.get_period())
            start_time_vec.append(actor.get_start_time())
            actor.abs_release = actor.get_start_time()
            actor.abs_deadline = actor.abs_release + actor.get_deadline()
        start_time = min(start_time_vec)
        end_time = max(start_time_vec) + 6*int(Utilities.lcmv(period_vec))
        return (start_time, end_time)            

    def schedule(self):
        """
        @return a list of tuples containing the schedule, where each tuple 
        is (t, actor). t is the time at which the actor is activated. If actor is None, 
        then this means an idle interval
        """
        schedule = []
        start, end = self.compute_start_end_times()
#         print("start = %s, end = %s" % (start, end))

        # Initially, all tasks are in the waiting queue
        for actor in self.processor.get_actors():
            self.waiting_queue.append((actor.abs_release, actor))

        # Build the schedule over min{S_i} + 2*H, where H is the hyperperiod
        t = start
        running_actor = None
        while t <= end:
            # Sort according to release times
            heapq.heapify(self.waiting_queue)
            while len(self.waiting_queue) > 0:
                if self.waiting_queue[0][0] <= t:
                    actor = self.waiting_queue.pop(0)[1]
                    self.ready_queue.append((actor.abs_deadline, actor))
                    schedule.append((t, "ARR", actor))
                else:
                    break
            # all release actors are now in the ready queue
            heapq.heapify(self.ready_queue)

            # One assertion to ensure that all deadlines are met
            for abs_deadline, actor in self.ready_queue:
                assert actor.abs_deadline >= t

            # check if we have tasks to schedule
            if len(self.ready_queue) > 0:
                if running_actor != None and self.ready_queue[0][1] != running_actor:
                    schedule.append((t, "PRE", running_actor))
                running_actor = self.ready_queue[0][1]
                schedule.append((t, "RUN", running_actor))

            # Compute the advancement of t
            if len(self.waiting_queue) > 0:
                next_waiting_wakeup = self.waiting_queue[0][0]
            else:
                next_waiting_wakeup = sys.maxsize

            if len(self.ready_queue) > 0:
                next_ready_wakeup = self.ready_queue[0][0]
            else:
                next_ready_wakeup = sys.maxsize

            if running_actor != None:
                next_running_event = t + running_actor.get_wcet()
            else:
                next_running_event = sys.maxsize

            # compute the next t
            next_t = min(next_running_event, next_waiting_wakeup, next_ready_wakeup)

            # compute how much cpu time did running_actor receive
            if running_actor != None:
                running_actor.received_time += (next_t - t)
                if running_actor.received_time == running_actor.get_wcet():
                    schedule.append((next_t, "FIN", running_actor))
                    running_actor.received_time = 0
                    running_actor.abs_release = running_actor.abs_release + running_actor.get_period()
                    running_actor.abs_deadline = running_actor.abs_deadline + running_actor.get_period()
                    self.ready_queue.pop(0)
                    self.waiting_queue.append((running_actor.abs_release, running_actor))
                    running_actor = None
                
            t = next_t

#         dump_schedule_to_txt(self.output_dir, self.schedule_filename, schedule)
        dump_schedule_to_grasp(self.output_dir, self.schedule_filename, self.processor, schedule)
        return schedule

class FPPS:
    """
    Builds a static schedule for a set of actors using the Fixed-Priority Preemptive
    Scheduling policy.
    """
    def __init__(self, output_dir, processor, processor_id=1):
        """
        @param processor is a Processor object containing tasks.
        By this point, we assume that SchedulabilityAnalysis has been used
        to check the feasibility and all the actors in processor are schedulable.
        """
        assert processor != None and len(processor) > 0
        assert os.path.isdir(output_dir)
        self.output_dir = output_dir
        self.ready_queue = []
        self.waiting_queue = []
        self.last_actor = None
        self.processor = processor
        self.processor_id = processor_id
        self.schedule_filename = "processor_%s.schedule" % str(self.processor_id)

    def clear(self):
        self.ready_queue = []
        self.waiting_queue = []
        self.last_actor = None

    def compute_start_end_times(self):
        period_vec = []
        start_time_vec = []
        for actor in self.processor.get_actors():
            period_vec.append(actor.get_period())
            start_time_vec.append(actor.get_start_time())
            actor.abs_release = actor.get_start_time()
            actor.abs_deadline = actor.abs_release + actor.get_deadline()
        start_time = min(start_time_vec)
        end_time = max(start_time_vec) + 6*int(Utilities.lcmv(period_vec))
        return (start_time, end_time)

    def schedule(self):
        """
        @return a list of tuples containing the schedule, where each tuple 
        is (t, actor). t is the time at which the actor is activated. If actor is None, 
        then this means an idle interval
        """
        schedule = []
        start, end = self.compute_start_end_times()
#         print("start = %s, end = %s" % (start, end))

        # Set the priority of each actor using to the Deadline Monotonic (DM) rule
        sorted_actors = sorted(self.processor.get_actors(), key = attrgetter('deadline'), reverse = True)
        priority = 1
        for actor in sorted_actors:
            actor.set_priority(priority)
            priority += 1

        # Initially, all tasks are in the waiting queue
        for actor in self.processor.get_actors():
            self.waiting_queue.append((actor.abs_release, actor))

        # Build the schedule over min{S_i} + 2*H, where H is the hyperperiod
        t = start
        running_actor = None
        while t <= end:
            # Sort according to release times
            heapq.heapify(self.waiting_queue)
            while len(self.waiting_queue) > 0:
                if self.waiting_queue[0][0] <= t:
                    actor = self.waiting_queue.pop(0)[1]
                    self.ready_queue.append((actor.priority, actor))
                    schedule.append((t, "ARR", actor))
                else:
                    break
            # all release actors are now in the ready queue
            heapq.heapify(self.ready_queue)

            # One assertion to ensure that all deadlines are met
            for priority, actor in self.ready_queue:
                assert actor.abs_deadline >= t

            # check if we have tasks to schedule
            if len(self.ready_queue) > 0:
                if running_actor != None and self.ready_queue[0][1] != running_actor:
                    schedule.append((t, "PRE", running_actor))
                running_actor = self.ready_queue[0][1]
                schedule.append((t, "RUN", running_actor))

            # Compute the advancement of t
            if len(self.waiting_queue) > 0:
                next_waiting_wakeup = self.waiting_queue[0][0]
            else:
                next_waiting_wakeup = sys.maxsize

            if running_actor != None:
                next_running_event = t + running_actor.get_wcet()
            else:
                next_running_event = sys.maxsize

            # compute the next t
            next_t = min(next_running_event, next_waiting_wakeup)

            # compute how much cpu time did running_actor receive
            if running_actor != None:
                running_actor.received_time += (next_t - t)
                if running_actor.received_time == running_actor.get_wcet():
                    schedule.append((next_t, "FIN", running_actor))
                    running_actor.received_time = 0
                    running_actor.abs_release = running_actor.abs_release + running_actor.get_period()
                    running_actor.abs_deadline = running_actor.abs_deadline + running_actor.get_period()
                    self.ready_queue.pop(0)
                    self.waiting_queue.append((running_actor.abs_release, running_actor))
                    running_actor = None
                
            t = next_t

#         dump_schedule_to_txt(self.output_dir, self.schedule_filename, schedule)
        dump_schedule_to_grasp(self.output_dir, self.schedule_filename, self.processor, schedule)
        return schedule

