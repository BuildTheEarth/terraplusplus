package net.buildtheearth.terraplusplus.projection.wkt;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.With;

/**
 * @author DaPorkchop_
 * @see <a href="https://docs.opengeospatial.org/is/12-063r5/12-063r5.html#143">WKT Specification §C.2: Backward compatibility of CRS common attributes</a>
 */
@Builder
@Data
@With
public final class WKTID {
    @NonNull
    private final String authorityName;

    @NonNull
    private final Object authorityUniqueIdentifier;

    @Override
    public String toString() {
        return "ID[\"" + this.authorityName.replace("\"", "\"\"") + "\", "
               + (this.authorityUniqueIdentifier instanceof Number ? this.authorityUniqueIdentifier.toString() : '"' + this.authorityUniqueIdentifier.toString().replace("\"", "\"\"") + '"')
               + ']';
    }
}
