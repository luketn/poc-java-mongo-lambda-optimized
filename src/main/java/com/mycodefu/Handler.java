package com.mycodefu;

import com.amazonaws.services.lambda.runtime.*;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.internal.build.MongoDriverVersion;
import org.bson.Document;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
	private static String CONNECTION_STRING;
	private static ConnectionString connectionString;
	private static MongoClientSettings settings;
	private static MongoClient mongoClient;

	static {
		long start = System.nanoTime();
		CONNECTION_STRING = System.getenv("CONNECTION_STRING");
		connectionString = new ConnectionString(CONNECTION_STRING);
		settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.serverApi(ServerApi.builder()
						.version(ServerApiVersion.V1)
						.build())
				.build();
		mongoClient = MongoClients.create(settings);
		long end = System.nanoTime();
		long timeTakenMillis = (end - start) / 1_000_000;
		System.out.println(String.format("Took %d milliseconds to initialize MongoDB outside handler.", timeTakenMillis));
	}

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		System.out.printf("received: %s%n", input);

		long start = System.nanoTime();
		MongoDatabase database = mongoClient.getDatabase("ctest");
		MongoCollection<Document> things = database.getCollection("things");
		Document firstThing = things.find().first();
		String thing = firstThing == null ? "{null}" : firstThing.getString("thing");
		long end = System.nanoTime();
		long timeTakenMillis = (end - start) / 1_000_000;
		System.out.println(String.format("Took %d milliseconds to run MongoDB query inside handler.", timeTakenMillis));

		Response responseBody = new Response(String.format("Go Serverless v1.x! Your function executed successfully! Mongo driver version %s. Thing: %s", MongoDriverVersion.VERSION, thing), input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}

	public static void main(String[] args) {
		new Handler().handleRequest(new HashMap<>(), new Context() {
			@Override
			public String getAwsRequestId() {
				return "null";
			}

			@Override
			public String getLogGroupName() {
				return "null";
			}

			@Override
			public String getLogStreamName() {
				return null;
			}

			@Override
			public String getFunctionName() {
				return null;
			}

			@Override
			public String getFunctionVersion() {
				return null;
			}

			@Override
			public String getInvokedFunctionArn() {
				return null;
			}

			@Override
			public CognitoIdentity getIdentity() {
				return null;
			}

			@Override
			public ClientContext getClientContext() {
				return null;
			}

			@Override
			public int getRemainingTimeInMillis() {
				return 0;
			}

			@Override
			public int getMemoryLimitInMB() {
				return 0;
			}

			@Override
			public LambdaLogger getLogger() {
				return null;
			}
		});
	}
}
