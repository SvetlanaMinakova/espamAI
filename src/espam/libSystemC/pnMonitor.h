#ifndef MONITOR_H
#define MONITOR_H

#include <map>
#include <fstream>
#include "aux_func.h"
#include "workload.h"

//-------------------------
// Process network monitor
//-------------------------
SC_MODULE(Monitor) {
  public:
    sc_in<bool> clk;
    
    sc_in<sc_lv<numProcs> > read_bus;
    sc_in<sc_lv<numProcs> > execute_bus;
    sc_in<sc_lv<numProcs> > write_bus;
    sc_in<sc_lv<numProcs> > finish_bus;
       
    sc_lv<numProcs> varReadBus;
    sc_lv<numProcs> varExecuteBus;
    sc_lv<numProcs> varWriteBus;
    
    int totalExec, nSinksEx;
    int currPar, currExec, currComm;
    int degPar[numProcs], ovrlpExec[numProcs], ovrlpComm[numProcs];
    int nRd[numProcs], nEx[numProcs], nWr[numProcs];
    double T[numProcs];
    
    double avrgPar, execPar, commPar;
    double relativeSp;
    double realSp;
    double ccRatioPPN;
    double efficiencyPPN, efficiency_1_PPN;
    double ccRatio[numProcs];
     
    std::map<const char*,int> stcs[numProcs];
    std::map<const char*,double> utlzn;
    char processName[numProcs][8];
    
    SC_HAS_PROCESS(Monitor);
    Monitor(sc_module_name nm);
    void finish_proc();
    void collect_statistics_proc();    
    void compute_statistics_proc();    
    void print_statistics_proc();    
    void collect(sc_logic rd, sc_logic ex, sc_logic wr, int proc);   
};

//-----------------------------------
// Constructor
//-----------------------------------
Monitor::Monitor(sc_module_name nm) {
  SC_METHOD(finish_proc);
  sensitive << finish_bus;
  
  SC_METHOD(collect_statistics_proc);
  sensitive << clk.pos();
  dont_initialize();

  currExec=currComm=totalExec=nSinksEx=0;
  avrgPar=execPar=commPar=relativeSp=realSp=ccRatioPPN=efficiencyPPN=efficiency_1_PPN=0.0;

  for( int i=0; i<numProcs; i++ ) {
    stcs[i]["Execution"]=stcs[i]["Read"]=stcs[i]["Write"]=stcs[i]["Block on Read"]=stcs[i]["Block on Write"]=0;
    degPar[i]=ovrlpExec[i]=ovrlpComm[i]=0;
    nRd[i]=nEx[i]=nWr[i]=0;
    T[i]=0.0;
    sprintf(processName[i], "P_%d", i+1 );  
  }    
}

//----------------------------------------------------------------------
// Statisitcs of process 'proc'
//----------------------------------------------------------------------
void Monitor::collect(sc_logic rd, sc_logic ex, sc_logic wr, int proc) {

  if( rd == sc_logic('1') ) {
    stcs[proc]["Read"]++;
    currComm++;
  } else if( ex == sc_logic('1') ) {
    stcs[proc]["Execution"]++;
    currExec++;
  } else if( wr == sc_logic('1') ) {
    stcs[proc]["Write"]++;
    currComm++;
  } else if( rd == sc_logic('X') ) {
    stcs[proc]["Block on Read"]++;
  } else if( wr == sc_logic('X') ) {
    stcs[proc]["Block on Write"]++;
  }
}

//---------------------------------------
// Collect PPN execution data
//---------------------------------------
void Monitor::collect_statistics_proc() {

    varReadBus = read_bus.read();
    varExecuteBus = execute_bus.read();
    varWriteBus = write_bus.read();
      
    // Every clock cycle capture how many processes are in Rd/Ex/Wr state (exclude blocking)
    currPar=0; 
    currExec=0; 
    currComm=0; 

    for( int i=0; i<numProcs; i++ ) {
       collect( varReadBus[i], varExecuteBus[i], varWriteBus[i], i );
    }     
    
    totalExec++;
    int degreeParal=currExec+currComm;
    if( degreeParal>0 ) {
      degPar[degreeParal-1]++;
    }
    if( currExec>0 ) {
      ovrlpExec[currExec-1]++;
    }
    if( currComm>0 ) {
      ovrlpComm[currComm-1]++;
    }
}
  
//---------------------------------------
// Compute PPN statistics
//--------------------------------------- 
/*
 * The period of a PPN is the period of its sink process
 * The period of the sink process is T=(Rd + Ex + Wr=0 )/nEx [clocks]/[firing]
 * The PPN period in case of multiple sinks is totalEx[clks]/sum(#sinkExecutions)
 * The throughput is 1/T [number of firings]/[clock]
 * The throughput in case of multiple sinks = sum of the throughputs of the sinks
 */
void Monitor::compute_statistics_proc() {
  
    totalExec--; // Counts one clock more because detecting the 'finish' signals happens "at the next" clock cycle
    totalExec--; // There is an initial 1-cycle delay to ensure FIFOs are ready  

    int seqExec=0, seqComm=0;
    for( int i=0; i<numProcs; i++ ) {
      // Compute 'idle time' of each process(or)
      stcs[i]["Idle"] = totalExec - (stcs[i]["Execution"]+stcs[i]["Read"]+stcs[i]["Write"]+stcs[i]["Block on Read"]+stcs[i]["Block on Write"]);
      
      // Compute the utilization of each process(or)
      utlzn[processName[i]] = (double)(stcs[i]["Execution"]+stcs[i]["Read"]+stcs[i]["Write"])/totalExec;
      
      // Computation/Communication ratio of each process(or)
      ccRatio[i] = (double)stcs[i]["Execution"]/(stcs[i]["Read"]+stcs[i]["Write"]);
      
      // Weighted sum to compute 'average' (degree of) parallelism (Rd+Ex+Wr)
      avrgPar += (double)(degPar[i]*(i+1));
      
      // Weighted sum to compute average (degree of) overlapped execution 
      execPar += (double)(ovrlpExec[i]*(i+1));
      
      // Weighted sum to compute average (degree of) overlapped communication (Rd+Wr)
      commPar += (double)(ovrlpComm[i]*(i+1));
      
      seqExec += stcs[i]["Execution"];
      seqComm += (stcs[i]["Read"]+stcs[i]["Write"]);
    }

    avrgPar /= totalExec; // This value is the same as the relative speed-up

    execPar /= totalExec; // This value is the same as the real speed-up
    // execution parallelism = real speed-up - we capture only the execution overlap (only currExec++, see status 'execute' above)

    commPar /= totalExec; // This value indicates the average number of processes(ors) that communicate at the same time
    // Indicates the "pressure" on the communication resource (in particular if it is a shared bus)

    // Ratio 'Execute'+'Read'+'Write' (PPN on one 'processor') -> total parallel execution (relative speed-up, blocking excluded)
    relativeSp = (double)(seqExec+seqComm)/totalExec;

    // Ratio 'Execute' only (sequential execution) -> total parallel execution (real speed-up, compared to the sequential program)
    realSp = (double)seqExec/totalExec;
    // relativeSp - realSp = overlapped communication

    // Computation/communication ratio 'Execute'/('Read'+'Write')
    ccRatioPPN = (double)seqExec/seqComm;     

    // Efficiency (maybe, we need to exclude the source and the sink processes) 
    efficiencyPPN = (double)relativeSp/numProcs;   
    
    // Another way to compute efficiency - as the average utilization (excluding the source and sink processes)
    int c = 0;
    for( int i=0; i<numProcs; i++ ) {
      if( stcs[i]["Read"]!=0 && stcs[i]["Write"]!=0 ) {
	c++;
        efficiency_1_PPN += utlzn[processName[i]];
      }
    }
    efficiency_1_PPN /= c;
    
    // Compute the number of process firings and the number ot tokens read/written 
    for( int i=0; i<numProcs; i++ ) {
	nRd[i] = stcs[i]["Read"]/latRead;
	nEx[i] = stcs[i]["Execution"]/latency[i];
	nWr[i] = stcs[i]["Write"]/latWrite;

	// 'Isolated' process period including read and write times
 	T[i] = (double)(stcs[i]["Execution"]+stcs[i]["Read"]+stcs[i]["Write"])/nEx[i];
	// collect the number of firings of the sinks in order to compute the period of the PPN
	if( nWr[i] == 0 ) 
	  nSinksEx += nEx[i];
    }   
}

//-------------------------------------
// Print PPN statistics
//-------------------------------------
void Monitor::print_statistics_proc() {
       
    printf("\n\nStatistics:\n");
    printf("+---------+--------------+----------------+\n");
    printf("| Process |      #clocks | Status         |\n");
    printf("+---------+--------------+----------------+\n");
    std::map<const char*,int>::iterator it;   
    for( int i=0; i<numProcs; i++ ) {
       printf("| %7s | %12s | %-14s |\n", processName[i], "", "");
       for( it = stcs[i].begin(); it != stcs[i].end(); ++it ) {
          printf("| %-7s | %12d | %-14s |\n", "", it->second,it->first);
       }          
       printf("+---------+--------------+----------------+\n");  
    }
  
    printf("\n+-----------------------------------------+\n");
    printf("|    Computation/Communication Ratio:     |\n");
    printf("+------------------------------+----------+\n");
    for( int i=0; i<numProcs; i++ ) {
      printf("| %28s | %-8.4g |\n", processName[i], ccRatio[i]);
    }
    printf("| %28s | %-8.4g |\n", "PPN", ccRatioPPN);
    printf("+------------------------------+----------+\n\n");
    
    printf("\n+-----------------------------------------+\n");
    printf("|         Utilization/Efficiency          |\n");
    printf("+------------------------------+----------+\n");
    std::map<const char*,double>::iterator it1;   
    for( it1 = utlzn.begin(); it1 != utlzn.end(); ++it1 ) {
      printf("| %28s | %-8.4g |\n", it1->first, it1->second);
    }
    printf("+------------------------------+----------+\n");
    printf("|       PPN Utilization/Efficiency:       |\n");
    printf("+------------------------------+----------+\n");
    printf("| %28s | %-8.4g |\n", "PPN", efficiencyPPN);
    printf("| %28s | %-8.4g |\n", "Sources and Sinks excluded", efficiency_1_PPN);
    printf("+------------------------------+----------+\n\n");
    
    printf("+-----------------------------------------+\n");
    printf("|       Speed-up and parallelism:         |\n");
    printf("+------------------------------+----------+\n");
    printf("| %28s | %-8.4g |\n", "PPN Sequential/PPN Parallel", relativeSp);
    printf("| %28s | %-8.4g |\n", "Sequential/PPN Parallel", realSp);
    printf("+------------------------------+----------+\n");
    printf("| %28s | %-8.4g |\n", "Average parallelism", avrgPar);
    printf("| %28s | %-8.4g |\n", "Overlapped execution (only)", execPar);
    printf("| %28s | %-8.4g |\n", "Overlapped communication", commPar);
    printf("+------------------------------+----------+\n\n");

    printf("+------------------------------+----------+\n");
    printf("| %28s | %8s |\n", "Degree of parallel execution", "#clocks");
    printf("+------------------------------+----------+\n"); 
    for( int i=0; i<numProcs; i++ ) {
      printf("| %28d | %8d |\n", i+1, degPar[i]);
    }
    printf("+------------------------------+----------+\n");
    printf("| %28s | %8d |\n", "Total PPN execution", totalExec);
    printf("+------------------------------+----------+\n\n\n");
    
    //----------------------
    // Additional statistics   
    //----------------------
    printf("\n+--------------------------------------------------------------------------------------------+\n");
    printf("| Additional statistics:                                                                     |\n");
    printf("+---------+--------------+--------------+--------------+--------------+----------------------+\n");
    printf("| Process | Wload [clks] | Rd. [tokens] | Ex.[firings] | Wr. [tokens] | T(iso) [clks/firing] |\n");
    printf("+---------+--------------+--------------+--------------+--------------+----------------------+\n");
    for( int i=0; i<numProcs; i++ ) {
       printf("| %7s | %12d | %12d | %12d | %12d | %20.4g |\n", processName[i], latency[i], nRd[i], nEx[i], nWr[i], T[i]);
       printf("+---------+--------------+--------------+--------------+--------------+----------------------+\n");
    }    
    printf("| %67s | %-20.4g |\n", "T(ppn): Average #clks per sink firing", (double)totalExec/nSinksEx);
    printf("+---------------------------------------------------------------------+----------------------+\n");
    printf(" (Latency read = %d [clks], Latency write = %d [clks])\n", latRead, latWrite);    
    printf("\n");  
}

//----------------------------------------------------
// Stops the simulation and manage the collected data
//----------------------------------------------------
void Monitor::finish_proc() {
  
  if (finish_bus.read().and_reduce()==1) {
    sc_stop();
    compute_statistics_proc();
    print_statistics_proc();
  }
}

#endif