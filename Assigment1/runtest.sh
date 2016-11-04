#!/usr/bin/env bash

# source library
. ./libtest.sh || exit 1

# in case of missing argument print usage
if [ $# -lt 2 ]; then
    echo "Usage: $0 \$JAR_FILE"
    exit 1
fi



PARSER_JAR_FILE="`realpath $1`"
INTERPRETER_JAR_FILE="`realpath $2`"

TESTS_FOLDER="tests/official_tests"
exit_code=0

cd "$TESTS_FOLDER"

# for every .decaf file in $TESTS_FOLDER
for decaf_source_file in `ls *.decaf`
do
	echo $decaf_source_file
	BASENAME="`basename $decaf_source_file .decaf`"
	run_command "java -jar $PARSER_JAR_FILE $decaf_source_file > $BASENAME.out.real" #2> $BASENAME.err.real
	run_command "java -jar $INTERPRETER_JAR_FILE $BASENAME.out.real > $BASENAME.int.real" #2> $BASENAME.err.real
	#echo "$?" > $BASENAME.exit_code
	run_command "diff $BASENAME.int.correct $BASENAME.int.real"
	echo "======================================="
done

cd ..

Exit
