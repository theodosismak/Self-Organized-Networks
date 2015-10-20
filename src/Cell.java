package beans;

import java.util.ArrayList;



public class Cell implements Cloneable {
	
	int id, type;
	double x, y, Pmax;
	ArrayList<Integer> userList;
	ArrayList<Double> plList;

	public Cell() {
		id = -1;
		type = -1;
		x = -1.0;
		y = -1.0;
		userList = new ArrayList<Integer>();
		plList = new ArrayList<Double>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public ArrayList<Integer> getUserList() {
		return userList;
	}
	
	public void addUser(int u) {
		if (!userList.contains(u)) {
			userList.add(u);
		}
		else {
			System.err.println("AddUser: Cell "+id+"already has User "+u);
		}
	}
	
	public void removeUser(int u) {
		if (userList.contains(u)) {
			userList.remove(u);
		}
		else {
			System.err.println("RemoveUser: Cell "+id+"does not serve User "+u);
		}
	}
	
	public void addPL(double k) {
		if (!plList.contains(k)) {
			plList.add(k);
		}
		else {
			System.err.println("AddPL: Cell "+id+" already has PL "+k);
		}
	}
	
	public void removePL(double k) {
		if (plList.contains(k)) {
			plList.remove(k);
		}
		else {
			System.err.println("RemovePL: Cell "+id+" does not have PL "+k);
		}
	}

	public double getPmax() {
		return Pmax;
	}

	public void setPmax(double pmax) {
		Pmax = pmax;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Cell clone = new Cell();
		clone.setId(id);
		clone.setType(type);
		clone.setX(x);
		clone.setY(y);
		clone.setPmax(Pmax);
		return clone;
	}

	@Override
	public String toString() {
		return "Cell ID: "+id+" Coord: ("+x+", "+y+") Type: "+type+" Pmax: "+Pmax;
	}
	
	
	
	

}
