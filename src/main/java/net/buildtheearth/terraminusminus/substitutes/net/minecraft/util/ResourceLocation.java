package net.buildtheearth.terraminusminus.substitutes.net.minecraft.util;
import java.lang.reflect.Type;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import lombok.Getter;

@Getter
public class ResourceLocation implements Comparable<ResourceLocation> {

	protected final String namespace;
	protected final String path;

	protected ResourceLocation(int unused, String... resourceName) {
		this.namespace = StringUtils.isEmpty(resourceName[0]) ? "minecraft" : resourceName[0].toLowerCase(Locale.ROOT);
		this.path = resourceName[1].toLowerCase(Locale.ROOT);
		Validate.notNull(this.path);
	}

	public ResourceLocation(String resourceName) {
		this(0, splitObjectName(resourceName));
	}

	public ResourceLocation(String namespaceIn, String resource) {
		this(0, namespaceIn, resource);
	}

	public static String[] splitObjectName(String toSplit) {
		String[] strs = new String[] {"minecraft", toSplit};
		int i = toSplit.indexOf(':');

		if (i >= 0) {
			strs[1] = toSplit.substring(i + 1, toSplit.length());

			if (i > 1) {
				strs[0] = toSplit.substring(0, i);
			}
		}

		return strs;
	}

	public String toString() {
		return this.namespace + ':' + this.path;
	}

	public boolean equals(Object other) {
		if(this == other) return true;
		if(!(other instanceof ResourceLocation)) return false;

		ResourceLocation resourcelocation = (ResourceLocation)other;
		return this.namespace.equals(resourcelocation.namespace) && this.path.equals(resourcelocation.path);
	}

	public int hashCode() {
		return 31 * this.namespace.hashCode() + this.path.hashCode();
	}

	public int compareTo(ResourceLocation other) {
		int i = this.namespace.compareTo(other.namespace);

		if (i == 0) i = this.path.compareTo(other.path);

		return i;
	}

	public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
		
		public ResourceLocation deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			String str;
	        if (json.isJsonPrimitive()) {
	            str =  json.getAsString();
	        } else {
	            throw new JsonSyntaxException("Expected location to be a string.");
	        }
			return new ResourceLocation(str);
		}

		public JsonElement serialize(ResourceLocation location, Type type, JsonSerializationContext context) {
			return new JsonPrimitive(location.toString());
		}
		
	}
}