
ROOT =		../../..

CLASSPATH = 	$(ROOT)

SUBPACKAGES = \

JSRCS = \
	JMatrixParser.java \
	ASCII_CharStream.java \
	JJTJMatrixParserState.java \
	JMatrixParser.java \
	JMatrixParserConstants.java \
	JMatrixParserTokenManager.java \
	JMatrixParserTreeConstants.java \
	Node.java \
	ParseException.java \
	SimpleNode.java \
	Token.java \
	TokenMgrError.java

package: compile

compile: JMatrixParser.class

JMatrixParser.class: $(JSRCS)

# Get the rest of the rules
include $(ROOT)/vars.mk
include $(ROOT)/default_functions.mk

