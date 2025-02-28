import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map;

public class Writer{
	private File file;

	public Writer(String out_name) {
		file = new File(out_name);
		try {
			file.createNewFile();
		}
		catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	public void write(TreeMap<String, Integer> map, int wrds_cnt) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			map.entrySet().stream().sorted((a, b) -> {
				int cmp = b.getValue().compareTo(a.getValue());
				return (cmp != 0) ? cmp : a.getKey().compareTo(b.getKey());
			}).forEach(entry -> {
				try {
					float stat = (float) entry.getValue() / wrds_cnt;
					writer.write(entry.getKey() + ";" + stat + ";" + (stat * 100));
					writer.newLine();
				}
				catch(IOException e) {
					System.out.println("Error: " + e.getMessage());
					System.exit(1);
				}
			});
		}
		catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}
}