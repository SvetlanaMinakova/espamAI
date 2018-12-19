import os

dirpath = os.getcwd()
libpath = os.getcwd() + "/lib"
os.chdir(libpath)

folder = []

for i in os.walk(libpath):
    folder.append(i)

manifest = open(dirpath + '/Manifest.txt', 'w')

#+ '\n'
manifest.write("Manifest-Version: 1.0 \n")
manifest.write("Main-Class: espam.main.Main \n")
manifest.write("Class-Path: ")


for address, dirs, files in folder:
    for file in files:
        if(file.endswith("jar")):
            reladdress = address.replace(libpath, "")
            manifest.write("  lib" +reladdress + "/" + file + "\n")

manifest.close()

print("manifest created")
