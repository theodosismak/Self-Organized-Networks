package beans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import jenes.chromosome.BooleanChromosome;

import com.rits.cloning.Cloner;

public class SolutionBean {
		
	int[][][] X;
	int[][] Y;
	
	DataBean db;
	int k;
	
	public SolutionBean(DataBean db) {
		this.db = db;
		X = new int[db.getNumOfUsers()][db.maxCells][db.getNumOfRBs()];
		Y = new int[db.maxCells][db.getNumOfPLs()];
		initX();
		initY();
	}

	public int[][][] getX() {
		return X;
	}	

	public void setX(int[][][] x) {
		X = x;
	}

	public int[][] getY() {
		return Y;
	}	

	public void setY(int[][] y) {
		Y = y;
	}
	
	public double getSignalPerRBPerCell(int[][] Y, int uid, int cid, int r) {
		double sum = 0.0;
		for (int k=0; k<db.getNumOfPLs(); k++) {
			sum += Y[cid][k] * db.getPL(k) * db.getCell(cid).getPmax() / (db.getNumOfRBs() * db.getLoss(uid, cid));
		}
		return sum;
	}
	//the upper getSignalPerRBPerCell is used here
	public double getSignal(int[][][] X, int[][] Y, int uid, int r) {
		double sum = 0.0;
		for (int c=0; c<db.maxCells; c++) {
			sum += X[uid][c][r] * getSignalPerRBPerCell(Y,uid,c,r);
		}
		return sum;
	}
	//Summation X(u,c,r)
	public int getNR(int[][][] X, int r, int cid) {
		int sum = 0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			sum += X[u][cid][r];
		}
		return sum;
		
	}
	
	//Binary variable that depicts whether RB of cell is assigned.
	//A(c,r)
	public int getIhelp(int[][][] X, int cid, int r) {
		return getNR(X,r,cid) > 0 ? 1 : 0;
	}
	//We calculate Interference.
	public double getInterference(int[][][] X, int[][] Y, int uid, int r) {
		double sum = 0.0;
		for (int c=0; c<db.maxCells; c++) {
			sum += getIhelp(X, c, r) * getSignalPerRBPerCell(Y,uid,c,r);
			
		}
		return sum - getSignal(X, Y, uid, r);
	}
	//SNIR
	public double getSNIR(int[][][] X, int[][] Y, int uid, int r) {
		return getSignal(X,Y,uid, r) / (getInterference(X,Y,uid, r) + db.getN() * db.getBw());
	}
	//T(u,r)
	public double getThr(int[][][] X, int[][] Y, int uid, int r) {
		return db.getBw() * Math.log1p(getSNIR(X, Y, uid, r)) / Math.log(2.0);
	}
	//T(u)
	public double getUserThroughput(int[][][] X, int[][] Y, int uid) {
		double sum = 0.0;
		for (int c = 0; c < db.maxCells; c++) {
			for (int r = 0; r < db.getNumOfRBs(); r++) {
				sum += X[uid][c][r] * getThr(X, Y, uid, r);
			}
		}
		return sum;
	}
	//the sum of the system's Throughput  (corresponds to all users)
	public double getTotalThroughput(int[][][] X, int[][] Y) {
		double sum = 0.0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			sum += getUserThroughput(X, Y, u);
		}
		return sum;
	}
	
	public int getSumOfRBs(int[][][] X, int uid, int cid) {
		int sum = 0;
		for (int r=0; r<db.getNumOfRBs(); r++) {
			sum += X[uid][cid][r];
		}
		return sum;
	}
	
	//one RB is assigned to each cell 
	public int getUserAssignedCell(int[][][] X, int uid) {
		for (int c=0; c<db.maxCells; c++) {
			if (getSumOfRBs(X, uid, c) > 0) {
				return c;
			}
		}
		return -1;
	}
	
	public double getCellThroughput(int[][][] X, int[][] Y, int cid) {
		double sum = 0.0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			for (int r=0; r<db.getNumOfRBs(); r++) {
				sum += X[u][cid][r] * getThr(X, Y, u, r);
			}
		}
		return sum;
	}
	//constrain 4
	public boolean checkThroughput(int[][][] X, int[][] Y) {
		for (int u=0; u<db.getNumOfUsers(); u++) {
			if (!isUserThroughputOK(X, Y, u)) {
				return false;
			}
		}
		return true;
	}
	//checks if the throughput offered to the user is greater than the demand
	public boolean isUserThroughputOK(int[][][] X, int[][] Y, int u) {
		return (getUserThroughput(X, Y, u) >= db.getUser(u).getDemand());
	}
	//constraint 1
	public boolean checkUniqueCellPerUser(int[][][] X) {
		for (int u=0; u<db.getNumOfUsers(); u++) {
			for (int c1=0; c1<db.maxCells-1; c1++) {
				for (int c2=c1+1; c2<db.maxCells; c2++) {
					if (getSumOfRBs(X, u, c1) * getSumOfRBs(X, u, c2) != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public boolean checkUniqueRBPerUser(int[][][] X) {
		for (int c=0; c<db.maxCells; c++) {
			for (int r=0; r<db.getNumOfRBs(); r++) {
				if (getNR(X, r, c) > 1) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isRBAssignedToOtherUser(int[][][] X, int uid, int cid, int r) {
		int sum = 0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			if (u != uid) {
				sum += X[u][cid][r];
			}
		}
		return (sum > 0);
	}
	
	public int getNumOfAssignedPLs(int[][] Y, int cid) {
		int sum = 0;
		for (int k=0; k<db.getNumOfPLs(); k++) {
			sum += Y[cid][k];
		}
		return sum;
	}
	//constraint 3
	public boolean checkUniquePLPerCell(int[][] Y) {
		for (int c=0; c<db.maxCells; c++) {
			if (getNumOfAssignedPLs(Y, c) != 1) {
				return false;
			}
		}
		return true;
	}
	
	public double getPowerRatio(int[][] Y, int cid) {
		double sum = 0.0;
		for (int k=0; k<db.getNumOfPLs(); k++) {
			sum += Y[cid][k] * db.getPL(k);
		}
		return sum;
	}
	//we want macro to operate always at 100%
	public boolean checkMacroPL(int[][] Y) {
		for (int c=0; c<db.maxCells; c++) {
			if (db.getCell(c).getType() == 0) {
				if (getPowerRatio(Y, c) != 1.0) {
					return false;
				}
			}
		}
		return true;
	}
		
	public void printConstraints(int[][][] X, int[][] Y){
		System.out.println("check Throughput: "+checkThroughput(X,Y));
		System.out.println("check Unique Cell Per User: "+checkUniqueCellPerUser(X));
		System.out.println("check Unique Power Level Per Cell: "+checkUniquePLPerCell(Y));
		System.out.println("check Unique RB Per User: "+checkUniqueRBPerUser(X));
		System.out.println("check Macro Power Level: "+checkMacroPL(Y));
		System.out.println("check Hard Constraint: "+checkHC(X));
	}
	
	public boolean checkSC(int[][][] X, int[][] Y) {
//		System.out.println("CT: "+checkThroughput(X,Y)+" CUC: "+checkUniqueCellPerUser(X)+" CUP: "+checkUniquePLPerCell(Y)+" CUR: "+checkUniqueRBPerUser(X)+"CMP: "+checkMacroPL(Y)+" OF: "+getOF(X, Y));
		return (checkThroughput(X,Y) && checkUniqueCellPerUser(X) && checkUniquePLPerCell(Y) && checkUniqueRBPerUser(X) && checkMacroPL(Y));
	}
	//constraint 5
	public boolean checkHC(int[][][] X) {
		//boolean flag = true;
		for (int r=0; r<db.getNumOfRBs(); r++) {
			for (int c1=0; c1<db.maxCells-1; c1++) {
				if (db.getCell(c1).getType() != 0) {
					continue;
				}
				else {
					for (int c2=c1+1; c2<db.maxCells; c2++) {
						if (getNR(X, r, c1) * getNR(X, r, c2) != 0) {
							return false;
						}
					}
				}
			}
		}
		return true;
		//System.out.println("******"+flag);
		//return flag;
	}
	
	public boolean isRBAssignedToOtherCell(int[][][] X, int r, int cid) {
		for (int c=0; c<db.maxCells; c++) {
			if (c == cid) {
				continue;
			}
			else {
				Cell ce = db.getCell(cid);
				if (ce.getType() == 0) {
					if (getNR(X, r, c) > 0) {
						return true;
					}
				}
				else {
					Cell co = db.getCell(c);
					if (co.getType() == 0) {
						if (getNR(X, r, c) > 0) {
							return true;
						}
					}
					else {
						continue;
					}
				}
			}
		}
		return false;
	}
	
	public void initX() {
		for (int u=0; u<db.getNumOfUsers(); u++) {
			for (int c=0; c<db.maxCells; c++) {
				for (int r=0; r<db.getNumOfRBs(); r++) {
					X[u][c][r] = 0;
				}
			}
		}
	}
	
	public void initY() {
		for (int c=0; c<db.maxCells; c++) {
			for (int k=0; k<db.getNumOfPLs(); k++) {
				Y[c][k] = 0;
			}
		}
	}
	
	public void createInitSolution() {
		System.out.println("Checking for initial solution...");
		do {
			for (int c=0; c<db.maxCells; c++) {
				for (int k=0; k<db.getNumOfPLs(); k++) {
					Y[c][k] = Global.rand.nextInt(2);
				}
			}
		} while (!checkMacroPL(Y) || !checkUniquePLPerCell(Y));
		System.out.println("CreateInitSolution: Y matrix is OK");
		
		do {
			for (int u=0; u<db.getNumOfUsers(); u++) {
				for (int c=0; c<db.maxCells; c++) {
					for (int r=0; r<db.getNumOfRBs(); r++) {
						X[u][c][r] = Global.rand.nextInt(2);
						//System.out.println("//////////////////////////////////////");
					}
				}
			}
		} while (!checkSC(X,Y));
		System.out.println("CreateInitSolution: X matrix is OK");
	}
	//create the matrix Y of the initial solution
	public void createInitYMatrix() {
		//System.out.print("CreateInitSolution: Computing Y matrix");
		do {
			for (int c=0; c<db.maxCells; c++) {     
				for (int k=0; k<db.getNumOfPLs(); k++) {
					Y[c][k] = Global.rand.nextInt(2);
					//System.out.println(Y[c][k]);
				}
			}
			//System.out.println(".");  //typwnei 45 "."
			
			
		} while (!checkMacroPL(Y) || !checkUniquePLPerCell(Y));
		//System.out.println("[OK]");
	}
	
	public void createInitXMatrix_withHC() {
	
		for (int u=0; u<db.getNumOfUsers(); u++) {
			int nc = db.getNearestCell(u);
//			System.out.println("User: "+u+" Nearest cell: "+nc);
			int r = 0;
			
			do {
//				System.out.println("User: "+u+" Cell: "+nc+" RB: "+r+" Throughput: "+getUserThroughput(X, Y, u)+" "+
//						isRBAssignedToOtherUser(X, u, nc, r)+" "+isRBAssignedToOtherCell(X, r, nc));
				if (!isRBAssignedToOtherUser(X, u, nc, r) && !isRBAssignedToOtherCell(X, r, nc)) {
					X[u][nc][r] = 1;
					//System.out.println("User: "+u+" Cell: "+nc+" RB: "+r+" Throughput: "+getUserThroughput(X, Y, u));
					
				}
				r=(r+1)%db.getNumOfRBs(); //till r reaches the number of RBs that we have defined in DataBean
				
			} while(!isUserThroughputOK(X, Y, u));
		}
		for (int r=0; r<db.getNumOfRBs(); r++) {
			if (getRBsAssignedCells(X, r).isEmpty()) {
				boolean flag = false;
				do {
					int u = Global.rand.nextInt(db.getNumOfUsers());
					int nc = db.getNearestCell(u);
					if (!isRBAssignedToOtherUser(X, u, nc, r) && !isRBAssignedToOtherCell(X, r, nc)) {
						X[u][nc][r] = 1;
						flag = true;
					}
				} while (!flag);
			}
		}
	}
	
	public BooleanChromosome makeChromosome(int[][][] X, int[][] Y){
		 
		BooleanChromosome bc = new BooleanChromosome(db.getNumOfUsers()*db.maxCells*db.getNumOfRBs() + db.maxCells*db.getNumOfPLs());
		int index = 0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			for (int c=0; c<db.maxCells; c++) {
				for (int r=0; r<db.getNumOfRBs(); r++) {
					if (X[u][c][r] == 1) {
						bc.setValue(index, true);
					} else {
						bc.setValue(index, false);
					}
				    index++;
				}
			}
		}
		for (int c=0; c<db.maxCells; c++) {
			for (int k=0; k<db.getNumOfPLs(); k++) {
				if (Y[c][k] == 1) {
					bc.setValue(index, true);
				} else {
					bc.setValue(index, false);
				}
				index++;
			}
		}
		return bc;
	}
	    	
	public void createInitXMatrix() {
		for (int u=0; u<db.getNumOfUsers(); u++) {
			int nc = db.getNearestCell(u);
//			System.out.println("User: "+u+" Nearest cell: "+nc);
			int r = 0;
			do {				
				if (!isRBAssignedToOtherUser(X, u, nc, r)) {
					X[u][nc][r] = 1;
				}
				r=(r+1)%db.getNumOfRBs();
			} while(!isUserThroughputOK(X, Y, u));
		}
		for (int r=0; r<db.getNumOfRBs(); r++) {
			if (getRBsAssignedCells(X, r).isEmpty()) {
				boolean flag = false;
				do {
					int u = Global.rand.nextInt(db.getNumOfUsers());
					int nc = db.getNearestCell(u);
					if (!isRBAssignedToOtherUser(X, u, nc, r)) {
						X[u][nc][r] = 1;
						flag = true;
					}
				} while (!flag);
			}
		}
	}
	
	public int[][][] makeX(BooleanChromosome bc){
		int index=0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			for (int c=0; c<db.maxCells; c++) {
				for (int r=0; r<db.getNumOfRBs(); r++) {
					if (bc.getValue(index) == true){
						X[u][c][r] = 1;	
					} else {
						X[u][c][r] = 0;
					}
					index++;					
				}
			}
		}
		return X;
	}
	
	public int [][] makeY(BooleanChromosome bc){
		int index=db.getNumOfUsers()*db.maxCells*db.getNumOfRBs();
		for (int c=0; c<db.maxCells; c++) {
			for (int k=0; k<db.getNumOfPLs(); k++) {
				if (bc.getValue(index)){
					Y[c][k] = 1;
				} else {
					Y[c][k] = 0;
				}
				index++;
			}
		}
		return Y;
	}
	
	//11 OF
	public double getOF(int[][][] X, int[][] Y) {
        double sum = 0.0;
		double sum2 = 0.0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			//System.out.println(u);
			sum += db.getW()[db.getUser(u).getUserClass()] * getUserThroughput(X, Y, u);
			//System.out.println(db.getUser(u).getUserClass());
		//System.out.println(db.getW()[db.getUser(u).getUserClass()] * getUserThroughput(X, Y, u));
		//System.out.println("||"+X);
		
		}
		//System.out.println("||||||||||||"+sum);
		
		//
		for (int c=0; c<db.maxCells; c++) {
			for (int k=0; k<db.getNumOfPLs(); k++) {	
			sum2 += Y[c][k] * db.getL()[k] * db.getCell(c).getPmax();
		    }
		}
		return sum/sum2;
	}
	
	public double getOF(BooleanChromosome bc){
		int[][][] X = makeX(bc);
		int[][] Y = makeY(bc);
		
			return getOF(X,Y);
		
	}

	public DataBean getDb() {
		return db;
	}
	
	public int getCellPL(int[][] Y, int cid) {
		for (int k=0; k<db.getNumOfPLs(); k++) {
			if (Y[cid][k] == 1) {
				return k;
			}
		}
		return -1;
	}
	
	public SolutionBean createNeighbYMatrix() {	
		Cloner cloner = new Cloner();
		SolutionBean neighb;
		int[][] Ytest;
		do {
			neighb = cloner.deepClone(this);
			Ytest = neighb.getY().clone();
			int randCell = Global.rand.nextInt(db.getNumOfCells());	
			int curPL = getCellPL(Ytest, randCell);
			int newPL = (curPL + 1) % db.getNumOfPLs();
			Ytest[randCell][curPL] = 0;
			Ytest[randCell][newPL] = 1;
		} while (!checkMacroPL(Ytest) || !checkUniquePLPerCell(Ytest));
//		System.out.println("CreateNeighbYMatrix: Found neighbor");
		neighb.setY(Ytest);
		return neighb;
	}
	
	public SolutionBean createNeighbXMatrix_withHC() {
//		System.out.println("Creating neighbor solution");
		Cloner cloner = new Cloner();
		SolutionBean neighb;
		int[][][] Xtest;
		int randUser = -1, randRB = -1, nc = -1;
		boolean flag = false;
		do {
			neighb = cloner.deepClone(this);
			Xtest = neighb.getX().clone();
			randUser = Global.rand.nextInt(db.getNumOfUsers());
			randRB = Global.rand.nextInt(db.getNumOfRBs());
			nc = db.getNearestCell(randUser);
			Xtest[randUser][nc][randRB] = 1 - Xtest[randUser][nc][randRB];
			flag = !isRBAssignedToOtherUser(Xtest, randUser, nc, randRB) && 
					!isRBAssignedToOtherCell(Xtest, randRB, nc) && checkThroughput(Xtest, neighb.getY());
//			System.out.println("User: "+randUser+" Cell: "+nc+" RB: "+randRB+" X: "+Xtest[randUser][nc][randRB]+" Flag: "+flag);			
		} while (!flag);		
		neighb.setX(Xtest);
//		System.out.println("User: "+randUser+" Cell: "+nc+" RB: "+randRB+" X: "+neighb.getX()[randUser][nc][randRB]+" Flag: "+flag);
		SolutionBean cs = cloner.deepClone(neighb);
		return cs;
	}
	
	public SolutionBean createNeighbXMatrix() {
//		System.out.println("Creating neighbor solution");
		Cloner cloner = new Cloner();
		SolutionBean neighb;
		int[][][] Xtest;
		int randUser = -1, randRB = -1, nc = -1;
		boolean flag = false;
		do {
			neighb = cloner.deepClone(this);
			Xtest = neighb.getX().clone();
			randUser = Global.rand.nextInt(db.getNumOfUsers());
			randRB = Global.rand.nextInt(db.getNumOfRBs());
			nc = db.getNearestCell(randUser);
//			System.out.println(randUser+" "+nc+" "+randRB);
			Xtest[randUser][nc][randRB] = 1 - Xtest[randUser][nc][randRB];
			flag = !isRBAssignedToOtherUser(Xtest, randUser, nc, randRB) && 
					checkThroughput(Xtest, neighb.getY());
//			System.out.println("User: "+randUser+" Cell: "+nc+" RB: "+randRB+" X: "+neighb.getX()[randUser][nc][randRB]+" Flag: "+flag+" OF: "+neighb.getOF(neighb.getX(), neighb.getY()));
		} while (!flag);
		neighb.setX(Xtest);
//		System.out.println("User: "+randUser+" Cell: "+nc+" RB: "+randRB+" X: "+neighb.getX()[randUser][nc][randRB]+" Flag: "+flag+" OF: "+neighb.getOF(neighb.getX(), neighb.getY()));
		SolutionBean cs = cloner.deepClone(neighb);
		return cs;
	}
	
	public void printXY() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("./XY.txt", false));
			bw.write("User\tCell\tRB\tValue");
			bw.newLine();
			for (int u=0; u<db.getNumOfUsers(); u++) {
				for (int c=0; c<db.maxCells; c++) {
					for (int r=0; r<db.getNumOfRBs(); r++) {
						bw.write(u+"\t"+c+"\t"+r+"\t"+X[u][c][r]);
						bw.newLine();
					}
				}
			}
			bw.newLine();
			bw.write("Cell\tPL\tValue");
			bw.newLine();
			for (int c=0; c<db.maxCells; c++) {
				for (int k=0; k<db.getNumOfPLs(); k++) {
					bw.write(c+"\t"+k+"\t"+Y[c][k]);
					bw.newLine();
				}
			}
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double getUCAvgThroughput(int[][][] X, int[][] Y, int uc) {
		double sum = 0.0;
		int usr = 0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			if (db.getUser(u).getUserClass() == uc) {
				usr++;
				sum += getUserThroughput(X, Y, u);				
			}
		}
		return sum / usr;
	}
	
	public double getUCThroughput(int[][][] X, int[][] Y, int uc) {
		double sum = 0.0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			if (db.getUser(u).getUserClass() == uc) {
				sum += getUserThroughput(X, Y, u);				
			}
		}
		return sum;
	}
	
	public int getUCUsers(int uc) {
		int usr = 0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			if (db.getUser(u).getUserClass() == uc) {
				usr++;				
			}
		}
		return usr;
	}
	
	public double getUCSquareThroughput(int[][][] X, int[][] Y, int uc) {
		double sum = 0.0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			if (db.getUser(u).getUserClass() == uc) {
				sum += Math.pow(getUserThroughput(X, Y, u), 2);				
			}
		}
		return sum;
	}
	
	public double getUCFairness(int[][][] X, int[][] Y, int uc) {
		return Math.pow(getUCThroughput(X, Y, uc), 2) / (getUCUsers(uc) * getUCSquareThroughput(X, Y, uc));
	}
	
	public int getCellUsers(int[][][] X, int cid) {
		int sum = 0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			for (int r=0; r<db.getNumOfRBs(); r++) {
				if (X[u][cid][r] == 1) {
					sum++;
					break;
				}
			}			
		}
		return sum;
	}
	
	public double getUserAvgInterferencePerRB(int[][][] X, int[][] Y, int uid) {
		double sum = 0.0;
		ArrayList<Integer> rbList = getUsersAssignedRBs(X, uid);
		for (int r : rbList) {
			sum += getInterference(X, Y, uid, r);
		}		
		return sum / getUserRBs(X, uid);
	}
	
	public double getUserAvgInterferencePerRB_DB(int[][][] X, int[][] Y, int uid) {
		return  10.0 * Math.log10(getUserAvgInterferencePerRB(X, Y, uid));
	}
	
	public double getUserAvgSignalPerRB(int[][][] X, int[][] Y, int uid) {
		double sum = 0.0;
		ArrayList<Integer> rbList = getUsersAssignedRBs(X, uid);
		for (int r : rbList) {
			sum += getSignal(X, Y, uid, r);
		}
		double sumdb = 10.0 * Math.log10(sum);
		return sumdb / getUserRBs(X, uid);
	}
	
	public double getUserAvgSNIRPerRB(int[][][] X, int[][] Y, int uid) {
		double sum = 0.0;
		for (int r=0; r<db.getNumOfRBs(); r++) {
			sum += getSNIR(X, Y, uid, r);
		}
		double sumdb = 10.0 * Math.log10(sum);
		return sumdb / getUserRBs(X, uid);
	}
	
	public int getUserRBs(int[][][] X, int uid) {
		int sum = 0;
		for (int c=0; c<db.maxCells; c++) {
			for (int r=0; r<db.getNumOfRBs(); r++) {
				if (X[uid][c][r] == 1) {
					sum++;
				}
			}
		}
		return sum;
	}
	
	public double getAllUserAvgInterferencePerRB(int[][][] X, int[][] Y) {
		double sum = 0.0;
		for (int u=0; u<db.getNumOfUsers(); u++) {
			int rbs = getUserRBs(X, u);
			double intrf = 0.0;
			for (int r=0; r<db.getNumOfRBs(); r++) {
				intrf += getInterference(X, Y, u, r);
			}
			intrf /= rbs;
			sum += intrf;
		}
		sum /= db.getNumOfUsers();
		return sum;
	}
	
	public double getCellUserAvgInterference_DB(int[][][] X, int[][] Y, int cid) {
		double sum = 0.0;
		ArrayList<Integer> userList = getCellUserList(X, cid);
		for (int uid : userList) {
			sum += getUserAvgInterferencePerRB(X, Y, uid);
		}
//		System.out.println("Cell: "+cid+" User list: "+userList+" Sum of Interferences: "+sum);
		sum /= userList.size();
		return 10.0 * Math.log10(sum);
	}
	
	public void printResults(int[][][] X, int[][] Y, String name) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(name, false));
			bw.write("Cell\tUsers\tThroughput\tPL\tUser avg. interference");
			bw.newLine();
			for (int c=0; c<db.maxCells; c++) {
				bw.write(c+"\t"+getCellUsers(X, c)+"\t"+getCellThroughput(X, Y, c)+"\t"+
						db.getPL(getCellPL(Y, c))+"\t"+getCellUserAvgInterference_DB(X, Y, c));
				bw.newLine();
			}
			bw.newLine();
			bw.write("UC\tThroughput\tFairness");
			bw.newLine();
			for (int uc=0; uc<db.getNumOfUCs(); uc++) {
				bw.write(uc+"\t"+getUCAvgThroughput(X, Y, uc)+"\t"+getUCFairness(X, Y, uc));
				bw.newLine();
			}
			bw.newLine();
			bw.write("User\tCell\tDemand\tThroughput\tAvg. Interference\tAvg. Signal\tAvg. SNIR\tRBs");
			bw.newLine();
			for (int u=0; u<db.getNumOfUsers(); u++) {
				bw.write(u+"\t"+getUserAssignedCell(X, u)+"\t"+db.getUser(u).getDemand()+
						"\t"+getUserThroughput(X, Y, u)+"\t"+getUserAvgInterferencePerRB_DB(X, Y, u)+
						"\t"+getUserAvgSignalPerRB(X, Y, u)+"\t"+getUserAvgSNIRPerRB(X, Y, u)+
						"\t"+getUsersAssignedRBs(X, u));
				bw.newLine();
			}
			bw.newLine();
			bw.write("RB\tCell List");
			bw.newLine();
			for (int r=0; r<db.getNumOfRBs(); r++) {
				bw.write(r+"\t"+getRBsAssignedCells(X, r));
				bw.newLine();
			}
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Integer> getCellUserList(int[][][] X, int cid) {
		ArrayList<Integer> userList = new ArrayList<Integer>();
		for (int u=0; u<db.getNumOfUsers(); u++) {
			if (getUserAssignedCell(X, u) == cid) {
				userList.add(u);
			}
		}
		return userList;
	}
	
	public ArrayList<Integer> getRBsAssignedCells(int[][][] X, int r) {
		ArrayList<Integer> cellList = new ArrayList<Integer>();
		for (int c=0; c<db.maxCells; c++) {
			if (getNR(X, r, c) > 0) {
				cellList.add(c);
			}
		}
		return cellList;
	}
	
	public ArrayList<Integer> getUsersAssignedRBs(int[][][] X, int uid) {
		ArrayList<Integer> rbList = new ArrayList<Integer>();
		int assCell = getUserAssignedCell(X, uid);
		if (assCell == -1) {
			System.err
					.println("GetUsersAssignedRBs : No assigned cell for user "
							+ uid);
			return null;
		} else {
			for (int r = 0; r < db.getNumOfRBs(); r++) {
				if (X[uid][assCell][r] == 1) {
					rbList.add(r);
				}
			}
			return rbList;
		}
	}	

}
