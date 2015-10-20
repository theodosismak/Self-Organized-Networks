package beans;

import java.util.ArrayList;

public class User {
	
	int id, userClass;
	double x, y, demand;
	ArrayList<Integer> rbList;
	
	public User() {
		id = -1;
		userClass = -1;
		x = -1.0;
		y = -1.0;
		demand = -1.0;
		rbList = new ArrayList<Integer>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserClass() {
		return userClass;
	}

	public void setUserClass(int userClass) {
		this.userClass = userClass;
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

	public double getDemand() {
		return demand;
	}

	public void setDemand(double demand) {
		this.demand = demand;
	}

	public ArrayList<Integer> getRbList() {
		return rbList;
	}

	public void addRB(int r) {
		if (!rbList.contains(r)) {
			rbList.add(r);			
		}
		else {
			System.err.println("AddRB: User "+id+" already has RB "+r);
		}
	}
	
	public void removeRB(int r) {
		if (rbList.contains(r)) {
			rbList.remove(r);			
		}
		else {
			System.err.println("RemoveRB: RB "+r+" was not found in User "+id);
		}
	}
	
	public boolean hasRB(int r) {
		return rbList.contains(r);
	}

	@Override
	public String toString() {
		return "User ID: "+id+" Coord: ("+x+", "+y+") UC: "+userClass+" Demand: "+demand;
	}
	
	public String getIlogStringX() {
		String sx = "Opl.item(Users, "+id+").x = "+x+";";
		return sx;
	}
	
	public String getIlogStringY() {
		String sy = "Opl.item(Users, "+id+").y = "+y+";";
		return sy;
	}
	
	

}
