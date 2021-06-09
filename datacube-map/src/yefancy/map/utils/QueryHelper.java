//package yefancy.map.utils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import javafx.geometry.Point3D;
//import yefancy.cube.interfaces.ICube;
//import yefancy.edgecubes.ACuboid;
//import yefancy.edgecubes.CuboidV2;
//import yefancy.edgecubes.EdgecubeV2;
//
//public class QueryHelper {
//	public static long[] queryTime = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
//	
//	public static List<Point3D> query(ICube<CuboidV2> cube, double ltLat, double ltLon, double rbLat, double rbLon,
//			int zoom, int type) {
//		List<Point3D> nodes = new ArrayList<Point3D>();
//		List<List<Long>> query = new ArrayList<List<Long>>();
//		query.add(new ArrayList<Long>());
//		query.add(new ArrayList<Long>());
//		if (type == 1)
//			query.get(1).add(0L);
//		else if(type == 2)
//			query.get(1).add(1L);
//		queryDFS(cube, nodes, (long) ((ltLat + 89) * 10000000), (long) ((ltLon + 179) * 10000000),
//				(long) ((rbLat + 89) * 10000000), (long) ((rbLon + 179) * 10000000), query, zoom);
//		return nodes;
//	}
//
//	public static void queryDFS(ICube<CuboidV2> cube, List<Point3D> list, long ltLat, long ltLon, long rbLat,
//			long rbLon, List<List<Long>> query, int zoom) {
//		List<Long> geo = query.get(0);
//		if (geo.size() == zoom) {
//			ACuboid cuboid = cube.query(query);
//			queryTime[zoom] += ((EdgecubeV2)cube).UpdateCount() * zoom;	
//			int count = (int) ((CuboidV2) cuboid).count;
//			if (count == 0)
//				return;
//			list.add(getGeo(geo, count));
//		} else {
//			for (int i = 0; i < 4; i++) {
//				geo.add((long) i);
//				if (checkAvaliabe(geo, ltLat, ltLon, rbLat, rbLon)) {
//					queryDFS(cube, list, ltLat, ltLon, rbLat, rbLon, query, zoom);
//				}
//				geo.remove(geo.size() - 1);
//			}
//		}
//	}
//	
////	public static List<Point3D> query(ICube<SummedTimeCountsTable> cube, double ltLat, double ltLon, double rbLat, double rbLon,
////			int zoom, int type) {
////		List<Point3D> nodes = new ArrayList<Point3D>();
////		List<List<Long>> query = new ArrayList<List<Long>>();
////		query.add(new ArrayList<Long>());
////		query.add(new ArrayList<Long>());
////		if (type == 1)
////			query.get(1).add(0L);
////		else if(type == 2)
////			query.get(1).add(1L);
////		queryDFS(cube, nodes, (long) ((ltLat + 89) * 10000000), (long) ((ltLon + 179) * 10000000),
////				(long) ((rbLat + 89) * 10000000), (long) ((rbLon + 179) * 10000000), query, zoom);
////		return nodes;
////	}
////
////	public static void queryDFS(ICube<SummedTimeCountsTable> cube, List<Point3D> list, long ltLat, long ltLon, long rbLat,
////			long rbLon, List<List<Long>> query, int zoom) {
////		List<Long> geo = query.get(0);
////		if (geo.size() == zoom) {
////			SummedTimeCountsTable aList = cube.query(query);
////			int count = 0;
////			if (aList == null)
////				return;
//////			for (ACuboid aCuboid : aList)
//////				count += ((Cuboid) aCuboid).getCount();
////			list.add(getGeo(geo, (int) aList.getCount()));
////		} else {
////			for (int i = 0; i < 4; i++) {
////				geo.add((long) i);
////				if (checkAvaliabe(geo, ltLat, ltLon, rbLat, rbLon)) {
////					queryDFS(cube, list, ltLat, ltLon, rbLat, rbLon, query, zoom);
////				}
////				geo.remove(geo.size() - 1);
////			}
////		}
////	}
//
//	public static List<Long> getGeo(double latD, double lonD, int depth) {
//		long lat = (long) ((latD + 89) * 10000000);
//		long lon = (long) ((lonD + 179) * 10000000);
//		List<Long> geo = new ArrayList<Long>();
//		long maxLat = 1800000000L;
//		long minLat = 0;
//		long maxLon = 3600000000L;
//		long minLon = 0;
//		for (int i = 0; i < depth; i++) {
//			long middleLat = minLat + (maxLat + 1 - minLat) / 2;
//			long latB = lat > middleLat ? 0 : 2;
//			if (lat > middleLat)
//				minLat = middleLat;
//			else
//				maxLat = middleLat;
//
//			long middleLon = minLon + (maxLon + 1 - minLon) / 2;
//			long lonB = lon > middleLon ? 1 : 0;
//			if (lon > middleLon)
//				minLon = middleLon;
//			else
//				maxLon = middleLon;
//			geo.add(latB + lonB);
//		}
//		return geo;
//	}
//
//	public static boolean checkAvaliabe(List<Long> path, long ltLat, long ltLon, long rbLat, long rbLon) {
//		long maxLat = 1800000000L;
//		long minLat = 0;
//		long maxLon = 3600000000L;
//		long minLon = 0;
//		for (int i = 0; i < path.size(); i++) {
//			switch (path.get(i).intValue()) {
//			case 0:
//				minLat = minLat + (maxLat + 1 - minLat) / 2;
//				maxLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			case 1:
//				minLat = minLat + (maxLat + 1 - minLat) / 2;
//				minLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			case 2:
//				maxLat = minLat + (maxLat + 1 - minLat) / 2;
//				maxLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			case 3:
//				maxLat = minLat + (maxLat + 1 - minLat) / 2;
//				minLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			}
//		}
//		if (minLon > rbLon || maxLat < rbLat)
//			return false;
//		else if (maxLon < ltLon || minLat > ltLat)
//			return false;
//		return true;
//	}
//
//	public static Point3D getGeo(List<Long> path, int count) {
//		long maxLat = 1800000000L;
//		long minLat = 0;
//		long maxLon = 3600000000L;
//		long minLon = 0;
//		for (int i = 0; i < path.size(); i++) {
//			switch (path.get(i).intValue()) {
//			case 0:
//				minLat = minLat + (maxLat + 1 - minLat) / 2;
//				maxLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			case 1:
//				minLat = minLat + (maxLat + 1 - minLat) / 2;
//				minLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			case 2:
//				maxLat = minLat + (maxLat + 1 - minLat) / 2;
//				maxLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			case 3:
//				maxLat = minLat + (maxLat + 1 - minLat) / 2;
//				minLon = minLon + (maxLon + 1 - minLon) / 2;
//				break;
//			}
//		}
//		return new Point3D((maxLon + minLon) * 1.0d / 20000000 - 179, (maxLat + minLat) * 1.0d / 20000000 - 89, count);
//	}
//}
