package yefancy.map.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sun.istack.internal.logging.Logger;

import yefancy.edgecubes.CuboidV2;
import yefancy.edgecubes.EdgecubeV2;
import yefancy.map.server.listener.TweetsDataCubeListener;

public class AggSplitServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger(TweetsDataCubeListener.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
//		PrintWriter out = resp.getWriter();
//		String type = req.getParameter("type");
//		EdgecubeV2<CuboidV2> cube = (EdgecubeV2<CuboidV2>) TweetsDataCubeListener.cube;
//		if(type.equals("receive")) {
//			logger.info("Recive aggSplit");
//			TweetsDataCubeListener.queryEngine.printRecords();
//			Gson gson = new Gson();
//			List<List<Integer>> split = new ArrayList<List<Integer>>();
//			for(Set<Integer> set : cube.getAggSplit())
//				split.add(new ArrayList<>(set));
//			out.write(gson.toJson(split));
//		} else if (type.equals("update")) {
//			logger.info("Update aggSplit: " + req.getParameter("split"));
//			Gson gson = new Gson();
//			JsonParser jsonParser = new JsonParser();
//			List<Set<Integer>> split = new ArrayList<Set<Integer>>();
//			JsonArray arry = jsonParser.parse(req.getParameter("split")).getAsJsonArray();
//			for(int i = 0; i < arry.size(); i++) {
//				JsonArray arry2 = arry.get(i).getAsJsonArray();
//				Set<Integer> set=new HashSet<Integer>();
//				for(int j = 0; j < arry2.size(); j++) 
//					set.add(arry2.get(j).getAsInt());
//				split.add(set);
//			}
//			cube.updateAggSplit(split);
//			out.write("finish!");
//		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	
}
