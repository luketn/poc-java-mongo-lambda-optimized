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

Add the environment variable and increase the RAM to 10240MB (10GB!):  
![img_8.png](img_8.png)

After running YourKit, see that Apache Log4J was adding 2MB to the bundle and 600 classes the classloader was taking 300ms to load them - remove Log4J!:  
![img_9.png](img_9.png)

After removing Jackson and switching from our own implementations of the Event classes to the AWS library:    
![img_10.png](img_10.png)

After increasing the memory to 4096 (1.4s):  
![img_11.png](img_11.png)

Switch to using JSON serializer built into MongoDB to seralize the response (no change in performance - 1.4s):  
![img_12.png](img_12.png)    
![img_13.png](img_13.png)  

Try using the HTTP data API (no Mongo driver at all!):  
![img_14.png](img_14.png)