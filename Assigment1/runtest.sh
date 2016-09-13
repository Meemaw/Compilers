#!/usr/bin/env bash

# source library
. ./libtest.sh || exit 1

# in case of missing argument print usage
if [ $# -eq 0 ]; then
    echo "Usage: $0 \$JAR_FILE"
    exit 1
fi



JAR_FILE="`realpath $1`"
TESTS_FOLDER="tests"
exit_code=0

cd "$TESTS_FOLDER"

# for every .decaf file in $TESTS_FOLDER
for decaf_source_file in `ls *.decaf`
do
	echo $decaf_source_file
	BASENAME="`basename $decaf_source_file .decaf`"
	run_command "java -jar $JAR_FILE $decaf_source_file > $BASENAME.out.real" #2> $BASENAME.err.real
	#echo "$?" > $BASENAME.exit_code
	run_command "diff $BASENAME.out.correct $BASENAME.out.real"
	echo "======================================="
done

cd ..

Exit
