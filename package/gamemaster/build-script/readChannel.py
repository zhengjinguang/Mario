#!/usr/bin/python

import sys
import os

def readChannel(filename,index):
    input = open(filename)
    lines = input.readlines()
    input.close()

    i = 0
    index = int(index)

    for line in lines:
        if not line:
            break

        if len(line.split('\n')) == 2:
                line = line.split('\n')[0]

        if i == index:
            print line
            break

        i = i + 1

def main():
    if len(sys.argv) == 3:
        filename = sys.argv[1]
        index = sys.argv[2]
        readChannel(filename,index)

    elif len(sys.argv) == 2:
        filename = sys.argv[1]
        readChannel(filename,0)


if __name__ == "__main__":
    main()
