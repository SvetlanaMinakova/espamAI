


ROOT = ../../..

SUBPACKAGES = \

JSRCS = \
	ASCII_CharStream.java\
	ASTIdentifier.java\
	ASTInteger.java\
	ASTcomplexExpression.java\
	ASTfraction.java\
	ASTlinearOperator.java\
	ASTsign.java\
	ASTsimpleExpression.java\
	ASTspecialOperator.java\
	ASTspecialTerm.java\
	ASTterm.java\
	ASTtermOrOperator.java\
	ExpressionParser.java\
	ExpressionParserConstants.java\
	ExpressionParserTokenManager.java\
	ExpressionParserTreeConstants.java\
	JJTExpressionParserState.java\
	Node.java\
	ParseException.java\
	SimpleNode.java\
	Token.java\
	TokenMgrError.java

package: compile

compile: ExpressionParser.class

ExpressionParser.class: $(JSRC)

# Include the shared compilation rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk
