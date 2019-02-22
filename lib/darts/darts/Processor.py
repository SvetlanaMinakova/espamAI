#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama and Teddy Zhai'
__description__ = 'Represents a single processor with utilization bound'

class Processor:
    """
    This class models a processor (i.e., bin) during the allocation procedure
    """
    def __init__(self, utilization_bound):
        """
        Create a Processor with maximum utilization equal to utilization_bound
        """
        self.actors = set()

        self.param_actors = dict()      # (sn_id : actors)
        self.utilization_dict = dict()  # (sn_id : utilization)
        
        self.set_utilization_bound(utilization_bound)

    def can_fit(self, actor):
        if (self.get_utilization() + actor.get_utilization()) <= self.utilization_bound:
            return True
        else:
            return False

    def add_actor(self, actor):
        """
        Tries to add an actor to the Processor
        @return True if the actor is added. Otherwise, it returns False
        """
        if (self.get_utilization() + actor.get_utilization()) <= self.utilization_bound:
            self.actors.add(actor)
            return True
        else:
            return False

    def add_param_actor(self, sn_id, actor):
        """
        Add a parametrized actor to the Processor

        Args:
            *sn_id*: Scenario ID (integer)
            *actor*: Actor (:class:`ActorModel.ActorModel` object)
        """
        
        if sn_id not in self.utilization_dict:
            self.utilization_dict[sn_id] = 0.0
            # Initialize set of actors for this scenario
            actors = set()
            self.param_actors[sn_id] = actors
        
        # Check schedulability
        if (self.utilization_dict[sn_id] + actor.get_utilization()) > self.utilization_bound:
            return False
        else:
            self.utilization_dict[sn_id] += actor.get_utilization()
            self.param_actors[sn_id].add(actor)
        
        return True

    def get_nr_param_actors(self, sn_id):
        return len(self.param_actors)

    def set_utilization_bound(self, utilization_bound):
        assert utilization_bound >= 0.0 and utilization_bound <= 1.0
        self.utilization_bound = utilization_bound

    def get_utilization_bound(self):
        return self.utilization_bound

    def get_actors(self):
        return self.actors

    def get_actor(self, sn_id, actor_id):
        assert sn_id in self.param_actors

        actors = self.param_actors[sn_id]
        for ac in actors:
            if ac.get_actor_id() != actor_id:
                continue
            return ac
        assert 1 != 1, "ERROR: Getting actor failed."

    def set_actors(self, actors):
        self.actors = set(actors)

    def get_param_actors(self, sn_id):
        assert sn_id in self.param_actors
        return self.param_actors[sn_id]

    def __len__(self):
        return len(self.actors)
    
    def get_utilization(self):
        utilization = 0.0
        for actor in self.actors:
            utilization += float(actor.get_wcet())/float(actor.get_period())
        return utilization

    utilization = property(get_utilization)

    def get_density(self):
        density = 0.0
        for actor in self.actors:
            density += float(actor.get_wcet())/float(min(actor.get_deadline(), actor.get_period()))
        return density
    
    def __repr__(self):
        s = ["\n{"]
        for i in self.actors:
            s.append("%s:%s," % (i.get_name(), i.get_global_id()))
        s.append("}\n")
        return ''.join(s)

