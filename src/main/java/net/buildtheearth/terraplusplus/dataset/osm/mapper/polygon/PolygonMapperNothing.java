package net.buildtheearth.terraplusplus.dataset.osm.mapper.polygon;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.MultiPolygon;
import net.buildtheearth.terraplusplus.dataset.osm.mapper.AbstractMapperNothing;

/**
 * A {@link PolygonMapper} which emits nothing.
 *
 * @author DaPorkchop_
 */
@JsonDeserialize
public final class PolygonMapperNothing extends AbstractMapperNothing<MultiPolygon, PolygonMapper> implements PolygonMapper {
}
