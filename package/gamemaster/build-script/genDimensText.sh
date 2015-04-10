#!/bin/sh
multiple=$1

if [ ! $multiple ]
then
   multiple=1
fi

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
echo "<resources>"

for((i=1;i<=50;i++))
do
    result=`echo "scale=2; $i * $multiple" | bc`
    echo "<dimen name=\"mario_${i}sp\">${result}sp</dimen>"
done

echo "</resources>"


