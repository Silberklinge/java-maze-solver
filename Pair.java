package main;

import java.util.Objects;

public class Pair<K, V> {
	public K first;
	public V second;
	
	public Pair(K first, V second) {
		this.first = first;
		this.second = second;
	}
	
	public static <A, B> Pair<A, B> of(A key, B value) {
		return new Pair<A, B>(key, value);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null)
			return false;
		if(!(other instanceof Pair<?, ?>))
			return false;
		Pair<?,?> pair = (Pair<?,?>) other;
		return first.equals(pair.first) && second.equals(pair.second);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}
