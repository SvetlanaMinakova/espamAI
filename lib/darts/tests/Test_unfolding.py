#!/usr/bin/python

import sys

sys.path.append('../darts/')

from ACSDFModel import ACSDFModel

UPPER_BOUND = 100

if __name__ == "__main__":
    
    g = ACSDFModel.load_graph_from_file("../tests/synthetic/chain121_src_snk.gph", 1, 1)
    
    unfold_max = g.find_upper_bounds_unfolding_factors()
    for a_id, f_max in unfold_max.items():
        print("id: %s, unfold max: %s" % (a_id, f_max))
    
    for i in range(1, UPPER_BOUND): # unfolding factor for A1
        for j in range(1, UPPER_BOUND): # unfolding factor for A2
            for k in range(1, UPPER_BOUND): # unfolding factor for A3
                print("--> %s, %s, %s" % (i, j, k))
                g.unfold_sdf({1:i, 2:j, 3:k})            
            
            
    #print("--> %s, %s, %s" % (2, 4, 1))
    #ACSDFModel.unfold_sdf(g, {1:2, 2:4})            
    
    #print("--> %s, %s, %s" % (4, 2, 1))
    #ACSDFModel.unfold_sdf(g, {1:4, 2:2})        
    
    print("end" )

