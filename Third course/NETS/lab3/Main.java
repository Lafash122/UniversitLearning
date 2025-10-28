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
				.thenCompose(points -> {
					System.out.println("Choose one location (enter the appropriate number from the list):");
					int locationNumber = 0;
					while (true) {
						try {
							String inputTry = sc.nextLine();
							locationNumber = Integer.parseInt(inputTry);
							if ((locationNumber >= 1) && (locationNumber <= points.length))
								break;
							else
								System.out.println("Enter the APPROPRIATE number FROM 1 TO " + points.length + ":");
						}
						catch (NumberFormatException e) {
							System.out.println("Enter the appropriate NUMBER from 1 to " + points.length + ":");
						}
					}

					String[] strPoint = points[locationNumber - 1].split(" ");
					double[] point = new double[2];
					point[0] = Double.parseDouble(strPoint[0]);
					point[1] = Double.parseDouble(strPoint[1]);

					return CompletableFuture.completedFuture(point);
				})
				.thenCompose(point -> s.getLocationInfo(point))
				.thenAccept(str -> System.out.println(str))
				.join();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}