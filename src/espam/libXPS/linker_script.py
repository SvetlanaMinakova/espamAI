#!/usr/bin/python

import sys, os, fileinput

script_dir = os.path.dirname(__file__)
print("Default size for stack and heap for all processors in the platform is 1KB.")
print("Do you want to change the sizes? (Y/N) \n")
answer = raw_input()
while ((answer == 'Y') or (answer == 'y')):
    print("For which processor do you want to check/change stack and heap (e.g. P_1)? \n")    
    p = raw_input()
    ls_path = "SDK/" + p + "/lscript.ld"
    ls_path = os.path.join(script_dir, ls_path)    
    f = open(ls_path, 'r')
    line = f.readline()
    sline = line.split(':') 
    stack = sline[1].split(';')
    s = int(stack[0], 16)
    line = f.readline()
    hline = line.split(':') 
    heap = hline[1].split(';')
    h = int(heap[0], 16)
    print("Processor %s has %dB stack and %dB heap" % (p, s, h))
    print("Do you want to change the sizes? (Y/N) \n")
    answer = raw_input()
    if ((answer == 'Y') or (answer == 'y')):
        print("Enter new stack and heap size (e.g. 2048 2048) \n")
        sizes = raw_input()
        sh = sizes.split() 
        s = hex(int(sh[0]))
        h = hex(int(sh[1]))
        with open(ls_path, 'r') as f:
            data = f.readlines()
        data[0] = data[0].replace(stack[0], s)
        data[1] = data[1].replace(heap[0], h)
        with open(ls_path, 'w') as f:
            f.writelines(data)
    print("Do you want to check/change stack and heap of another processor? (Y/N) \n")
    answer = raw_input()


