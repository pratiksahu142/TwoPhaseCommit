mkdir -p classes
javac src/*.java
mv src/*.class classes
cp resources/input.txt classes
cd classes
rmic Leader
rmic KeyValueStore
rmiregistry
