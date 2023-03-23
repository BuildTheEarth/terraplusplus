package net.buildtheearth.terraplusplus.projection.wkt.datum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTWriter;

import java.io.IOException;
import java.util.List;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Getter
public abstract class WKTDatumEnsemble extends WKTDatum {
    @NonNull
    private final List<Member> members;

    @NonNull
    private final Number accuracy;

    /**
     * @author DaPorkchop_
     */
    @Jacksonized
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static final class Member extends WKTObject.WithNameAndID {
        @Override
        public void write(@NonNull WKTWriter writer) throws IOException {
            writer.beginObject("MEMBER")
                    .writeQuotedLatinString(this.name())
                    .writeOptionalObject(this.id())
                    .endObject();
        }
    }
}
