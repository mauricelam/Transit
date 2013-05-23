package include;

public class GeoPoint {
	private int lat, lng;

	public GeoPoint(int latE6, int lngE6) {
		lat = latE6;
		lng = lngE6;
	}

    public GeoPoint(double latitude, double longitude) {
        lat = (int) (latitude * 1e6);
        lng = (int) (longitude * 1e6);
    }

    public int getLatitudeE6() {
		return lat;
	}

	public int getLongitudeE6() {
		return lng;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof GeoPoint){
			GeoPoint p = (GeoPoint) obj;
			return this.lat == p.lat && this.lng == p.lng;
		}else
			return false;
	}
}