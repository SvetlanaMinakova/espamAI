#ifndef SPATCONV_H
#define SPATCONV_H

#ifdef __cplusplus
extern "C" {
#endif

#include "types2.h"
#include "soc_drivers.h"
#include <stdbool.h>
#include "arm_neon.h"

typedef struct Convlayer* CONVLAYER;
typedef struct Spatconv*  SPATCONV;
typedef struct Spatparam  SPATPARAM;

SPATCONV spatconv_create(void);
CONVLAYER convlayer_create(void);

RET spatconv_init(SPATCONV sc, NAME weightsFile, NAME biasFile, SIZE pin,
		SIZE pout, SIZE kern_s[2], DATA** wPointer);

/*RET spatconv_forward_sw(SPATCONV sc, DATA* input, DATA* output, SIZE in_s[3],
                     	SIZE out_s[3], SIZE stride[2], SIZE pad[2], bool activate = false, int ncol=4);
*/

RET spatconv_forward_sw(CONVLAYER sc, DATA* weights, DATA* output, DATA* input, SIZE in_s[3],
                     	SIZE out_s[3], SIZE stride[2], SIZE pad[2], bool activate, int ncol, int qf);

RET zero_pad(DATA* input, DATA* output, SIZE in_s[3], SIZE pad[2]);

RET spatconv_merge(DATA* out, DATA** b, SIZE out_s[3], SIZE kernel_size, SIZE stride[2], bool activate);

RET spatconv_destroy(SPATCONV sc);

/*RET spatconv_sw(DATA* input, DATA* output, DATA* kernel,
		DATA* bias, SIZE in_s[3], SIZE out_s[3], SIZE kern_s[2], SIZE pad[2],
		SIZE stride[2], bool activate = false, int ncol=4);
*/
RET spatconv_sw(DATA* input, DATA* output, DATA* kernel,
		DATA* bias, SIZE in_s[3], SIZE out_s[3], SIZE kern_s[2], SIZE pad[2],
		SIZE stride[2], bool activate, int ncol, int qf);

static inline long long int saturate(long long int mac, const char* module)
{

	if(mac > _MAX_) {
		//printf(RED "%s mac: %lld -> %llx _MAX_: %d  _MIN_: %d  res: %d\n" NC, module, mac, mac, _MAX_, _MIN_, _MAX_);
		return _MAX_;
	}

	if(mac < _MIN_){
		//printf(RED "%s mac: %lld -> %llx _MAX_: %d  _MIN_: %d  res: %d\n" NC, module, mac, mac, _MAX_, _MIN_, _MIN_);
		return _MIN_;
	}

	//printf("mac: %lld -> %llx _MAX_: %lld  _MIN_: %lld  res: %lld\n", mac, mac, _MAX_, _MIN_, mac);
    return mac;

}

struct Spatparam{
    int in_s  [3];
    int out_s [3];
    int stride[2];
    int pad   [2];
    bool activate;
};

struct Spatconv {
	DATA* kernel;
	DATA* bias;
	VARSIZE pin;
	VARSIZE pout;
	VARSIZE kern_s[4];
	VARSIZE maxog;
	VARSIZE IH;
	VARSIZE IW;

	//TODO: move here SOCMAP that are at the moment passed as argument.
	//Note: in this way we can have same API for spatconv_forward.
	//
	//Neuraghe_map dev_map;
	//
	//Where the structure will be defined as follow inside soc_driver.h:
	//
	//	//One Map for each layer!
	//	typedef struct Neuraghe_map_s {
	//		void* dev_in;
	//		void* dev_out;
	//		void* dev_w;
	//	} Neuraghe_map;
	//
	//  //Neuraghe Device Control
	// 	struct Socmap {
	// 		int* soc_addr;
	// 		int* soc_cntr_addr;
	// 		int* ddr_addr;
	// 		int* ps7_slcr_addr;
	// 		Neuraghe_map *dev_maps;
	// 	};
	//
	//  typedef struct Socmap* SOCMAP;
	//  typedef SOCMAP Neuraghe_dev;
	//
};


struct Convlayer {
       
                  DATA*    kernel;
	          DATA*    bias;
	          VARSIZE  pin;
	          VARSIZE  pout;
	          VARSIZE  kern_s[4];
                  int     in_s[3];
                  int     out_s[3];
                  int     stride[2];
                  int     pad[2];
                  int     activate;
                  int     pooling;
                  int      maxog;
};


#ifdef __cplusplus
}
#endif
#endif
