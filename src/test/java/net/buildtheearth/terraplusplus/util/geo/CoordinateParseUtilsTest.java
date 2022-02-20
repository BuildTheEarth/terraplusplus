package net.buildtheearth.terraplusplus.util.geo;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author SmylerMC
 */
public class CoordinateParseUtilsTest {

    private static final double PRECISION = 1e-7;

    @Test
    public void testValid() {
        this.testValidStringParsing("02°49'52\"N131°47'03\"E", 131.784167, 2.831111);
        this.testValidStringParsing("02°49'52''N 131°47'03''E", 131.784167, 2.831111);
        this.testValidStringParsing("02°49'52\"n 131°47'03\"E", 131.784167, 2.831111);
        this.testValidStringParsing("02°49'52\"N 131°47'03\"e", 131.784167, 2.831111);
        this.testValidStringParsing("02°49'52\"S 131°47'03\"E", 131.784167, -2.831111);
        this.testValidStringParsing("02°49'52\"N 131°47'03\"W", -131.784167, 2.831111);
        this.testValidStringParsing("02°49'52\"s 131°47'03\"w", -131.784167, -2.831111);

        this.testValidStringParsing("2.831111s 131.784167w", -131.784167, -2.831111);
        this.testValidStringParsing("-2.831111 -131.784167", -131.784167, -2.831111);
        this.testValidStringParsing("2,831111s 131,784167w", -131.784167, -2.831111);
        this.testValidStringParsing("-2,831111 -131,784167", -131.784167, -2.831111);

        this.testValidStringParsing("02°49'52\"N;131°47'03\"E", 131.784167, 2.831111);
        this.testValidStringParsing("2.831111s;131.784167w", -131.784167, -2.831111);
        this.testValidStringParsing("-2.831111;-131.784167", -131.784167, -2.831111);
        this.testValidStringParsing("02°49'52\"N,131°47'03\"E", 131.784167, 2.831111);
        this.testValidStringParsing("2.831111s,131.784167w", -131.784167, -2.831111);
        this.testValidStringParsing("-2.831111,-131.784167", -131.784167, -2.831111);
        this.testValidStringParsing("-2.831111, -131.784167", -131.784167, -2.831111);
        this.testValidStringParsing("41.86086476755931, -87.7630805146585", -87.763081, 41.860865);

        // Comma variant of the above
        this.testValidStringParsing("2,831111s;131,784167w", -131.784167, -2.831111);
        this.testValidStringParsing("-2,831111;-131,784167", -131.784167, -2.831111);
        this.testValidStringParsing("2,831111s 131,784167w", -131.784167, -2.831111);
        this.testValidStringParsing("-2,831111 -131,784167", -131.784167, -2.831111);

        this.testValidStringParsing("2 131", 131, 2);
        this.testValidStringParsing("-2 -131", -131, -2);
        this.testValidStringParsing("0. 0.", 0, 0);
        this.testValidStringParsing("0 0", 0, 0);
        this.testValidStringParsing("0.1 0", 0, 0.1);

        // Comma variant of the above
        this.testValidStringParsing("2 131", 131, 2);
        this.testValidStringParsing("-2 -131", -131, -2);
        this.testValidStringParsing("0, 0,", 0, 0);
        this.testValidStringParsing("0 0", 0, 0);
        this.testValidStringParsing("0,1 0", 0, 0.1);

        // More than one space on the middle
        this.testValidStringParsing("0,1  0", 0, 0.1);
    }

    @Test
    public void testInvalid() {

        // Too many values
        this.testInvalidStringParsing("48.58, 85.26 75.2");
        this.testInvalidStringParsing("48.58,  85.26 75.2");
        this.testInvalidStringParsing("48.58, 85.26 75.2");
        this.testInvalidStringParsing("48.58, 85.26, 75.2");
        this.testInvalidStringParsing("48.58/85.26/75.2");
        this.testInvalidStringParsing("48.58, 85.26, 75.2");
        this.testInvalidStringParsing("48.58, 85.26     75.2");

        // Too many values: comma variant
        this.testInvalidStringParsing("48,58; 85,26 75,2");
        this.testInvalidStringParsing("48,58;  85,26 75,2");
        this.testInvalidStringParsing("48,58; 85,26 75,2");
        this.testInvalidStringParsing("48,58; 85,26, 75,2");
        this.testInvalidStringParsing("48,58/85,26/75,2");
        this.testInvalidStringParsing("48,58; 85,26, 75,2");
        this.testInvalidStringParsing("48,58; 85,26     75,2");

        // Too few values
        this.testInvalidStringParsing("48.35");
        this.testInvalidStringParsing("48.35,");
        this.testInvalidStringParsing(",48.35");
        this.testInvalidStringParsing("/48.35");
        this.testInvalidStringParsing("/48,35");

        // Invalid char
        this.testInvalidStringParsing("48.56@23.9");
        this.testInvalidStringParsing("{48.56 23.9}");
        this.testInvalidStringParsing("48,56@23.9");
        this.testInvalidStringParsing("{48,56;23,9}");

        // Mix of different decimal separators
        this.testInvalidStringParsing("78.52 45,8");
        this.testInvalidStringParsing("78.52;45,8");

        // Out of bounds
        this.testInvalidStringParsing("45 -195");
        this.testInvalidStringParsing("95 125");
        this.testInvalidStringParsing("-97, -172");
        this.testInvalidStringParsing("45, 223");

    }

    private void testValidStringParsing(String string, double longitude, double latitude) {
        LatLng latLng = CoordinateParseUtils.parseVerbatimCoordinates(string);
        Assert.assertNotNull(String.format("Failed to parse a valid coordinate string: %s", string), latLng);
        final double lng = latLng.getLng();
        final double lat = latLng.getLat();
        Assert.assertEquals("Parsed a wrong longitude value", longitude, lng, PRECISION);
        Assert.assertEquals("Parsed a wrong latitude value", latitude, lat, PRECISION);
    }

    private void testInvalidStringParsing(String string) {
        assertNull(CoordinateParseUtils.parseVerbatimCoordinates(string));
    }

}
