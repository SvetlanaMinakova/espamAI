#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Schedulability Analysis for EDF'

import math
import sys
from operator import attrgetter

import Utilities

class EDF:

    def __init__(self, taskset, is_implicit_deadline=False, 
                                   is_async=True, use_exact_for_async=False):
        assert taskset != None and type(taskset) == set
        self.taskset = taskset
        self.is_implicit_deadline = is_implicit_deadline
        self.is_async = is_async
        self.use_exact_for_async = use_exact_for_async
   

    @staticmethod
    def dbf(task, t_start, t_end, is_synchronous=False):
        """
        Demand bound function for a single task 
        defined by Baruah in 1990 J. RTS article
        """
        assert t_end >= t_start

        if is_synchronous:
            S = 0
        else:
            S = task.get_start_time()

        P = task.get_period()
        D = task.get_deadline()

        term1 = int(math.floor(float(t_end - S - D)/float(P))) 
        term2 = max(0, int(math.ceil(float(t_start - S)/float(P))))
        eta = max(0, term1 - term2  + 1)

        return task.get_wcet() * eta


    def cdbf(self, t_start, t_end, is_synchronous=False):
        """Cumulative DBF for a set of tasks"""
        total_demand = 0
        for task in self.taskset:
            total_demand += EDF.dbf(task, t_start, t_end, is_synchronous)
        return total_demand


    def edf_ll_test(self):
        """
        EDF (for implicit-deadline tasks) with Liu & Layland (LL) famous test: 
        U_total <= 1.0
        """
        U_total = sum(map(attrgetter('utilization'), self.taskset))
        if U_total > 1.0:
            return False
        else:
            return True


    def edf_baruah_test(self):
        """
        EDF exact test proposed by Baruah in 1990 RTS article
        """
        S_min = sys.maxint
        S_max = -1
        periods = []
        for task in self.taskset:
            periods.append(task.get_period())
            if task.get_start_time() > S_max:
                S_max = task.get_start_time()
            if task.get_start_time() < S_min:
                S_min = task.get_start_time()
        H = Utilities.lcmv(periods)

        for t1 in range(S_min, S_max + 2*H + 1):
            for t2 in range(t1, S_max + 2*H + 1):
                demand = self.cdbf(t1, t2)
                if demand > (t2 - t1):
                    sys.stderr.write("Deadline miss in: [%s, %s]\n" % (t1, t2))
                    return False
        return True


    def qpa_compute_L_a(self):
        """Compute L_a, Eq. 4 in QPA TC paper"""
        deadlines = set()
        last_term = 0
        for task in self.taskset:
            deadlines.add(task.get_deadline())
            last_term += (task.get_period() - task.get_deadline()) * \
                         task.get_utilization()
        U_total = sum(map(attrgetter('utilization'), self.taskset))
        L_a = max(reduce(max,deadlines), int(math.ceil(float(last_term)/ \
              float(1.0 - U_total))))
        return L_a

    def qpa_compute_L_b(self):
        """Compute L_b in the QPA TC paper"""
        w_old = 0
        w_new = 0
        L_b = -1 # Invalid value        
        for task in self.taskset:
            w_old += task.get_wcet()

        while True:
            tmp = 0
            for task in self.taskset:
                tmp += int(math.ceil(float(w_old)/float(task.get_period())))*\
                       task.get_wcet()
            w_new = tmp
            if w_new == w_old:
                L_b = w_new
                return L_b
            w_old = w_new

    @staticmethod
    def absolute_deadline(task, t, is_synchronous=False):
        """Compute d_i in the QPA TC paper"""
        if is_synchronous:
            S = 0
        else:
            S = task.get_start_time()

        D = task.get_deadline()
        P = task.get_period()
        d_i = S + int(math.floor(float(t - S - D)/float(P))) * P + D
        return d_i

    def qpa_d_max(self, t):
        """Compute d_max in the QPA TC paper"""
        d_max = 0
        for task in self.taskset:
            if task.get_deadline() < t:
                d_i = EDF.absolute_deadline(task, t, True)
                if d_i == t:
                    d_i -= task.get_period()
                if d_i > d_max:
                    d_max = d_i
        return d_max

    def edf_qpa_test(self):
        """QPA algorithm - top level"""

        U_total = sum(map(attrgetter('utilization'), self.taskset))
        print("U_total = %s" % U_total)
        L_b = self.qpa_compute_L_b()
        print("L_b = %s" % L_b)
        if U_total < 1.0:
            L_a = self.qpa_compute_L_a()
            print("L_a = %s" % L_a)
            L = min(L_a, L_b)
        else:
            L = L_b
        
        d_min = min(map(attrgetter('deadline'), self.taskset))

        print("L = max_S + min(L_a, L_b) = %s" % L)
        t = self.qpa_d_max(L)

        print("t = %s, D_min = %s" % (t, d_min))

        while self.cdbf(0, t, True) <= t and self.cdbf(0, t, True) > d_min:
            print("t = %s, h(t) = %s" % (t, self.cdbf(0, t, True)))
            if self.cdbf(0, t, True) < t:
                t = self.cdbf(0, t, True)
            else:
                t = self.qpa_d_max(t)
        print("t = %s, h(t) = %s" % (t, self.cdbf(0, t, True)))
        if self.cdbf(0, t, True) <= d_min:
            print("QPA: Schedulable")
            return True
        else:
            print("QPA: Unschedulable")
            return False

    def is_schedulable(self):
        if self.is_implicit_deadline:
            return self.edf_ll_test()
        else:
            if self.is_async:
                if self.use_exact_for_async:
                    return self.edf_baruah_test()
                else:
                    return self.edf_qpa_test()
            else:
                return self.edf_qpa_test()
                
    
