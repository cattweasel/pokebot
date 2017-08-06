package net.cattweasel.pokebot.tools;

import org.junit.Assert;
import org.junit.Test;

import net.cattweasel.pokebot.tools.GeoLocation.BoundingCoordinates;

public class GeoLocationTest {

	@Test
	public void testDistanceTo() throws Exception {
		GeoLocation loc = GeoLocation.fromDegrees(50.108288D, 8.624360D);
		Double distance = loc.distanceTo(GeoLocation.fromDegrees(50.108769D, 8.635067D));
		Assert.assertNotNull(distance);
		Assert.assertEquals(Double.valueOf(765.4218687764466), distance);
	}
	
	@Test
	public void test() throws Exception {
		GeoLocation loc = GeoLocation.fromDegrees(50.108288D, 8.624360D);
		BoundingCoordinates coords = loc.boundingCoordinates(1000D);
		Assert.assertNotNull(coords);
		Assert.assertNotNull(coords.getX());
		Assert.assertNotNull(coords.getY());
		Assert.assertEquals(Double.valueOf(50.099294783954925),
				Double.valueOf(coords.getX().getLatitudeInDegrees()));
		Assert.assertEquals(Double.valueOf(8.610337430072201),
				Double.valueOf(coords.getX().getLongitudeInDegrees()));
		Assert.assertEquals(Double.valueOf(50.11728121604507),
				Double.valueOf(coords.getY().getLatitudeInDegrees()));
		Assert.assertEquals(Double.valueOf(8.638382569927796),
				Double.valueOf(coords.getY().getLongitudeInDegrees()));
	}
}
