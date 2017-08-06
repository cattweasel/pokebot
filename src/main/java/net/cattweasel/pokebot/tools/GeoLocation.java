package net.cattweasel.pokebot.tools;

public class GeoLocation {

	private static final double MIN_LAT = Math.toRadians(-90d);
	private static final double MAX_LAT = Math.toRadians(90d);
	private static final double MIN_LON = Math.toRadians(-180d);
	private static final double MAX_LON = Math.toRadians(180d);
	
	private static final double EARTH_RADIUS = 6371000.01D;
	
	private double radLat;
	private double radLon;

	private double degLat;
	private double degLon;

	public static GeoLocation fromDegrees(Double latitude, Double longitude) {
		GeoLocation result = new GeoLocation();
		result.radLat = Math.toRadians(latitude);
		result.radLon = Math.toRadians(longitude);
		result.degLat = latitude;
		result.degLon = longitude;
		result.checkBounds();
		return result;
	}

	public static GeoLocation fromRadians(Double latitude, Double longitude) {
		GeoLocation result = new GeoLocation();
		result.radLat = latitude;
		result.radLon = longitude;
		result.degLat = Math.toDegrees(latitude);
		result.degLon = Math.toDegrees(longitude);
		result.checkBounds();
		return result;
	}

	private void checkBounds() {
		if (radLat < MIN_LAT || radLat > MAX_LAT || radLon < MIN_LON || radLon > MAX_LON) {
			throw new IllegalArgumentException();
		}
	}

	public double getLatitudeInDegrees() {
		return degLat;
	}

	public double getLongitudeInDegrees() {
		return degLon;
	}

	public double getLatitudeInRadians() {
		return radLat;
	}

	public double getLongitudeInRadians() {
		return radLon;
	}
	
	public double distanceTo(GeoLocation location) {
		return distanceTo(location, EARTH_RADIUS);
	}

	public double distanceTo(GeoLocation location, Double radius) {
		return Math.acos(Math.sin(radLat) * Math.sin(location.radLat)
				+ Math.cos(radLat) * Math.cos(location.radLat)
				* Math.cos(radLon - location.radLon)) * radius;
	}
	
	public BoundingCoordinates boundingCoordinates(Double distance) {
		return boundingCoordinates(distance, EARTH_RADIUS);
	}

	public BoundingCoordinates boundingCoordinates(Double distance, Double radius) {
		if (radius < 0d || distance < 0d) {
			throw new IllegalArgumentException();
		}
		double radDist = distance / radius;
		double minLat = radLat - radDist;
		double maxLat = radLat + radDist;
		double minLon, maxLon;
		if (minLat > MIN_LAT && maxLat < MAX_LAT) {
			double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(radLat));
			minLon = radLon - deltaLon;
			if (minLon < MIN_LON) {
				minLon += 2d * Math.PI;
			}
			maxLon = radLon + deltaLon;
			if (maxLon > MAX_LON) {
				maxLon -= 2d * Math.PI;
			}
		} else {
			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLon = MIN_LON;
			maxLon = MAX_LON;
		}
		return new BoundingCoordinates(fromRadians(minLat, minLon), fromRadians(maxLat, maxLon));
	}
	
	@Override
	public String toString() {
		return String.format("GeoLocation[radLat: %s, radLon: %s, degLat: %s, degLon: %s]",
				radLat, radLon, degLat, degLon);
	}
	
	public class BoundingCoordinates {
		
		private final GeoLocation x;
		private final GeoLocation y;
		
		public BoundingCoordinates(GeoLocation x, GeoLocation y) {
			this.x = x;
			this.y = y;
		}
		
		public GeoLocation getX() {
			return x;
		}
		
		public GeoLocation getY() {
			return y;
		}
		
		@Override
		public String toString() {
			return String.format("BoundingCoordinates[x: %s, y: %s]", x, y);
		}
	}
}
