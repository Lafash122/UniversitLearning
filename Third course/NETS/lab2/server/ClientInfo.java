import java.util.concurrent.TimeUnit;

public class ClientInfo {
	private long totalReceived = 0;
	private long intervalReceived = 0;

	private long connectionTime;
	private long lastCheckTime;

	private int clientNumber;

	public ClientInfo(int clientNumber) {
		connectionTime = System.currentTimeMillis();
		lastCheckTime = connectionTime;
		this.clientNumber = clientNumber;
	}

	public int getClientNumber() {
		return clientNumber;
	}

	public long getTotalReceived () {
		return totalReceived;
	}

	public void addReceived(long bytes) {
		totalReceived += bytes;
		intervalReceived += bytes;
	}

	public double countInstantSpeed() {
		double currentPeriodTime = (System.currentTimeMillis() - lastCheckTime) / 1000.0;
		if (currentPeriodTime == 0.0)
			return 0;

		double instantSpeed = intervalReceived / currentPeriodTime / 1024;
		intervalReceived = 0;
		lastCheckTime = System.currentTimeMillis();

		return instantSpeed;
	}

	public double countAverageSpeed() {
		double currentTime = (System.currentTimeMillis() - connectionTime) / 1000.0;
		if (currentTime == 0)
			return 0;

		return (totalReceived / currentTime / 1024);
	}
}