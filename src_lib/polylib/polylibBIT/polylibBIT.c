/*
 * Copyright (c) 1999 Delft University of Technology All rights
 * Copyright (c) 2000 - 2005 Leiden University (LERC group at LIACS).
 * Copyright (c) 2005 - 2007 Compaan Design BV, Netherlands
 * 
 * Permission is granted to copy, use, and distribute for any commercial or noncommercial 
 * purpose under the terms of the GNU General Public license, version 2, June 1991
 * (see file : LICENSE_GPL).
 * 
 * @author Edwin Rijpkema, Bart Kienhuis
 * @version $Revision: 1.1 $
*/

// jni assumes MSVC and __int64
#ifdef __GNUC__
typedef long long __int64;
#endif

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
//#include <unistd.h>

/* #define PCHAR 'O'	       first-1 parameter name char. */

#ifdef BIT64
#include <polylib/polylib64.h>
#else
#include <polylib/polylib32.h>
#endif
#include <polylib/ranking.h>

/* JNI SPECIFIC */
#include <jni.h>
#include "espam_utils_polylib_polylibBIT_PolyLibBIT.h"

/* Polylib SPECIFIC */
#include "polylibBIT.h"
#define REDUCE_DEGREE

#ifdef DEGENERATE
extern int degenerate;
#else
static int degenerate = 0;
#endif

static int dimension;
static JNIEnv *envi ;
static jclass jclazz;
static jobject jfunction;

static jclass theClass;
static jmethodID functionID ; 
static jmethodID setID; 

void push_context(char * file, char * function, int line)
{
  /*
    pips_debug(9, "%s %s:%d\n", function, file, line);
  if (!debug_stack) debug_stack = stack_make(0, 50, 0);
  stack_push((void*) get_debug_stack_pointer(), debug_stack);
  */
  printf(" EXCEPITION: function:%s file:%s line:%d ", function, file, line);
}

void pop_context(char * file, char * function, int line)
{
  /*
    pips_debug(9, "%s %s:%d\n", function, file, line);
  if (!debug_stack)
  pips_internal_error("unexpected pop without push %s %s:%d\n",
    function, file, line);
			set_debug_stack_pointer((int) stack_pop(debug_stack));
  */
  printf(" EXCEPITION: function:%s file:%s line:%d ", function, file, line);

}

/***********************************************************************/
/*			                                               */
/*  Here is where the real interface begins:                           */
/*  The functions in this section are the function called via the via  */
/*  the JNI. In fact the function are wrappers around the C function   */
/*  implemention.                                                      */
/*  A function named "F()" in java is declared in java calls the       */
/*  function "Java_espam_utils_polylib_PolyLib_F()" in C.                    */
/*  In C we implement this function by wrapping conversions around a   */
/*  call to the function "F()". Before we call "F()" we convert the    */
/*  java types to C types. After the call we convert C types back to   */
/*  java types and return the value back to the Java world.            */
/*  First I give some test functions followed by my additional         */
/*  functions.                                                         */
/*  Then I give the PolyLib Function Interface (PFI).                  */
/*			                                               */
/***********************************************************************/

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    JMatrix2JMatrix
 * Signature: (Lespam/utils/symbolic/matrix/JMatrix;)Lespam/utils/symbolic/matrix/JMatrix;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_JMatrix2JMatrix
(JNIEnv *env, jclass clazz, jobject aJMatrix) {
    Matrix *Mat;
    jobject newJMatrix;

    Mat = JMatrix2CMatrix( env, aJMatrix );
    /*                                           */
    /* Stuff that must be wrapped is put in here */
    /* So according to the scheme we must put    */
    /* newMatrix = Matrix2Matrix(Mat);           */
    /* But for this example it is quit stupid to */
    /* do that.                                  */
    /*                                           */
    newJMatrix = CMatrix2JMatrix( env, Mat );
    
    return newJMatrix;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    Polyhedron2Polyhedron
 * Signature: (Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_Polyhedron2Polyhedron
(JNIEnv *env, jclass clazz, jobject aJPol) {

    Polyhedron *Pol = JPolyhedron2CPolyhedron( env, aJPol );
    jobject newJPol = CPolyhedron2JPolyhedron( env, Pol );
    
    return newJPol;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    ConstraintsSimplify
 * Signature: (Lespam/utils/symbolic/matrix/JMatrix;Lespam/utils/symbolic/matrix/JMatrix;)Lespam/utils/symbolic/matrix/JMatrix;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_ConstraintsSimplify
(JNIEnv *env, jclass clazz, jobject JMatrixD, jobject JMatrixC) {

    Matrix *MatD = JMatrix2CMatrix( env, JMatrixD );
    Matrix *MatC = JMatrix2CMatrix( env, JMatrixC );
    
    Matrix *MatS = ConstraintsSimplify( MatD, MatC );
    
    jobject JMatrixS = CMatrix2JMatrix( env, MatS );

    Matrix_Free( MatD );
    Matrix_Free( MatC );
    Matrix_Free( MatS );

    return JMatrixS;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainIntersection
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainIntersection
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Polyhedron *d2 = JPolyhedron2CPolyhedron( env, jD2 );

    Polyhedron *d  = DomainIntersection( d1, d2, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );
    
    Domain_Free( d1 );
    Domain_Free( d2 );
    Domain_Free( d  );

    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainUnion
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainUnion
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Polyhedron *d2 = JPolyhedron2CPolyhedron( env, jD2 );

    Polyhedron *d  = DomainUnion( d1, d2, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );
    
    Domain_Free( d1 );
    Domain_Free( d2 );
    Domain_Free( d  );

    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainDifference
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainDifference
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Polyhedron *d2 = JPolyhedron2CPolyhedron( env, jD2 );

    Polyhedron *d  = DomainDifference( d1, d2, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );
    
    Domain_Free( d1 );
    Domain_Free( d2 );
    Domain_Free( d  );

    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainSimplify
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainSimplify
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) { 
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Polyhedron *d2 = JPolyhedron2CPolyhedron( env, jD2 );

    Polyhedron *d  = DomainSimplify( d1, d2, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );
    
    Domain_Free( d1 );
    Domain_Free( d2 );
    Domain_Free( d  );

    return jD;

}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainConvex
 * Signature: (Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainConvex
(JNIEnv *env, jclass clazz, jobject jD1) {

    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );

    Polyhedron *d  = DomainConvex( d1, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );
    
    Domain_Free( d1 );
    Domain_Free( d  );

    return jD;   
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainImge
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/symbolic/matrix/JMatrix;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainImage
(JNIEnv *env, jclass clazz, jobject jD1, jobject jM) {

    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Matrix     *m  = JMatrix2CMatrix( env, jM );

    Polyhedron *d  = DomainImage( d1, m, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );

    Domain_Free( d1 );
    Matrix_Free( m  );
    Domain_Free( d  );

    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainPreimage
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/symbolic/matrix/JMatrix;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainPreimage
(JNIEnv *env, jclass clazz, jobject jD1, jobject jM) {

    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Matrix     *m  = JMatrix2CMatrix( env, jM );

    Polyhedron *d  = DomainPreimage( d1, m, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );

    Domain_Free( d1 );
    Matrix_Free( m  );
    Domain_Free( d  );    

    return jD ;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    Constraints2Polyhedron
 * Signature: (Lespam/utils/symbolic/matrix/JMatrix;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_Constraints2Polyhedron
(JNIEnv *env, jclass clazz, jobject jM) {
    Matrix     *m;
    Polyhedron *d;
    jobject    jD;
    
    m = JMatrix2CMatrix( env, jM );
    
    /*
      printf("in interface.c: 525.  m = \n");
      Matrix_Print(stdout, P_VALUE_FMT, m);
      printf("--------------------------------------------\n");
     */

    d = Constraints2Polyhedron( m, 8096 );

    /*
      printf("in interface.c: 531.  m = \n");
      Matrix_Print(stdout, P_VALUE_FMT, m);
      printf("--------------------------------------------\n");
     */

    jD = CPolyhedron2JPolyhedron( env, d );
     
    Matrix_Free( m );
    Domain_Free( d );

    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    Rays2Polyhedron
 * Signature: (Lespam/utils/symbolic/matrix/JMatrix;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_Rays2Polyhedron
(JNIEnv *env, jclass clazz, jobject jM) {
    
    Matrix *m = JMatrix2CMatrix( env, jM );

    Polyhedron *d = Rays2Polyhedron( m, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );
    
    Matrix_Free( m );
    Domain_Free( d );

    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    UniversePolyhedron
 * Signature: (I)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_UniversePolyhedron
(JNIEnv *env, jclass clazz, jint dimension) {

    Polyhedron *d = Universe_Polyhedron( (unsigned) dimension );
    jobject jD = CPolyhedron2JPolyhedron( env, d );
    Domain_Free( d );
    
    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    EmptyPolyhedron
 * Signature: (I)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_EmptyPolyhedron
(JNIEnv *env, jclass clazz, jint dimension) {

    Polyhedron *d = Empty_Polyhedron( (unsigned) dimension );

    jobject jD = CPolyhedron2JPolyhedron( env, d );

    Domain_Free( d );
    
    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    DomainCopy
 * Signature: (Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_DomainCopy
(JNIEnv *env, jclass clazz, jobject jD1) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    
    Polyhedron *d = Domain_Copy( d1 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );

    Domain_Free( d1 );
    Domain_Free( d  );
    
    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    Polyhedron2Constraints
 * Signature: (Lespam/utils/polylib/Polyhedron;)Lespam/utils/symbolic/matrix/JMatrix;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_Polyhedron2Constraints
(JNIEnv *env, jclass clazz, jobject jD1) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Matrix *m = Polyhedron2Constraints( d1 );
    jobject jM = CMatrix2JMatrix( env, m );
    Domain_Free(d1);
    Matrix_Free(m);

    return jM;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    PolyhedronScan
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Polyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_PolyhedronScan
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Polyhedron *d2 = JPolyhedron2CPolyhedron( env, jD2 );

    Polyhedron *d  = Polyhedron_Scan( d1, d2, 8096 );

    jobject jD = CPolyhedron2JPolyhedron( env, d );
    
    Domain_Free( d1 );
    Domain_Free( d2 );
    Domain_Free( d  );
    
    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    Polyhedron2ParamVertices
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/ParamPolyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_Polyhedron2ParamVertices
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Polyhedron *d2 = JPolyhedron2CPolyhedron( env, jD2 );
    Param_Polyhedron * p = Polyhedron2Param_Vertices( d1, d2, 8096 );
    jobject jD = CParamPolyhedron2JParamPolyhedron( env, p );
    Domain_Free( d1 );
    Domain_Free( d2 );
    Param_Polyhedron_Free( p );
    
    return jD;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    Polyhedron2ParamDomain
 * Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/ParamPolyhedron;
 */
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_Polyhedron2ParamDomain
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) {
    
    Polyhedron *d1 = JPolyhedron2CPolyhedron( env, jD1 );
    Polyhedron *d2 = JPolyhedron2CPolyhedron( env, jD2 );
    
    Param_Polyhedron * p = Polyhedron2Param_Domain( d1, d2, 8096 );

    jobject jD = CParamPolyhedron2JParamPolyhedron( env, p );

    Domain_Free( d1 );
    Domain_Free( d2 );
    Param_Polyhedron_Free( p );

    return jD;
}


/***********************************************************************/
/*			                                               */
/* PFI part II (PolyLib Function Interface)                            */
/*   This second part describes the interface for the Ehrhart related  */
/*   functions.		                                               */
/*			                                               */
/***********************************************************************/

/*
* Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
* Method:    PolyhedronEnumerate
* Signature: (Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/Polyhedron;)Lespam/utils/polylib/Enumeration;
*/
JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_PolyhedronEnumerate
(JNIEnv *env, jclass clazz, jobject jD1, jobject jD2) {
    Polyhedron  *d1, *d2;
    Enumeration *e;
    jobject     jE;
	
    //mtrace(); 
    // set_exception_callbacks(push_context, pop_context);
	
    d1 = JPolyhedron2CPolyhedron( env, jD1 );    
    d2 = JPolyhedron2CPolyhedron( env, jD2 );
	
	
#ifdef POLYLIB511
    // For new release of polylib 5.11 and higher
    e  = Polyhedron_Enumerate( d1, d2, 8096, NULL );
#else
    e  = Polyhedron_Enumerate( d1, d2, 8096 );
#endif

    Polyhedron_Free(d1);
    Polyhedron_Free(d2);

    if ( e != NULL ) {
		jE = CEnumeration2JEnumeration( env, e);
		Enumeration_Free(e);
    } else {
        jE = NULL;
    }
    
    return jE;
}

JNIEXPORT jobject JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_LexSmallerEnumerate
(JNIEnv *env, jclass clazz, jobject jP, jobject jD, jint dim, jobject jC) {
    Matrix  *mP, *mD, *mC;
    Polyhedron  *P, *D, *C;
    Enumeration *e;
    jobject     jE;
	
    //mtrace(); 
    // set_exception_callbacks(push_context, pop_context);
	
    mP = JMatrix2CMatrix(env, jP);
    mD = JMatrix2CMatrix(env, jD);
    mC = JMatrix2CMatrix(env, jC);

    P = Constraints2Polyhedron(mP, POL_NO_DUAL);
    D = Constraints2Polyhedron(mD, POL_NO_DUAL);
    C = Constraints2Polyhedron(mC, POL_NO_DUAL);

    Matrix_Free(mP);
    Matrix_Free(mD);
    Matrix_Free(mC);

    e = Polyhedron_LexSmallerEnumerate(P, D, dim, C, POL_NO_DUAL);

    Polyhedron_Free(P);
    Polyhedron_Free(D);
    Polyhedron_Free(C);

    if ( e != NULL ) {
		jE = CEnumeration2JEnumeration( env, e);
		Enumeration_Free(e);
    } else {
		jE = NULL;
    }
    
    return jE;
}

/***********************************************************************/
/*                                                                     */
/*                   Java --> C    conversions                         */
/*                                                                     */
/***********************************************************************/

/**
 * Converts a Java Matrix to a PolyLib Matrix
 */
Matrix *JMatrix2CMatrix(JNIEnv *env, jobject aJMatrix) {
    Matrix *Mat = NULL;
    Value *p=NULL;
    int NbRows = 0;
    int NbColumns = 0;
    int i=0;
    int j=0;
    jclass    classJMatrix;
    jmethodID getElementID;
    jfieldID  fid;
    
    classJMatrix = (*env)->GetObjectClass(env, aJMatrix);
    getElementID = (*env)->GetMethodID(env, classJMatrix, "getElement", 
            "(II)J");

    fid          = (*env)->GetFieldID(env, classJMatrix, "_nbRows", "I");
    NbRows       = (int)(*env)->GetIntField(env, aJMatrix, fid);

    fid          = (*env)->GetFieldID(env, classJMatrix, "_nbColumns", "I");
    NbColumns    = (int)(*env)->GetIntField(env, aJMatrix, fid);

    Mat = Matrix_Alloc(NbRows, NbColumns);
    if (!Mat) {
	errormsg1("Matrix_Read", "outofmem", "out of memory space");
	exit(0);
    }
    p = Mat->p_Init;
    
    for (j=0; j<NbRows; j++) {
	for (i=0; i<NbColumns; i++) {
            *p = (Value) (*env)->CallLongMethod(env, aJMatrix, 
                    getElementID, (jint) j, (jint) i);
            p++;
	}
    }

    return Mat;
}

/**
 * Converts a Java Polyhedron to a C Polyhedron
 *
 */
Polyhedron *JPolyhedron2CPolyhedron(JNIEnv *env, jobject aJPol) {
    int j,i;

    jclass cls = (*env)->GetObjectClass(env, aJPol);

    jfieldID dimID= (*env)->GetFieldID(env, cls, "Dimension"    , "I");
    jfieldID nbcID= (*env)->GetFieldID(env, cls, "NbConstraints", "I");
    jfieldID nbrID= (*env)->GetFieldID(env, cls, "NbRays"       , "I");
    jfieldID nbeID= (*env)->GetFieldID(env, cls, "NbEq"         , "I");
    jfieldID nbbID= (*env)->GetFieldID(env, cls, "NbBid"        , "I");

    unsigned Dimension     = (*env)->GetIntField(env, aJPol, dimID);
    unsigned NbConstraints = (*env)->GetIntField(env, aJPol, nbcID);
    unsigned NbRays        = (*env)->GetIntField(env, aJPol, nbrID);
    unsigned NbEq          = (*env)->GetIntField(env, aJPol, nbeID);
    unsigned NbBid         = (*env)->GetIntField(env, aJPol, nbbID);

    Polyhedron *Pol = Polyhedron_Alloc(Dimension, NbConstraints, NbRays);
    /*The next line is maybe not fool proof, when an error occurs
      consider to change it to Pol->Constraint[0], and further in
      the code to Pol->Ray[0]*/
    Value *p = Pol->p_Init;

    jmethodID getConstraintID = (*env)->GetMethodID(env, cls, 
            "getConstraint", "(II)J");
    jmethodID getRayID        = (*env)->GetMethodID(env, cls, 
            "getRay", "(II)J");

    jmethodID hasNextID = (*env)->GetMethodID(env, cls, "hasNext", "()Z");
    jmethodID nextID = (*env)->GetMethodID(env, cls, "next", 
            "()Lespam/utils/polylib/Polyhedron;");

    Pol->NbEq  = (unsigned) NbEq;
    Pol->NbBid = (unsigned) NbBid;

    if (!Pol) {
	errormsg1("Polyhedron_Read", "outofmem", "out of memory space");
	exit(0);
    }

    for (j=0; j<NbConstraints; j++) {
	for (i=0; i<Dimension+2; i++) {
	    *p = (Value) (*env)->CallLongMethod(env, aJPol, 
                    getConstraintID, (jint) j, (jint) i);
            p++;
	}
    }
    for (j=0; j<NbRays; j++) {
	for (i=0; i<Dimension+2; i++) {
	    *p = (Value) (*env)->CallLongMethod(env, aJPol, getRayID, 
                    (jint) j, (jint) i);
            p++;
	}
    }

    if ( (*env)->CallBooleanMethod(env, aJPol, hasNextID ) ) {
	Pol->next = JPolyhedron2CPolyhedron( env, 
                (*env)->CallObjectMethod(env, aJPol, nextID) );
    }

    return Pol;
}


/***********************************************************************/
/*                                                                     */
/*                   C --> Java    conversions                         */
/*                                                                     */
/***********************************************************************/

/**
 * Converts a C Matrix to a Java Matrix
 */
jobject CMatrix2JMatrix(JNIEnv *env , Matrix *Mat) {

    int i,j;
    jclass    theClass     = (*env)->FindClass(env, 
            "espam/utils/symbolic/matrix/JMatrix");
    jmethodID aMethodID    = (*env)->GetMethodID(env, theClass, 
            "<init>", "(II)V");
    jobject   aJMatrix     = (*env)->NewObject(env, theClass, aMethodID, 
            (jint) Mat->NbRows, (jint) Mat->NbColumns);
    jmethodID setElementID = (*env)->GetMethodID(env, theClass, 
            "setElement", "(IIJ)V");
    Value *p = Mat->p_Init;
    
    for (j=0; j<Mat->NbRows; j++) {
        for (i=0; i<Mat->NbColumns; i++) {
            (*env)->CallVoidMethod(env, aJMatrix, setElementID, (jint) j, 
                    (jint) i, (jlong) *(p++));
        }
    }
    
    return aJMatrix;    
}

/**
 * Converts a C Polyhedron to a Java Polyhedron
 */
jobject  CPolyhedron2JPolyhedron( JNIEnv *env, Polyhedron *Pol) {
    int i,j;
    jclass    theClass;
    jmethodID mid, setConstraintID, setRayID, addID;
    jobject aJPol;
    Value *p;

    if (!Pol)
	return NULL;
    
    theClass = (*env)->FindClass(env, "espam/utils/polylib/Polyhedron");
    mid      = (*env)->GetMethodID(env, theClass, "<init>", "(IIIII)V");
    aJPol    = (*env)->NewObject(env, theClass, mid, (jint) Pol->Dimension,
            (jint) Pol->NbConstraints, (jint) Pol->NbRays,
            (jint) Pol->NbEq, (jint) Pol->NbBid  );
    
    setConstraintID = (*env)->GetMethodID(env, theClass, "setConstraint", 
            "(IIJ)V");
    setRayID        = (*env)->GetMethodID(env, theClass, "setRay","(IIJ)V");
    addID = (*env)->GetMethodID(env, theClass, "add", 
            "(Lespam/utils/polylib/Polyhedron;)V");
    /* Value *p = Pol->p_Init;*/
    p = Pol->Constraint[0];

    for (j=0; j<Pol->NbConstraints; j++) {
	for (i=0; i<Pol->Dimension+2; i++) {
	    (*env)->CallVoidMethod(env, aJPol, setConstraintID, (jint) j, 
                    (jint) i, (jlong) *(p++));
	}
    }
    p = Pol->Ray[0];
    for (j=0; j<Pol->NbRays; j++) {
	for (i=0; i<Pol->Dimension+2; i++) {
	    (*env)->CallVoidMethod(env, aJPol, setRayID, (jint) j, 
                    (jint) i, (jlong) *(p++));
	}
    }
    
    if (Pol->next) {
	(*env)->CallVoidMethod(env, aJPol, addID, 
                CPolyhedron2JPolyhedron(env, Pol->next)  );
    }
    return aJPol;
}

/**
 * Converts a C Enumeration to a Java Enumeration
 */
jobject CEnumeration2JEnumeration( JNIEnv *env, Enumeration *e) {
    jclass    theClass;
    jmethodID aMethodID, addID;
    jobject ValidityDomain, EV, jE;
    
    theClass     = (*env)->FindClass(env, "espam/utils/polylib/Enumeration");
    aMethodID    = (*env)->GetMethodID(env, theClass, "<init>", 
            "(Lespam/utils/polylib/Polyhedron;Lespam/utils/polylib/EValue;)V" );
    addID = (*env)->GetMethodID(env, theClass, "add", 
            "(Lespam/utils/polylib/Enumeration;)V");

    ValidityDomain = CPolyhedron2JPolyhedron( env, e->ValidityDomain );
    EV             = CEValue2JEValue( env, &(e->EP) );
    jE             = (*env)->NewObject(env, theClass, aMethodID, 
            ValidityDomain, EV);
    
    if ( e->next ) {
	(*env)->CallVoidMethod(env, jE, addID, 
                CEnumeration2JEnumeration(env, e->next)  );	
    }
    
    return jE;    
}

/**
 * Converts a C evalue to a Java EValue
 */
jobject CEValue2JEValue( JNIEnv *env, evalue *e) {
    jclass    theClass     = (*env)->FindClass(env, "espam/utils/polylib/EValue");
    jmethodID constDnotZeroID = (*env)->GetMethodID(env, theClass, "<init>", 
            "(II)V");
    jmethodID constDisZeroID  = (*env)->GetMethodID(env, theClass, "<init>", 
            "(Lespam/utils/polylib/ENode;)V");
   jobject EV;
   
    if( value_notzero_p(e->d) ) {
	EV = (*env)->NewObject(env, theClass, constDnotZeroID, (jint) e->d, 
                (jint) e->x.n);
    } else {
	EV = (*env)->NewObject(env, theClass, constDisZeroID, 
                CENode2JENode(env, e->x.p) );
    }
   return EV;
}

/**
 * Converts a C enode to a Java ENode
 */
jobject CENode2JENode( JNIEnv *env, enode *p ) {
    int i;
    jclass    theClass     = (*env)->FindClass(env, "espam/utils/polylib/ENode");
    jmethodID constrID = (*env)->GetMethodID(env, theClass, "<init>", 
            "(IILjava/lang/String;)V");
    jmethodID setEValueID = (*env)->GetMethodID(env, theClass, "setEValue", 
            "(ILespam/utils/polylib/EValue;)V");
    jstring jS;
    jobject EN;

    if (p->type == polynomial) {
	jS = (*env)->NewStringUTF(env, "polynomial");
    } else {
	jS = (*env)->NewStringUTF(env, "periodic");
    }

    EN = (*env)->NewObject(env, theClass, constrID, (jint) p->size, 
            (jint) p->pos, jS);

    for (i=0; i<p->size; i++) {
	(*env)->CallVoidMethod(env, EN, setEValueID, (jint) i,  
                CEValue2JEValue( env, &p->arr[i] ) );
    }   
    return EN;
}


/**
 * Converts a C ParamPolyhedron to a Java ParamPolyhedron
 */
jobject CParamPolyhedron2JParamPolyhedron( JNIEnv *env, Param_Polyhedron *p ) {
    
    jclass    cls = (*env)->FindClass(env, "espam/utils/polylib/ParamPolyhedron");
    jmethodID mid = (*env)->GetMethodID(env, cls, "<init>", "()V");
    jobject   PP  = (*env)->NewObject(env, cls, mid );

    jmethodID setVertexID = (*env)->GetMethodID(env, cls, "setParamVertices", 
            "(Lespam/utils/polylib/ParamVertices;)V");
    jmethodID setDomainID = (*env)->GetMethodID(env, cls, "setParamDomain", 
            "(Lespam/utils/polylib/ParamDomain;)V");
    
    if ( p->V ) {
	(*env)->CallVoidMethod(env, PP, setVertexID, 
                CParamVertices2JParamVertices( env, p->V ) );
    }
    
    if ( p->D ) {
	(*env)->CallVoidMethod(env, PP, setDomainID, 
                CParamDomain2JParamDomain( env, p->D ) );	
    }
    
    return PP;
}


/**
 * Converts a C ParamVertices to a Java ParamVertices
 */
jobject CParamVertices2JParamVertices( JNIEnv *env, Param_Vertices *v ) {

    jclass    cls = (*env)->FindClass(env, "espam/utils/polylib/ParamVertices");
    jmethodID mid = (*env)->GetMethodID(env, cls, "<init>", "()V");
    jobject   V   = (*env)->NewObject(env, cls, mid );

    jmethodID setVertexID = (*env)->GetMethodID(env, cls, "setVertex", 
            "(Lespam/utils/symbolic/matrix/JMatrix;)V");
    jmethodID setDomainID = (*env)->GetMethodID(env, cls, "setDomain", 
            "(Lespam/utils/symbolic/matrix/JMatrix;)V");
    jmethodID addID       = (*env)->GetMethodID(env, cls, "add", 
            "(Lespam/utils/polylib/ParamVertices;)V");

    (*env)->CallVoidMethod(env, V, setVertexID, 
            CMatrix2JMatrix( env, v->Vertex ) );
    (*env)->CallVoidMethod(env, V, setDomainID, 
            CMatrix2JMatrix( env, v->Domain ) );


    if (v->next) {
	(*env)->CallVoidMethod(env, V, addID, 
                CParamVertices2JParamVertices( env, v->next ) );
    }
    
    return V;
}

/**
 * Converts a C ParamDomain to a Java ParamDomain
*/
jobject CParamDomain2JParamDomain( JNIEnv *env, Param_Domain *d ) {

    jclass    cls = (*env)->FindClass(env, "espam/utils/polylib/ParamDomain");
    jmethodID mid = (*env)->GetMethodID(env, cls, "<init>", "()V");
    jobject   D   = (*env)->NewObject(env, cls, mid );


    jmethodID addID       = (*env)->GetMethodID(env, cls, "add", 
            "(Lespam/utils/polylib/ParamDomain;)V");

    if (d->next) {
	(*env)->CallVoidMethod(env, D, addID, 
                CParamDomain2JParamDomain(env, d->next)  );
    }
    
    return D;
}


/***********************************************************************/
/*                                                                     */
/*  All the following function operates on C types only.               */
/*                                                                     */
/***********************************************************************/

/**
 * ConstaintSimplify is the Matrix version of DomainSimplify
 */
Matrix *ConstraintsSimplify( Matrix *MatD, Matrix *MatC ) {

    Matrix *MatS;

    Polyhedron *PolD, *PolC, *PolS;
  
    PolD   = Constraints2Polyhedron(MatD, 8096);
    PolC   = Constraints2Polyhedron(MatC,  8096);

    PolS = DomainSimplify(PolD, PolC, 8096);

    Domain_Free( PolD );
    Domain_Free( PolC  );  
  
    MatS = Polyhedron2Constraints(PolS);
  
    Domain_Free( PolS );
  
    return MatS;
}

/*
 * Class:     espam_utils_polylib_polylibBIT_PolyLibBIT
 * Method:    isTrustedSolution
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_espam_utils_polylib_polylibBIT_PolyLibBIT_isTrustedSolution
(JNIEnv * env, jclass clazz) {
    if ( degenerate == 1 ) {
        // We do not trust the solution. 
        return JNI_FALSE;
    } else {
        // We trust the solution
        return JNI_TRUE;
    }
}






