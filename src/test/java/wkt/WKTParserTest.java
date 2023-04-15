package wkt;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.buildtheearth.terraplusplus.projection.wkt.WKTObject;
import net.buildtheearth.terraplusplus.projection.wkt.WKTStyle;
import net.buildtheearth.terraplusplus.projection.wkt.WKTToTPPConverter;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTCompoundCRS;
import net.buildtheearth.terraplusplus.projection.wkt.crs.WKTGeographicCRS;
import net.buildtheearth.terraplusplus.projection.wkt.cs.WKTCS;
import net.daporkchop.lib.common.function.throwing.EFunction;
import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.unsafe.PUnsafe;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.buildtheearth.terraplusplus.util.TerraConstants.*;
import static org.junit.Assert.*;

/**
 * @author DaPorkchop_
 */
public class WKTParserTest {
    private static final Properties EPSG_WKT1 = new Properties();
    private static final Properties EPSG_PROJJSON = new Properties();

    @BeforeClass
    public static void loadProperties() throws IOException {
        try (InputStream in = new BufferedInputStream(Objects.requireNonNull(WKTParserTest.class.getResourceAsStream("epsg.properties")))) {
            EPSG_WKT1.load(in);
        }

        try (InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get("/media/daporkchop/data/srs/srs-renamed/one_line/PROJJSON.properties")))) {
            EPSG_PROJJSON.load(in);
        }
    }

    @Test
    public void testFormatWKT() {
        EPSG_WKT1.forEach((key, wkt) -> {
            String formatted = WKTStyle.ONE_LINE.format(wkt.toString());
            assertEquals(wkt.toString(), formatted);
        });
    }

    @Test
    public void testParsePROJJSON() {
        AtomicInteger successful = new AtomicInteger();
        AtomicInteger total = new AtomicInteger();
        EPSG_PROJJSON.forEach((rawKey, rawProjjson) -> {
            String key = rawKey.toString();
            String projjson = rawProjjson.toString();

            total.getAndIncrement();
            try {
                WKTObject parsed = JSON_MAPPER.readValue(projjson, WKTObject.AutoDeserialize.class);

                successful.incrementAndGet();
            } catch (JsonProcessingException e) {
                //ignore
                PUnsafe.throwException(new RuntimeException(key, e));
            }
        });
        System.out.printf("parsed %d/%d (%.2f%%)\n", successful.get(), total.get(), (double) successful.get() / total.get() * 100.0d);
    }

    @Test
    public void testConvertToTPP() {
        System.out.println("convert to t++: " + EPSG_PROJJSON.values().stream()
                .map((EFunction<Object, WKTCRS>) projjson -> JSON_MAPPER.readValue(projjson.toString(), WKTCRS.class))
                .filter(WKTGeographicCRS.class::isInstance)
                .mapToInt(crs -> {
                    try {
                        WKTToTPPConverter.convertCRS(crs);
                        return 1;
                    } catch (RuntimeException e) {
                        //ignore
                        //PUnsafe.throwException(e);
                        return 0;
                    }
                })
                .summaryStatistics());
    }

    @Test
    public void findCoordinateSystemDimensions() {
        System.out.println((Object) EPSG_PROJJSON.values().stream()
                .map((EFunction<Object, WKTCRS>) projjson -> JSON_MAPPER.readValue(projjson.toString(), WKTCRS.class))
                .map(crs -> {
                    try {
                        return dimensionCount(crs);
                    } catch (IllegalArgumentException e) {
                        return -1;
                    }
                })
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting())));
    }

    private static int dimensionCount(WKTObject obj) {
        if (obj instanceof WKTCRS) {
            if (obj instanceof WKTCRS.WithCoordinateSystem) {
                return ((WKTCRS.WithCoordinateSystem) obj).coordinateSystem().axes().size();
            } else if (obj instanceof WKTCompoundCRS) {
                return ((WKTCompoundCRS) obj).components().stream().mapToInt(WKTParserTest::dimensionCount).sum();
            }
        }

        throw new IllegalArgumentException(PorkUtil.className(obj));
    }

    @Test
    public void findCoordinateSystemAxisOrders() {
        EPSG_PROJJSON.values().stream()
                .map((EFunction<Object, WKTCRS>) projjson -> JSON_MAPPER.readValue(projjson.toString(), WKTCRS.class))
                .filter(WKTCRS.WithCoordinateSystem.class::isInstance)
                .collect(Collectors.groupingBy(Object::getClass,
                        Collectors.groupingBy(crs -> ((WKTCRS.WithCoordinateSystem) crs).coordinateSystem(),
                                Collectors.counting())))
                .entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().getTypeName()))
                .forEachOrdered(topEntry -> {
                    System.out.println(topEntry.getKey() + ":");
                    topEntry.getValue().entrySet().stream().sorted(Map.Entry.<WKTCS, Long>comparingByValue().reversed())
                            .forEachOrdered(entry -> {
                                WKTCS cs = entry.getKey();
                                System.out.println("    " + entry.getValue() + "x " + cs.type() + ' ' + cs.axes() + " unit=" + cs.unit());
                            });
                    System.out.println();
                });
    }

    @Test
    public void testEllipsoid() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"Ellipsoid\",\"name\": \"WGS 84\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257223563,\"id\": {\"authority\": \"EPSG\",\"code\": 7030}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());
    }

    @Test
    public void testDatum() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"DynamicGeodeticReferenceFrame\",\"name\": \"IGS97\",\"frame_reference_epoch\": 1997,\"ellipsoid\": {\"name\": \"GRS 1980\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257222101},\"scope\": \"Geodesy.\",\"area\": \"World.\",\"bbox\": {\"south_latitude\": -90,\"west_longitude\": -180,\"north_latitude\": 90,\"east_longitude\": 180},\"id\": {\"authority\": \"EPSG\",\"code\": 1244}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());

        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"DatumEnsemble\",\"name\": \"World Geodetic System 1984 ensemble\",\"members\": [{\"name\": \"World Geodetic System 1984 (Transit)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1166}},{\"name\": \"World Geodetic System 1984 (G730)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1152}},{\"name\": \"World Geodetic System 1984 (G873)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1153}},{\"name\": \"World Geodetic System 1984 (G1150)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1154}},{\"name\": \"World Geodetic System 1984 (G1674)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1155}},{\"name\": \"World Geodetic System 1984 (G1762)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1156}},{\"name\": \"World Geodetic System 1984 (G2139)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1309}}],\"ellipsoid\": {\"name\": \"WGS 84\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257223563},\"accuracy\": \"2.0\",\"id\": {\"authority\": \"EPSG\",\"code\": 6326}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());
    }

    @Test
    public void testGeographicCRS() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"GeographicCRS\",\"name\": \"IGS97\",\"datum\": {\"type\": \"DynamicGeodeticReferenceFrame\",\"name\": \"IGS97\",\"frame_reference_epoch\": 1997,\"ellipsoid\": {\"name\": \"GRS 1980\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257222101}},\"coordinate_system\": {\"subtype\": \"ellipsoidal\",\"axis\": [{\"name\": \"Geodetic latitude\",\"abbreviation\": \"Lat\",\"direction\": \"north\",\"unit\": \"degree\"},{\"name\": \"Geodetic longitude\",\"abbreviation\": \"Lon\",\"direction\": \"east\",\"unit\": \"degree\"},{\"name\": \"Ellipsoidal height\",\"abbreviation\": \"h\",\"direction\": \"up\",\"unit\": \"metre\"}]},\"scope\": \"Geodesy.\",\"area\": \"World.\",\"bbox\": {\"south_latitude\": -90,\"west_longitude\": -180,\"north_latitude\": 90,\"east_longitude\": 180},\"id\": {\"authority\": \"EPSG\",\"code\": 9002}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());

        /*System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"GeographicCRS\",\"name\": \"WGS 84\",\"datum_ensemble\": {\"name\": \"World Geodetic System 1984 ensemble\",\"members\": [{\"name\": \"World Geodetic System 1984 (Transit)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1166}},{\"name\": \"World Geodetic System 1984 (G730)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1152}},{\"name\": \"World Geodetic System 1984 (G873)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1153}},{\"name\": \"World Geodetic System 1984 (G1150)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1154}},{\"name\": \"World Geodetic System 1984 (G1674)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1155}},{\"name\": \"World Geodetic System 1984 (G1762)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1156}},{\"name\": \"World Geodetic System 1984 (G2139)\",\"id\": {\"authority\": \"EPSG\",\"code\": 1309}}],\"ellipsoid\": {\"name\": \"WGS 84\",\"semi_major_axis\": 6378137,\"inverse_flattening\": 298.257223563},\"accuracy\": \"2.0\",\"id\": {\"authority\": \"EPSG\",\"code\": 6326}},\"coordinate_system\": {\"subtype\": \"ellipsoidal\",\"axis\": [{\"name\": \"Geodetic latitude\",\"abbreviation\": \"Lat\",\"direction\": \"north\",\"unit\": \"degree\"},{\"name\": \"Geodetic longitude\",\"abbreviation\": \"Lon\",\"direction\": \"east\",\"unit\": \"degree\"},{\"name\": \"Ellipsoidal height\",\"abbreviation\": \"h\",\"direction\": \"up\",\"unit\": \"metre\"}]},\"scope\": \"Geodesy. Navigation and positioning using GPS satellite system.\",\"area\": \"World: Afghanistan, Albania, Algeria, American Samoa, Andorra, Angola, Anguilla, Antarctica, Antigua and Barbuda, Argentina, Armenia, Aruba, Australia, Austria, Azerbaijan, Bahamas, Bahrain, Bangladesh, Barbados, Belgium, Belgium, Belize, Benin, Bermuda, Bhutan, Bolivia, Bonaire, Saint Eustasius and Saba, Bosnia and Herzegovina, Botswana, Bouvet Island, Brazil, British Indian Ocean Territory, British Virgin Islands, Brunei Darussalam, Bulgaria, Burkina Faso, Burundi, Cambodia, Cameroon, Canada, Cape Verde, Cayman Islands, Central African Republic, Chad, Chile, China, Christmas Island, Cocos (Keeling) Islands, Comoros, Congo, Cook Islands, Costa Rica, Côte d'Ivoire (Ivory Coast), Croatia, Cuba, Curacao, Cyprus, Czechia, Denmark, Djibouti, Dominica, Dominican Republic, East Timor, Ecuador, Egypt, El Salvador, Equatorial Guinea, Eritrea, Estonia, Eswatini (Swaziland), Ethiopia, Falkland Islands (Malvinas), Faroe Islands, Fiji, Finland, France, French Guiana, French Polynesia, French Southern Territories, Gabon, Gambia, Georgia, Germany, Ghana, Gibraltar, Greece, Greenland, Grenada, Guadeloupe, Guam, Guatemala, Guinea, Guinea-Bissau, Guyana, Haiti, Heard Island and McDonald Islands, Holy See (Vatican City State), Honduras, China - Hong Kong, Hungary, Iceland, India, Indonesia, Islamic Republic of Iran, Iraq, Ireland, Israel, Italy, Jamaica, Japan, Jordan, Kazakhstan, Kenya, Kiribati, Democratic People's Republic of Korea (North Korea), Republic of Korea (South Korea), Kosovo, Kuwait, Kyrgyzstan, Lao People's Democratic Republic (Laos), Latvia, Lebanon, Lesotho, Liberia, Libyan Arab Jamahiriya, Liechtenstein, Lithuania, Luxembourg, China - Macao, Madagascar, Malawi, Malaysia, Maldives, Mali, Malta, Marshall Islands, Martinique, Mauritania, Mauritius, Mayotte, Mexico, Federated States of Micronesia, Monaco, Mongolia, Montenegro, Montserrat, Morocco, Mozambique, Myanmar (Burma), Namibia, Nauru, Nepal, Netherlands, New Caledonia, New Zealand, Nicaragua, Niger, Nigeria, Niue, Norfolk Island, North Macedonia, Northern Mariana Islands, Norway, Oman, Pakistan, Palau, Panama, Papua New Guinea (PNG), Paraguay, Peru, Philippines, Pitcairn, Poland, Portugal, Puerto Rico, Qatar, Reunion, Romania, Russian Federation, Rwanda, St Barthelemy, St Kitts and Nevis, St Helena, Ascension and Tristan da Cunha, St Lucia, St Martin, St Pierre and Miquelon, Saint Vincent and the Grenadines, Samoa, San Marino, Sao Tome and Principe, Saudi Arabia, Senegal, Serbia, Seychelles, Sierra Leone, Singapore, Slovakia (Slovak Republic), Slovenia, St Maarten, Solomon Islands, Somalia, South Africa, South Georgia and the South Sandwich Islands, South Sudan, Spain, Sri Lanka, Sudan, Suriname, Svalbard and Jan Mayen, Sweden, Switzerland, Syrian Arab Republic, Taiwan, Tajikistan, United Republic of Tanzania, Thailand, The Democratic Republic of the Congo (Zaire), Togo, Tokelau, Tonga, Trinidad and Tobago, Tunisia, Turkey, Turkmenistan, Turks and Caicos Islands, Tuvalu, Uganda, Ukraine, United Arab Emirates (UAE), United Kingdom (UK), United States (USA), United States Minor Outlying Islands, Uruguay, Uzbekistan, Vanuatu, Venezuela, Vietnam, US Virgin Islands, Wallis and Futuna, Western Sahara, Yemen, Zambia, Zimbabwe.\",\"bbox\": {\"south_latitude\": -90,\"west_longitude\": -180,\"north_latitude\": 90,\"east_longitude\": 180},\"id\": {\"authority\": \"EPSG\",\"code\": 4979}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());*/
    }

    @Test
    public void testProjectedCRS() throws JsonProcessingException {
        System.out.println(JSON_MAPPER.readValue(
                "{\"$schema\": \"https://proj.org/schemas/v0.5/projjson.schema.json\",\"type\": \"ProjectedCRS\",\"name\": \"OSGB36 / British National Grid\",\"base_crs\": {\"name\": \"OSGB36\",\"datum\": {\"type\": \"GeodeticReferenceFrame\",\"name\": \"Ordnance Survey of Great Britain 1936\",\"ellipsoid\": {\"name\": \"Airy 1830\",\"semi_major_axis\": 6377563.396,\"inverse_flattening\": 299.3249646}},\"coordinate_system\": {\"subtype\": \"ellipsoidal\",\"axis\": [{\"name\": \"Geodetic latitude\",\"abbreviation\": \"Lat\",\"direction\": \"north\",\"unit\": \"degree\"},{\"name\": \"Geodetic longitude\",\"abbreviation\": \"Lon\",\"direction\": \"east\",\"unit\": \"degree\"}]},\"id\": {\"authority\": \"EPSG\",\"code\": 4277}},\"conversion\": {\"name\": \"British National Grid\",\"method\": {\"name\": \"Transverse Mercator\",\"id\": {\"authority\": \"EPSG\",\"code\": 9807}},\"parameters\": [{\"name\": \"Latitude of natural origin\",\"value\": 49,\"unit\": \"degree\",\"id\": {\"authority\": \"EPSG\",\"code\": 8801}},{\"name\": \"Longitude of natural origin\",\"value\": -2,\"unit\": \"degree\",\"id\": {\"authority\": \"EPSG\",\"code\": 8802}},{\"name\": \"Scale factor at natural origin\",\"value\": 0.9996012717,\"unit\": \"unity\",\"id\": {\"authority\": \"EPSG\",\"code\": 8805}},{\"name\": \"False easting\",\"value\": 400000,\"unit\": \"metre\",\"id\": {\"authority\": \"EPSG\",\"code\": 8806}},{\"name\": \"False northing\",\"value\": -100000,\"unit\": \"metre\",\"id\": {\"authority\": \"EPSG\",\"code\": 8807}}]},\"coordinate_system\": {\"subtype\": \"Cartesian\",\"axis\": [{\"name\": \"Easting\",\"abbreviation\": \"E\",\"direction\": \"east\",\"unit\": \"metre\"},{\"name\": \"Northing\",\"abbreviation\": \"N\",\"direction\": \"north\",\"unit\": \"metre\"}]},\"scope\": \"Engineering survey, topographic mapping.\",\"area\": \"United Kingdom (UK) - offshore to boundary of UKCS within 49°45'N to 61°N and 9°W to 2°E; onshore Great Britain (England, Wales and Scotland). Isle of Man onshore.\",\"bbox\": {\"south_latitude\": 49.75,\"west_longitude\": -9,\"north_latitude\": 61.01,\"east_longitude\": 2.01},\"id\": {\"authority\": \"EPSG\",\"code\": 27700}}",
                WKTObject.AutoDeserialize.class).asWKTObject().toPrettyString());
    }
}
