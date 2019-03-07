CC := gcc
FLAGS := -Os
all: clean
	$(CC) $(FLAGS) split_join.c -o split_join
	strip split_join
clean:
	rm -f split_join
