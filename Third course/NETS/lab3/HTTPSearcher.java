import org.json.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class HTTPSearcher {
	private String GRAPHHOPPER_API_KEY = "";
	private String OPENWEATHER_API_KEY = "";
	private String MEDIAWIKI_HEADER = "";

	private final HttpClient apiClient;

	public HTTPSearcher() throws IOException {
		File apiFile = new File("APIkeys.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(apiFile))) {
			HashMap<String, String> apis = new HashMap<>();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] apiSet = line.split("=");
				apis.put(apiSet[0], apiSet[1]);
			}

			GRAPHHOPPER_API_KEY = apis.get("GRAPHHOPPER_API_KEY");
			OPENWEATHER_API_KEY = apis.get("OPENWEATHER_API_KEY");
			MEDIAWIKI_HEADER = apis.get("MEDIAWIKI_HEADER");
		}

		apiClient = HttpClient.newHttpClient();
	}

	private HttpRequest makeHttpRequest(String request) {
		return HttpRequest.newBuilder().GET().uri(URI.create(request)).build();
	}

	private HttpRequest makeWikiHttpRequest(String request) {
		return HttpRequest.newBuilder().GET().uri(URI.create(request)).header("User-Agent", MEDIAWIKI_HEADER).build();
	}

	public CompletableFuture<String[]> getLocation(String placeName) {
		String placeQuerryName = URLEncoder.encode(placeName, StandardCharsets.UTF_8);

		HttpRequest request = makeHttpRequest(String.format("https://graphhopper.com/api/1/geocode?q=%s&locale=en&key=%s", placeQuerryName, GRAPHHOPPER_API_KEY));

		return apiClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApply(HttpResponse::body)
			.thenApply(body -> {
				JSONObject jsonObjPlaces = new JSONObject(body);
				JSONArray jsonArrPlaces = jsonObjPlaces.getJSONArray("hits");
				if (jsonArrPlaces.length() == 0)
					throw new RuntimeException("Cannot find places by request: " + placeName);

				String[] points = new String[jsonArrPlaces.length()];
				for (int i = 0; i < jsonArrPlaces.length(); i++) {
					JSONObject jsonObjPlace = jsonArrPlaces.getJSONObject(i);
					JSONObject jsonObjPoint = jsonObjPlace.getJSONObject("point");
					System.out.printf("%d. %s, Country: %s, type: %s, coordinates: %f, %f\n",
						(i + 1),
						jsonObjPlace.getString("name"),
						jsonObjPlace.getString("country"),
						jsonObjPlace.getString("osm_value"),
						jsonObjPoint.getDouble("lng"),
						jsonObjPoint.getDouble("lat")
					);

					points[i] = jsonObjPoint.getDouble("lng") + " " + jsonObjPoint.getDouble("lat");
				}

				return points;
			});
	}

	private CompletableFuture<String> getWeather(double[] point) {
		HttpRequest request = makeHttpRequest(String.format("https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric", point[1], point[0], OPENWEATHER_API_KEY));

		return apiClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApply(HttpResponse::body)
			.thenApply(body -> {
				JSONObject jsonObject = new JSONObject(body);
				JSONObject jsonObjMain = jsonObject.getJSONObject("main");
				JSONObject jsonObjClouds = jsonObject.getJSONObject("clouds");
				JSONObject jsonObjWeather = jsonObject.getJSONArray("weather").getJSONObject(0);
				JSONObject jsonObjWind = jsonObject.getJSONObject("wind");

				String weatherString = String.format("The weather in this location:\nTemperature:\n\treal: %.2f C\n\tfeels like: %.2f C\nSky:\n\tcloudiness: %d%%\n\tdescription: %s\n\tstatus: %s\nWind:\n\tspeed: %.1f m/s",
					jsonObjMain.getDouble("temp"), jsonObjMain.getDouble("feels_like"),
					jsonObjClouds.getInt("all"), jsonObjWeather.getString("description"), jsonObjWeather.getString("main"),
					jsonObjWind.getDouble("speed"));

				return weatherString;
			});
	}

	public CompletableFuture<int[]> getPoints(double[] startPoint) {
		String coords = String.format(Locale.US, "%f|%f", startPoint[1], startPoint[0]);
		String coordsQuerry = URLEncoder.encode(coords, StandardCharsets.UTF_8);

		HttpRequest request = makeWikiHttpRequest(String.format("https://en.wikipedia.org/w/api.php?action=query&list=geosearch&gscoord=%s&gsradius=5000&gslimit=5&format=json", coordsQuerry));

		return apiClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApply(HttpResponse::body)
			.thenApply(body -> {
				JSONObject points = new JSONObject(body);
				JSONObject query = points.getJSONObject("query");
				JSONArray jsonArrPoints = query.getJSONArray("geosearch");
				if (jsonArrPoints.length() == 0)
					throw new RuntimeException("Cannot find points by coords: " + coords + " at 5000 m");

				int[] pages = new int[jsonArrPoints.length()];
				for (int i = 0; i < jsonArrPoints.length(); i++) {
					JSONObject obj = jsonArrPoints.getJSONObject(i);
					pages[i] = obj.getInt("pageid");
				}

				return pages;
			});
	}

	private CompletableFuture<String> getDescription(int pageId) {
		HttpRequest request = makeWikiHttpRequest(String.format("https://en.wikipedia.org/w/api.php?action=query&prop=extracts&pageids=%d&exintro&explaintext&format=json", pageId));

		return apiClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApply(HttpResponse::body)
			.thenApply(body -> {
				JSONObject obj = new JSONObject(body);
				JSONObject query = obj.getJSONObject("query");
				JSONObject pages = query.getJSONObject("pages");
				JSONObject desc = pages.getJSONObject(Integer.toString(pageId));

				String descriptionString = desc.getString("title") + ":\n\t" + desc.getString("extract");
				return descriptionString;
			});
	}

	private CompletableFuture<String> getInterestingPoints(double[] startPoint) {
		return getPoints(startPoint)
			.thenCompose(pages -> {
				List<CompletableFuture<String>> descriptions = new ArrayList<>();
				for (int pageId : pages)
					descriptions.add(getDescription(pageId));

			return CompletableFuture.allOf(descriptions.toArray(new CompletableFuture[0]))
				.thenApply(val -> {	
					StringBuilder sb = new StringBuilder();
					sb.append("Interesting Places:\n");
					int counter = 0;
					for (CompletableFuture<String> f : descriptions)
						sb.append((++counter) + ". ").append(f.join()).append("\n\n");

					return sb.toString();
				});
			});
	}

	public CompletableFuture<String> getLocationInfo(double[] point) {
		return getWeather(point).thenCombine(getInterestingPoints(point), (weather, points) -> {
			return weather + "\n\n" + points;
		});
	}
}