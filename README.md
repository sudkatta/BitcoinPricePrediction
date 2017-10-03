Simple REST API in Play
-----------------------

Simple REST API for predicting the BitCoin Price trends.

Technology:
- Play framework  
- Spark 1.4.1 MLlib

This app is based on the TypeSafe activator template 'simple-rest-scala', see http://www.typesafe.com/activator/template/simple-rest-scala.

To run app:  
activator run / sbt run

To run Tests:
- sbt test (has integration and unit tests)

To debug app:  
- Open Intellij IDEA  
- Open project - browse your app directory  
- Add new configuration - Remote  
- Add name Setting transport : socket, debugger mode : attach, Host : localhost, port : 9999 module clashpath : your app  
- activator -jvm-debug 9999 run  
- Run debug in IntelliJ IDEA  
- Open browser localhost:9000  

API endpoints:
- Past Price data:
    - GET http://localhost:9000/pastPrices?range=09/05/2017-10/05/2017
    - response : {"status":"OK","data":[{"price":1744.12,"date":"10/05/2017"},{"price":1686.35,"date":"09/05/2017"}]
    - Request : range=lastweek or range=lastweek or range =dd/MM/yyyy-dd/MM/yyyy(daterange in this format)
- Moving Average Data:
    - GET http://localhost:9000/movingAvg?from=09/05/2017&to=10/05/2017&window=10
    - response: {"status":"OK","data":[{"price":1563.47,"date":"10/05/2017"},{"price":1524.57,"date":"09/05/2017"}]}
- Get Prediction:
    - GET http://localhost:9000/forecast
    - response: {"status":"OK","prediction":[{"price":3721.49,"date":"2017-10-03"},{"price":3731.87,"date":"2017-10-04"},{"price":3742.25,"date":"2017-10-05"},{"price":3752.63,"date":"2017-10-06"},{"price":3763.01,"date":"2017-10-07"},{"price":3773.39,"date":"2017-10-08"},{"price":3783.76,"date":"2017-10-09"},{"price":3794.14,"date":"2017-10-10"},{"price":3804.52,"date":"2017-10-11"},{"price":3814.9,"date":"2017-10-12"},{"price":3825.28,"date":"2017-10-13"},{"price":3835.66,"date":"2017-10-14"},{"price":3846.04,"date":"2017-10-15"},{"price":3856.42,"date":"2017-10-16"},{"price":3866.79,"date":"2017-10-17"}]}
