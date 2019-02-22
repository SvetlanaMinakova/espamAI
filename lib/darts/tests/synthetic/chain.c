/* 
	chain: the first example 
*/

#define M 1000

// define UNFOLD_FACTOR in the header, it will be updated during DSE
#include "chain1.h"

void source(int* in);
void transform(const int* in, int* out);
void sink(const int* out);

int main() 
{
	int i, j, t;
	int x[M][UNFOLD_FACTOR], y[M][UNFOLD_FACTOR];

	for (i = 1; i<=M; i++) {
		for (j = 1; j<=UNFOLD_FACTOR; j++) {
			source(&(x[i][j]));
			transform(&(x[i][j]), &(x[i][j]));
			sink(&(x[i][j]));
		}
	}
	return 0;
}
