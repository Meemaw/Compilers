#!/usr/bin/env bash
#
#   Authors:      Peter Hostacny <phostacn@redhat.com>
#                 Ondrej Lysonek <olysonek@redhat.com>
#
#   Library with functions for colorizing output & handling TEST exit code.
#
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

overall_res=0
SETUP_FAILED=0
CASE_FAILED=0

function print_result {
    local RESET='\e[0m'
    local RED='\e[0;31m'
    local GREEN='\e[0;32m'
    local YELLOW='\e[1;33m'
    local CYAN='\e[0;36m'
    local PASS="[ PASS ]"
    local FAIL="[ FAIL ]"
    local INFO="[ INFO ]"
    local BAD_STATUS="[ BAD STATUS ]"
    local WORKING="[ .... ]"
    local STATUS="$1"
    shift

    if [ "${STATUS}" = pass ]; then
        if color_terminal; then
            echo -en "${RESET}${GREEN}"
        fi
        echo -en "${PASS}"
    elif [ "${STATUS}" = fail ]; then
        if color_terminal; then
            echo -en "${RESET}${RED}"
        fi
        echo -en "${FAIL}"
    elif [ "${STATUS}" = working ]; then
        if color_terminal; then
            echo -en "${RESET}${YELLOW}"
        fi
        echo -en "${WORKING}"
    elif [ "${STATUS}" = info ]; then
        if color_terminal; then
            echo -en "${RESET}${CYAN}"
        fi
        echo -en "${INFO}"
    else
        update_overall_result 1
        if color_terminal; then
            echo -en "${RESET}${RED}"
        fi
        echo -en "${BAD_STATUS}"
    fi

    echo -en "${RESET}"

    echo -en " ${@}"
    if color_terminal; then
        echo -en "${RESET}"
    fi
    echo
}

# check if downloaded image is the expected one
# $1 = image_name
# $2 = expeced image id
function check_image_id() {
    image="$1"
    expected_img_id="$2"
    downloaded_img_id="`docker inspect --format='{{.Id}}' $image`"
    echo
    echo "   Downloaded image: $1"
    echo "  Expected image-id: $2"
    echo "Downloaded image-id: $expected_img_id"
    echo
    if [ "$downloaded_img_id" == "$expected_img_id" ]; then
        print_pass "Correct image-id."
    else
        print_fail "Unexpected image id - please investigate."
    fi
    echo
}


function errcho() {
    echo "$@" 1>&2
}

function color_terminal {
    [[ "$TERM" =~ '256color' ]]
}

# $1 = string
function print_info()
{
    print_result info "$1"
}

function print_pass {
    print_result pass "$@"
}

function print_fail {
    update_overall_result 1
    CASE_FAILED=1
    print_result fail "$@"
}

# $1 is the exit code
function update_overall_result {
    res="$1"
    if [ "$res" != 0 ]; then
        overall_res="$res"
    fi
}

function get_overall {
    echo $overall_res
}

# Run command and print result (pass/fail) in pretty colors
# $1 should equal '1' if heads up should be printed
# $2 is the command
# $3 is the expected exit code (optional, defaults to 0)
# $4 is the message to be printed (optional)
function run_command {
    local cmd="$1"
    local msg="${2:-Running command '$cmd'}"
    eval $cmd
    local res="$?"
    if [ "$res" -eq 0 ]; then
        print_pass "$msg"
    else
        print_fail "$msg"
    fi
    update_overall_result "$res"
    return "$res"
}

# $1 = string
# $2 = (optional) output file [default=stderr]
function print_setup_fail()
{
    update_overall_result 2
    SETUP_FAILED=1
    CASE_FAILED=1
    if [ "$#" -ge 2 ]; then
        print_result fail "$1" >>$2
    else
        print_result fail "$1" >&2
    fi
}

function Setup_failed()
{
    if [ "$SETUP_FAILED" -eq 0 ]; then
        return 1    # 1 = false in bash
    else
        return 0    # 0 = true in bash
    fi
}

function section {
    echo
}

function exit_overall {
    exit "$overall_res"
}

function Exit() {
    exit_overall
}
