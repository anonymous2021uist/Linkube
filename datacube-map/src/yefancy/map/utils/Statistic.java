package yefancy.map.utils;

public class Statistic {
	public long queryTime;
	public long memoryCost;
	public double ltLat;
	public double ltLon;
	public double rbLat;
	public double rbLon;
	public int zoom;
	public int type;
	public int from;
	public int to;

	public Statistic(long queryTime, long memoryCost, double ltLat, double ltLon, double rbLat, double rbLon, int zoom,
			int type, int from, int to) {
		this.queryTime = queryTime;
		this.memoryCost = memoryCost;
		this.ltLat = ltLat;
		this.ltLon = ltLon;
		this.rbLat = rbLat;
		this.rbLon = rbLon;
		this.zoom = zoom;
		this.type = type;
		this.from = from;
		this.to = to;
	}
}
