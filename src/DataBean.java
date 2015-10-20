package beans;


import java.util.List;
import java.util.Arrays;

public class DataBean {
	int numOfCells, numOfUsers, numOfRBs, numOfPLs, numOfUCs, maxCells, B;
	double N_db, N, bw, radious;
	double[] demand = {256e3, 512e3, 1024e3};
	double[] l = {1.0, 0.8, 0.6};
	double[] powerPerHz = {3.981e-6, 1.259e-6};
	double[] w = {0.028571, 0.057143, 0.11429};
	Cell[] cellArray;
	User[] userArray;

	public DataBean() {
		radious = 500.0;
		bw = 180e3;
		numOfUCs = demand.length;
		numOfPLs = l.length;
		N_db = -204.0;
		N = Math.pow(10.0, N_db/10.0);
		
	}
	
	public void initBean(){
		numOfRBs = 5 * B;		
		userArray = new User[numOfUsers];
		cellArray = new Cell[maxCells];
		cellArray[0] = new Cell();
		cellArray[0].setId(0);
		cellArray[0].setX(radious/2.0);
		cellArray[0].setY(radious/2.0);
		cellArray[0].setType(0);
		cellArray[0].setPmax(B * 1e6 * powerPerHz[cellArray[0].getType()]);
		cellArray[0].addPL(1.0);
		initUsers();
		if (maxCells>1)	
			initCells();
	}
	
	public void initCells() {
		for (int c=1; c<maxCells; c++) {
			cellArray[c] = new Cell();
			cellArray[c].setId(c);
			cellArray[c].setX(radious/4.0);
			cellArray[c].setY(radious/4.0);
			cellArray[c].setType(1);
			cellArray[c].setPmax(B * 1e6 * powerPerHz[cellArray[1].getType()]);
			cellArray[c].addPL(1.0);
			cellArray[c].addPL(0.8);
			cellArray[c].addPL(0.6);
			do {
				cellArray[c].setX(Global.rand.nextDouble() * radious);
				cellArray[c].setY(Global.rand.nextDouble() * radious);
			} while (getCellDistance(c,0) >= radious);
		}
	}
	
	public void initUsers() {
		for (int u=0; u<numOfUsers; u++) {
			userArray[u] = new User();
			userArray[u].setId(u);
			userArray[u].setUserClass(u % numOfUCs);
			userArray[u].setDemand(demand[u % numOfUCs]);
			userArray[u].setX(10.0 * radious);
			userArray[u].setY(10.0 * radious);
			do {
				userArray[u].setX(Global.rand.nextDouble() * radious);
				userArray[u].setY(Global.rand.nextDouble() * radious);
			} while (getDistance(u,0) >= radious);
		}
	}
	
	public double getDistance(int uid, int cid) {
		User u = getUser(uid);
		Cell c = getCell(cid);
		return Math.sqrt(Math.pow(u.getX()-c.getX(), 2) + Math.pow(u.getY()-c.getY(), 2));
	}
	
	public double getCellDistance(int scid, int mcid) {
		Cell sc = getCell(scid);
		Cell mc = getCell(mcid);
		return Math.sqrt(Math.pow(sc.getX()-mc.getX(), 2) + Math.pow(sc.getY()-mc.getY(), 2));
	}
	
	public int getNearestCell(int uid) {
		int bestCell = -1;
		double bestDist = Double.MAX_VALUE;
		for (int cid=0; cid<maxCells; cid++) {
			double dist = getDistance(uid, cid);
			if (dist < bestDist) {
				bestDist = dist;
				bestCell = cid;
			}
		}
		return bestCell;
	}
	
	public double getLoss(int uid, int cid) {
		return Math.pow(10.0, (128.1 + 37.6 * Math.log10(getDistance(uid, cid)/1000)) / 10.0);
	}
	
	public Cell getCell(int id) {
		if (id >= maxCells) {
			System.err.println("GetCell: Cell "+id+" was not found");
			return null;
		}
		else {
			return cellArray[id];
		}
	}
	
	public User getUser(int id) {
		if (id >= numOfUsers) {
			System.err.println("GetUser: User "+id+" was not found");
			return null;
		}
		else {
			return userArray[id];
		}
	}


	public int getNumOfCells() {
		return numOfCells;
	}

	public int getNumOfUsers() {
		return numOfUsers;
	}

	public int getNumOfRBs() {
		return numOfRBs;
	}

	public int getNumOfPLs() {
		return numOfPLs;
	}

	public int getNumOfUCs() {
		return numOfUCs;
	}

	public int getMaxCells() {
		return maxCells;
	}

	public int getB() {
		return B;
	}

	public double getN_db() {
		return N_db;
	}

	public double getN() {
		return N;
	}

	public double getBw() {
		return bw;
	}

	public double getRadious() {
		return radious;
	}

	public double[] getDemand() {
		return demand;
	}

	public double[] getL() {
		return l;
	}

	public User[] getUserArray() {
		return userArray;
	}

	public List<Cell> getCellList() {
		return Arrays.asList(Arrays.copyOfRange(cellArray, 0, numOfCells));
	}
	
	public double getPL(int k) {
		if (k>=0 && k<numOfPLs) {
			return l[k];
		}
		else {
			System.err.println("GetPL: Power level "+k+" was not found");
			return -1;
		}
	}

	public double[] getW() {
		return w;
	}

	public void setNumOfCells(int numOfCells) {
		this.numOfCells = numOfCells;
		this.maxCells = this.numOfCells+1;
	}

	
	public void setNumOfUsers(int numOfUsers) {
		this.numOfUsers = numOfUsers;
	}

	public void setNumOfRBs(int numOfRBs) {
		this.numOfRBs = numOfRBs;
	}

	public void setNumOfPLs(int numOfPLs) {
		this.numOfPLs = numOfPLs;
	}

	public void setNumOfUCs(int numOfUCs) {
		this.numOfUCs = numOfUCs;
	}

	public void setB(int b) {
		B = b;
	}

	public void setRadious(double radious) {
		this.radious = radious;
	}
	
	

}
