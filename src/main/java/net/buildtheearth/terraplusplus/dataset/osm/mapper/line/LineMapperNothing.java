package net.buildtheearth.terraplusplus.dataset.osm.mapper.line;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiLineString;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.AbstractMapperNothing;

/**
 * A {@link LineMapper} which emits nothing.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class LineMapperNothing extends AbstractMapperNothing<MultiLineString, LineMapper> implements LineMapper {
}
