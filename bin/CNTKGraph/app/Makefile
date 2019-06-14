CXX=g++

CXXFLAGS= -std=c++11 -pthread 

OBJS = Input3.o Convolution28.o Plus30.o ReLU32.o Pooling66.o Convolution110.o Plus112.o ReLU114.o Pooling160.o Times212_reshape0.o Times212.o Plus214.o Plus214_Output_0.o dnnFunc.o appFunc.o csdfNode.o appMain.o run.o fifo.o 

PRG = run

all: ${PRG}


run:  ${OBJS}
	${CXX} ${CXXFLAGS} -o $@ ${OBJS}

Input3.o: Input3.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Convolution28.o: Convolution28.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Plus30.o: Plus30.cpp
	${CXX} ${CXXFLAGS} -c -g $?

ReLU32.o: ReLU32.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Pooling66.o: Pooling66.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Convolution110.o: Convolution110.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Plus112.o: Plus112.cpp
	${CXX} ${CXXFLAGS} -c -g $?

ReLU114.o: ReLU114.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Pooling160.o: Pooling160.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Times212_reshape0.o: Times212_reshape0.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Times212.o: Times212.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Plus214.o: Plus214.cpp
	${CXX} ${CXXFLAGS} -c -g $?

Plus214_Output_0.o: Plus214_Output_0.cpp
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
