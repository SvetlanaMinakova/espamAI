CXX=g++

CXXFLAGS= -std=c++11 -pthread 

OBJS = input.o C1.o C2.o SM.o output.o dnnFunc.o appFunc.o csdfNode.o appMain.o run.o fifo.o 

PRG = run

all: ${PRG}


run:  ${OBJS}
	${CXX} ${CXXFLAGS} -o $@ ${OBJS}

input.o: input.cpp
	${CXX} ${CXXFLAGS} -c -g $?

C1.o: C1.cpp
	${CXX} ${CXXFLAGS} -c -g $?

C2.o: C2.cpp
	${CXX} ${CXXFLAGS} -c -g $?

SM.o: SM.cpp
	${CXX} ${CXXFLAGS} -c -g $?

output.o: output.cpp
	${CXX} ${CXXFLAGS} -c -g $?

dnnFunc.o: dnnFunc.cpp
	${CXX} ${CXXFLAGS} -c -g $?

appFunc.o: appFunc.cpp
	${CXX} ${CXXFLAGS} -c -g $?

csdfNode.o: csdfNode.cpp
	${CXX} ${CXXFLAGS} -c -g $?

appMain.o: appMain.cpp
	${CXX} ${CXXFLAGS} -c -g $?

run.o: run.cpp
	${CXX} ${CXXFLAGS} -c -g $?

fifo.o: fifo.cpp
	${CXX} ${CXXFLAGS} -c -g $?



clean:
	rm -rf *~ *.o 
