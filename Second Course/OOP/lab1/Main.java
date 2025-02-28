public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("You entered " + args.length + " parameters; correct input is <input.txt> <output.txt>");
			return;
		}
		
		Collector col = new Collector(args[0]);
		Writer wr = new Writer(args[1]);
		wr.write(col.getMap(), col.getWrdsNum());

		System.out.println("All work done");
	}
}