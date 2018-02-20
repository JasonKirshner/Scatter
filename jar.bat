cd C:\Users\Jason\Documents\School\Java2\Scatter
set path=C:\Program Files\Java\jdk1.8.0_112\bin

javac *.java
jar -cefv ScatterServer ScatterServer.jar ScatterServer*.class
jar -cefv ScatterClient ScatterClient.jar ScatterClient*.class

java -jar ScatterClient.jar
java -jar ScatterServer.jar