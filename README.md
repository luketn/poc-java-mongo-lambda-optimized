# poc-java-mongo-lambda-optimized
Look at a few recent optimizations for Java lambda to see if they reduce cold-start for lambda to an acceptable latency for APIs.

Initial version (basic deps - Jackson, Log4J, AWS handler interface):  
![img.png](img.png)

After adding the environment variable:  
JAVA_TOOL_OPTIONS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"  
![img_1.png](img_1.png)

Add the Mongo driver and print the version in the response:
![img_2.png](img_2.png)