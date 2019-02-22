#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Schedulability Analysis for FPPS'

import math
import sys
from operator import attrgetter

import Utilities


class FPPS:

    def __init__(self, taskset, is_implicit_deadline=False, 
                                   is_async=True, use_exact_for_async=False):
        assert taskset != None and type(taskset) == set
        self.taskset = taskset
        self.is_implicit_deadline = is_implicit_deadline
        self.is_async = is_async
        self.use_exact_for_async = use_exact_for_async

    @staticmethod
    def rate_monotonic_bound(n):
        """Liu and Layland bound defined in the J. ACM 1973 article"""
        if n > 0:
            k = float(n)
            return (k * (2.0**(1.0/k) -  1.0))
        else:
            return 0.0

    def fpps_ll_test(self):
        """
        FPPS with Rate Monotonic (RM) priority assignment and LL Test: 
        U_total <= n(2^(1/n) - 1)
        """
        n = float(len(self.taskset))
        bound = FPPS.rate_monotonic_bound(n)
        U_total = sum(map(attrgetter('utilization'), self.taskset))
        if U_total > bound:
            return False
        else:
            return True


    ### Response Time Analysis (RTA) functions ###
    def fpps_rta_test(self):
        """
        Response Time Analysis
        Assuming Deadline Monotonic (DM) Priority assignment
        """
        R = [0 for i in range(len(self.taskset))]
        I = [0 for i in range(len(self.taskset))]

        tasks = list(self.taskset)
        sorted_tasks = sorted(tasks, key=attrgetter('deadline'),reverse=False)

        i = 0
        for task in sorted_tasks:
            while (I[i] + task.get_wcet()) > R[i]:
                R[i] = I[i] + task.get_wcet()
                if R[i] > task.get_deadline():
                    return False
                I[i] = sum([max(0, int(math.ceil(float(R[i])/float(sorted_tasks[k].get_period()))))*sorted_tasks[k].get_wcet() for k in range(i)])
            i += 1

        return True


    def is_schedulable(self):
        if self.is_implicit_deadline:
            return self.fpps_ll_test()
        else:
            if self.is_async:
                if self.use_exact_for_async:
                    sys.stderr.write("Exact test for asynchronous tasks is not" +
                                     " implemented yet\n")
                    assert False
                else:
                    return self.fpps_rta_test()
            else:
                return self.fpps_rta_test()   


    @staticmethod
    def compute_response_time(taskset):
        """
        Compute response time
        Assuming Deadline Monotonic (DM) Priority assignment

        Returns:
            *R_I*: a list [R I], R is a list storing all response time and I is a list
                   storing all interference
        """
        R = [0 for i in range(len(taskset))]
        I = [0 for i in range(len(taskset))]

        R_I = list()
        
        actors = list(taskset)
        sorted_actors = sorted(actors, key=attrgetter('deadline'), reverse=False)

        i = 0
        for actor in sorted_actors:
            R[i] =  actor.get_start_time() + actor.get_wcet()
            I[i] = sum([max(0, int(math.ceil(float(R[i] - actor.get_start_time())/float(sorted_actors[k].get_period()))))*sorted_actors[k].get_wcet() for k in range(i)])
            while (actor.get_start_time() + I[i] + actor.get_wcet()) != R[i]:
                R[i] = actor.get_start_time() + I[i] + actor.get_wcet()
                I[i] = sum([max(0, int(math.ceil(float(R[i] - actor.get_start_time())/float(sorted_actors[k].get_period()))))*sorted_actors[k].get_wcet() for k in range(i)])
            i += 1

        R_I.append(R)
        R_I.append(I)
        return R_I


    @staticmethod
    def compute_x(sorted_actors, actor, R_ss, is_old_sn = True):
        """
        Compute range of x used to determine W window

        *sorted_actors*: actors sorted in increasing order of deadline
        *actors_hp* : actors with higher priority
        *R_ss*      : response time in steady state

        Return:
            a set containing all values of x
        """
        x = set()
        x.add(0)        # minimum value of x
        x.add(R_ss)     # maximum value of x
        epslon = 1

        for ac in sorted_actors:
            if ac.get_actor_id() == actor.get_actor_id():
                return x

            if is_old_sn == True:
                const = epslon
            else:
                const = ac.get_wcet()
            f = 0
            while f * ac.get_period() + const < R_ss:
                x.add(f*ac.get_period() + const)    
                f += 1
        return x

    @staticmethod
    def compute_fpps_rta_mc(processor, old_sn_id, new_sn_id):
        """
        Compute response time in case of scenario change
        Assuming FPPS and Deadline Monotonic (DM) Priority assignment
        """
        W = [0 for i in range(processor.get_nr_param_actors(old_sn_id))]
        W_new = [0 for i in range(processor.get_nr_param_actors(new_sn_id))]
        I_old = [0 for i in range(processor.get_nr_param_actors(old_sn_id))]
        I = [0 for i in range(processor.get_nr_param_actors(old_sn_id))]
        
        actors_old = list(processor.get_param_actors(old_sn_id))
        actors_new = list(processor.get_param_actors(new_sn_id))

        sorted_actors_old = sorted(actors_old, key = attrgetter('deadline'), reverse = False)
        sorted_actors_new = sorted(actors_new, key = attrgetter('deadline'), reverse = False)

        processor.set_actors(actors_old)
        R_I = FPPS.compute_response_time(processor.get_actors())    
        R_sso = R_I[0]      # steady-state analysis in the old scenario
        # print("R_sso: %r" % R_sso)
        
        # Compute response time for old scenario actors
        W_max = -1
        i = 0
        for actor in sorted_actors_old:
            # k = 0
            # for k in range(i):
            #     I[i] = sorted_actors[k].get_wcet()
            #     k += 1
            W[i] =  actor.get_start_time() + I[i] + actor.get_wcet()
            
            x_set = FPPS.compute_x(sorted_actors_old, actor, R_sso[i], is_old_sn = True)
            x_new = FPPS.compute_x(sorted_actors_new, actor, R_sso[i], is_old_sn = False)
            x_set = x_set | x_new
            # print("ac id, %d, x: %r" % (actor.get_actor_id(), x_set))

            # All actors in the new scenario with higher priority
            actors_new_hp = [sorted_actors_new[a] for a in range(len(sorted_actors_new)) if
                             (sorted_actors_new[a].get_actor_id() != actor.get_actor_id() and
                              sorted_actors_new[a].get_deadline() <= actor.get_deadline())]
            # print("%r" % (actors_new_hp))

            # Try all x in the range (0, R_ss)
            for x in x_set:
                W[i] = actor.get_start_time() + I[i] + actor.get_wcet()

                # Interference from old scenario actors with higher priority            
                I_old[i] = sum([int(math.ceil(float(x)/float(sorted_actors_old[k].get_period())))
                                * sorted_actors_old[k].get_wcet() for k in range(i)])

                # Interference from old and new scenario actors with higher priority
                I[i] = sum([int(math.ceil(float(W[i] - x - actors_new_hp[l].get_offset())
                                / float(actors_new_hp[l].get_period())))
                            * actors_new_hp[l].get_wcet() for l in range(len(actors_new_hp))])\
                       + I_old[i]
                # Recursively compute R until it converges
                while actor.get_start_time() + I[i] + actor.get_wcet() != W[i]:
                    W[i] = actor.get_start_time() + I[i] + actor.get_wcet()

                    # Updated interference from new scenario actors with higher priority
                    I[i] = sum([int(math.ceil(float(W[i] - x - actors_new_hp[l].get_offset())
                                / float(actors_new_hp[l].get_period())))
                           * actors_new_hp[l].get_wcet() for l in range(len(actors_new_hp))])\
                           + I_old[i]
                if W[i] > W_max:
                    W_max = W[i]
            W[i] = W_max
            i += 1
        
        ############################## Compute response time for new scenario actors
        i = 0
        for actor in sorted_actors_new:
            W_new[i] = actor.get_start_time() + actor.get_wcet() +\
                       processor.get_actor(old_sn_id, actor.get_actor_id()).get_wcet()
           
            actors_old_hp = [sorted_actors_old[a] for a in range(len(sorted_actors_old)) if
                             sorted_actors_old[a].get_actor_id() != actor.get_actor_id()]
           
            I_old[i] = sum([actors_old_hp[l].get_wcet() for l in range(len(actors_old_hp))])
            I[i] = sum([int(math.ceil((W_new[i] - sorted_actors_new[k].get_offset())
                            / float(sorted_actors_new[k].get_period())))
                        * sorted_actors_new[k].get_wcet() for k in range(i)])
            # Recursively compute W until it converges
            while actor.get_start_time() + I[i] + actor.get_wcet() != W_new[i]:
                W_new[i] = actor.get_start_time() + I[i] + actor.get_wcet()

                # Updated I
                I[i] = sum([int(math.ceil((W_new[i] - sorted_actors_new[k].get_offset())
                            / float(sorted_actors_new[k].get_period())))
                       * sorted_actors_new[k].get_wcet() for k in range(i)])
            W_new[i] -= actor.get_offset()
            i += 1

        # print(W)
        # print(W_new)
        R_lst = list()
        R_lst.append(W)
        R_lst.append(W_new)

        return R_lst

