package algo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.*;


import beans.Global;
import beans.SolutionBean;

import com.rits.cloning.*;

public class SA {

    SolutionBean best, curbest;
    double bcost;
    int pointer = 0, counter = 0;
    boolean umscript = true;
    long start = 0, stop_best = 0, stop = 0, stop_first = 0;

    public SolutionBean simple_Annealing(SolutionBean INS, boolean hc) {    	
        SolutionBean CBS, NS, IS;
       
        double CT = 100.0, T = 100.0, Dcost = Double.MAX_VALUE, d = 0.0;
        double tol = 0.01, c_factor = 0.95, cbcost = Double.MAX_VALUE;
        int iteration = 3, max_it = 10000, tol_it = 500;
        int countToStop = 0;
        Cloner cloner = new Cloner();
        bcost = Double.MAX_VALUE;
        IS = cloner.deepClone(INS);
        
        System.out.println(IS.getUCUsers(2)+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        CBS = cloner.deepClone(IS);
        best = cloner.deepClone(IS);
        NumberFormat df = new DecimalFormat("#.##########");
        System.out.println("***** Simulated Annealing *****");
        try {
        	BufferedWriter bw2 = new BufferedWriter(new FileWriter("./SA.txt"));
			bw2.write(counter + "\t" + df.format(CBS.getOF(CBS.getX(), CBS.getY())) + "\r\n");
            start = System.currentTimeMillis();
            temploop: for (CT = T; CT > 1E-30; CT *= c_factor) {
                for (int it = 0; it < iteration; it++) {
                    counter++;
//                    System.out.println("Iteration " + counter);
                    SolutionBean neighb;
                    if (hc) {
                    	neighb=createNeighb_withHC(CBS);
                    }
                    else {
                    	neighb = createNeighb(CBS);
                    }
                    NS = cloner.deepClone(neighb);
//                    if (NS==null) {
//                    	curbest = cloner.deepClone(CBS);
//                        cbcost = CBS.getOF();
//                    	break temploop;                    	
//                    }
                    if (CT == T) {
                        stop_first = System.currentTimeMillis();
                    }
//                    if (counter%20 == 0) {
                        bw2.write(counter + "\t" + df.format(NS.getOF(NS.getX(), NS.getY())) + "\r\n");
//                    }
                    if (NS.getOF(NS.getX(), NS.getY()) < bcost) {
                        stop_best = System.currentTimeMillis();
                        pointer = counter;
                        if ((cbcost - NS.getOF(NS.getX(), NS.getY())) / cbcost > tol) {
                            countToStop = 0;
                            curbest = cloner.deepClone(NS);
                            cbcost = NS.getOF(NS.getX(), NS.getY());
                        } else {
                            ++countToStop;
//                            System.out.println("countToStop: " + countToStop);
                        }
                        best = cloner.deepClone(NS);
                        bcost = best.getOF(best.getX(), best.getY());
                    } else {
                        ++countToStop;
//                        System.out.println("countToStop: " + countToStop);
                    }

                    Dcost = NS.getOF(NS.getX(), NS.getY()) - CBS.getOF(CBS.getX(), CBS.getY());
                    if (Dcost < 0) {
                        CBS = cloner.deepClone(NS);                        
                    } else {
                        d = Global.rand.nextDouble();
                        if ((Math.exp(-Dcost / CT)) > d) {
                            CBS = cloner.deepClone(NS);
                        }
                    }

                }
                if (counter > max_it) {
                    System.out.println("Reached " + max_it + " iterations.");
                    break temploop;
                }
                if (countToStop > tol_it) {
                    System.out.println("No better solution found for "+tol_it+" iterations. SA is now stopping.");
                    break temploop;
                }
            }
            bw2.write(pointer + "\t" + df.format(bcost));
			bw2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stop = System.currentTimeMillis();
        System.out.println("End of Simulated Annealing");
        System.out.println("Duration: " + (stop - start) + " ms.");
        System.out.println("Temperature: " + CT);
        System.out.println("Iterations: " + counter);
        System.out.println("Best cost of Simulated Annealing: " + best.getOF(best.getX(), best.getY()));
        System.out.println("Best solution found at place: " + pointer + "/" + counter);
        System.out.println("Best solution found in: " + (stop_best - start) + " ms.");
        return best;

    }

    public SolutionBean createNeighb_withHC(SolutionBean sol) {
    	Cloner cloner = new Cloner();
    	SolutionBean cs = cloner.deepClone(sol);
		SolutionBean neighby = cs.createNeighbYMatrix();
		SolutionBean csy = cloner.deepClone(neighby);
		SolutionBean neighbx = csy.createNeighbXMatrix_withHC();
		SolutionBean csx = cloner.deepClone(neighbx);
		return csx;
		
	}
        
    
	public SolutionBean createNeighb(SolutionBean sol) {
		Cloner cloner = new Cloner();
		SolutionBean cs = cloner.deepClone(sol);
		if (sol.getDb().getNumOfCells() > 1) {
			SolutionBean neighby = cs.createNeighbYMatrix();
			SolutionBean csy = cloner.deepClone(neighby);
			SolutionBean neighbx = csy.createNeighbXMatrix();
			SolutionBean csx = cloner.deepClone(neighbx);
			csx.checkSC(csx.getX(), csx.getY());
			return csx;
		} else {
			SolutionBean neighbx = cs.createNeighbXMatrix();
			SolutionBean csx = cloner.deepClone(neighbx);
			csx.checkSC(csx.getX(), csx.getY());
			
			return csx;
		}

	}

    
    public double getBcost() {
        return bcost;
    }

    public long getBestSolDuration() {
        return stop_best - start;
    }

    public long getFirstSolDuration() {
        return stop_first - start;
    }

	public SolutionBean getBestSolution() {
		return best;
	}
    
    
}
