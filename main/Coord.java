package main;

import java.util.Objects;

public class Coord {
	public int x;
	public int y;
	
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Coord add(int delX, int delY) {
		return new Coord(x+delX, y+delY);
	}
	
	public Coord add(Direction d) {
		return add(d.delX(), d.delY());
	}
	
	public Coord copy() {
		return new Coord(x, y);
	}
	
	public boolean isZero() {
		return x == 0 && y == 0;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Coord) {
			Coord coord = (Coord) other;
			return x == coord.x && y == coord.y;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}