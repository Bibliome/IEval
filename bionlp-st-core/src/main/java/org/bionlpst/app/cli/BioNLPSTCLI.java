package org.bionlpst.app.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.app.Task;
import org.bionlpst.app.source.CorpusSource;
import org.bionlpst.app.source.DirectoryCorpusSource;
import org.bionlpst.app.source.ZipFileCorpusSource;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.parser.bionlpst.BioNLPSTParser;
import org.bionlpst.evaluation.AnnotationEvaluation;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.evaluation.Measure;
import org.bionlpst.evaluation.MeasureResult;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.evaluation.Scoring;
import org.bionlpst.evaluation.ScoringResult;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;
import org.bionlpst.util.message.CheckMessage;
import org.bionlpst.util.message.CheckMessageLevel;

public class BioNLPSTCLI {
	private static final Location COMMAND_LINE_LOCATION = new Location("", -1);
	private final CheckLogger logger = new CheckLogger();
	private String taskName = null;
	private String set = null;
	private CorpusSource referenceSource = null;
	private CorpusSource predictionSource = null;
	private boolean detailedEvaluation = false;
	private boolean alternateScores = false;
	private boolean forceEvaluation = false;
	private Action action = Action.EVALUATE;

	private static enum Action {
		EVALUATE,
		CHECK,
		HELP,
		LIST_TASKS;
	}
	
	public static void main(String[] args) throws Exception {
		BioNLPSTCLI cli = new BioNLPSTCLI();
		cli.parseArgs(args);
		cli.run();
	}
	
	private void run() throws Exception {
		switch (action) {
			case EVALUATE: {
				doCheckAndEvaluate(true);
				exit(0);
				break;
			}
			case CHECK: {
				doCheckAndEvaluate(false);
				exit(0);
				break;
			}
			case HELP: {
				doHelp();
				exit(0);
				break;
			}
			case LIST_TASKS: {
				if (taskName == null) {
					doListTasks();
				}
				else {
					Task task = getSelectedTask();
					if (task != null) {
						displayTask(task);
					}
				}
				exit(0);
				break;
			}
		}
	}
	
	private static void doHelp() {
		try (InputStream is = BioNLPSTCLI.class.getResourceAsStream("BioNLPSTCLIHelp.txt")) {
			Reader r = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(r);
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	private void doCheckAndEvaluate(boolean evaluate) throws Exception {
		Task task = getSelectedTask();
		if (task == null) {
			exit(1);
		}
		logger.information(COMMAND_LINE_LOCATION, "loading corpus and reference data");
		Corpus corpus = loadReference(task, evaluate);
		flushLogger();

		logger.information(COMMAND_LINE_LOCATION, "loading prediction data");
		BioNLPSTParser.getPredictions(logger, predictionSource, corpus);
		flushLogger();
		
		logger.information(COMMAND_LINE_LOCATION, "resolving references");
		corpus.resolveReferences(logger);
		flushLogger();

		logger.information(COMMAND_LINE_LOCATION, "checking data");
		Task.checkParsedPredictions(logger, corpus, predictionSource.getName());
		task.checkSchema(logger, corpus);
		CheckMessageLevel highestLevel = logger.getHighestLevel();
		flushLogger();
		if (evaluate) {
			if (highestLevel != CheckMessageLevel.INFORMATION) {
				if (forceEvaluation) {
					logger.serious(COMMAND_LINE_LOCATION, "I will evaluate this garbage because you made me to");
					flushLogger();
				}
				else {
					logger.serious(COMMAND_LINE_LOCATION, "I refuse to evaluate this garbage");
					flushLogger();
					exit(1);
				}
			}
			doEvaluate(task, corpus);
		}
		else {
			if (highestLevel != CheckMessageLevel.INFORMATION) {
				exit(1);
			}
		}
	}

	private void doEvaluate(Task task, Corpus corpus) {
		logger.information(COMMAND_LINE_LOCATION, "evaluation");
		flushLogger();
		if (detailedEvaluation) {
			for (Document doc : corpus.getDocuments()) {
				doEvaluateDocument(task, doc);
			}
		}
		doEvaluateCorpus(task, corpus);
	}

	private void doEvaluateCorpus(Task task, Corpus corpus) {
		System.out.println("Evaluation for corpus " + getCorpusName());
		if (alternateScores) {
			Map<String,EvaluationResult<Annotation>> evalMap = task.evaluate(logger, corpus, false, null/*boostrap*/);
			for (EvaluationResult<Annotation> eval : evalMap.values()) {
				displayEvaluationResult(eval, false);
			}
		}
		else {
			EvaluationResult<Annotation> eval = task.evaluateMain(logger, corpus, false, null/*boostrap*/);
			displayEvaluationResult(eval, false);
		}
	}
	
	private String getCorpusName() {
		if (referenceSource != null) {
			return referenceSource.getName();
		}
		return set;
	}

	private void doEvaluateDocument(Task task, Document doc) {
		System.out.println("Evaluation for document " + doc.getId());
		if (alternateScores) {
			Map<String,EvaluationResult<Annotation>> evalMap = task.evaluate(logger, doc, true, null/*boostrap*/);
			for (EvaluationResult<Annotation> eval : evalMap.values()) {
				displayEvaluationResult(eval, detailedEvaluation);
			}
		}
		else {
			EvaluationResult<Annotation> eval = task.evaluateMain(logger, doc, true, null/*boostrap*/);
			displayEvaluationResult(eval, detailedEvaluation);
		}
	}
	
	private static String getAnnotationId(Annotation ann) {
		if (ann == null) {
			return "--";
		}
		return ann.getId();
	}

	private static void displayEvaluationResult(EvaluationResult<Annotation> eval, boolean detailedEvaluation) {
		System.out.printf("  %s\n", eval.getEvaluation().getName());
		if (detailedEvaluation) {
			System.out.println("    Pairing");
			Similarity<Annotation> sim = eval.getEvaluation().getMatchingSimilarity();
			for (Pair<Annotation> pair : eval.getPairs()) {
				Annotation ref = pair.getReference();
				Annotation pred = pair.getPrediction();
				System.out.printf("      %-4s %-4s %.4f\n", getAnnotationId(ref), getAnnotationId(pred), pair.hasBoth() ? sim.compute(ref, pred) : 0);
			}
		}
		for (ScoringResult<Annotation> scoring : eval.getScoringResults()) {
			System.out.printf("    %s\n", scoring.getScoring().getName());
			for (MeasureResult measure : scoring.getMeasureResults()) {
				System.out.printf("      %s: %s\n", measure.getMeasure().getName(), measure.getResult());
			}
		}
	}

	@SuppressWarnings("static-method")
	private void doListTasks() throws Exception {
		Map<String,Task> taskMap = Task.loadTasks();
		for (Task task : taskMap.values()) {
			displayTask(task);
		}
	}
	
	private static void displayTask(Task task) {
		System.out.println(task.getName());
		for (AnnotationEvaluation eval : task.getEvaluations()) {
			System.out.println("  " + eval.getName());
			for (Scoring<Annotation> scoring : eval.getScorings()) {
				System.out.print("    " + scoring.getName() + ":");
				for (Measure measure : scoring.getMeasures()) {
					System.out.print(' ');
					System.out.print(measure.getName());
				}
				System.out.println();
			}
		}
	}
	
	private Corpus loadReference(Task task, boolean loadOutput) throws BioNLPSTException, IOException {
		if (referenceSource != null) {
			return BioNLPSTParser.getCorpusAndReference(logger, referenceSource, loadOutput);
		}
		switch (set) {
			case "train": return task.getTrainCorpus(logger);
			case "dev": return task.getDevCorpus(logger);
			case "train+dev": return task.getTrainAndDevCorpus(logger);
			case "test": {
				if (!task.hasTest()) {
					logger.serious(COMMAND_LINE_LOCATION, "test set is not available for " + task.getName());
					exit(1);
				}
				if (loadOutput && !task.isTestHasReferenceAnnotations()) {
					logger.serious(COMMAND_LINE_LOCATION, "test set has no reference annotations for " + task.getName());
					exit(1);
				}
				return task.getTestCorpus(logger);
			}
			default: {
				throw new RuntimeException();
			}
		}
	}
	
	private Task getSelectedTask() throws Exception {
		Map<String,Task> taskMap = Task.loadTasks();
		if (taskMap.containsKey(taskName)) {
			return taskMap.get(taskName);
		}
		logger.serious(COMMAND_LINE_LOCATION, "unknown task: " + taskName + " (" + Util.join(taskMap.keySet(), ", ") + ")");
		return null;
	}
	
	private void exit(int retval) {
		flushLogger();
		System.exit(retval);
	}
	
	private void flushLogger() {
		for (CheckMessage msg : logger.getMessages()) {
			System.err.println(msg.getCompleteMessage());
		}
		logger.clear();
	}
	
	private void parseArgs(String[] args) {
		List<String> argList = Arrays.asList(args);
		Iterator<String> argIt = argList.iterator();
		while (parseNext(argIt)) {}
		if (!finishArgs()) {
			exit(1);
		}
	}
	
	private boolean parseNext(Iterator<String> argsIt) {
		if (argsIt.hasNext()) {
			String opt = argsIt.next();
			switch (opt) {
				case "-task": {
					if (taskName != null) {
						logger.serious(COMMAND_LINE_LOCATION, "duplicate option: -task");
					}
					taskName = requireArgument(argsIt, opt, taskName);
					break;
				}
				case "-train":
				case "-dev":
				case "-train+dev":
				case "-test": {
					String set = opt.substring(1);
					if (this.set != null) {
						if (set.equals(this.set)) {
							logger.suspicious(COMMAND_LINE_LOCATION, "duplicate option: " + opt);
						}
						else {
							logger.suspicious(COMMAND_LINE_LOCATION, "conflicting options: -" + this.set + " " + opt);
						}
					}
					this.set = set;
					break;
				}
				case "-reference": {
					if (referenceSource != null) {
						logger.suspicious(COMMAND_LINE_LOCATION, "duplicate option: " +opt);
					}
					if (set != null) {
						logger.suspicious(COMMAND_LINE_LOCATION, "conflicting options: -" + this.set + " " + opt);
					}
					String arg = requireArgument(argsIt, opt, null);
					if (arg != null) {
						referenceSource = getSource(arg);
					}
					break;
				}
				case "-prediction": {
					if (predictionSource != null) {
						logger.suspicious(COMMAND_LINE_LOCATION, "duplicate option: " + opt);
					}
					String arg = requireArgument(argsIt, opt, null);
					if (arg != null) {
						predictionSource = getSource(arg);
					}
					break;
				}
				case "-detailed": {
					if (detailedEvaluation) {
						logger.suspicious(COMMAND_LINE_LOCATION, "duplicate option: " + opt);
					}
					detailedEvaluation = true;
					break;
				}
				case "-check": {
					action = Action.CHECK;
					break;
				}
				case "-help": {
					action = Action.HELP;
					break;
				}
				case "-list-tasks": {
					action = Action.LIST_TASKS;
					break;
				}
				case "-alternate": {
					alternateScores = true;
					break;
				}
				case "-force": {
					forceEvaluation = true;
					break;
				}
				default: {
					if (opt.charAt(0) == '-') {
						logger.serious(COMMAND_LINE_LOCATION, "unknown option: " + opt);
					}
					else {
						logger.serious(COMMAND_LINE_LOCATION, "junk argument: " + opt);
					}
					break;
				}
			}
			return true;
		}
		return false;
	}

	private String requireArgument(Iterator<String> argsIt, String opt, String defaultValue) {
		if (argsIt.hasNext()) {
			return argsIt.next();
		}
		logger.serious(COMMAND_LINE_LOCATION, opt + " requires argument");
		return defaultValue;
	}
	
	private static CorpusSource getSource(String arg) {
		File f = new File(arg);
		if (f.isDirectory()) {
			return new DirectoryCorpusSource(f);
		}
		return new ZipFileCorpusSource(f);
	}

	private boolean finishArgs() {
		boolean result = true;
		if (action == Action.LIST_TASKS || action == Action.HELP) {
			return result;
		}
		if (taskName == null) {
			logger.serious(COMMAND_LINE_LOCATION, "option -task is mandatory");
			result = false;
		}
		if (set == null && referenceSource == null) {
			logger.serious(COMMAND_LINE_LOCATION, "either one of these options is required: -train -dev -test -reference");
			result = false;
		}
		if (predictionSource == null) {
			logger.serious(COMMAND_LINE_LOCATION, "option -prediction is mandatory");
			result = false;
		}
		if (referenceSource != null) {
			set = null;
		}
		else if (set != null && set.equals("test") && detailedEvaluation) {
			logger.tolerable(COMMAND_LINE_LOCATION, "option -detailed is not compatible with test evaluation");
			detailedEvaluation = false;
		}
		return result;
	}
}
