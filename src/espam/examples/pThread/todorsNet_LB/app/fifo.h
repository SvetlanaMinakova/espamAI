

void writeSWF_CPU(void* fifo, void* memobj_cpu, int len, int fifo_size);

void readSWF_CPU(void* fifo, void* memobj_cpu, int len, int fifo_size);

int FIFOisFull(void *fifo);

int FIFOisEmpty(void *fifo);

void setaffinity(int core);





