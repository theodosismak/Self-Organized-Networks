package beans;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class inputPanelDouble extends JPanel{
	
	//private static final long serialVersionUID = 821813319096992188L;
	
	JLabel Label;
	JLabel Error;
    JTextField Field;
    //Test ref;
	public inputPanelDouble(String text) {  //,Test test) {
		//ref=test;
    	setLayout (new GridLayout(0,2));
    	Label = new JLabel(text);
    	add(Label);
    	Field = new JTextField();
    	add (Field);    	
    }
	public double getValue(){
		double value;
		
		try{
			value = Double.parseDouble(Field.getText());
		}catch(Exception e){
			e.printStackTrace();
			value=-1;
		}
		return value;
		
	}
	public void setDefault(String input){
		Field.setText(input);
	}
}