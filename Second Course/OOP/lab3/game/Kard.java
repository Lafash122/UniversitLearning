package game;

public class Kard {
	private String suit;
	private Integer value;

	public Kard(String name, Integer val) {
		suit = name;
		value = val;
	}

	public String getSuit() {
		return suit;
	}

	public Integer getValueId() {
		return value;
	}

	public String getValueName() {
		String nameValue = "value";
		switch (value) {
			case 1:
				nameValue = "A";
				break;
			case 2:
				nameValue = "2";
				break;
			case 3:
				nameValue = "3";
				break;
			case 4:
				nameValue = "4";
				break;
			case 5:
				nameValue = "5";
				break;
			case 6:
				nameValue = "6";
				break;
			case 7:
				nameValue = "7";
				break;
			case 8:
				nameValue = "8";
				break;
			case 9:
				nameValue = "9";
				break;
			case 10:
				nameValue = "10";
				break;
			case 11:
				nameValue = "J";
				break;
			case 12:
				nameValue = "Q";
				break;
			case 13:
				nameValue = "K";
				break;
		}

		return nameValue;
	}

	@Override
	public String toString() {
		return "Kard: " + getValueName() + " " + suit;
	}
}