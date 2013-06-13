#! /bin/bash

SCRIPT=$(readlink -f "$0")
PROJECTPATH=$(dirname "$SCRIPT")
PROJECT=$(basename "$PROJECTPATH")
echo "Enter the full path to Xilinx settings.sh file (e.g. /home/jelena/tools/Xilinx/ISE_DS/settings.sh)"
read xilinx_path
echo "To only develop/compile the software press S, to only synthesize the hardware press H, to do both press B"
read answer
source $xilinx_path
if [ "$answer" == "S" ] || [ "$answer" == "s" ] || [ "$answer" == "B" ] || [ "$answer" == "b" ]; then    
    echo run exporttosdk | xps -nw "$PROJECTPATH/system.xmp"
    mkdir "$PROJECTPATH/SDK/$PROJECT""_hw_platform"
    cd "$PROJECTPATH/SDK/SDK_Export/hw"
    cp system.xml ../../$PROJECT"_hw_platform"
    cd ../..
    i=0 
    for dir in */; do
        if [[ "$dir" =~ "BSP_" ]]; then
            let "i = $i + 1"   
        fi
    done
    echo "Do you want to check stack and heap sizes for processors? (Y/N)"
    read stach_heap
    if [ "$stach_heap" == "Y" ] || [ "$stach_heap" == "y" ]; then
        ../linker_script.py
    fi    
    cd BSP_host_if
    make all
    cd ..
    cd host_if/Debug
    make clean
    make all
    cd ../..
    j=1
    while [ $j -lt $i ]; do 
        cd BSP_P_$j
        make all
        cd ..
        cd "P_$j/Debug"
        make clean
        make all
        cd ../..
        let "j = $j + 1"
    done
    cd ..
    if [ "$answer" == "S" ] || [ "$answer" == "s" ]; then
        echo "Do you want to merge compiled software with the hardware (if the hardware is already sinthesized)? (Y/N)"
        read merge
        if [ "$merge" == "Y" ] || [ "$merge" == "y" ]; then
            echo run init_bram | xps -nw "$PROJECTPATH/system.xmp"
        fi
    fi
fi
if [ "$answer" == "H" ] || [ "$answer" == "h" ] || [ "$answer" == "B" ] || [ "$answer" == "b" ]; then
    echo run bits | xps -nw "$PROJECTPATH/system.xmp"
fi
if [ "$answer" == "B" ] || [ "$answer" == "b" ]; then 
    echo run init_bram | xps -nw "$PROJECTPATH/system.xmp"
fi