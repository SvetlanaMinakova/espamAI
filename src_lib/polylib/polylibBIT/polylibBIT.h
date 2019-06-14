/***********************************************************************/
/*                   Java --> C    conversions                         */
/***********************************************************************/
Matrix     *JMatrix2CMatrix(JNIEnv *env, jobject aJMatrix);
Polyhedron *JPolyhedron2CPolyhedron(JNIEnv *env, jobject aJPol);
/***********************************************************************/
/*                   C --> Java    conversions                         */
/***********************************************************************/
jobject CMatrix2JMatrix(JNIEnv *env , Matrix *Mat);
jobject CPolyhedron2JPolyhedron( JNIEnv *env, Polyhedron *Pol);
jobject CEnumeration2JEnumeration( JNIEnv *env, Enumeration *e);
jobject CEValue2JEValue( JNIEnv *env, evalue *e);
jobject CENode2JENode( JNIEnv *env, enode *p );
jobject CParamPolyhedron2JParamPolyhedron( JNIEnv *env, Param_Polyhedron *p );
jobject CParamVertices2JParamVertices( JNIEnv *env, Param_Vertices *p );
jobject CParamDomain2JParamDomain( JNIEnv *env, Param_Domain *d );

/***********************************************************************/
/*                   Help functions (not available in PolyLib)         */
/***********************************************************************/
Matrix  *ReadMatrix( const char * fileName );
Matrix  *Matrix_fromFile( FILE *matrixFile );
Matrix  *ConstraintsSimplify( Matrix *MatD, Matrix *MatC );
