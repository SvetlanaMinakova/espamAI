//the topology and WCETs mimic one band in FM radio
//

// define UNFOLDING_FACTORS in the header, it will be
// updated during DSE
#include "fm.h"

#define EQ_BANDS 6

void source(int* in);
void transform(const int* in, int* out);
void sink(const int* out);

void lp(int* in);
void demod(const int* in, int* x1, int* x2, int* x3, int* x4, int* x5, int* x6);

void lp2(const int* in, int* out);
void sub(const int* in, const int* in1, int* out);

void annot(const int* in, int* out);

void add(const int* in1, const int* in2, const int* in3, const int* in4,
         const int* in5, const int* in6);

int main(){
  int i, j, k;
  int x[10][UNFOLD_FACTOR], y[10][UNFOLD_FACTOR];
  int x1[10][UNFOLD_FACTOR], x2[10][UNFOLD_FACTOR];
  int x3[10][UNFOLD_FACTOR], x4[10][UNFOLD_FACTOR];
  int x5[10][UNFOLD_FACTOR], x6[10][UNFOLD_FACTOR];
  int x7[10][UNFOLD_FACTOR], x8[10][UNFOLD_FACTOR];
//   for (t = 1 ; t<=3; t++){
    
    // original execution
    for (i = 1; i<=10; i++){
        for (j = 1 ; j<= UNFOLD_FACTOR; j++){
            lp(&(x[i][j]));
            demod(&(x[i][j]),
                    &(x1[i][j]), &(x2[i][j]),
                    &(x3[i][j]), &(x4[i][j]),
                    &(x5[i][j]), &(x6[i][j]));
          
            for (k = 0 ; k< EQ_BANDS; k++){
                if (k == 0){
                    lp2(&(x1[i][j]), &(x[i][j]));
                    lp2(&(x1[i][j]), &(y[i][j]));
                    
                    sub(&(x[i][j]), &(y[i][j]), &(x[i][j]));
                    
                    annot(&(x[i][j]), &(x1[i][j]));
                } else if (k == 1){
                    lp2(&(x2[i][j]), &(x[i][j]));
                    lp2(&(x2[i][j]), &(y[i][j]));
                    
                    sub(&(x[i][j]), &(y[i][j]), &(x[i][j]));
                    
                    annot(&(x[i][j]), &(x2[i][j]));
                } else if (k == 2){
                    lp2(&(x3[i][j]), &(x[i][j]));
                    lp2(&(x3[i][j]), &(y[i][j]));
                    
                    sub(&(x[i][j]), &(y[i][j]), &(x[i][j]));
                    
                    annot(&(x[i][j]), &(x3[i][j]));
                } else if (k == 3){
                    lp2(&(x4[i][j]), &(x[i][j]));
                    lp2(&(x4[i][j]), &(y[i][j]));
                    
                    sub(&(x[i][j]), &(y[i][j]), &(x[i][j]));
                    
                    annot(&(x[i][j]), &(x4[i][j]));
                } else if (k == 4){
                    lp2(&(x5[i][j]), &(x[i][j]));
                    lp2(&(x5[i][j]), &(y[i][j]));
                    
                    sub(&(x[i][j]), &(y[i][j]), &(x[i][j]));
                    
                    annot(&(x[i][j]), &(x5[i][j]));
                } else if (k == 5){
                    lp2(&(x6[i][j]), &(x[i][j]));
                    lp2(&(x6[i][j]), &(y[i][j]));
                    
                    sub(&(x[i][j]), &(y[i][j]), &(x[i][j]));
                    
                    annot(&(x[i][j]), &(x6[i][j]));
                }
            }         
            add(&(x1[i][j]), &(x2[i][j]), &(x3[i][j]), &(x4[i][j]), &(x5[i][j]), &(x6[i][j]));
        }
    }       
    
    // plain blocking
//     for (i = 1; i<=10; i++){
//       for (j = 1 ; j<= 10; j++){
// 	if (i <= 5){
// 	  transform(&(x[i][j]), &(x[i][j+1]));
// 	} else {
// 	  transform(&(x[i][j]), &(x[i][j+1]));
// 	}
//         
//         sink(&(x[i][j+1]));	
//       }
//     }

    
    // modulo (2 gives the maximum performanace)
//     for (i = 1; i<=100; i++){
//       for (j = 1 ; j<=100; j++){
// 	if (j%2 == 0){
// 	  transform(&(x[i][j]), &(x[i+2][j]));
// 	} else {
// 	  transform(&(x[i][j]), &(x[i+2][j]));
// 	}
// 	
// 	sink(&(x[i+2][j]));
//       }
//     }
     
  
//   }
  return 0;
}
