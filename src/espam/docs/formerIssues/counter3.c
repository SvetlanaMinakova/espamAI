/*
 * Testcase with iterator-dependence across multiple loops.
 * Originally, when copy2 was mapped to a LAURA node, the counters would not work properly, because the innermost counter
 * depends on the outermost counter and there is one loop inbetween.
 * Added by Sven van Haastregt
 */
void source(int *d);
void copy1(int s, int *d);
void copy2(int s, int *d);
void sink(int s);

#ifdef __GNUC__
#include <stdio.h>

void source(int *d) {
  static int v = 0x00001;
  *d = v++;
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
//  printf("%08X\n", s);
}
#endif


#define N 5

int main(void)
{
  int j, i, k;
  int a[N+1][N+1][N+1];

  for (i = 1; i <= N; ++i) {
    for (j = 1; j <= N; ++j) {
      for (k = 1; k <= N; ++k) {
        source(&a[i][j][k]);
      }
    }
  }

  for (i = 1; i <= N; ++i) {
    for (j = 1; j <= N; ++j) {
      for (k = 1; k <= N; ++k) {
        copy1(a[i][j][k], &a[i][j][k]);
      }
    }
  }

  for (i = 1; i <= N; ++i) {
    for (j = i; j <= N; ++j) {
      for (k = i; k <= N; ++k) {
        copy2( a[i][j][k], &a[i][j][k] );
      }
    }
  }

  for (i = 1; i <= N; ++i) {
    for (j = i; j <= N; ++j) {
      for (k = i; k <= N; ++k) {
        sink( a[i][j][k] );
      }
    }
  }

}
