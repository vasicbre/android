package rs.ac.bg.etf.vn110012d;

import java.util.List;

public class Player {

	String name;
	int id, ord;
	List<Move> moves;
	
	public Player(int id, String name, int ord, List<Move> moves) {
		this.id = id;
		this.name = name;
		this.moves = moves;
		this.ord = ord;
	}
}
