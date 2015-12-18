package mahout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.LogisticModelParameters;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.RecordFactory;
import org.apache.mahout.classifier.sgd.RunLogistic;
import org.apache.mahout.classifier.sgd.TrainLogistic;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

import com.google.common.io.Resources;

public class LostTrain {
	private static final String COLUMN_SPLIT = "\t";

	private static String inputFile;
	private static String outputFile;
	private static LogisticModelParameters lmp;
	private static int passes;
	private static boolean scores;
	private static OnlineLogisticRegression model;

	private static boolean parseArgs(String[] args) {
		DefaultOptionBuilder builder = new DefaultOptionBuilder();

		Option help = builder.withLongName("help").withDescription("print this list").create();

		Option quiet = builder.withLongName("quiet").withDescription("be extra quiet").create();

		ArgumentBuilder argumentBuilder = new ArgumentBuilder();
		Option scores = builder.withLongName("scores")
				.withArgument(
						argumentBuilder.withName("scores")
								.withDefault("false")
								.create())
				.withDescription("output score diagnostics during training")
				.create();

		Option inputFile = builder.withLongName("input")
				.withRequired(true)
				.withArgument(argumentBuilder.withName("input").withMaximum(1).create())
				.withDescription("where to get training data")
				.create();

		Option outputFile = builder.withLongName("output")
				.withRequired(true)
				.withArgument(argumentBuilder.withName("output").withMaximum(1).create())
				.withDescription("where to get training data")
				.create();

		Option predictors = builder.withLongName("predictors")
				.withRequired(true)
				.withArgument(argumentBuilder.withName("p").create())
				.withDescription("a list of predictor variables")
				.create();

		Option types = builder.withLongName("types")
				.withRequired(true)
				.withArgument(argumentBuilder.withName("t").create())
				.withDescription("a list of predictor variable types (numeric, word, or text)")
				.create();

		Option target = builder.withLongName("target")
				.withRequired(true)
				.withArgument(argumentBuilder.withName("target").withMaximum(1).create())
				.withDescription("the name of the target variable")
				.create();

		Option features = builder.withLongName("features")
				.withArgument(
						argumentBuilder.withName("numFeatures")
								.withDefault("1000")
								.withMaximum(1).create())
				.withDescription("the number of internal hashed features to use")
				.create();

		Option passes = builder.withLongName("passes")
				.withArgument(
						argumentBuilder.withName("passes")
								.withDefault("2")
								.withMaximum(1).create())
				.withDescription("the number of times to pass over the input data")
				.create();

		Option lambda = builder.withLongName("lambda")
				.withArgument(argumentBuilder.withName("lambda").withDefault("1e-4").withMaximum(1).create())
				.withDescription("the amount of coefficient decay to use")
				.create();

		Option rate = builder.withLongName("rate")
				.withArgument(argumentBuilder.withName("learningRate").withDefault("1e-3").withMaximum(1).create())
				.withDescription("the learning rate")
				.create();

		Option noBias = builder.withLongName("noBias")
				.withDescription("don't include a bias term")
				.create();

		Option targetCategories = builder.withLongName("categories")
				.withRequired(true)
				.withArgument(argumentBuilder.withName("number").withMaximum(1).create())
				.withDescription("the number of target categories to be considered")
				.create();

		Group normalArgs = new GroupBuilder()
				.withOption(help)
				.withOption(quiet)
				.withOption(inputFile)
				.withOption(outputFile)
				.withOption(target)
				.withOption(targetCategories)
				.withOption(predictors)
				.withOption(types)
				.withOption(passes)
				.withOption(lambda)
				.withOption(rate)
				.withOption(noBias)
				.withOption(features)
				.withOption(scores)
				.create();

		Parser parser = new Parser();
		parser.setHelpOption(help);
		parser.setHelpTrigger("--help");
		parser.setGroup(normalArgs);
		parser.setHelpFormatter(new HelpFormatter(" ", "", " ", 130));
		CommandLine cmdLine = parser.parseAndHelp(args);

		if (cmdLine == null) {
			return false;
		}

		LostTrain.inputFile = getStringArgument(cmdLine, inputFile);
		LostTrain.outputFile = getStringArgument(cmdLine, outputFile);

		List<String> typeList = new ArrayList<>();
		for (Object x : cmdLine.getValues(types)) {
			typeList.add(x.toString());
		}

		List<String> predictorList = new ArrayList<>();
		for (Object x : cmdLine.getValues(predictors)) {
			predictorList.add(x.toString());
		}

		lmp = new LogisticModelParameters();
		lmp.setTargetVariable(getStringArgument(cmdLine, target));
		lmp.setMaxTargetCategories(getIntegerArgument(cmdLine, targetCategories));
		lmp.setNumFeatures(getIntegerArgument(cmdLine, features));
		lmp.setUseBias(!getBooleanArgument(cmdLine, noBias));
		lmp.setTypeMap(predictorList, typeList);

		lmp.setLambda(getDoubleArgument(cmdLine, lambda));
		lmp.setLearningRate(getDoubleArgument(cmdLine, rate));

		LostTrain.scores = getBooleanArgument(cmdLine, scores);
		LostTrain.passes = getIntegerArgument(cmdLine, passes);

		return true;
	}

	private static String getStringArgument(CommandLine cmdLine, Option inputFile) {
		return (String) cmdLine.getValue(inputFile);
	}

	private static boolean getBooleanArgument(CommandLine cmdLine, Option option) {
		return cmdLine.hasOption(option);
	}

	private static int getIntegerArgument(CommandLine cmdLine, Option features) {
		return Integer.parseInt((String) cmdLine.getValue(features));
	}

	private static double getDoubleArgument(CommandLine cmdLine, Option op) {
		return Double.parseDouble((String) cmdLine.getValue(op));
	}

	private static double predictorWeight(OnlineLogisticRegression lr, int row, RecordFactory csv, String predictor) {
		double weight = 0;
		for (Integer column : csv.getTraceDictionary().get(predictor)) {
			weight += lr.getBeta().get(row, column);
		}
		return weight;
	}

	public static OnlineLogisticRegression getModel() {
		return model;
	}

	public static LogisticModelParameters getParameters() {
		return lmp;
	}

	static BufferedReader open(String inputFile) throws IOException {
		InputStream in;
		try {
			in = Resources.getResource(inputFile).openStream();
		} catch (IllegalArgumentException e) {
			in = new FileInputStream(new File(inputFile));
		}
		return new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
	}

	/***
	 * 获取指定目录下的所有的文件（不包括文件夹），采用了递归
	 * 
	 * @param obj
	 * @return
	 */
	public static ArrayList<File> listFiles(Object obj) {
		File directory = null;
		if (obj instanceof File) {
			directory = (File) obj;
		} else {
			directory = new File(obj.toString());
		}
		ArrayList<File> files = new ArrayList<File>();
		if (directory.isFile()) {
			files.add(directory);
			return files;
		} else if (directory.isDirectory()) {
			File[] fileArr = directory.listFiles();
			for (File f : fileArr) {
				files.addAll(listFiles(f));
			}
		}
		return files;
	}

	public static void main(String[] args) {

		String[] tranArgs = { "--input", "D:/bigdata/data/lost"
				, "--output", "D:/bigdata/lr_model_param.txt"
				, "--target", "color"
				, "--categories", "2"
				, "--predictors", "x", "y", "a", "b", "c"
				, "--types", "numeric", "numeric", "numeric", "numeric", "numeric"
				, "--features", "20"
				, "--passes", "1"
				, "--rate", "50"
				, "--scores", "true" };
		for (String s : tranArgs)
			System.out.println(s);

		// String firstLine = "\"x\"\t\"y\"\t\"a\"\t\"b\"\t\"c\"\t\"color\"";
		String firstLine = "x\ty\ta\tb\tc\tcolor";

		if (!parseArgs(tranArgs)) {
			System.out.println("parseArgs failed");
			return;
		}

		PrintWriter output = new PrintWriter(new OutputStreamWriter(System.out, Charsets.UTF_8), true);

		// OnlineLogisticRegression lr = new OnlineLogisticRegression(2,
		// index.length, new L1())
		// .lambda(1e-4)// 先验分布的加权因子
		// .learningRate(50)// 1e-3
		// .alpha(1 - 1.0e-3);// 学习率的指数衰减率

		OnlineLogisticRegression lr = lmp.createRegression();
		CsvRecordFactory csv = lmp.getCsvRecordFactory();
		TxtRecordFactory txt = new TxtRecordFactory(lmp.getTargetVariable(), lmp.getTypeMap())
				.maxTargetValue(lmp.getMaxTargetCategories())
				.includeBiasTerm(lmp.useBias());
		if (csv.getTargetCategories() != null) {
			txt.defineTargetCategories(csv.getTargetCategories());
		}

		int passes = 100;

		List<File> files = listFiles(inputFile);

		for (int pass = 0; pass < passes; pass++) {

			txt.firstLine(firstLine);
			double logPEstimate = 0;
			int samples = 0;

			for (File file : files) {
				LineIterator it = null;
				try {
					it = FileUtils.lineIterator(file);
					while (it.hasNext()) {
						String line = it.nextLine();
						Vector input = new RandomAccessSparseVector(lmp.getNumFeatures());
						int targetValue = txt.processLine(line, input);
						System.out.println("line  : " + line);
						System.out.println("vector: " + input);

						// check performance while this is still news
						double logP = lr.logLikelihood(targetValue, input);
						if (!Double.isInfinite(logP)) {
							if (samples < 20) {
								logPEstimate = (samples * logPEstimate + logP) / (samples + 1);
							} else {
								logPEstimate = 0.95 * logPEstimate + 0.05 * logP;
							}
							samples++;
						}
						double p = lr.classifyScalar(input);
						if (scores) {
							output.printf(Locale.ENGLISH, "%10d %2d %10.2f %2.4f %10.4f %10.4f%n",
									samples, targetValue, lr.currentLearningRate(), p, logP, logPEstimate);
						}

						// now update model
						lr.train(targetValue, input);
					}
				} catch (IOException e) {
					System.out.println("!!!file read failed:" + e);
				} finally {
					if (it != null)
						LineIterator.closeQuietly(it);
				}// while lines
			}// for files
		}// for passes

		System.out.println(lr.getBeta());

		try (DataOutputStream modelOutput = new DataOutputStream(new FileOutputStream(outputFile))) {
			lr.write(modelOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}

		output.println(lmp.getNumFeatures());
		output.println(lmp.getTargetVariable() + " ~ ");
		String sep = "";
		for (String v : csv.getTraceDictionary().keySet()) {
			double weight = predictorWeight(lr, 0, csv, v);
			if (weight != 0) {
				output.printf(Locale.ENGLISH, "%s%.3f*%s", sep, weight, v);
				sep = " + ";
			}
		}
		output.printf("%n");
		model = lr;
		for (int row = 0; row < lr.getBeta().numRows(); row++) {
			for (String key : csv.getTraceDictionary().keySet()) {
				double weight = predictorWeight(lr, row, csv, key);
				if (weight != 0) {
					output.printf(Locale.ENGLISH, "%20s %.5f%n", key, weight);
				}
			}
			for (int column = 0; column < lr.getBeta().numCols(); column++) {
				output.printf(Locale.ENGLISH, "%15.9f ", lr.getBeta().get(row, column));
			}
			output.println();
		}

		//
		// String[] runArgs = new String[]{"--input", "donut-test.csv"
		// , "--model", "model"
		// , "--scores"
		// , "--auc"
		// , "--confusion"};
		// RunLogistic.main(runArgs);

		// 计算准确率和覆盖率
		/*
		 * int all = 0, lost = 0, preLost = 0, right = 0;
		 * for (File file : files) {
		 * LineIterator it = null;
		 * try {
		 * it = FileUtils.lineIterator(file);
		 * while (it.hasNext()) {
		 * String line = it.nextLine();
		 * String[] columns = line.split(COLUMN_SPLIT);
		 * Vector input = new RandomAccessSparseVector(index.length);
		 * int targetValue = Integer.parseInt(columns[target]);
		 * if (targetValue != 1)
		 * targetValue = 0;
		 * for (int i = 0; i < index.length - 1; i++) {
		 * input.set(i, Double.parseDouble(columns[index[i]]) / 1.3);
		 * }
		 * double score = lr.classifyScalar(input);
		 * int predictValue = score > 0.5 ? 1 : 0;
		 * all++;
		 * if (targetValue == 1) {
		 * lost++;
		 * if (predictValue == 1) {
		 * preLost++;
		 * }
		 * }
		 * 
		 * if (predictValue == targetValue) {
		 * right++;
		 * }
		 * }// while
		 * } catch (IOException e) {
		 * System.out.println("!!!file read failed:" + e);
		 * } finally {
		 * if (it != null)
		 * LineIterator.closeQuietly(it);
		 * }
		 * }// for files
		 * 
		 * double coverRate = (double) preLost / lost;
		 * double rightRate = (double) right / all;
		 * 
		 * output.printf(Locale.ENGLISH,
		 * "passes:%d cover rate:%2.4f   right rate:%2.4f %n",
		 * passes, coverRate, rightRate);
		 */

	}// main

}
