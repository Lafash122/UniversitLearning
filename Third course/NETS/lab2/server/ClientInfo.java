import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientInfo {
	private AtomicLong totalReceived = new AtomicLong(0);
	private AtomicLong intervalReceived = new AtomicLong(0);

	private volatile long connectionTime;
	private volatile long lastCheckTime;

	private int clientNumber;

	public ClientInfo(int clientNumber) {
		connectionTime = System.nanoTime();
		lastCheckTime = connectionTime;
		this.clientNumber = clientNumber;
	}

	public int getClientNumber() {
		return clientNumber;
	}

	public long getTotalReceived () {
		return totalReceived.get();
	}

	public void addReceived(long bytes) {
		totalReceived.addAndGet(bytes);
		intervalReceived.addAndGet(bytes);
	}

	public double countInstantSpeed() {
		double currentPeriodTime = (System.nanoTime() - lastCheckTime) / 1000000000.0;
		if (currentPeriodTime < 1e-9)
			return 0;

		double instantSpeed = intervalReceived.get() / (currentPeriodTime * 1024.0);
		intervalReceived.set(0);
		lastCheckTime = System.nanoTime();

		return instantSpeed;
	}

	public double countAverageSpeed() {
		double currentTime = (System.nanoTime() - connectionTime) / 1000000000.0;
		if (currentTime == 0)
			return 0;

		return (totalReceived.get() / (currentTime * 1024.0));
	}
}