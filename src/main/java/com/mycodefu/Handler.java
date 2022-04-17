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
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private static Logger log = LoggerFactory.getLogger("Handler");

	private static String CONNECTION_STRING;
	private static String DATABASE_NAME;
	private static String COLLECTION_NAME;
	private static ConnectionString connectionString;
	private static MongoClientSettings settings;
	private static MongoClient mongoClient;
	private static JsonWriterSettings jsonWriterSettings;


	static {
		long start = System.nanoTime();
		CONNECTION_STRING = System.getenv("CONNECTION_STRING");
		DATABASE_NAME = System.getenv("DATABASE_NAME");
		COLLECTION_NAME = System.getenv("COLLECTION_NAME");
		connectionString = new ConnectionString(CONNECTION_STRING);
		settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.serverApi(ServerApi.builder()
						.version(ServerApiVersion.V1)
						.build())
				.build();
		mongoClient = MongoClients.create(settings);
		jsonWriterSettings = JsonWriterSettings.builder()
				.objectIdConverter((objectId, strictJsonWriter) -> strictJsonWriter.writeString(objectId.toHexString()))
				.build();
		long end = System.nanoTime();
		long timeTakenMillis = (end - start) / 1_000_000;
		log.info(String.format("Took %d milliseconds to initialize MongoDB outside handler.", timeTakenMillis));
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
		if (log.isTraceEnabled()) {
			log.trace(String.format("received: %s%n", apiGatewayV2HTTPEvent));
		}

		long start = System.nanoTime();
		MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
		MongoCollection<Document> things = database.getCollection(COLLECTION_NAME);
		Document firstThing = things.find().first();
		long end = System.nanoTime();
		long timeTakenMillis = (end - start) / 1_000_000;
		log.info(String.format("Took %d milliseconds to run MongoDB query inside handler.", timeTakenMillis));

		String responseBody = firstThing == null ? "{}" : firstThing.toJson(jsonWriterSettings);

		Map<String, String> headers = new HashMap<>();
		headers.put("X-Powered-By", "AWS Lambda & serverless");
		headers.put("Content-Type", "application/json");
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse(
				200,
				headers,
				null,
				null,
				responseBody,
				false
		);
		return response;
	}

	public static void main(String[] args) {
		APIGatewayV2HTTPResponse response = new Handler().handleRequest(new APIGatewayV2HTTPEvent(null, null, null, null, null, null, null, null, null, null, false, null), null);
		log.info(response.toString());
	}
}