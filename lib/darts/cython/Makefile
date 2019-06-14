
BASE_DIR=..
CWD=$(shell pwd)
PY_SRC=$(shell cd $(BASE_DIR); ls *.py)

build: copy
	python setup.py build_ext --inplace
	@rm -f *.pyx

copy: clean
	@cp -r $(BASE_DIR)/examples .
	@for p in $(PY_SRC); do \
    	p_name=`echo $$p | sed 's/.py//'`; \
    	cp $(BASE_DIR)/$$p $$p_name.pyx; \
	done
	@echo ".pyx files generated"

run: 
	python run.py

clean:
	@rm -f *.pyx
	@rm -f *.so
	@rm -f *.c
	@rm -rf build
	@rm -rf examples
	@echo "All generated files removed"

rmpyx:
	rm -f *.pyx

