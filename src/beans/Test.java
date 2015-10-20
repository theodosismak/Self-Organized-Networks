package beans;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import beans.inputPanel;
import jenes.GenerationEventListener;
import jenes.GeneticAlgorithm;
import jenes.chromosome.BooleanChromosome;
import jenes.population.Fitness;
import jenes.population.Individual;
import jenes.population.Population;
import jenes.stage.AbstractStage;
import jenes.stage.operator.common.OnePointCrossover;
import jenes.stage.operator.common.SimpleMutator;
import jenes.stage.operator.common.TournamentSelector;


public class Test extends JFrame{

	private static final long serialVersionUID = 821813319096992188L;
	
	
    //Graphics
    XYSeries series1 = new XYSeries("Average Score");
    XYSeries series2 = new XYSeries("Best Score");
    JProgressBar bar = new JProgressBar();
    inputPanel band;
    inputPanel users;
    inputPanel cells;
    inputPanel population;
    inputPanel generation;
    inputPanel se;
    inputPanel selection;
    inputPanelDouble crossover;
    inputPanelDouble mutation;
    inputPanel elite;
    JButton button;
    JPanel buttonPanel;
    JLabel title;
    JLabel Label;
	JLabel Error;
    JTextField Field;
    JCheckBox seed;
    JFrame frame;
	
    //System
    boolean flag=false;
    boolean hang=false;
    int val3; 
    double val4;
    double val5;
    int val6;
    int valb;
    int valu;
    int valc;
    private int POPULATION_SIZE;
    private int GENERATION_LIMIT;
    long start;
    private SolutionBean sb;
    private DataBean db;
    Individual<BooleanChromosome> bestIndividual = new Individual<BooleanChromosome>();
    double bestGenerationScore=0;
    //Threads
    Thread th;
	
    //Constructor
    public Test(){
    	File file = new File("results");
        if(!file.exists()){
        	file.mkdir();
        }
		bar.setStringPainted(true);
       // (ETA) Estimate Time of Arrivals
        bar.setString("ETA :"+0+ "Seconds");
        bar.setValue(0);
        XYSeriesCollection col = new XYSeriesCollection();
        
        col.addSeries(series1);
        col.addSeries(series2);
        JFreeChart chart = ChartFactory.createXYLineChart(
        		"Line Chart Demo",
        		"Generations",
        		"Fitness Value",
        		col);
        final ChartPanel chartPanel = new ChartPanel(chart);
        
    	frame = new JFrame("Algorithm Progress");    	
    	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(0,1));
		frame.setSize(450, 400);
		frame.setLocationRelativeTo(null);
		
		title = new JLabel("NETWORK CHARACTERISTICS");
		frame.add(title);
		band = new inputPanel("Bandwidth");
		band.setDefault("5");
		frame.add(band);
		users = new inputPanel("Number of Users");
		users.setDefault("5");
		frame.add(users);
		cells = new inputPanel("Number of Small Cells");
		cells.setDefault("3");
		frame.add(cells);
		
		title = new JLabel("ALGORITHM CHARACTERISTICS");
		frame.add(title);	
		population = new inputPanel("Population Size");
		population.setDefault("20");
	    frame.add(population);
	    generation = new inputPanel("Generation Limit");
	    generation.setDefault("20");
	    frame.add(generation);
	    seed = new JCheckBox("Seed");
	    seed.setSelected(true);
	    frame.add(seed);
	    selection = new inputPanel("Number of Selectors Chromosomes");
	    selection.setDefault("3");
	    frame.add(selection);
	    crossover = new inputPanelDouble("Crossover Probability");
	    crossover.setDefault("0.5");
	    frame.add(crossover);
	    mutation = new inputPanelDouble("Mutation Probability");
	    mutation.setDefault("0.02");
	    frame.add(mutation);
	    elite = new inputPanel("Elite Chromosomes");
	    elite.setDefault("1");
	    frame.add(elite);
	    
	    
	    JButton button = new JButton("Run Genetic Algorithm");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				db = new DataBean();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				valb = band.getValue();
				db.setB(valb);
				valu = users.getValue();
				db.setNumOfUsers(valu);
				valc = cells.getValue();
				db.setNumOfCells(valc);
				db.initBean();
				int val1 = population.getValue();
				POPULATION_SIZE=val1;
				int val2 = generation.getValue();
				GENERATION_LIMIT=val2;
				if (seed.isSelected()){
					flag=seed.isSelected();
				}
				val3 = selection.getValue();
				val4 = crossover.getValue();
				val5 = mutation.getValue();
				val6 = elite.getValue();
				frame.dispose();
				th=new Thread(){
					public void run(){
						runGenetic();
					}
				};
				th.start();
				
				JFrame frame2 = new JFrame("Algorithm Results");
				frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame2.setLayout(new BorderLayout());
				frame2.setSize(500, 500);
				frame2.setLocationRelativeTo(null);
				frame2.add(chartPanel,BorderLayout.CENTER);
				frame2.add(bar,BorderLayout.SOUTH);
				frame2.addKeyListener(new KeyAdapter(){
					public void keyPressed(KeyEvent ev){
						
					}
				});
				frame2.setVisible(true);
				frame2.requestFocus();
			}		
		}); 
		frame.add(button);
		frame.setVisible(true);
		
	} // end of test
   	
    	
		public void runGenetic(){
     /* System.out.println("SONs with ON capabilities");
			
		System.out.println("Hard Constraints: "+hc);
	
		System.out.println("---Constraints---");
		sb.printConstraints(sb.X, sb.Y);  */
		bestIndividual.setScore(0);
        Population<BooleanChromosome> pop = new Population<BooleanChromosome>();
        //pop.clear();
        //System.out.println("Entering");
       
        boolean hc = true;
        //System.out.println("Hard Constraints: "+hc);
        
        //Create initial population
		
        //CHROMOSOME_LENGTH=db.getNumOfUsers()*db.getNumOfCells()*db.getNumOfRBs() + db.getNumOfCells()*db.getNumOfPLs();
        for(int i=0;i<POPULATION_SIZE;i++){	
    		sb = new SolutionBean(db);
    		sb.createInitYMatrix();
    		//We check whether we have Hard Constraint or Soft Constraint in order to call the table X 
			//that is created in SolutionBean according to the kind of constraint that we defineand
    		if (hc) {
    			sb.createInitXMatrix_withHC();
    		} else {
    			sb.createInitXMatrix();
    		}
    		//System.out.println("---Constraints---");
    		//sb.printConstraints(sb.X, sb.Y);
    		BooleanChromosome bc = sb.makeChromosome(sb.X, sb.Y);
    		//System.out.println("Score "+sb.getOF(bc)+" Chromosome #"+(i+1)+" "+bc);
        	pop.add(new Individual<BooleanChromosome>(bc));
        	sb.printResults(sb.getX(), sb.getY(),"./results/initial-chromosome"+(i+1)+".txt");
        }
        
        Fitness<BooleanChromosome> fit = new Fitness<BooleanChromosome>(true) {

            public void evaluate(Individual<BooleanChromosome> individual) {
                BooleanChromosome chrom = individual.getChromosome();
                SolutionBean bn = new SolutionBean(db);
                bn.makeX(chrom);
                bn.makeY(chrom);
                double count = bn.getOF(chrom);
                individual.setScore(count);
                individual.setLegal(bn.checkSC(bn.getX(), bn.getY())&&(bn.checkHC(bn.getX())));
            }
            
        };
        
        GeneticAlgorithm<BooleanChromosome> ga = new GeneticAlgorithm<BooleanChromosome>
                (fit, pop, GENERATION_LIMIT);
        ga.setRandomization(false);
        start=System.currentTimeMillis();
        ga.addGenerationEventListener(new GenerationEventListener<BooleanChromosome>(){
			
			@Override
			public void onGeneration(GeneticAlgorithm<BooleanChromosome> arg0, long arg1) {
				
				double[] arr = arg0.getLastPopulation().getScores(0);
				double average=0.0;
				long stop = System.currentTimeMillis();
				double double_stop=(double)stop;
				double double_start=(double)start;
				double diff = (double_stop-double_start)/1000;
				int ss=(int)((arg0.getGenerationLimit()-arg0.getGeneration())*diff);
				start = stop;
				bar.setString("ETA: "+ss+" seconds");
				for(Double d:arr){
					average+=d;
					
				}
				average = average/arr.length;
				series1.add(arg0.getGeneration(), average);
				
				//System.out.println("The average score of the generation: "+average);
				//System.out.println();
				bar.setValue(((arg0.getGeneration()+1)*100/GENERATION_LIMIT));
				if (arg0.getGeneration()==GENERATION_LIMIT-1){
					bar.setString("finished");
				}
			
				Population<BooleanChromosome>temp = arg0.getLastPopulation();
				
				for(Individual<BooleanChromosome>abc:temp.getIndividuals()){
					//System.out.println(abc);
					
					double t=abc.getScore();
					
					if (t>bestIndividual.getScore()) {	
						bestIndividual = abc.clone();
						//System.out.println("Best score updated: "+t);
					}
					//sb.printConstraints(sb.makeX(abc.getChromosome()),sb.makeY(abc.getChromosome()));
				}
				
				series2.add(arg0.getGeneration(), bestIndividual.getScore());
			}
        });
        
        AbstractStage<BooleanChromosome> selection = new TournamentSelector<BooleanChromosome>(val3);
        AbstractStage<BooleanChromosome> crossover = new OnePointCrossover<BooleanChromosome>(val4);
        AbstractStage<BooleanChromosome> mutation = new SimpleMutator<BooleanChromosome>(val5);
        
        if (flag=true){
        	ga.setRandomSeed(5);
        }
        
        ga.addStage(selection);
        ga.addStage(crossover);
        ga.addStage(mutation);
        
        ga.setElitism(val6);
        
        ga.evolve();
        
        GeneticAlgorithm.Statistics algostats = ga.getStatistics();
        
        System.out.println("Objective: " + (fit.getBiggerIsBetter()[0] ? "Max! (All true)" : "Min! (None true)"));
        
        System.out.print("Solution: ");
        System.out.println(bestIndividual);
        System.out.format("found in %d ms.\n", algostats.getExecutionTime());
        
		sb.printResults(sb.getX(), sb.getY(), "./results/final-results.txt");
		sb.printXY();
			
    } // runGenetic
		
    
	public static void main(String[] args) {
		
		new Test();
		
	}

}

