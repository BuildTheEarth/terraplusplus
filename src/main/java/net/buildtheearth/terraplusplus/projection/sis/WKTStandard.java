package net.buildtheearth.terraplusplus.projection.sis;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.buildtheearth.terraplusplus.util.compat.sis.SISHelper;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import org.apache.sis.internal.referencing.ReferencingFactoryContainer;
import org.apache.sis.io.wkt.Convention;
import org.apache.sis.io.wkt.KeywordCase;
import org.apache.sis.io.wkt.KeywordStyle;
import org.apache.sis.io.wkt.Symbols;
import org.apache.sis.io.wkt.WKTFormat;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransformFactory;

import java.text.ParseException;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author DaPorkchop_
 */
public enum WKTStandard {
    /**
     * WKT2:2015 (ISO 19162:2015)
     */
    WKT2_2015,
    ;

    static {
        {
            WKTFormat baseFormat = new WKTFormat(Locale.ROOT, TimeZone.getDefault());
            baseFormat.setKeywordCase(KeywordCase.UPPER_CASE);
            baseFormat.setKeywordStyle(KeywordStyle.SHORT);
            baseFormat.setConvention(Convention.WKT2);
            baseFormat.setSymbols(Symbols.SQUARE_BRACKETS);
            baseFormat.setIndentation(WKTFormat.SINGLE_LINE);
            WKT2_2015.setFormat(baseFormat);
        }
    }

    private Cached<WKTFormat> format;

    private void setFormat(@NonNull WKTFormat baseFormat) {
        this.format = Cached.threadLocal(() -> {
            ReferencingFactoryContainer factories = SISHelper.factories();

            //clone the base WKTFormat instance, and configure it to use the thread-local factory instances from SISHelper.factories()
            WKTFormat format = baseFormat.clone();
            format.setFactory(CRSFactory.class, factories.getCRSFactory());
            format.setFactory(CSFactory.class, factories.getCSFactory());
            format.setFactory(DatumFactory.class, factories.getDatumFactory());
            format.setFactory(MathTransformFactory.class, factories.getMathTransformFactory());
            format.setFactory(CoordinateOperationFactory.class, factories.getCoordinateOperationFactory());
            return format;
        }, ReferenceStrength.SOFT);
    }

    public Object parse(@NonNull String wkt) throws ParseException {
        return this.format.get().parseObject(wkt);
    }

    @SneakyThrows(ParseException.class)
    public Object parseUnchecked(@NonNull String wkt) {
        return this.parse(wkt);
    }

    public String format(@NonNull Object object) {
        return this.format.get().format(object);
    }
}
