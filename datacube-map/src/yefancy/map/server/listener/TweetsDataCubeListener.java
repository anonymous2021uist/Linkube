package yefancy.map.server.listener;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.sun.istack.internal.logging.Logger;

import FlightTest.FlightDataPoint;
import TweetsTest.TweetDataPoint;
import vmarcinko.nanocubes.LabellingFn;
import vmarcinko.nanocubes.Nanocube;
import vmarcinko.nanocubes.Schema;
import vmarcinko.nanocubes.TimeLabellingFn;
import yefancy.cube.interfaces.ICube;
import yefancy.cube.interfaces.IDataPoint;
import yefancy.edgecubes.CuboidV2;
import yefancy.edgecubes.EdgecubeV2;
import yefancy.edgecubes.QueryEngine;

public class TweetsDataCubeListener implements ServletContextListener{
	private static Logger logger = Logger.getLogger(TweetsDataCubeListener.class);
	
	private static Map<String, Double> lat = new HashMap<String, Double>();
	private static Map<String, Double> lon = new HashMap<String, Double>();
	
//	int[][] split = {{10, 12, 25}, {1}, {0, 1, 2, 3}};
	
	int[][] split = {{12, 24}, {1}, {0, 1, 2, 3}};
	public static QueryEngine<CuboidV2> queryEngine;
	public final static int levelSize = 4;
	public static ICube<CuboidV2> cube;
	int times = 0;
	
//	public static Nanocube<TweetDataPoint> cube;
//	public static QueryEngineSM queryEngine;
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
		preAirport();
		cube = new EdgecubeV2<CuboidV2>(CuboidV2.class, split) ;
		queryEngine = new QueryEngine<CuboidV2>(((EdgecubeV2)cube).getRoot(), CuboidV2.class);
		//cube = new Nanocube();
		t1 = new Thread(()->{
			for (int i = 1987; i <= 2008; i++)
				init("F:\\Datasets\\Flight\\" + i + ".csv");
			//queryEngine = new QueryEngineSM(cube);
			logger.info("DataCube : context Initialized");
		});
        t1.start();
	}
	
	private void initSM(String filePath) {
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
        //cube = new Nanocube<>(schema, timeLabellingFn);
        
		
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
					cube.insert(dp);
				}
				if (times%1000000 == 0)
					logger.info(times/10000 + "W\t");
			}
			bf.close();
			fr.close();
			csvReader.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init(String filePath) {
		logger.info(filePath);
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
					cube.insert(dp);
				}
				if (times%1000000 == 0)
					logger.info(times/10000 + "W\t");
			}
			bf.close();
			fr.close();
			csvReader.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
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
}
