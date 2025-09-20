public class Main {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("You need to pass the address of the multicast group as the first parameter to the application.");
			return;
		}

		Discoverer  dis = new Discoverer(args[0]);
	}
}
