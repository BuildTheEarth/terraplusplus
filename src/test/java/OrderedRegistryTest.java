import io.github.terra121.util.OrderedRegistry;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
public class OrderedRegistryTest {
    @Test
    public void test() {
        Object dummy = new Object();
        OrderedRegistry<Object> registry = new OrderedRegistry<>();

        checkState(registry.entryStream().count() == 0L);

        registry.addFirst("c", dummy)
                .addFirst("a", dummy)
                .addLast("m", dummy);
        checkState(Arrays.asList("a", "c", "m").equals(registry.entryStream().map(Map.Entry::getKey).collect(Collectors.toList())), registry);

        registry.addBefore("a", "", dummy);
        registry.addBefore("m", "h", dummy);
        checkState(Arrays.asList("", "a", "c", "h", "m").equals(registry.entryStream().map(Map.Entry::getKey).collect(Collectors.toList())), registry);

        registry.addAfter("a", "b", dummy);
        registry.addAfter("m", "n", dummy);
        checkState(Arrays.asList("", "a", "b", "c", "h", "m", "n").equals(registry.entryStream().map(Map.Entry::getKey).collect(Collectors.toList())), registry);

        registry.set("h", dummy);
        checkState(Arrays.asList("", "a", "b", "c", "h", "m", "n").equals(registry.entryStream().map(Map.Entry::getKey).collect(Collectors.toList())), registry);

        registry.set("h", "i", dummy);
        checkState(Arrays.asList("", "a", "b", "c", "i", "m", "n").equals(registry.entryStream().map(Map.Entry::getKey).collect(Collectors.toList())), registry);

        registry.remove("c");
        checkState(Arrays.asList("", "a", "b", "i", "m", "n").equals(registry.entryStream().map(Map.Entry::getKey).collect(Collectors.toList())), registry);
    }
}
