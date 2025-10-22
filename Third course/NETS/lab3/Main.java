import java.util.concurrent.CompletableFuture;
import java.util.*;
import java.io.*;

public class Main {
	public static void main(String[] args) {
		try {
			HTTPSearcher s = new HTTPSearcher();

			Scanner sc = new Scanner(System.in);
			System.out.println("Enter the location name:");
			String placeName = sc.nextLine();

			s.getLocation(placeName)
				.thenCompose(point -> s.getLocationInfo(point))
				.thenAccept(str -> System.out.println(str))
				.join();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
