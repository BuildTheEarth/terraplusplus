package net.buildtheearth.terraplusplus.projection.sis;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import org.apache.sis.io.wkt.Convention;
import org.apache.sis.io.wkt.KeywordCase;
import org.apache.sis.io.wkt.KeywordStyle;
import org.apache.sis.io.wkt.Symbols;
import org.apache.sis.io.wkt.WKTFormat;

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
            WKTFormat format = new WKTFormat(Locale.ROOT, TimeZone.getDefault());
            format.setKeywordCase(KeywordCase.UPPER_CASE);
            format.setKeywordStyle(KeywordStyle.SHORT);
            format.setConvention(Convention.WKT2);
            format.setSymbols(Symbols.SQUARE_BRACKETS);
            format.setIndentation(WKTFormat.SINGLE_LINE);
            WKT2_2015.format = Cached.threadLocal(format::clone, ReferenceStrength.SOFT);
        }
    }

    private Cached<WKTFormat> format;

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
