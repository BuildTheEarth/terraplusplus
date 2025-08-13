package net.buildtheearth.terraminusminus.substitutes;

import lombok.Getter;

@Getter
public class Identifier implements Comparable<Identifier> {

	private final String namespace;
	private final String name;

    public Identifier(String path) {
        if (path == null || path.length() <= 0) throw new IllegalArgumentException("Name cannot be null or empty");
        int found = path.indexOf(':');
        if (found < 0) {
            this.namespace = "minecraft";
            this.name = path;
        } else if (found == 0 || found + 1 >= path.length() || path.indexOf(':', found + 1) > 0){
            throw new IllegalArgumentException("Invalid name: " + path);
        } else {
            this.namespace = path.substring(0, found);
            this.name = path.substring(found + 1);
        }
    }

	public Identifier(String namespace, String name) {
        if (namespace == null || namespace.length() <= 0) throw new IllegalArgumentException("Invalid namespace:" + namespace);
        if (name == null || name.length() <= 0) throw new IllegalArgumentException("Invalid name: " + name);
        this.namespace = namespace;
        this.name = name;
	}

    @Override
	public String toString() {
		return this.namespace + ':' + this.name;
	}

    @Override
	public boolean equals(Object other) {
		if(this == other) return true;
		if(!(other instanceof Identifier)) return false;
        Identifier otherName = (Identifier) other;
        return this.namespace.equals(otherName.namespace) && this.name.equals(otherName.name);
	}

    @Override
	public int hashCode() {
		return 31 * this.namespace.hashCode() + this.name.hashCode();
	}

    @Override
	public int compareTo(Identifier other) {
		int spaces = this.namespace.compareTo(other.namespace);
        return spaces != 0 ? spaces: this.name.compareTo(other.name);
	}

}