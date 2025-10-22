import java.util.concurrent.CompletableFuture;
import java.io.*;

public class Main {
	public static void main(String[] args) {
		try {
			HTTPSearcher s = new HTTPSearcher();
			s.getLocation().thenCompose(point -> s.getLocationInfo(point)).thenAccept(str -> System.out.println(str)).join();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}