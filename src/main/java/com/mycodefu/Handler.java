package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
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

import java.util.HashMap;
import java.util.Map;

// Handler value: example.Handler
public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
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
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
		System.out.printf("received: %s%n", apiGatewayV2HTTPEvent);

		long start = System.nanoTime();
		MongoDatabase database = mongoClient.getDatabase("ctest");
		MongoCollection<Document> things = database.getCollection("things");
		Document firstThing = things.find().first();
		String thing = firstThing == null ? "{null}" : firstThing.getString("thing");
		long end = System.nanoTime();
		long timeTakenMillis = (end - start) / 1_000_000;
		System.out.println(String.format("Took %d milliseconds to run MongoDB query inside handler.", timeTakenMillis));

		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "AWS Lambda & serverless");
		headers.put("Content-Type", "text/plain");
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse(
				200,
				headers,
				null,
				null,
				String.format("Go Serverless v1.x! Your function executed successfully! Mongo driver version %s. Thing: %s", MongoDriverVersion.VERSION, thing),
				false
		);
		return response;
	}

	public static void main(String[] args) {
		APIGatewayV2HTTPResponse response = new Handler().handleRequest(new APIGatewayV2HTTPEvent(null, null, null, null, null, null, null, null, null, null, false, null), null);
		System.out.println(response);
	}
}