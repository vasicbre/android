package rs.ac.bg.etf.vn110012d;

import java.util.List;

import android.content.Context;
import android.webkit.WebView.FindListener;
import android.widget.GridView;

public class Simulator extends Thread {

	List<Player> players;
	int player, move, roll, moveCnt;

	String locked, result;

	GameplayActivity cb;

	DataAccessHandler dataHandler;

	Context context;

	public Simulator(Context context, int gameId, GameplayActivity cb) {
		this.context = context;
		dataHandler = new DataAccessHandler(context);
		dataHandler.open();
		players = dataHandler.getPlayers(gameId);
		player = move = roll = 0;
		moveCnt = countMoves();
		this.cb = cb;
		/*
		 * currentCnt = totalCnt = 0;
		 * 
		 * for (int i = 0; i < players.size(); i++) for (int j = 0; j <
		 * players.get(i).moves.size(); j++) for (int k = 0; k <
		 * players.get(i).moves.get(j).rolls.size(); k++) totalCnt++;
		 */
	}

	private int countMoves() {
		int cnt = 0;
		Move prev = null;
		for (Move m : players.get(0).moves) {
			if(prev == null) {
				prev = m;
				cnt++;
			}
			else {
				if(m.x != prev.x || m.y != prev.y)
					cnt++;
				
				prev = m;
			}
		}
		
		return cnt;
	}

	public int getMoveCnt() {
		return moveCnt;
	}

	public synchronized String getLocked() {
		return locked;
	}

	public synchronized String getResult() {
		return result;
	}

	public synchronized void moveFinished() {
		notifyAll();
	}

	public void run() {
		try {
			int cnt = 0;
			while (true) {

				Player p = players.get(player);
				Move m = p.moves.get(0);

				for (Roll r : m.rolls) {

					synchronized (this) {
						locked = r.locked;
						result = r.result;
					}

					cb.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							cb.shakingStarted();
						}
					});

					sleep(1000);

					cb.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							cb.shakingStopped();
						}
					});
				}

				final long parentId = Board.getParentViewId(m.x);
				int row = m.x - Board.getRowBase(parentId);
				int col = m.y;
				final int position = row * 6 + col;
				final GridView gv = (GridView) cb.findViewById((int) parentId);

				cb.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						gv.performItemClick(gv.getChildAt(position), position,
								parentId);
					}
				});

				synchronized (this) {
					wait(1000);
				}

				sleep(1000);

				p.moves.remove(0);

				if (player == players.size() - 1 && p.moves.isEmpty())
					break;

				player = (player + 1) % players.size();
				cnt++;

			}
		} catch (InterruptedException e) {
		}
	}
}
