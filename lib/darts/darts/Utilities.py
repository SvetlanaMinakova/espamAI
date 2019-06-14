#!/usr/bin/python

__author__ =  'Mohamed A. Bamakhrama'
__description__ = 'Utility Functions'

import fractions
from functools import reduce

def gcd(a, b):
    """Returns the greatest-common-divisor (GCD) of *a* and *b*
    """
    return fractions.gcd(a, b)

def lcm(a, b):
    """Returns the least common multiple (LCM) of *a* and *b*
    """
    return (a*b)/gcd(a,b)

def gcdv(vec):
    """Returns the GCD of the elements in list *vec*
    """
    return reduce(gcd,vec)

def lcmv(vec):
    """Returns the LCM of the elements in list *vec*
    """
    return reduce(lcm,vec)

def tabs(n):
    """Returns a string composed of *n* tabs.
    Useful for producing indentation in output files
    """
    s = []
    for i in range(n):
        s.append("\t")
    return ''.join(s)


def sanitize_id(id):
    return id.strip().replace(" ", "")


# translate original id to a new base id 
# new base id = original id * 10^((#digits in f))
def translate_id(old_id, f):
    return int(str(old_id) + str("0" * len(str(f))))

# translate new id to the original base id 
# new base id = original id * 10^((#digits in f))
def translate_id_reverse(new_id, f):
    # assuming f = 89, then base_nr = 100
    base_nr = pow(10, len(str(f)))
    
    # assuming a new_id = 376 (f = 89), then old_id = int(376 / 100) = 3
    return int(new_id / base_nr)


DARTS_NAME = "darts"
DARTS_HEADER = """
******************************************************************************
*   %s: Dataflow Analysis for Hard-Real-Time Scheduling                   *
*   Copyrights (c) 2012 by Leiden University. All rights reserved.           *
*   See LICENSE.md for license information                                   *
******************************************************************************

""" % (DARTS_NAME)

