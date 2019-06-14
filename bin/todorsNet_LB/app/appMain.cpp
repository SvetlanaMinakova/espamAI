// File automatically generated by ESPAM

#include "input.h"
#include "C1.h"
#include "C2.h"
#include "SM.h"
#include "output.h"
#include <stdlib.h>
#include <iostream>
#include <map>
#include <vector>
#include "csdfNode.h"
#include "appMain.h"
#include "appFunc.h"
#include "fifo.h"
#include <cstddef>
#include "types.h"
#include <thread>
using namespace std;

appMain::appMain() {}
appMain::~appMain() {}

// Main function
  void appMain::main()
    {
      // list of all available nodes
      std::map< std::string, csdfNode* > nodes = std::map< std::string,csdfNode* >();
      input input_inst = input();
      nodes["input"] = &input_inst;
      C1 C1_inst = C1();
      nodes["C1"] = &C1_inst;
      C2 C2_inst = C2();
      nodes["C2"] = &C2_inst;
      SM SM_inst = SM();
      nodes["SM"] = &SM_inst;
      output output_inst = output();
      nodes["output"] = &output_inst;

      /**
        Define schedule 
        Dummy schedule for LB-mode: layers in traverse order
        Dummy schedule for NB-mode: neurons of one layer can run in parallel
      */
      vector<std::string> schedule = vector<std::string>();

      schedule.push_back("input");
      schedule.push_back("C1");
      schedule.push_back("C2");
      schedule.push_back("SM");
      schedule.push_back("output");

      // Prepare shared data and memory
 
      //Size of one token of FIFO in ints
      int token_len=sizeof(long)/sizeof(int)+(sizeof(long)%sizeof(int)+(sizeof(int)-1))/sizeof(int);
 
      //fifo channels definition and initialization. Fifo sizes are given in tokens.
      std::vector<fifo_buf> fifos = std::vector<fifo_buf>();
 
      // FIFO input_OP0-->C1_IP0
      void *fifo_0=NULL;
      int fifo_size_0 = max(input_inst.OP0_fifo_size ,C1_inst.IP0_fifo_size);
      fifo_0 = calloc(fifo_size_0 +2, sizeof(int));
      struct fifo_buf buf_0 = fifo_buf (fifo_0,fifo_size_0, "input_OP0" , "C1_IP0");
      fifos.push_back(buf_0);
 
      // FIFO C1_OP1-->C2_IP0
      void *fifo_2=NULL;
      int fifo_size_2 = max(C1_inst.OP1_fifo_size ,C2_inst.IP0_fifo_size);
      fifo_2 = calloc(fifo_size_2 +2, sizeof(int));
      struct fifo_buf buf_2 = fifo_buf (fifo_2,fifo_size_2, "C1_OP1" , "C2_IP0");
      fifos.push_back(buf_2);
 
      // FIFO C2_OP1-->SM_IP0
      void *fifo_4=NULL;
      int fifo_size_4 = max(C2_inst.OP1_fifo_size ,SM_inst.IP0_fifo_size);
      fifo_4 = calloc(fifo_size_4 +2, sizeof(int));
      struct fifo_buf buf_4 = fifo_buf (fifo_4,fifo_size_4, "C2_OP1" , "SM_IP0");
      fifos.push_back(buf_4);
 
      // FIFO SM_OP0-->output_IP0
      void *fifo_5=NULL;
      int fifo_size_5 = max(SM_inst.OP0_fifo_size ,output_inst.IP0_fifo_size);
      fifo_5 = calloc(fifo_size_5 +2, sizeof(int));
      struct fifo_buf buf_5 = fifo_buf (fifo_5,fifo_size_5, "SM_OP0" , "output_IP0");
      fifos.push_back(buf_5);
 
      // Preparation work for threads on CPU

      // Allocate memory for pthread_create() arguments
      const int num_threads = schedule.size();
      struct thread_info *thread_info = (struct thread_info*)(calloc(num_threads, sizeof(struct thread_info)));

      // Main threads

      //input thread info
      thread_info[0].core_id = 1;
      fifo_buf* buf_ref_input_OP0 = appFunc::get_buf_by_src( "input_OP0", fifos);
      thread_info[0].add_fifo_buf_ref( buf_ref_input_OP0);

      //C1 thread info
      thread_info[1].core_id = 2;
      fifo_buf* buf_ref_C1_IP0 = appFunc::get_buf_by_dst( "C1_IP0", fifos);
      thread_info[1].add_fifo_buf_ref( buf_ref_C1_IP0);
      fifo_buf* buf_ref_C1_OP1 = appFunc::get_buf_by_src( "C1_OP1", fifos);
      thread_info[1].add_fifo_buf_ref( buf_ref_C1_OP1);

      //C2 thread info
      thread_info[2].core_id = 3;
      fifo_buf* buf_ref_C2_IP0 = appFunc::get_buf_by_dst( "C2_IP0", fifos);
      thread_info[2].add_fifo_buf_ref( buf_ref_C2_IP0);
      fifo_buf* buf_ref_C2_OP1 = appFunc::get_buf_by_src( "C2_OP1", fifos);
      thread_info[2].add_fifo_buf_ref( buf_ref_C2_OP1);

      //SM thread info
      thread_info[3].core_id = 4;
      fifo_buf* buf_ref_SM_IP0 = appFunc::get_buf_by_dst( "SM_IP0", fifos);
      thread_info[3].add_fifo_buf_ref( buf_ref_SM_IP0);
      fifo_buf* buf_ref_SM_OP0 = appFunc::get_buf_by_src( "SM_OP0", fifos);
      thread_info[3].add_fifo_buf_ref( buf_ref_SM_OP0);

      //output thread info
      thread_info[4].core_id = 5;
      fifo_buf* buf_ref_output_IP0 = appFunc::get_buf_by_dst( "output_IP0", fifos);
      thread_info[4].add_fifo_buf_ref( buf_ref_output_IP0);

      // Create and run thread_input
      std::thread thread_input(&input::main, &input_inst, &thread_info[0]);
      cout<<"Joined with thread thread_input"<<endl;
      //thread_input.join();

      // Create and run thread_C1
      std::thread thread_C1(&C1::main, &C1_inst, &thread_info[1]);
      cout<<"Joined with thread thread_C1"<<endl;
      //thread_C1.join();

      // Create and run thread_C2
      std::thread thread_C2(&C2::main, &C2_inst, &thread_info[2]);
      cout<<"Joined with thread thread_C2"<<endl;
      //thread_C2.join();

      // Create and run thread_SM
      std::thread thread_SM(&SM::main, &SM_inst, &thread_info[3]);
      cout<<"Joined with thread thread_SM"<<endl;
      //thread_SM.join();

      // Create and run thread_output
      std::thread thread_output(&output::main, &output_inst, &thread_info[4]);
      cout<<"Joined with thread thread_output"<<endl;
      //thread_output.join();


      // Join threads that should be awaited
      thread_input.join();
      thread_C1.join();
      thread_C2.join();
      thread_SM.join();
      thread_output.join();

      cout<<"Generated program is finished!"<<endl;
    }

