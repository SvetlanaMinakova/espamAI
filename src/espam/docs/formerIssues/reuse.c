/*
 * Testcase with reuse
 * Reuse was not properly supported by ESPAM's LAURA visitor.
 * Added by Sven van Haastregt
 */
void source(int *d);
void copy1(int s, int *d);
void copy2(int s, int *d);
void addd(int a, int b, int *d, int *x);
void sink(int s);

#ifdef __GNUC__
#include <stdio.h>

void source(int *d) {
  static int v = 0x00001;
  *d = v++;
}

void addd(int a, int b, int *d, int *x) {
  *d = a + b;
  *x = 1;
}

void copy1(int s, int *d) {
//  printf("%08X\n", s);
  *d = s;
}

void copy2(int s, int *d) {
//  printf("%08X\n", s);
  *d = s;
}

void sink(int s) {
  printf("%08X\n", s);
}
#endif


#define N 5

int main(void)
{
  int j, i, k;
  int a[N+1][N+1];
  int b[N+1][N+1];
  int x[N+1][N+1];

  for (i = 1; i <= N; ++i) {
    for (j = 1; j <= N; ++j) {
      source(&a[i][j]);
    }
  }

  for (i = 1; i <= N; ++i) {
    for (j = 1; j <= N; ++j) {
      copy1(a[i][j], &a[i][j]);
    }
  }
  for (i = 1; i <= N; ++i) {
    for (j = 3; j <= N; ++j) {
      addd( a[i][j-2], a[i][j], &b[i][j], &x[i][j]);
    }
  }

  for (i = 1; i <= N; ++i) {
    for (j = 3; j <= N; ++j) {
      sink( b[i][j] );
    }
  }

}
