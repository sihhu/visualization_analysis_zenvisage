package edu.uiuc.zenvisage.service.utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import edu.uiuc.zenvisage.model.BaselineQuery;
import edu.uiuc.zenvisage.model.ZvQuery;
import edu.uiuc.zenvisage.service.Representative;
import edu.uiuc.zenvisage.service.cluster.Clustering;
import edu.uiuc.zenvisage.service.cluster.RepresentativeTrend;
import edu.uiuc.zenvisage.service.distance.Distance;
import edu.uiuc.zenvisage.service.utility.ChartOutput;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.BiMap;

/**
 *
 */
public class ChartOutput {
	public Result finalOutput;
	public ZvQuery args;
	public BiMap<Float, String> xMap;
	
	/**
	 * @param finalOutput
	 * @param args
	 */
	public ChartOutput(Result finalOutput, ZvQuery args, BiMap<String, Float> xMap) {
		this.finalOutput = finalOutput;
		this.args = args;
		this.xMap = xMap.inverse();
	}
		
	/**
	 * @param result
	 * @param orig
	 * @param orders
	 * @param mappings
	 * @param args
	 * @param finalOutput
	 * @throws JsonProcessingException
	 */
	public void chartOutput(List<double[][]> output,List<LinkedHashMap<String,LinkedHashMap<Float,Float>>> orig,List<Integer> orders,ArrayList<String> mappings, List<BiMap<Float,String>> xMaps, ZvQuery args, Result finalOutput) throws JsonProcessingException{
		if (args.outlierCount==0)
			args.setOutlierCount(4);
		int outputLength = args.outlierCount;
		if(output != null && !output.isEmpty()){
			outputLength = output.get(0).length;
		}
		for(int i = 0; i < Math.min(outputLength, args.outlierCount); i++) {
			// initialize a new chart
			int j = 0;
			for (double[][] result : output) {
				Chart chartOutput = new Chart();
				chartOutput.setxType((i+1)+" : "+mappings.get(orders.get(i)));
				chartOutput.setyType(args.getSketchPoints()[j].aggrFunc+"("+args.getSketchPoints()[j].yAxis+")");
				// fill in chart data
				String key = mappings.get(orders.get(i));
				LinkedHashMap<Float,Float> points = orig.get(j).get(key);
				if (points == null) continue;
				int c = 0;
				for(Float k : points.keySet()) {
					chartOutput.xData.add(xMaps.get(j%2).get(k));
					chartOutput.yData.add(Double.toString(result[orders.get(i)][c]));
					c++;
				}
				finalOutput.outputCharts.add(chartOutput);
				j++;
			}
		}

		return;	
	}
	
	
	public void chartOutput(List<RepresentativeTrend> representativeTrends,LinkedHashMap<String,LinkedHashMap<Float,Float>> orig, ZvQuery args, Result finalOutput) throws JsonProcessingException{
			
		for(int i = 0; i < representativeTrends.size() - 1; i++) {
			// initialize a new chart
			Chart chartOutput = new Chart();
			RepresentativeTrend repTrend = representativeTrends.get(i);
			chartOutput.setxType((i+1)+" : "+repTrend.getKey());
			chartOutput.setyType(args.aggrFunc+"("+args.yAxis+")");
			// fill in chart data
			LinkedHashMap<Float,Float> points = orig.get(repTrend.getKey());
			int c = 0;
			double[] p = repTrend.getP();
			for(Float k : points.keySet()) {
				chartOutput.xData.add(this.xMap.get(k));
				chartOutput.yData.add(Double.toString(p[c]));
				c++;
			}
			chartOutput.count = repTrend.getSimilarTrends();
			finalOutput.outputCharts.add(chartOutput);
		}

		return;	
	}
	
	// all baseline one time stuff...
	public void baselineOutput(List<LinkedHashMap<String,LinkedHashMap<Float,Float>>> output, BaselineQuery bq, Result finalOutput) {
		for (String zAxis : output.get(0).keySet()) {
			boolean filtered = false;
			Chart[] outputArray = new Chart[output.size()];
			for (int index = 0; index < output.size(); index++) {
				LinkedHashMap<Float,Float> trend = output.get(index).get(zAxis);
				// can add more logic here -- filter some trends
				outputArray[index] = new Chart();
				outputArray[index].setxType(zAxis);
				outputArray[index].setyType(bq.aggrFunc+"("+bq.yAxis.get(index)+")");
				List<String> yOperators;
				List<Float> yValues;
				if (index == 0) {
					yOperators = bq.y1Operator;
					yValues = bq.y1Value;
				}
				else {
					yOperators = bq.y2Operator;
					yValues = bq.y2Value;
				}
				for (Float k : trend.keySet()) {
					for (int i = 0; i < bq.xOperator.size(); i++) {
						Operator xOperator = Operator.getValue((bq.xOperator.get(i)));
						if (xOperator.filter(bq.xValue.get(i), k)) {
							Operator yOperator = Operator.getValue(yOperators.get(i));
							//System.out.println(trend.get(k)+" > "+yValues.get(i));
							//System.out.println(yOperator.filter(yValues.get(i), trend.get(k)));
							if (!yOperator.filter(yValues.get(i), trend.get(k))) {
								filtered = true;
								break;
							}
						}
					}
					if (filtered) break;
					outputArray[index].xData.add(Float.toString(k));
					outputArray[index].yData.add(Float.toString(trend.get(k)));
				}
				if (filtered) break;
			}
			
			if (!filtered) {
				for (int i = 0; i < output.size(); i++)
				finalOutput.outputCharts.add(outputArray[i]);
			}
		}
	}
	
	public enum Operator
	{
		EQUAL("=") {
			@Override public boolean filter(float constraint, float value) {
				return (int) constraint == (int) value;
			}
		},
		GREATER_THAN(">") {
			@Override public boolean filter(float constraint, float value) {
				return value > constraint;
			}
		}, 
		GREATER_THAN_OR_EQUAL(">=") {
			@Override public boolean filter(float constraint, float value) {
				return value >= constraint;
			}
		}, 
		LESS_THAN("<") {
			@Override public boolean filter(float constraint, float value) {
				return value < constraint;
			}
		}, 
		LESS_THAN_OR_EQUAL("<=") {
			@Override public boolean filter(float constraint, float value) {
				return value <= constraint;
			}
		}, 
		NOT_EQUAL("!=") {
			@Override public boolean filter(float constraint, float value) {
				return (int) constraint != (int) value;
			}
		};
	    // You'd include other operators too...

	    private final String text;

	    private Operator(String text) {
	        this.text = text;
	    }

	    // Yes, enums *can* have abstract methods. This code compiles...
	    public abstract boolean filter(float constraint, float value);

	    @Override public String toString() {
	        return text;
	    }
	    
	    public static Operator getValue(String text) {
	    	for (Operator op : Operator.values()) {
	    		if (op.text.equals(text)) {
	    			return op;
	    		}
	    	}
	    	return null;
	    }
	}
	
}