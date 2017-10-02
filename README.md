Simple REST API in Play
-----------------------

Simple REST API for predicting the BitCoin Price trends.

Technology:
- Play framework  
- Spark 1.4.1 MLlib

This app is based on the TypeSafe activator template 'simple-rest-scala', see http://www.typesafe.com/activator/template/simple-rest-scala.

To run app:  
activator run

To debug app:  
Open Intellij IDEA  
Open project - browse your app directory  
Run - Edit Configuration  
Add new configuration - Remote  
Add name Setting transport : socket, debugger mode : attach, Host : localhost, port : 9999 module clashpath : your app  
activator -jvm-debug 9999 run  
Run debug in IntelliJ IDEA  
Open browser localhost:9000  
