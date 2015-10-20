package beans;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class inputPanel extends JPanel{
   
	//private static final long serialVersionUID = 106315850400227275L;
	
	JLabel Label;
	JLabel Error;
    JTextField Field;
	public inputPanel(String text) {
    	setLayout (new GridLayout(0,2));
    	Label = new JLabel(text);
    	add(Label);
    	Field = new JTextField();
    	add (Field);
    }
	public int getValue(){
		int value;
		try{
			value = Integer.parseInt(Field.getText());
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