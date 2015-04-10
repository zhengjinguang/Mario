#!/usr/bin/python

import sys
import os

def modifyTestFlag(filename,isTestVersion):
    input = open(filename)
    lines = input.readlines()
    input.close()

    output = open(filename,'w')
    for line in lines:
        if not line:
            break

        if 'public static final boolean IS_IN_TEST =' in line:
            temp = 'public static final boolean IS_IN_TEST = '
            if isTestVersion == 'true':
                temp = temp + 'true;\n'
            else:
                temp = temp + 'false;\n'

            output.write(temp)
            continue

        output.write(line)

    output.close()




def main():
    if len(sys.argv) == 3:
        filename = sys.argv[1]
        isTestVersion = sys.argv[2]

    if bool(filename) & bool(isTestVersion):
        modifyTestFlag(filename,isTestVersion)

if __name__ == "__main__":
    main()
