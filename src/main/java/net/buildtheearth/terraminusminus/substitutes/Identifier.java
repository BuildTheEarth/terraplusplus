package net.buildtheearth.terraminusminus.substitutes;

import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import net.buildtheearth.terraminusminus.substitutes.exceptions.SubstituteParseException;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.*;


/**
 * A unique identifier for a namespaced game resource.
 * Also known as a <i>resource location</i> or <i>namespaced key</i>.
 * <br><br>
 * An identifier is made of two parts:
 * <ol>
 *     <li>a <b><i>namespace</i></b>, identifying whatever owns the object (e.g. "minecraft", "myplugin", "custommod", ...)</li>
 *     <li>a <b><i>path</i></b>, identifying the object itself (e.g. "my_block_name", "textures/my/custom/texture.png", "my_biome", ...)</li>
 * </ol>
 * A valid namespace matches the following regular expression: <code>[a-z0-9_.-]+</code>.
 * A valid path matches the following regular expression: <code>[a-z0-9_./-]+</code>.
 * <br><br>
 * Identifier as represented as text in the following format: <code>namespace:path</code>.
 * <br><br>
 * This class is immutable and thread safe.
 * <br>
 * This implementation of the resource location concept is more restrictive than vanilla Minecraft's
 *           with the values it accepts for the namespace and path.
 *
 * @see <a href="https://minecraft.wiki/w/Resource_location">Minecraft wiki page on resource locations</a>
 *
 * @author Smyler
 */
@Getter @With
public final class Identifier implements Comparable<Identifier> {

    /**
     * The namespace of this identifier.
     * Is never <code>null</code> and always matches <code>[a-z0-9_.-]+</code>.
     */
	public final @NonNull String namespace;

    /**
     * The path of this identifier.
     * Is never <code>null</code> and always matches <code>[a-z0-9_./-]+</code>.
     */
	public final @NonNull String path;

    /**
     * Creates a new {@link Identifier} with the given namespace and path.
     *
     * @param namespace the namespace for the new {@link Identifier}
     *                  (must match <code>[a-z0-9_.-]+</code>)
     * @param path      the path for the new {@link Identifier}
     *                  (must match <code>[a-z0-9_./-]+</code>)
     *
     * @throws NullPointerException if either the namespace or path are <code>null</code>
     * @throws IllegalArgumentException if either the namespace or path are not valid
     */
	public Identifier(@NonNull String namespace, @NonNull String path) {
        this.namespace = checkNamespaceString(namespace);
        this.path = checkPathString(path);
	}

    /**
     * Creates a new {@link Identifier} with the given path in the namespace <code>minecraft</code>.
     *
     * @param path the path for the new {@link Identifier}
     *
     * @throws NullPointerException if path is <code>null</code>
     * @throws IllegalArgumentException if the path is not valid
     */
    public Identifier(@NonNull String path) {
        this.namespace = "minecraft";
        this.path = checkPathString(path);
    }

    public static @NotNull Identifier parse(@NonNull String identifier) {
        if (identifier.isEmpty()) {
            throw new SubstituteParseException(identifier, Identifier.class, "identifier cannot be empty");
        }
        final int separatorIndex = identifier.indexOf(':');
        if (separatorIndex < 0) {
            return new Identifier(identifier);
        } else {
            final String namespace = identifier.substring(0, separatorIndex);
            final String name = identifier.substring(separatorIndex + 1);
            return new Identifier(namespace, name);
        }
    }

    private static String checkNamespaceString(@NonNull String namespace) {
        if (namespace.isEmpty()) {
            throw new SubstituteParseException(namespace, Identifier.class, "identifier namespace cannot be empty");
        }
        for (int i = 0; i < namespace.length(); i++) {
            char ch = namespace.charAt(i);
            final boolean isValidChar =
                    ch == '_'
                    || ch == '-'
                    || ch == '.'
                    || (ch >= '0' && ch <= '9')
                    || (ch >= 'a' && ch <= 'z');
            if (!isValidChar) {
                throw new SubstituteParseException(namespace, Identifier.class, "invalid identifier namespace character '" + ch + "' at index " + i + " in string \"" + namespace + "\"");
            }
        }
        return namespace;
    }

    private static String checkPathString(@NonNull String path) {
        if (path.isEmpty()) {
            throw new SubstituteParseException(path, Identifier.class, "identifier path cannot be empty");
        }
        for (int i = 0; i < path.length(); i++) {
            char ch = path.charAt(i);
            final boolean isValidChar =
                    ch == '_'
                    || ch == '-'
                    || ch == '.'
                    || ch == '/'
                    || (ch >= '0' && ch <= '9')
                    || (ch >= 'a' && ch <= 'z');
            if (!isValidChar) {
                throw new SubstituteParseException(path, Identifier.class, "invalid identifier path character '" + ch + "' at index " + i + " in string \"" + path + "\"");
            }
        }
        return path;
    }

    @Override
	public String toString() {
		return this.namespace + ':' + this.path;
	}

    @Override
	public boolean equals(Object other) {
		if(this == other) return true;
		if(!(other instanceof Identifier)) return false;
        Identifier otherName = (Identifier) other;
        return this.namespace.equals(otherName.namespace) && this.path.equals(otherName.path);
	}

    @Override
	public int hashCode() {
        return hash(this.namespace, this.path);
	}

    @Override
	public int compareTo(Identifier other) {
		int spaces = this.namespace.compareTo(other.namespace);
        return spaces != 0 ? spaces: this.path.compareTo(other.path);
	}

}