#!/usr/bin/python

import random

from deap import algorithms
from deap import base
from deap import creator
from deap import tools

# TODO: Parameters to change
M = 10
N = 5
POPULATION_SIZE = 100
NR_GENERATIONS = 10
NR_BEST_INDIVIDUALS=2

# TODO: Change evaluation function
def phrt_eval(individual):
    return sum(individual), individual[0]


# Construct the GA
creator.create("FitnessMulti", base.Fitness, weights=(-1.0, -1.0))
creator.create("Individual", list, fitness=creator.FitnessMulti)

toolbox = base.Toolbox()

toolbox.register("attr_int", random.randint, 0, M)
toolbox.register("individual", tools.initRepeat, creator.Individual, toolbox.attr_int, N)
toolbox.register("population", tools.initRepeat, list, toolbox.individual)

toolbox.register("evaluate", phrt_eval)
toolbox.register("mate", tools.cxUniform, indpb=0.1)
toolbox.register("mutate", tools.mutUniformInt, low=0, up=M, indpb=0.05)
toolbox.register("select", tools.selNSGA2)


def main():
    pop = toolbox.population(n=POPULATION_SIZE)
    #hof = tools.HallOfFame(NR_BEST_INDIVIDUALS)
    pareto=tools.ParetoFront()
    
    algorithms.eaSimple(pop, toolbox, cxpb=0.1, mutpb=0.05, ngen=NR_GENERATIONS, halloffame=pareto, verbose=False)
    print(pareto)

if __name__ == "__main__":
    main()

