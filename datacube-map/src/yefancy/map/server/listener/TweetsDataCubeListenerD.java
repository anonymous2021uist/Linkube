package yefancy.map.server.listener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.istack.internal.logging.Logger;

import BrightkiteTest.BrightkiteDataPoint;
import TweetsTest.TweetDataPoint;
import vmarcinko.nanocubes.LabellingFn;
import vmarcinko.nanocubes.Nanocube;
import vmarcinko.nanocubes.QueryEngineSM;
import vmarcinko.nanocubes.Schema;
import vmarcinko.nanocubes.TimeLabellingFn;
import yefancy.cube.interfaces.ICube;
import yefancy.cube.interfaces.IDataPoint;
import yefancy.cube.interfaces.IQueryEngine;
import yefancy.edgecubes.CuboidV2;
import yefancy.edgecubes.EdgecubeV2;
import yefancy.edgecubes.QueryEngine;



public class TweetsDataCubeListenerD implements ServletContextListener{
	private static Logger logger = Logger.getLogger(TweetsDataCubeListenerD.class);
	int[][] split = {{8,12,15,24}, {0,1}, {0, 1, 2, 3}};
	
	public static IQueryEngine queryEngine;
	
//	int[][] split = {{24}, {1}, {0, 1, 2, 3}};
	public final static int levelSize = 4;
	public static ICube<CuboidV2> cube;
	
//	public static Nanocube<TweetDataPoint> cube;
	
	private Thread t1;
	private static Random r = new Random(1);

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		logger.info("class : context destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext context = servletContextEvent.getServletContext();
		logger.info("DataCube : Initialing");
		if (cube != null){
			logger.info("DataCube : context Initialized--fast load");
			return;
		}			
		//cube = new Nanocube();
		t1 = new Thread(()->{
			init();
//			initSM();
//			try {
//				//GowallaTest.initCache(cube.getRoot(), 2, "E:\\JavaWorkSpace\\nanocubes-master\\cache\\");
//			} catch (IOException | ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			logger.info("DataCube : context Initialized");
		});
        t1.start();
	}
	
	private void initSM() {
//		Schema<TweetDataPoint> schema = new Schema<>();
//        List<LabellingFn<TweetDataPoint>> locationChain = schema.addChain();
//        for (int i = 1; i < 25; i++) {
//        	final int index = i;
//            locationChain.add(dp->{
//            	return dp.getLabel(0, index-1);
//            });
//        }
//
//        List<LabellingFn<TweetDataPoint>> typeChain = schema.addChain();
//        typeChain.add(dp->{
//        	return dp.getType();
//        });
//        
//        List<LabellingFn<TweetDataPoint>> timeChain = schema.addChain();
//        timeChain.add(dp->{
//        	return dp.getLabel(2, 0);
//        });
//        timeChain.add(dp->{
//        	return dp.getLabel(2, 1);
//        });
//        timeChain.add(dp->{
//        	return dp.getLabel(2, 2);
//        });
//
//        
//        TimeLabellingFn<TweetDataPoint> timeLabellingFn = dp->{
//        	return dp.getTime();
//        };
//        cube = new Nanocube<>(schema, timeLabellingFn);
//        
//		int times = 0;
//		int timesW = 0;
//		try {		
//			FileReader fr = new FileReader("F:\\Datasets\\twitter\\tc2014.json");
//			BufferedReader bf = new BufferedReader(fr);
//			String str;
//			while ((str = bf.readLine()) != null) {
//				TweetDataPoint dp = parseTweetJson(str);
//				if (dp != null) {
//					times++;
//					cube.insert_sm(dp);
//				}
//				if (times == 100000) {
//					times = 0;
//					timesW += 10;
//					logger.info(timesW + "W\t");
//				}
//			}
//			bf.close();
//			fr.close();
//			fr = new FileReader("F:\\Datasets\\twitter\\tc2015.json");
//			bf = new BufferedReader(fr);
//			while ((str = bf.readLine()) != null) {
//				TweetDataPoint dp = parseTweetJson(str);
//				if (dp != null) {
//					times++;
//					cube.insert_sm(dp);
//				}
//				if (times == 100000) {
//					times = 0;
//					timesW += 10;
//					logger.info(timesW + "W\t");
//				}
//			}
//			bf.close();
//			fr.close();
//		
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		queryEngine = new QueryEngineSM(cube);
	}
	
	private void init() {
		cube = new EdgecubeV2<CuboidV2>(CuboidV2.class, split) ;
		queryEngine = new QueryEngine<CuboidV2>(((EdgecubeV2)cube).getRoot(), CuboidV2.class);
		int times = 0;
		int timesW = 0;
		try {
			
//			FileReader fr = new FileReader("C:\\Users\\lfz47\\Desktop\\tweet-geolocation-5m\\Brightkite_totalCheckins.txt");
//			BufferedReader bf = new BufferedReader(fr);
//			String str;
//			while ((str = bf.readLine()) != null) {
//				IDataPoint dp = parseBrightJson(str);
//				if (dp != null) {
//					times++;
//					cube.insert(dp);
//				}
//				if (times == 100000) {
//					times = 0;
//					timesW += 10;
//					logger.info(timesW + "W\t");
//				}
//			}
//			bf.close();
//			fr.close();
		
			FileReader fr = new FileReader("F:\\Datasets\\twitter\\tc2014.json");
			BufferedReader bf = new BufferedReader(fr);
			String str;
			while ((str = bf.readLine()) != null) {
				IDataPoint dp = parseTweetJson(str);
				if (dp != null) {
					times++;
					cube.insert(dp);
				}
				if (times == 100000) {
					times = 0;
					timesW += 10;
					logger.info(timesW + "W\t");
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
					cube.insert(dp);
				}
				if (times == 100000) {
					times = 0;
					timesW += 10;
					logger.info(timesW + "W\t");
				}
			}
			bf.close();
			fr.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	private static BrightkiteDataPoint parseBrightJson(String str) {
		String[] splits =  str.split("\t");
		try {
			return new BrightkiteDataPoint(Double.parseDouble(splits[2]), Double.parseDouble(splits[3]), Long.parseLong(splits[1].substring(0, 4)), Long.parseLong(splits[1].substring(5, 7)), Long.parseLong(splits[1].substring(8, 10)), Long.parseLong(splits[1].substring(11, 13)));
		}
		catch (Exception e) {
			return null;
		}		
	}
}
