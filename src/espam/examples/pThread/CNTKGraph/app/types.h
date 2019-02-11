#ifndef pthread_H
#define pthread_H
#include <pthread.h>
#include <vector>
#include <cstddef>
#include <string>
#include <memory>
#include <map>

/** description of buffer of one FIFO channel*/
struct fifo_buf{
	std::string src; //fifo name
	std::string dst; //fifo name
	void* fifo; //ptr to shared memory
	int fifo_size; // size of the buffer (in tokens)

	fifo_buf(void* fifo, int fifo_size, std::string src, std:: string dst){
		this->fifo = fifo;
		this->fifo_size = fifo_size;
		this->src = src;
		this->dst = dst;
	}
};

struct thread_info{

  char *message;
  pthread_t thread_id; // ID returned by pthread_create()
  int core_id; // Core ID we want this pthread to set its affinity to
  //references to fifos
  std::vector<fifo_buf*> fifo_refs;

  // get fifo by source
  fifo_buf* get_fifo_buf_by_src(std::string srcname){
	  for (auto & fifos_elem : fifo_refs) {
		  if(srcname.compare(fifos_elem->src) == 0)
			  return fifos_elem;
		  }
		  return nullptr;
  }

  // get fifo by name
  fifo_buf* get_fifo_buf_by_dst(std::string dstname){
	  for (auto & fifos_elem : fifo_refs) {
		  if(dstname.compare(fifos_elem->dst) == 0)
			  return fifos_elem;
		  }
		  return nullptr;
  }

  void add_fifo_buf_ref(fifo_buf* fifo_buf_ref){
	  fifo_refs.push_back(fifo_buf_ref);
  }
};

#endif // types_H

