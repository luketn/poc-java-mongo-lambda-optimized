package com.mycodefu;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.internal.build.MongoDriverVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.bson.BsonDocument;
import org.bson.Document;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
	private static final Logger LOG = LogManager.getLogger(Handler.class);
	private static String CONNECTION_STRING = System.getenv("CONNECTION_STRING");
	private static ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
	private static MongoClientSettings settings = MongoClientSettings.builder()
			.applyConnectionString(connectionString)
			.serverApi(ServerApi.builder()
					.version(ServerApiVersion.V1)
					.build())
			.build();
	private static MongoClient mongoClient = MongoClients.create(settings);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: {}", input);

		MongoDatabase database = mongoClient.getDatabase("ctest");
		MongoCollection<Document> things = database.getCollection("things");
		Document firstThing = things.find().first();
		String thing = firstThing == null ? "{null}" : firstThing.getString("thing");

		Response responseBody = new Response(String.format("Go Serverless v1.x! Your function executed successfully! Mongo driver version %s. Thing: %s", MongoDriverVersion.VERSION, thing), input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
