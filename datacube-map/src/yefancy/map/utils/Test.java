package yefancy.map.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

public class Test {
	private static Random r = new Random(1);
	private static ICube<CuboidV2> linkube;
	public static Nanocube<TweetDataPoint> smartcube;
	
	public static void main(String[] args) throws Exception {
		Gson gson = new Gson();
		Statistic[] stats = gson.fromJson(readStats("F:\\Datasets\\stats1.txt"), Statistic[].class);
		int last_zoom = stats[0].zoom;
		long zoomQ = 0;
		long panQ = 0;
		int zoomT = 0;
		int panT = 0;
		long st = System.currentTimeMillis();
		
		//linkube
		QueryEngine<CuboidV2> queryEngine = LinkubeTest();
		System.out.println(System.currentTimeMillis() - st);
		System.out.println("finish!" + printMemoryState());

		//smartcube
//		QueryEngineSM queryEngine = Smartcube();
//		System.out.println(System.currentTimeMillis() - st);
//		System.out.println("finish!" + printMemoryState());
		
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
			queryEngine.queryMap(stats[i].ltLat, 
					stats[i].ltLon, stats[i].rbLat, 
					stats[i].rbLon, stats[i].zoom-5, 
					cate, 
//					randomTime.get(0),
//					randomTime.get(1));
					new Date(from/10000,from%10000/100,from%100),
					new Date(to/10000,to%10000/100,to%100));
			long dur = System.currentTimeMillis() - st;
			if (last_zoom == stats[i].zoom) {
				panT++;
				panQ += dur;
			} else {
				zoomT++;
				last_zoom = stats[i].zoom;
				zoomQ += dur;
			}
			System.out.println(String.format("%d\t%d\t%d\t%d\t%.4f", 
					dur, 
					queryEngine.getLastSpatialTime()/1000000,
					queryEngine.getLastCategoricalTime()/1000000,
					queryEngine.getLastTemporalTime()/1000000,
					1.0));
			
			st = System.currentTimeMillis();
			//LinkubeUpdateAgg(queryEngine,4);
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
		int[][] split = {{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24}, {0,1}, {0,1,2,3}};
//		int[][] split = {{12, 24}, {1}, {0,1,2,3}};
		linkube = new EdgecubeV2<CuboidV2>(CuboidV2.class, split);
		QueryEngine<CuboidV2> queryEngine = queryEngine = new QueryEngine<CuboidV2>(((EdgecubeV2)linkube).getRoot(), CuboidV2.class);
		int times = 0;
		int timesW = 0;
		try {		
			FileReader fr = new FileReader("F:\\Datasets\\twitter\\tc2014.json");
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while ((str = bf.readLine()) != null) {
				IDataPoint dp = parseTweetJson(str);
				if (dp != null) {
					times++;
					linkube.insert(dp);
				}
				if (times == 100000) {
					times = 0;
					timesW += 10;
					System.out.println(timesW + "W\t");
				}
			}
			bf.close();
			fr.close();
			fr = new FileReader("F:\\Datasets\\twitter\\tc2015.json");
			bf = new BufferedReader(fr);
			while ((str = bf.readLine()) != null) {
				IDataPoint dp = parseTweetJson(str);
				if (dp != null) {
					times++;
					linkube.insert(dp);
				}
				if (times == 100000) {
					times = 0;
					timesW += 10;
					System.out.println(timesW + "W\t");
				}
			}
			bf.close();
			fr.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queryEngine;
	}
	
	private static QueryEngineSM Smartcube() {
		Schema<TweetDataPoint> schema = new Schema<>();
        List<LabellingFn<TweetDataPoint>> locationChain = schema.addChain();
        for (int i = 1; i < 25; i++) {
        	final int index = i;
            locationChain.add(dp->{
            	return dp.getLabel(0, index-1);
            });
        }

        List<LabellingFn<TweetDataPoint>> typeChain = schema.addChain();
        typeChain.add(dp->{
        	return dp.getType();
        });
        
        List<LabellingFn<TweetDataPoint>> timeChain = schema.addChain();
        timeChain.add(dp->{
        	return dp.getLabel(2, 0);
        });
        timeChain.add(dp->{
        	return dp.getLabel(2, 1);
        });
        timeChain.add(dp->{
        	return dp.getLabel(2, 2);
        });

        
        TimeLabellingFn<TweetDataPoint> timeLabellingFn = dp->{
        	return dp.getTime();
        };
        smartcube = new Nanocube<>(schema, timeLabellingFn);
        
		int times = 0;
		int timesW = 0;
		try {
			FileReader fr = new FileReader("F:\\Datasets\\twitter\\tc2014.json");
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while ((str = bf.readLine()) != null) {
				TweetDataPoint dp = parseTweetJson(str);
				if (dp != null) {
					times++;
					smartcube.insert_sm(dp);
				}
				if (times == 100000) {
					times = 0;
					timesW += 10;
					System.out.println(timesW + "W\t");
				}
			}
			bf.close();
			fr.close();
			fr = new FileReader("F:\\Datasets\\twitter\\tc2015.json");
			bf = new BufferedReader(fr);
			while ((str = bf.readLine()) != null) {
				TweetDataPoint dp = parseTweetJson(str);
				if (dp != null) {
					times++;
					smartcube.insert_sm(dp);
				}
				if (times == 100000) {
					times = 0;
					timesW += 10;
					System.out.println(timesW + "W\t");
				}
			}
			bf.close();
			fr.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new QueryEngineSM(smartcube);
	}
	
	private static TweetDataPoint parseTweetJson(String json) {
		JsonParser parse = new JsonParser(); // 创建json解析器
		try {
			JsonObject jObj = (JsonObject) parse.parse(json); // 创建jsonObject对象
			long tweetid = jObj.get("tweetid").getAsLong() % 2;
			JsonObject location = jObj.get("location").getAsJsonObject();
			long lat = (long) ((location.get("lat").getAsDouble() + 89) * 10000000);
			long lon = (long) ((location.get("lon").getAsDouble() + 179) * 10000000);
			return new TweetDataPoint(lat, lon, tweetid, 2014 + r.nextInt(2) , 1 + r.nextInt(12), 1 + r.nextInt(31));
		} catch (Exception e) {
			// TODO: handle exception

		}
		return null;
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
//        System.out.println(usedMem);
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
