#!/bin/sh
multiple=$1

if [ ! $multiple ]
then
   multiple=1
fi
echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
echo "<resources>"

for((i=0;i<=1920;i++))
do
    result=`echo "scale=2; $i * $multiple" | bc`
    echo "<dimen name=\"mario_${i}dp\">${result}dp</dimen>"
done

negative=-1

for((i=-1;i>=-1920;i--))
do
    j=`expr $i \* $negative`
    result=`echo "scale=2; $i * $multiple" | bc`
    echo "<dimen name=\"_${j}dp\">${result}dp</dimen>"
done

echo "</resources>"


