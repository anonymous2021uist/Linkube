package yefancy.map.server.servlet;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.sun.istack.internal.logging.Logger;

import vmarcinko.nanocubes.QueryEngineSM;
import yefancy.edgecubes.utils.Point2D;
import yefancy.edgecubes.utils.Point3D;
import yefancy.map.server.listener.TweetsDataCubeListenerD;
import yefancy.map.utils.Statistic;

public class QueryServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger(TweetsDataCubeListenerD.class);
	private List<Statistic> stats = new ArrayList<Statistic>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		long mem = 1;//GowallaTest.printMemoryState();
		long st = System.currentTimeMillis();
		logic(req, resp);
		long qt =System.currentTimeMillis() - st;
//		if (Integer.parseInt(req.getParameter("type")) == 0) {
//			int from = Integer.parseInt(req.getParameter("from"));
//			int to = Integer.parseInt(req.getParameter("to"));
//			stats.add(new Statistic(qt, mem, Double.parseDouble(req.getParameter("ltLat")), 
//					Double.parseDouble(req.getParameter("ltLon")), 
//					Double.parseDouble(req.getParameter("rbLat")), 
//					Double.parseDouble(req.getParameter("rbLon")), 
//					Integer.parseInt(req.getParameter("zoom")), 
//					Integer.parseInt(req.getParameter("decive")), 
//					from, 
//					to));
//			System.out.println(stats.size());
//		}
//		if(stats.size() == 200) {
//			Gson gson = new Gson();
//			String fileName="F:\\Datasets\\stats.txt";
//            try{
//                FileWriter writer=new FileWriter(fileName);
//                writer.write(gson.toJson(stats));
//                writer.close();
//            } catch (IOException e){
//                e.printStackTrace();
//            }
//            System.out.println("finish save");
//		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
	
	private void logic(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		logger.info("Recive Query:" + req.getQueryString());
		PrintWriter out = resp.getWriter();
		double ltLat = Double.parseDouble(req.getParameter("ltLat"));
		double ltLon = Double.parseDouble(req.getParameter("ltLon"));
		double rbLat = Double.parseDouble(req.getParameter("rbLat"));
		double rbLon = Double.parseDouble(req.getParameter("rbLon"));
		int zoom = Integer.parseInt(req.getParameter("zoom"));
		int decive = Integer.parseInt(req.getParameter("decive"));
		List<List<Long>> cate = new ArrayList<List<Long>>();
		cate.add(new ArrayList<Long>());
		if(decive == 1)
			cate.get(0).add(0L);
		else if(decive == 2)
			cate.get(0).add(1L);
		
		int type = Integer.parseInt(req.getParameter("type"));
		if (type == 0) {
			int from = Integer.parseInt(req.getParameter("from"));
			int to = Integer.parseInt(req.getParameter("to"));
//			int from = 20131231;
//			int to = 20160101;
			
			List<Point3D> nodes = TweetsDataCubeListenerD.queryEngine.queryMap(ltLat, ltLon, rbLat, rbLon, zoom, cate, new Date(from/10000, from%10000/100, from%100), new Date(to/10000, to%10000/100, to%100));
			Gson gson = new Gson();
			out.write(gson.toJson(nodes));
			if(TweetsDataCubeListenerD.queryEngine instanceof QueryEngineSM) {
				Thread thread1 = new Thread() {
					public void run() {			
						long st = System.currentTimeMillis();
						((QueryEngineSM) TweetsDataCubeListenerD.queryEngine).updateAgg();
						System.out.println("updateAgg finish:" + (System.currentTimeMillis() - st) / 1000);
					}
				};
				thread1.start();
			}
		} else if (type == 1) {
			int timeLevel = Integer.parseInt(req.getParameter("timeLevel"));
			
			List<Point2D> nodes = TweetsDataCubeListenerD.queryEngine.queryTime(ltLat, ltLon, rbLat, rbLon, zoom, cate, timeLevel);
			Gson gson = new Gson();
			out.write(gson.toJson(nodes));
		}
		
		
//		if(TweetsDataCubeListenerD.queryEngine instanceof QueryEngineSM)
//			((QueryEngineSM) TweetsDataCubeListenerD.queryEngine).updateAgg();
		
//		Thread thread1 = new Thread() {
//			public void run() {			
//				logger.info("DataCube updateAgg");
//				long st = System.currentTimeMillis();
//				updateAgg();
//				System.out.println("updateAgg finish:" + (System.currentTimeMillis() - st) / 1000);
//			}
//		};
//		thread1.start();
	}
	
//	private void updateAgg() {
//		List<Integer> topK = new ArrayList<Integer>();
//		long[] qt = TweetsDataCubeListener.queryEngine.queryTime;
//		for (int i = 0; i < qt.length; i++) {
//			if (qt[i] == 0)
//				continue;
//			if (topK.size() < TweetsDataCubeListener.levelSize) {			
//				int base = topK.size();
//				for (int j = 0; j < base; j++) {
//					if (qt[i] > qt[topK.get(j)]) {
//						topK.add(j, i);
//						break;
//					}
//
//				}
//				if (topK.size() == base)
//					topK.add(topK.size(), i);
//			}				
//			else
//				for (int j = 0; j < TweetsDataCubeListener.levelSize; j++) {
//					if (qt[i] > qt[topK.get(j)]) {
//						topK.add(j, i);
//						break;
//					}
//
//				}
//		}
//		List<Set<Integer>> split = new ArrayList<Set<Integer>>();
//		Set<Integer> s1 = new HashSet<Integer>();
//		Set<Integer> s2 = new HashSet<Integer>();
//		s1.add(25);
//		s2.add(0);
//		s2.add(1);
//		s2.add(2);
//		s2.add(3);
//		split.add(s1);
//		split.add(s2);
//		for (int i = 0; i < topK.size() && i < TweetsDataCubeListener.levelSize; i++)
//			s1.add(topK.get(i));
//		((EdgecubeV2) (TweetsDataCubeListener.cube)).updateAggSplit(split);
//	}
	
	private Date toDate(int time) {
		return new Date(time / 10000, (time % 10000) / 100, time % 100);
	}
}
