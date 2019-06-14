#ifndef TYPES_H
#define TYPES_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
#include "xos.h"
#include "colors.h"
#include <float.h>
#include <limits.h>


#ifdef _HALFFLOAT_
#include "hls_half.h"
#endif

#define STRLEN 200
//#define _FIXED_
//#define _FIXED8_


#ifdef _HALFFLOAT_
	typedef half DATA;
	#define FIXED2FLOAT(a) a
	#define FLOAT2FIXED(a) a
	#define _MAX_ 0
	#define _MIN_ 0
#else
	#ifdef _FIXED_
	  #ifdef _FIXED8_
		  typedef int8_t DATA;
		  #define FIRST_ELEM 8
		  #define PRECISION8 1
		  #define DATA_MAX SCHAR_MAX
		  #define FIXPOINT 8
      #define QF 4
		  #define FIXED2FLOAT(a, qf) (((float) (a)) / (1<<qf))
      #define FLOAT2FIXED(a, qf) ((int8_t) round((a) * (1<<qf)))
	    #define _MAX_ (1 << (sizeof(DATA)*8-1))-1
		  #define _MIN_ -(_MAX_+1)
		  #define datatype int8_t
    #elif _FIXED32_
		  typedef int DATA;
		  #define FIRST_ELEM 2
		  #define FIXPOINT 32
      #define QF 13
		  #define FIXED2FLOAT(a, qf) (((float) (a)) / (1<<qf))
		  #define FLOAT2FIXED(a, qf) ((int) round((a) * (1<<qf)))
		  #define _MAX_ (1 << (sizeof(DATA)*8-1))-1
	    #define _MIN_ -(_MAX_+1)
	    #define datatype int
	  #else
		  typedef int16_t DATA;
		  #define FIRST_ELEM 4
		  #define PRECISION8 0
		  #define DATA_MAX SHRT_MAX
		  #define FIXPOINT 16
      #define QF 10
		  #define FIXED2FLOAT(a, qf) (((float) (a)) / (1<<qf))
		  #define FLOAT2FIXED(a, qf) ((int16_t) round((a) * (1<<qf)))
		  #define _MAX_ (1 << (sizeof(DATA)*8-1))-1
	    #define _MIN_ -(_MAX_+1)
	    #define datatype int16_t
	  #endif
	#else
		typedef float DATA;
		#define FIRST_ELEM 2
		#define DATA_MAX FLT_MAX
		#define FIXPOINT 0
		#define datatype float
		#define FIXED2FLOAT(a) a
   		#define FLOAT2FIXED(a) a
		#define _MAX_ 0
		#define _MIN_ 0
	#endif 
 #endif


#define MAX_POOL 0
#define AVG_POOL 1
#define SUBSAMP  2

#define POOL_2x2 1
#define POOL_4x4 3

typedef const size_t SIZE;
typedef size_t VARSIZE;
typedef const char* NAME;
typedef char VARNAME[STRLEN];
typedef int ID;
typedef void* USER_DATA;

int equalSize(SIZE* a, SIZE* b, SIZE nelements);
RET assignSize(VARSIZE* target, SIZE* source, SIZE nelements);
RET loadData(const char* filename, size_t arraysize, DATA* array);
RET load_f62tof32_from_file(float* array, const char* filename, size_t arraysize);
RET load_f62tofp16_from_file(int16_t* array, const char* filename, size_t arraysize);
RET load_f62tofp8_from_file(signed char* array, const char* filename, size_t arraysize);

unsigned long get_wall_time(void);

#define MAX_JOBS 15

typedef struct Conv_params {
  unsigned int IF;
  unsigned int OF;
  unsigned int IH;
  unsigned int IW;
  unsigned int FS;
  unsigned int MAX_OG;
  unsigned int max_stores;
  unsigned int w_ext;
  unsigned int x_ext;
  unsigned int y_ext;
  unsigned int rectifier_activ;
  unsigned int pooling_hw;
  unsigned int qf;
  //unsigned int precision8;
  unsigned int zero_padding;
  unsigned int run;
} Conv_params;

#ifdef __cplusplus
}
#endif

#endif
