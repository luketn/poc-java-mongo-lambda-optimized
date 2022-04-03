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
import com.mongodb.connection.StreamFactoryFactory;
import com.mongodb.connection.netty.NettyStreamFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private static String CONNECTION_STRING;
	private static ConnectionString connectionString;
	private static MongoClientSettings settings;
	private static MongoClient mongoClient;
	private static JsonWriterSettings jsonWriterSettings;
	private static final String operatingSystem =  System.getProperty("os.name");
	private static final boolean isLinux = operatingSystem.startsWith("Linux") || operatingSystem.startsWith("LINUX");

	static {
		long start = System.nanoTime();
		//CONNECTION_STRING=mongodb://?:?@luke-shard-00-00.wr1mf.mongodb.net:27017,luke-shard-00-01.wr1mf.mongodb.net:27017,luke-shard-00-02.wr1mf.mongodb.net:27017/ctest?ssl\=true&replicaSet\=atlas-dscbdj-shard-0&authSource\=admin&retryWrites\=true&w\=majority&maxPoolSize\=1&readPreference\=nearest
		CONNECTION_STRING = System.getenv("CONNECTION_STRING");
		connectionString = new ConnectionString(CONNECTION_STRING);
		settings = MongoClientSettings.builder()
				.streamFactoryFactory(getStreamFactoryFactory())
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
		System.out.println(String.format("Took %d milliseconds to initialize MongoDB outside handler.", timeTakenMillis));
	}

	private static StreamFactoryFactory getStreamFactoryFactory() {
		return (socketSettings, sslSettings) -> {
			EventLoopGroup eventLoopGroup;
//			if (isLinux) {
//				System.out.println("Loading epoll event loop group for Netty");
//				eventLoopGroup = new EpollEventLoopGroup();
//			} else {
				eventLoopGroup = new NioEventLoopGroup();
//			}
			return new NettyStreamFactory(socketSettings, sslSettings, eventLoopGroup);
		};
	}

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {
//		System.out.printf("received: %s%n", apiGatewayV2HTTPEvent);

		long start = System.nanoTime();
		MongoDatabase database = mongoClient.getDatabase("ctest");
		MongoCollection<Document> things = database.getCollection("things");
		Document firstThing = things.find().first();
		long end = System.nanoTime();
		long timeTakenMillis = (end - start) / 1_000_000;
		System.out.println(String.format("Took %d milliseconds to run MongoDB query inside handler.", timeTakenMillis));

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

	public static void main(String[] args) throws InterruptedException {
		Handler handler = new Handler();
//		for(int i=0; i<10; i++) {
			APIGatewayV2HTTPResponse response = handler.handleRequest(new APIGatewayV2HTTPEvent(null, null, null, null, null, null, null, null, null, null, false, null), null);
			System.out.println(response);
			Thread.sleep(1000);
//		}
		System.exit(0);
	}
}