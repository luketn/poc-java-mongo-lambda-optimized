package com.mycodefu;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.mongodb.client.model.Projections;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private static final String API_KEY = System.getenv("API_KEY");
	private static final HttpClient httpClient = HttpClient.newHttpClient();

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
//		System.out.printf("received: %s%n", apiGatewayV2HTTPEvent);
		long start = System.nanoTime();

		String responseBody;
		try {
			BsonDocument post = new BsonDocument();
			post.append("dataSource", new BsonString("luke"));
			post.append("database", new BsonString("ctest"));
			post.append("collection", new BsonString("things"));
			post.append("projection", Projections.include("_id").toBsonDocument());
			String postJson = post.toJson();

			HttpRequest request = HttpRequest
					.newBuilder(new URI("https://data.mongodb-api.com/app/data-qqgpb/endpoint/data/beta/action/findOne"))
					.POST(HttpRequest.BodyPublishers.ofString(postJson))
					.header("Content-Type", "application/json")
					.header("Access-Control-Request-Headers", "*")
					.header("api-key", API_KEY)
					.build();


			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			responseBody = response.body();

		} catch (InterruptedException | URISyntaxException | IOException e) {
			throw new RuntimeException(e);
		}

		long end = System.nanoTime();
		long timeTakenMillis = (end - start) / 1_000_000;
		System.out.printf("Took %d milliseconds to run MongoDB query inside handler.%n", timeTakenMillis);


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
		System.out.println(response);
	}
}