import java.util.*;
import java.io.*;


public class Main {
	static int PacR, PacC;
	static HashMap<String, ArrayList<Integer>> DeadMon, Egg;
	static ArrayList<Monster> AliveMon;
	static ArrayList<int[]> pacmanMove;
	
	static int[][] dir = {{-1,0},{-1,-1},{0,-1},{1,-1},{1,0},{1,1},{0,1},{-1,1}};
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		int m = Integer.parseInt(st.nextToken());
		int t = Integer.parseInt(st.nextToken());
		
		st = new StringTokenizer(br.readLine());
		PacR = Integer.parseInt(st.nextToken()) - 1;
		PacC = Integer.parseInt(st.nextToken()) - 1;
		
		DeadMon = new HashMap<>();
		Egg = new HashMap<>();
		AliveMon = new ArrayList<>();
		pacmanMove = new ArrayList<>();
		
		for(int i=0;i<4;i++) {
			for(int j=0;j<4;j++) {
				for(int k=0;k<4;k++) {
					int[] move = {i, j, k};
					pacmanMove.add(move);
				}
			}
		}
		
		for(int i=0;i<m;i++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken()) - 1;
			int c = Integer.parseInt(st.nextToken()) - 1;
			int d = Integer.parseInt(st.nextToken()) - 1;
			
			AliveMon.add(new Monster(r, c, d));
		}
		
		for(int i=0;i<t;i++) {
			copyEgg();
			moveMonster();
			movePacMan();
			removeDead();
			makeEggToMonster();
			updateDeadLife();
		}
		

		System.out.println(AliveMon.size());
	}
	
	public static boolean isOutOfRange(int r, int c) {
		return (r < 0 || c < 0 || r >= 4 || c >= 4);
	}
	
	public static String makeStr(int r, int c) {
		return Integer.toString(r) + " " + Integer.toString(c);
	}
	
	public static void copyEgg() {
		// 몬스터의 각자 위치에서 자신과 방향이 똑같은 알을 복제한다
		for(int i=0;i<AliveMon.size();i++) {
			Monster monster = AliveMon.get(i);
			String pos = makeStr(monster.r, monster.c);
			if (!Egg.containsKey(pos)) {
				ArrayList<Integer> eggDir = new ArrayList<>();
				eggDir.add(monster.d);
				Egg.put(pos, eggDir);
			}
			else {
				ArrayList<Integer> eggDir = Egg.get(pos);
				eggDir.add(monster.d);
				Egg.put(pos, eggDir);
			}
			//System.out.println(pos + " " + monster.d);
		}
	}
	
	public static void moveMonster() {
		// 자신의 방향에서 1칸 이동
		for(int i=0;i<AliveMon.size();i++) {
			Monster monster = AliveMon.get(i);
			int cnt = 0;
			int d = monster.d;
			
			while (cnt < 8) {
				int nr = monster.r + dir[d][0];
				int nc = monster.c + dir[d][1];
				//System.out.println("next " + nr + " " + nc + " " + d);
				if (!isOutOfRange(nr, nc)) {
					//System.out.println("no out");
					String pos = makeStr(nr, nc);
					if ((DeadMon.get(pos) == null || DeadMon.get(pos).size() == 0) && !(PacR == nr && PacC == nc)) {
						// 현재 위치에 시체가 없거나, 팩맨과 같은 위치가 아닐 경우 이동
						Monster update = new Monster(nr, nc, d);
						AliveMon.set(i, update);
						break;
					}
				}
				d++;
				if (d >= 8) d = 0;
				cnt++;
			}
		}
	}
	
	public static void movePacMan() {
		int[][] dirPac = {{-1,0},{0,-1},{1,0},{0,1}};
		int[][] cnt = new int[4][4];
		
		for(int i=0;i<AliveMon.size();i++) {
			Monster monster = AliveMon.get(i);
			cnt[monster.r][monster.c] += 1;
		}
		

		int maxEat = -1;
		int maxIdx = -1;
		int maxR = -1, maxC = -1;
		for(int i=0;i<pacmanMove.size();i++) {
			int[] path = pacmanMove.get(i);
			int cr = PacR, cc = PacC;
			boolean out = false;
			int eat = 0;
			boolean[][] check = new boolean[4][4];
			check[cr][cc] = true;
			for(int j=0;j<3;j++) {
				int d = path[j];
				int nr = cr + dirPac[d][0], nc = cc + dirPac[d][1];
				if (isOutOfRange(nr, nc)) {
					out = true;
					break;
				}
				if (!check[nr][nc]) {
					eat += cnt[nr][nc];
					check[nr][nc] = true;
				}
				cr = nr; cc = nc;
			}

			if (!out) {
				if (eat > maxEat) {
					maxEat = eat;
					maxR = cr;
					maxC = cc;
					maxIdx = i;
				}
			}
		}
		
		if (maxIdx != -1) {
			int[] path = pacmanMove.get(maxIdx);
			int cr = PacR, cc = PacC;
			ArrayList<Monster> newAliveMon = new ArrayList<>();
			boolean[] deadCheck = new boolean[AliveMon.size()];
			HashMap<String, ArrayList<Integer>> AliveMap = new HashMap<>();
			
			
			for(int i=0;i<AliveMon.size();i++) {
				Monster mon = AliveMon.get(i);
				String pos = makeStr(mon.r, mon.c);
				if (!AliveMap.containsKey(pos)) {
					ArrayList<Integer> newList = new ArrayList<>();
					newList.add(i);
					AliveMap.put(pos, newList);
				}
				else {
					ArrayList<Integer> newList = AliveMap.get(pos);
					newList.add(i);
					AliveMap.put(pos, newList);
				}
			}
			
			for(int i=0;i<3;i++) {
				int d = path[i];
				int nr = cr + dirPac[d][0], nc = cc + dirPac[d][1];
				
//				for(int j=0;j<AliveMon.size();j++) {
//					Monster monster = AliveMon.get(j);
//					if (monster.r == nr && monster.c == nc) {
//						String pos = makeStr(nr, nc);
//						if (DeadMon.containsKey(pos)) {
//							ArrayList<Integer> dead = DeadMon.get(pos);
//							dead.add(0);
//							DeadMon.put(pos, dead);
//						}
//						else {
//							ArrayList<Integer> dead = new ArrayList<>();
//							dead.add(0);
//							DeadMon.put(pos, dead);
//						}
//						deadCheck[j] = true;
//					}
//				}
				
				String pos = makeStr(nr, nc);
				ArrayList<Integer> deadList = DeadMon.get(pos);
				if (deadList == null) deadList = new ArrayList<>();
				
				ArrayList<Integer> list = AliveMap.get(pos);
				if (list != null) { // 해당 위치에 살아있는 몬스터가 있으니까 팩맨이 먹음 
					for(int idx: list) {
						deadList.add(0);
						deadCheck[idx] = true;
					}
					DeadMon.put(pos, deadList);
				}
				cr = nr; cc = nc;
				
			}
			PacR = maxR;
			PacC = maxC;
			for(int i=0;i<AliveMon.size(); i++) {
				if (!deadCheck[i]) {
					Monster alive = AliveMon.get(i);
					newAliveMon.add(alive);
				}
			}
			
			AliveMon = newAliveMon;
		}
	}
	
	public static void removeDead() {
		Set<String> positions = DeadMon.keySet();
		for(String pos: positions) {
			ArrayList<Integer> dead = DeadMon.get(pos);
			if (dead != null) {
				boolean[] check = new boolean[dead.size()];
				ArrayList<Integer> newDead = new ArrayList<>();
				for(int i=0;i<dead.size();i++) {
					if (dead.get(i) == 2) {
						check[i] = true;
					}
				}
				for(int i=0;i<dead.size();i++) {
					if (!check[i]) {
						newDead.add(dead.get(i));
					}
				}
				DeadMon.put(pos, newDead);
			}
		}
	}
	
	public static void makeEggToMonster() {
		Set<String> positions = Egg.keySet();
		for(String pos: positions) {
			ArrayList<Integer> egg = Egg.get(pos);
			if (egg != null) {
				for(int d: egg) {
					StringTokenizer st = new StringTokenizer(pos);
					int r = Integer.parseInt(st.nextToken());
					int c = Integer.parseInt(st.nextToken());
					AliveMon.add(new Monster(r, c, d));
				}
			}
		}
		Egg = new HashMap<>();
	}

	public static void updateDeadLife() {
		Set<String> positions = DeadMon.keySet();
		for(String pos: positions) {
			ArrayList<Integer> dead = DeadMon.get(pos);
			for(int i=0;i<dead.size(); i++) {
				int life = dead.get(i) + 1;
				dead.set(i, life);
			}
			DeadMon.put(pos, dead);
		}
	}
	
	static class Monster {
		int r, c, d;
		Monster(int r, int c, int d) {
			this.r = r;
			this.c = c;
			this.d = d;
		}
	}
	
}