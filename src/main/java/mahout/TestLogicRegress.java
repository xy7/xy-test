package mahout;

import org.apache.mahout.classifier.sgd.RunLogistic;
import org.apache.mahout.classifier.sgd.TrainLogistic;

public class TestLogicRegress {

	public static void main(String[] args) throws Exception {

		String[] tranArgs = new String[]{"--input", "donut.csv"
				,  "--output", "model"
				, "--target", "color"
				, "--categories", "2"
				, "--predictors", "x", "y", "a", "b", "c"
				, "--types", "numeric", "numeric", "numeric", "numeric", "numeric"
				, "--features", "20"
				, "--passes", "100"
				, "--rate", "50"};
		for(String s:tranArgs)
			System.out.println(s);
		TrainLogistic.main(tranArgs);
		
		String[] runArgs = new String[]{"--input", "donut-test.csv"
				, "--model", "model"
				, "--scores"
				, "--auc"
				, "--confusion"};
		RunLogistic.main(runArgs);
	}
	
}
