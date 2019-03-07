
PROGRAM = synth1
CC = gcc
FLAGS = -std=c99 -pedantic -Wall -O2

all:
	${CC} ${FLAGS} ${PROGRAM}.c -o ${PROGRAM}
	strip ${PROGRAM}
clean:
	rm -f ${PROGRAM}
