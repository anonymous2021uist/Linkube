package yefancy.map.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import FlightTest.FlightDataPoint;
import TweetsTest.TweetDataPoint;
import vmarcinko.nanocubes.LabellingFn;
import vmarcinko.nanocubes.Nanocube;
import vmarcinko.nanocubes.QueryEngineSM;
import vmarcinko.nanocubes.Schema;
import vmarcinko.nanocubes.TimeLabellingFn;
import yefancy.cube.interfaces.ICube;
import yefancy.cube.interfaces.IDataPoint;
import yefancy.edgecubes.CuboidV2;
import yefancy.edgecubes.EdgecubeV2;
import yefancy.edgecubes.QueryEngine;

public class Test2 {
	private static Random r = new Random(1);
	private static ICube<CuboidV2> linkube;
	public static Nanocube<FlightDataPoint> smartcube;
	private static Map<String, Double> lat = new HashMap<String, Double>();
	private static Map<String, Double> lon = new HashMap<String, Double>();

	public static void main(String[] args) throws Exception {
		Gson gson = new Gson();
		Statistic[] stats = gson.fromJson(readStats("F:\\Datasets\\statsTime.txt"), Statistic[].class);
		int last_zoom = stats[0].zoom;
		long zoomQ = 0;
		long panQ = 0;
		int zoomT = 0;
		int panT = 0;
		
		preAirport();
		
		//linkube
//		long st = System.currentTimeMillis();
//		QueryEngine<CuboidV2> queryEngine = LinkubeTest();
//		long qt =System.currentTimeMillis() - st;
//		System.out.println(qt);
//		System.out.println("finish!" + printMemoryState());

		//smartcube
		long st = System.currentTimeMillis();
		QueryEngineSM queryEngine = Smartcube();
		long qt =System.currentTimeMillis() - st;
		System.out.println(qt);
		printMemoryState();
//		
		int totalTime = 0;
		for(int i=0;i<stats.length;i++) {
			List<List<Long>> cate = new ArrayList<List<Long>>();
			cate.add(new ArrayList<Long>());
			if(stats[i].type == 1)
				cate.get(0).add(0L);
			else if(stats[i].type == 2)
				cate.get(0).add(1L);
			int from = stats[i].from; int to = stats[i].to;
//			List<Date> randomTime = randomDate("20130101","20160101",r.nextInt(500)+1);
//			stats[i].from = Integer.parseInt(new SimpleDateFormat("yyyyMMdd" ).format(randomTime.get(0)));
//			stats[i].from = Integer.parseInt(new SimpleDateFormat("yyyyMMdd" ).format(randomTime.get(1)));
			st = System.currentTimeMillis();
			for(int t = 0;t<5;t++) {
				queryEngine.queryMap(49.15296965617042, 
						-125.55175781250001, 28.92163128242129, 
						-73.95996093750001, 11, 
						cate, 
//						randomTime.get(0),
//						randomTime.get(1));
						new Date(from/10000,from%10000/100,from%100),
						new Date(to/10000,to%10000/100,to%100));
			}
			qt = System.currentTimeMillis() - st;
			if (last_zoom == stats[i].zoom) {
				panT++;
				panQ += qt;
			} else {
				zoomT++;
				last_zoom = stats[i].zoom;
				zoomQ += qt;
			}
			System.out.println(String.format("%d\t%d\t%d\t%d\t%.4f", 
					System.currentTimeMillis() - st, 
					queryEngine.getLastSpatialTime()/1000000,
					queryEngine.getLastCategoricalTime()/1000000,
					queryEngine.getLastTemporalTime()/1000000,
					1.0));
					//queryEngine.hit*1.0/queryEngine.cacheSize));
			
			
			st = System.currentTimeMillis();
			if((Object)queryEngine instanceof QueryEngineSM)
				SmartcubeUpdateAgg((QueryEngineSM)(Object)queryEngine);
			totalTime += System.currentTimeMillis() - st;
			//printMemoryState();
			//long mem = printMemoryState();
		}	
		System.out.println("totalUpdate:" + totalTime);
		System.out.println(String.format("zoom:%d\t%d\npan:%d\t%d", zoomQ,zoomT,panQ,panT));
		
//		String fileName="C:\\Users\\lfz47\\Desktop\\statsTime.txt";
//        try{
//            FileWriter writer=new FileWriter(fileName);
//            writer.write(gson.toJson(stats));
//            writer.close();
//        } catch (IOException e){
//            e.printStackTrace();
//        }
//        System.out.println("finish save");
	}
	
	private static String readStats(String fileName) throws Exception {
		  File file = new File(fileName);
		    if(!file.exists()){
		        return null;
		    }
		    FileInputStream inputStream = new FileInputStream(file);
		    int length = inputStream.available();
		    byte bytes[] = new byte[length];
		    inputStream.read(bytes);
		    inputStream.close();
		    String str =new String(bytes, StandardCharsets.UTF_8);
		    return str; 
	}
	
	private static QueryEngine LinkubeTest() {
//		int[][] split = {{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24}, {0,1}, {0,1,2,3}};
		int[][] split = {{10,24}, {1}, {0,1,2,3}};
		linkube = new EdgecubeV2<CuboidV2>(CuboidV2.class, split);
		QueryEngine<CuboidV2> queryEngine = queryEngine = new QueryEngine<CuboidV2>(((EdgecubeV2)linkube).getRoot(), CuboidV2.class);
		int times = 0;
		for (int i = 1987; i <= 2008; i++) {
			String filePath = "F:\\Datasets\\Flight\\" + i + ".csv";
			try {
				FileReader fr = new FileReader(filePath);
				BufferedReader bf = new BufferedReader(fr);
				CSVReader csvReader = new CSVReaderBuilder(bf).build();
				Iterator<String[]> iterator = csvReader.iterator();
				iterator.next();
				while (iterator.hasNext()) {
					String[] as = iterator.next();
					IDataPoint dp = parseFlightJson(as);
					if (dp != null) {
						times++;
						linkube.insert(dp);
					}
					if (times%1000000 == 0)
						System.out.println(times/10000 + "W\t");
				}
				bf.close();
				fr.close();
				csvReader.close();		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		return queryEngine;
	}
	
	private static QueryEngineSM Smartcube() {
		Schema<FlightDataPoint> schema = new Schema<>();
        List<LabellingFn<FlightDataPoint>> locationChain = schema.addChain();
        for (int i = 1; i < 25; i++) {
        	final int index = i;
            locationChain.add(dp->{
            	return dp.getLabel(0, index-1);
            });
        }

        List<LabellingFn<FlightDataPoint>> typeChain = schema.addChain();
        typeChain.add(dp->{
        	return dp.carrier;
        });
        
        List<LabellingFn<FlightDataPoint>> timeChain = schema.addChain();
        timeChain.add(dp->{
        	return dp.getLabel(2, 0);
        });
        timeChain.add(dp->{
        	return dp.getLabel(2, 1);
        });
        timeChain.add(dp->{
        	return dp.getLabel(2, 2);
        });

        
        TimeLabellingFn<FlightDataPoint> timeLabellingFn = dp->{
        	return dp.getTime();
        };
        smartcube = new Nanocube<>(schema, timeLabellingFn);
        int times = 0;
        for (int i = 1987; i <= 2008; i++) {
			String filePath = "F:\\Datasets\\Flight\\" + i + ".csv";
			try {
				FileReader fr = new FileReader(filePath);
				BufferedReader bf = new BufferedReader(fr);
				CSVReader csvReader = new CSVReaderBuilder(bf).build();
				Iterator<String[]> iterator = csvReader.iterator();
				iterator.next();
				while (iterator.hasNext()) {
					String[] as = iterator.next();
					FlightDataPoint dp = parseFlightJson(as);
					if (dp != null) {
						times++;
						smartcube.insert_sm(dp);
					}
					if (times%1000000 == 0) 
						System.out.println(times/10000 + "W\t");
				}
				bf.close();
				fr.close();
				csvReader.close();		
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new QueryEngineSM(smartcube);
	}
	
	private static FlightDataPoint parseFlightJson(String[] as) {
		FlightDataPoint dp = null;
		if (lat.containsKey(as[16]))
			dp = new FlightDataPoint(lat.get(as[16]), lon.get(as[16]), as[8].hashCode(), Long.parseLong(as[0]),
					Long.parseLong(as[1]), Long.parseLong(as[2]));
		return dp;
	}
	
	public static void preAirport() {
		try {
			FileReader fr = new FileReader("F:\\Datasets\\Flight\\airports.csv");
			BufferedReader bf = new BufferedReader(fr);
			CSVReader csvReader = new CSVReaderBuilder(bf).build();
			Iterator<String[]> iterator = csvReader.iterator();
			iterator.next();
			while (iterator.hasNext()) {
				String[] as = iterator.next();
				lat.put(as[0], Double.parseDouble(as[5]));
				lon.put(as[0], Double.parseDouble(as[6]));
			}
			bf.close();
			fr.close();
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static long printMemoryState() {
    	System.gc();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        long totalMem = convertToMB(Runtime.getRuntime().totalMemory());
        long freeMem = convertToMB(Runtime.getRuntime().freeMemory());
        long usedMem = totalMem - freeMem;
//        System.out.println(
//                "Total mem: " + totalMem + " MB" +
//                ", Free mem: " + freeMem + " MB" +
//                ", Used mem: " + usedMem + " MB"
//        );
        System.out.println(usedMem);
        return usedMem;
    }
	
	private static void SmartcubeUpdateAgg(QueryEngineSM queryEngine) {
		queryEngine.updateAgg();
	}
	
	private static void LinkubeUpdateAgg(QueryEngine<CuboidV2> queryEngine, int levelSize) {
		List<Integer> topK = new ArrayList<Integer>();
		long[] qt = queryEngine.queryTime;
		for (int i = 0; i < qt.length; i++) {
			if (qt[i] == 0)
				continue;
			if (topK.size() < levelSize) {			
				int base = topK.size();
				for (int j = 0; j < base; j++) {
					if (qt[i] > qt[topK.get(j)]) {
						topK.add(j, i);
						break;
					}

				}
				if (topK.size() == base)
					topK.add(topK.size(), i);
			}				
			else
				for (int j = 0; j < levelSize; j++) {
					if (qt[i] > qt[topK.get(j)]) {
						topK.add(j, i);
						break;
					}

				}
		}
		List<Set<Integer>> split = new ArrayList<Set<Integer>>();
		Set<Integer> d1 = new HashSet<Integer>();
		Set<Integer> d2 = new HashSet<Integer>();
		Set<Integer> d3 = new HashSet<Integer>();
		d1.add(24);
		d2.add(1);
		d3.add(0);
		d3.add(1);
		d3.add(2);
		d3.add(3);
		split.add(d1);
		split.add(d2);
		split.add(d3);
		for (int i = 0; i < topK.size() && i < levelSize; i++)
			d1.add(topK.get(i));
		((EdgecubeV2) (linkube)).updateAggSplit(split);
	}
	
	private static long convertToMB(long byteCount) {
        return byteCount / (1024 * 1024);
    }
	
	public static List<Date> randomDate(String beginDate, String endDate,int randNum) {
        List<Date> list = new ArrayList<Date>();
        Date startTime = null;
        Date endTime = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date start = format.parse(beginDate);// 构造开始日期
            Date end = format.parse(endDate);// 构造结束日期
            // getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
            if (start.getTime() >= end.getTime()) {
                return null;
            }
            long date = random(start.getTime(), end.getTime());
            startTime =  new Date(date);
            //System.out.println(date);
            date = date + randNum * 24*60*60*1000;
            //System.out.println(date);
            endTime = new Date(date);
            
            //System.out.println(dateToString(startTime, "yyyy-MM-dd"));
            //System.out.println(dateToString(endTime, "yyyy-MM-dd"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        list.add(startTime);
        list.add(endTime);
        return list;
    }
	
	private static long random(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        // 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
        if (rtn == begin || rtn == end) {
            return random(begin, end);
        }
        return rtn;
    }
}
