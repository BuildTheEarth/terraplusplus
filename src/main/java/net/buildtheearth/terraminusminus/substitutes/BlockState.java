package net.buildtheearth.terraminusminus.substitutes;

import java.util.Map;

public interface BlockState {
	
    NamespacedName getBlock();

    BlockPropertyValue getProperty(String property);

    BlockState withProperty(String property, String value);

    BlockState withProperty(String property, int value);

    BlockState withProperty(String property, boolean value);

    Map<String, BlockPropertyValue> getProperties();

    default int getPreviewColor() {
        return 0xFFFFFFFF;
    }
    
}