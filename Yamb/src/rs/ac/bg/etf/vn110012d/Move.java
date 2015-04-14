package rs.ac.bg.etf.vn110012d;

import java.util.List;

public class Move {

	int id, playerId;
	int x, y, ord, value;
	List<Roll> rolls;
	
	public Move(int id, int x, int y, int ord, int value, int playerId, List<Roll> rolls) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.ord = ord;
		this.value = value;
		this.playerId = playerId;
		this.rolls = rolls;
	}
}
