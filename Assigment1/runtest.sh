#!/bin/bash



if [ $# -eq 0 ]; then
    echo "Usage: $0 \$JAR_FILE"
    exit 1
fi

JAR_FILE="$1"
TESTS_FOLDER="tests"
exit_code=0


for decaf_source_file in `ls $TESTS_FOLDER/*.decaf`
do
	echo $decaf_source_file
	BASENAME="`basename $decaf_source_file .decaf`"
	java -jar $JAR_FILE $decaf_source_file > $BASENAME.stdout 2> $BASENAME.stderr
	#echo "$?" > $BASENAME.exit_code
	diff $BASENAME.stdout.correct $BASENAME.stdout
	if [ "$?" -ne 0 ]; then
		exit_code=1
	fi
done

exit $exit_code
