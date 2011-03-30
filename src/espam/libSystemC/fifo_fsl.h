/*
 * Timed FIFO
 * Sven van Haastregt
 * LERC, LIACS, Leiden University
 */
#ifndef _FIFO_P2P_H_
#define _FIFO_P2P_H_

#include "systemc.h"

// TODO: does support back-to-back reads, but not in a proper way (read immediately returns)
//

template <class T>
SC_MODULE(fsl) , public sc_fifo<T> {
  public:
    sc_in<bool> clk;
//     sc_in<bool> rst;

    fsl(sc_module_name mn, int size);
    fsl(sc_module_name mn, int size, sc_trace_file *tf);
    //fsl(sc_module_name mn, int size, int rlatency, int wlatency);
    ~fsl();

    sc_signal<bool> exist;
    sc_signal<bool> full;

    void read( T& );
    void write(const T&);

    SC_HAS_PROCESS(fsl);

  private:
    void init(int size, sc_trace_file *tf);
    void fsl_process();

    sc_trace_file *tf;
    int read_pending;
    T read_val;
    sc_event read_done;
    bool *read_pipeline;
    bool *write_pipeline;
    T *read_queue;
    T *write_queue;
    int read_latency;     // Time between read call and return (simulates time to fetch data from buffer)
    int write_latency;    // Time between write call and actual write into buffer; write() returns immediately if not blocking! Multiple writes in subsequent cycles are pipelined.
    int flen;             // Resembling fsl.fifo_length
    int max_tokens;
};


template <class T>
fsl<T>::fsl(sc_module_name mn, int size) : sc_fifo<T> (size) {
  init(size, NULL);
}


template <class T>
fsl<T>::fsl(sc_module_name mn, int size, sc_trace_file *tf) : sc_fifo<T> (size) {
  init(size, tf);
}


template <class T>
fsl<T>::~fsl() {
  delete[] read_pipeline;
  delete[] read_queue;
  delete[] write_pipeline;
  delete[] write_queue;
  cout << sc_module::name() << ": Maximum number of tokens simultaneously in FIFO: " << max_tokens << endl;
}


template <class T>
void fsl<T>::init(int size, sc_trace_file *tf) {
  read_pending = -1;
  read_latency = 1;
  write_latency = 1;
  SC_CTHREAD(fsl_process, clk.pos());
  this->read_pipeline = new bool[read_latency+1];
  this->read_queue = new T[read_latency+1];
  this->write_pipeline = new bool[write_latency+1];
  this->write_queue = new T[write_latency+1];

  for (int i = 0; i <= read_latency; i++) {
    read_pipeline[i] = false;
  }
  for (int i = 0; i <= write_latency; i++) {
    write_pipeline[i] = false;
  }

  this->tf = tf;
  if (tf) {
    std::string sige("E");
    sige.append(sc_module::name());
    sige.append(".exist");
    sc_trace(tf, exist, sige);
    std::string sigf("E");
    sigf.append(sc_module::name());
    sigf.append(".full");
    sc_trace(tf, full, sigf);
  }

  flen = 0;
  exist.write(false);
  full.write(false);
  max_tokens = 0;
}


template <class T>
void fsl<T>::read( T& val_ )
{
  if (this->read_pending == 1) {
    cout << sc_module::name() << ": Warning: pending read will be overwritten!" << endl;
  }

  // Block if empty
  while( this->num_available() == 0 ) {
    cout << sc_module::name() << ": Blocking read at " << sc_time_stamp() << endl;
    sc_core::wait(1, SC_NS);
    //sc_core::wait( this->m_data_written_event );
    //cout << sc_module::name() << ": waking up at " << sc_time_stamp() << endl;
  }

  // Do actual read in next clock cycle
  this->read_pending = 1;//read_latency;

  this->m_num_read++;
  this->buf_read(this->read_val);
  this->request_update();
  //sc_core::wait(this->read_done);

  val_ = read_val;
}

template <class T>
void fsl<T>::write(const T& val) {
  cout << "Write " << sc_module::name() << " at " << sc_time_stamp() << endl;
  if (write_pipeline[0] == true) {
    cout << sc_module::name() << ": Warning: pending write will be overwritten! " << endl;
  }

  // Block if full
  while (this->num_free() == 0) {
    cout << sc_module::name() << ": Blocking write at " << sc_time_stamp() << endl;
    // TODO: shouldn't this be a wait 1 SC_NS as well..?
    sc_core::wait( this->m_data_read_event );
    //sc_core::wait(1, SC_NS);
  }

  // Queue the write operation
  write_queue[0] = val;
  write_pipeline[0] = true;
/*  if (this->write_pending >= 0) {
    cout << sc_module::name() << ": Warning: pending write will be overwritten! " << sc_time_stamp() << endl;
  }

  // Block if full
  while (this->num_free() == 0) {
    sc_core::wait( this->m_data_read_event );
  }

  // Keep value and do actual write in next clock cycle
  this->write_pending = write_latency;
  this->write_val = val;*/
}


// Clocked process
template <class T>
void fsl<T>::fsl_process() {
  while (1) {
    // Advance write queue:
    for (int i = write_latency; i > 0; i--) {
      write_pipeline[i] = write_pipeline[i-1];
      write_queue[i] = write_queue[i-1];
    }
    write_pipeline[0] = false;

    //exist.write(this->num_available() != 0);
    //full.write(this->num_free() == 0);

    if (this->read_pending != 1  &&  write_pipeline[write_latency] == true  &&  full.read()==false) {
      // write and no read: increment
      flen++;
    }
    else if (this->read_pending == 1  &&  write_pipeline[write_latency] == false  &&  exist.read()==true) {
      // read and no write: decrement
      flen--;
    }
    exist.write(flen != 0);
    full.write(flen == this->m_size);

    // Keep track of maximum number of tokens simultaneously in FIFO (which is the maximum buffersize for self-timed execution)
    if (flen > max_tokens)
      max_tokens = flen;

    // Handle read
    if (this->read_pending > 0) {
      this->read_pending--;
    }
    /*else if (this->read_pending == 0) {
      //cout << sc_module::name() << " committing read at " << sc_time_stamp() << endl;
      this->read_pending = -1;
      this->m_num_read++;
      this->buf_read(this->read_val);
      read_done.notify(SC_ZERO_TIME);
      this->request_update();
      //exist.write(this->m_size - this->m_free != 0);
      //full.write(this->m_free == 0);
    }*/
/*
    // Advance read queue
    for (int i = read_latency; i > 0; i--) {
      read_pipeline[i] = read_pipeline[i-1];
      read_queue[i] = read_queue[i-1];
    }
    read_pipeline[0] = false;
    if (read_pipeline[read_latency] == true) {
      // A read is coming out of the queue...:
    }*/

    if (write_pipeline[write_latency] == true) {
      // A write is coming out of the queue, commit it:
      cout << sc_module::name() << " committing write at " << sc_time_stamp() << endl;
      this->m_num_written++;
      this->buf_write(write_queue[write_latency]);
      this->request_update();
      //exist.write(this->m_size - this->m_free != 0);
      //full.write(this->num_free() == 0);
    }

    // Handle write
/*    if (this->write_pending > 0) {
      this->write_pending--;
    }
    else if (this->write_pending == 0) {
      cout << sc_module::name() << " committing write at " << sc_time_stamp() << endl;
      this->write_pending = -1;
      this->m_num_written++;
      this->buf_write(this->write_val);
      this->request_update();
    }*/
    //full.write(this->m_free == 0);
    //exist.write(this->m_size - this->m_free != 0);
    sc_core::wait();
  }
}


/*
template <class T>
SC_MODULE(fifo) , public sc_fifo_in_if<T>,
                  public sc_fifo_out_if<T> {
  public:
    sc_in<bool> clk;
    sc_in<bool> rst;
    //sc_fifo_in<T> in;
    //sc_fifo_out<T> out;
    //sc_port<sc_fifo_in_if<T> > in;
    //sc_port<sc_fifo_out_if<T> > out;
    //sc_in<T> slave;
    //sc_out<T> master;

    fifo(sc_module_name mn, int size);
    ~fifo();

    T read();
    void read(T&);
    bool nb_read( T& );
    void write(const T&);
    bool nb_write(const T&);

    int num_available() const;
    int num_free() const;

    const sc_event& data_read_event() const;
    const sc_event& data_written_event() const;

    SC_HAS_PROCESS(fifo);

  private:
    T *buffer;

  protected:
    sc_event m_data_read_event;
    sc_event m_data_written_event;
};


template <class T>
fifo<T>::fifo(sc_module_name mn, int size) {
//  SC_THREAD(read);
//  sensitive << slave;

  //SC_THREAD(write);
  //sensitive << slave;
  //dont_initialize();
  buffer = new T[size];
}


template <class T>
fifo<T>::~fifo() {
  delete[] buffer;
}


template<class T>
T fifo<T>::read() {
  return NULL;
}

template<class T>
void fifo<T>::read(T&) {
}

template<class T>
bool fifo<T>::nb_read( T& ) {
}

template<class T>
void fifo<T>::write(const T&) {
}

template<class T>
bool fifo<T>::nb_write(const T&) {
}


template<class T>
int fifo<T>::num_available() const {
  return 0;
}

template<class T>
int fifo<T>::num_free() const {
  return 0;
}


template<class T>
const sc_event& fifo<T>::data_read_event() const {
  return m_data_read_event;
}

template<class T>
const sc_event& fifo<T>::data_written_event() const {
  return m_data_written_event;
}
*/

/*template<class T>
class fifo_p2p: public sc_fifo<T> {
  public:
    fifo_p2p(int size) :
      sc_fifo<T> (size) {
    }

    ~fifo_p2p() {
    }

    void write( const T& val ) {
      sc_fifo<T>::write(val);
    }

    T read() {
      return sc_fifo<T>::read();
    }

    sc_in<bool> clk;
    sc_in<bool> rst;
};
*/
#endif
