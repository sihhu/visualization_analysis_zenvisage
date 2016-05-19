package edu.uiuc.zenvisage.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import edu.uiuc.zenvisage.data.ScatterResult;
import edu.uiuc.zenvisage.data.ScatterResult.*;
import edu.uiuc.zenvisage.model.ScatterPlotQuery;
import edu.uiuc.zenvisage.service.utility.Chart;
import edu.uiuc.zenvisage.service.utility.Result;

/**
 * @author xiaofo
 *
 */
public class ScatterRep {

	public static void generateAnalysis(Map<String, ScatterResult> output, ScatterPlotQuery q, Result finalOutput) {
		List<ScatterResult> datas = new ArrayList<ScatterResult>(output.values());
		generateCharts(datas, q.numOfResults, q.yAxis, finalOutput);
	}
	
	public static void generateCharts(List<ScatterResult> datas, int numOfResult, String yAxis, Result finalOutput) {
		int len = Math.min(datas.size(), numOfResult);
		for (int i = 0; i < len; i++) {
			Chart chartOutput = new Chart();
			ScatterResult data = datas.get(i);
			System.out.println(data.name + Integer.toString(data.count / data.points.size()));
			chartOutput.setxType((i+1)+" : "+data.name);
			chartOutput.setyType(yAxis);
			for (Tuple point : data.points) {
				chartOutput.xData.add(Double.toString(point.x));
				chartOutput.yData.add(Double.toString(point.y));
			}
			finalOutput.outputCharts.add(chartOutput);
		}
	}
}