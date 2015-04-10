#!/usr/bin/python

import sys
import os

def modifyChannel(filename,channelValue):
    input = open(filename)
    lines = input.readlines()
    input.close()

    if len(channelValue.split('\n')) == 2:
        channelValue = channelValue.split('\n')[0]

    lastLineMark = 0

    output = open(filename,'w')
    for line in lines:
        if not line:
            break

        if lastLineMark == 1:
            lastLineMark = 0

            if 'android:value=' in line:
                temp = '\t\t\tandroid:value="'+ str(channelValue) +'"/>\n'
                output.write(temp)
                continue

        if ('android:name="channel"' in line) or ('android:name="UMENG_CHANNEL"' in line):
            lastLineMark = 1

        if 'android:name="channel" android:value=' in line:
            temp = '\t\t\tandroid:name="channel" android:value="'+ str(channelValue) +'"/>\n'
            output.write(temp)
            continue

        if 'android:name="UMENG_CHANNEL" android:value=' in line:
            temp = '\t\t\tandroid:name="UMENG_CHANNEL" android:value="'+ str(channelValue) +'"/>\n'
            output.write(temp)
            continue

        output.write(line)

    output.close()




def main():
    if len(sys.argv) == 3:
        filename = sys.argv[1]
        channelValue = sys.argv[2]

    if bool(filename) & bool(channelValue):
        modifyChannel(filename,channelValue)

if __name__ == "__main__":
    main()
