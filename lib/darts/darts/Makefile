# This Makefile contains several targets used for checking and 
# cleanin the code. Python is automatically compiled by the 
# CPython compiler so there is no need for a target to compile
# the code
#
# Author: Mohamed A. Bamakhrama

PY_STATIC_CHECK=py_static_check
PYCHECKER=pychecker
PYLINT=pylint
STAR=-s
IGNORE=-i
PY_SRC=$(shell ls *.py)
PYTHON2=python
PYTHON3=python3
PY_OPT=

all: clean pychecker

# Run Python static checker on all the python source files
## pychecker
pychecker: ${PY_SRC}
	@echo "***************************************************************************"
	@echo " Invoking Python static code checker "
	@echo "***************************************************************************"
	@for p in $(PY_SRC); do \
		${PYCHECKER} $$p; \
	done

## pylint
pylint: ${PY_SRC}
	@echo "***************************************************************************"
	@echo " Invoking Python static code checker "
	@echo "***************************************************************************"
	@for p in $(PY_SRC); do \
		${PYLINT} $$p; \
	done


# Remove all the generated .pyc files and __pycache__
clean:
	@echo "***************************************************************************"
	@echo " Cleaning the directory from all the .pyc files "
	@echo "***************************************************************************"
	rm -f *.pyc *.pyo 
	rm -rf __pycache__

