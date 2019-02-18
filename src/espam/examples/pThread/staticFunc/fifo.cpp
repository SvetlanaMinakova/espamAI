#include "fifo.h"	
#include <pthread.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>

using namespace std;

void writeSWF_CPU(void* fifo, void* memobj_cpu, int len, int fifo_size) {
 
while( FIFOisFull(fifo)) {pthread_yield(); };

        int w_cnt = ((int*)fifo)[0];
	               				
        // Will copy data from cpu device memory to FIFO
int i;
 for ( i = 0; i < len; i++) {
                   ((int*)fifo)[(w_cnt & 0x7FFFFFFF) + 2 + i] = ((int*)memobj_cpu)[i];
                }

        w_cnt += len;	
			
        if( (w_cnt & 0x7FFFFFFF) == fifo_size ) {
            w_cnt &= 0x80000000;
            w_cnt ^= 0x80000000;
         }             
                
         ((int*)fifo)[0] = w_cnt;

}



void readSWF_CPU(void* fifo, void* memobj_cpu, int len, int fifo_size) {
 
while( FIFOisEmpty(fifo)) { pthread_yield(); };
      
        int w_cnt = ((int*)fifo)[0];
        int r_cnt = ((int*)fifo)[1];
        
        // Will copy data from FIFO to cpu device memory
int i;
                for ( i = 0; i < len; i++) {
                     ((int*)memobj_cpu)[i] = ((int*)fifo)[(r_cnt & 0x7FFFFFFF) + 2 + i];
                }
				
        r_cnt += len;
        
        if( (r_cnt & 0x7FFFFFFF) == fifo_size ) {
                    r_cnt &= 0x80000000;
                    r_cnt ^= 0x80000000;
         }
               
        ((int*)fifo)[1] = r_cnt;
                  
}
 


int FIFOisFull(void *fifo){
	
    int r_cnt = ((int *)fifo)[1];
    int w_cnt = ((int *)fifo)[0];
          	
	 if ( r_cnt == (w_cnt ^ 0x80000000) ){
		 return (1);
	 } else {
		  return(0);	
	 }
}



int FIFOisEmpty(void *fifo){
	
    int r_cnt = ((int *)fifo)[1];
    int w_cnt = ((int *)fifo)[0];
          	
	 if ( w_cnt == r_cnt ){
		 return (1);
	 } else {
		  return(0);	
	 }
}






void setaffinity(int core){

    pthread_t pid = pthread_self();
    int core_id = core; 

//   cpu_set_t: This data set is a bitset where each bit represents a CPU.
  cpu_set_t cpuset;
//  CPU_ZERO: This macro initializes the CPU set set to be the empty set.
  CPU_ZERO(&cpuset);
//   CPU_SET: This macro adds cpu to the CPU set set.
  CPU_SET(core_id, &cpuset);
 
//   pthread_setaffinity_np: The pthread_setaffinity_np() function sets the CPU affinity mask of the thread thread to the CPU set pointed to by cpuset. If the call is successful, and the thread is not currently running on one of the CPUs in cpuset, then it is migrated to one of those CPUs.
   const int set_result = pthread_setaffinity_np(pid, sizeof(cpu_set_t), &cpuset);
  if (set_result != 0) {
    printf("pthread setaffinity failed!\n");
  }

 //  Check what is the actual affinity mask that was assigned to the thread.
 //  pthread_getaffinity_np: The pthread_getaffinity_np() function returns the CPU affinity mask of the thread thread in the buffer pointed to by cpuset.
  const int get_affinity = pthread_getaffinity_np(pid, sizeof(cpu_set_t), &cpuset);
  if (get_affinity != 0) {
    printf("pthread getaffinity failed!\n");
  }

  char *buffer;
  // CPU_ISSET: This macro returns a nonzero value (true) if cpu is a member of the CPU set set, and zero (false) otherwise.
  if (CPU_ISSET(core_id, &cpuset)) {
    printf("Successfully set thread %u to cpu core %d\n", pid, core_id);
  } else {
    printf("failed!\n");
  }
}
