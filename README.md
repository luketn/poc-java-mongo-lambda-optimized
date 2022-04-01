# poc-java-mongo-lambda-optimized
Look at a few recent optimizations for Java lambda to see if they reduce cold-start for lambda to an acceptable latency for APIs.

Initial version (basic deps - Jackson, Log4J, AWS handler interface):  
![img.png](img.png)

After adding the environment variable:  
JAVA_TOOL_OPTIONS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"  
![img_1.png](img_1.png)

Add the Mongo driver and print the version in the response:
![img_2.png](img_2.png)

Remove the environment variable:
![img_3.png](img_3.png)

Add the environment variable back:
![img_4.png](img_4.png)

Add a Mongo query:
![img_5.png](img_5.png)

Mongo query without the environment variable - timed out after 6 seconds!!:
![img_6.png](img_6.png)
Changed the timeout to 15s:
![img_7.png](img_7.png)
