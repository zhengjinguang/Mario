#!/bin/sh
channel=$1

CHANNEL_APK_PATH=../channel/apk
RELEASE_APK=../build/outputs/apk/gamemaster-release.apk
MANIFEST_FILE=../AndroidManifest.xml
CONSTANTS_FILE=../src/com/lemi/controller/lemigameassistance/config/Constants.java
ORIGINAL_CHANNEL="lemi"


clean_path(){
    if [ -d ${CHANNEL_APK_PATH} ]; then
        rm -r ${CHANNEL_APK_PATH}
    fi
    mkdir -p ${CHANNEL_APK_PATH}
}

clean_file(){
    mkdir -p ${CHANNEL_APK_PATH}
    file=$1
    if [ -f ${file} ]; then
        rm ${file}
    fi
}

change_channel(){
    channel=$1
    python changeMainifest.py ${MANIFEST_FILE} ${channel}
}

change_test_flag(){
    test_flag=$1
    python changeTestVersion.py ${CONSTANTS_FILE} ${test_flag}
}

build_channel_apk()
{
    channel=$1
    change_channel ${channel}
    cd ..
    gradle asR
    cd build-script
    if [ ! -f ${RELEASE_APK} -a ! -f "gamemaster-release.apk" ]; then
        echo "build failed! gamemaster-release.apk is not exist"
        exit -1
    fi
    cp ${RELEASE_APK} ${CHANNEL_APK_PATH}/${channel}.apk
    change_channel ${ORIGINAL_CHANNEL}
}

change_test_flag false

if [ ! ${channel} ]
then
   all=1
fi

if [[ ${all} == 1 ]]
then
    if [ ! -f "channel.txt" ]; then
        echo "channel.txt is not exist"
        exit -1
    fi

    clean_path

    channelCount=`cat channel.txt |wc -l`

    for((i=0;i<${channelCount};i++))
    do
        channel=`python readChannel.py channel.txt ${i}`
        if [ ! ${channel} ]
        then
           echo "channel null error"
           exit -1
        fi
        echo ${channel}
        build_channel_apk ${channel}
    done

else
    echo ${channel}
    clean_file ${CHANNEL_APK_PATH}/${channel}.apk
    build_channel_apk ${channel}
fi

change_test_flag true
