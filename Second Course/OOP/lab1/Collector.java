import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.TreeMap;

public class Collector {
	private int wrds_cnt = 0;
	private TreeMap<String, Integer> map;

	public Collector(String input_name) {
		File file = new File(input_name);
		if (!file.exists()) {
			System.out.println("File: <" + input_name + "> doesn\'t exist");
			System.exit(1);
		}

		map = new TreeMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(input_name))) {
			Pattern ptrn = Pattern.compile("\\b[A-Za-z0-9_][A-Za-z0-9_-]*");
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher match = ptrn.matcher(line);
				while (match.find()) {
					map.put(match.group(), map.getOrDefault(match.group(), 0) + 1);
					//the line of code below can be used instead of the line above
					//map.merge(match.group(), 1, Integer::sum); 
					wrds_cnt++;
				}
			}
		}
		catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	public int getWrdsNum() {
		return wrds_cnt;
	}

	public TreeMap<String, Integer> getMap() {
		return map;
	}
}
