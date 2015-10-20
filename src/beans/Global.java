package beans;

import java.util.Random;

import com.rits.cloning.Cloner;

public abstract class Global {
	
	public static long seed = 0;
	public static Random rand = new Random(seed);
	Cloner cloner = new Cloner(); 	

}
