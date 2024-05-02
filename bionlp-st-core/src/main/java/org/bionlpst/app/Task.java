package org.bionlpst.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.app.xml.TaskConverter;
import org.bionlpst.app.xml.TaskMapConverter;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.DocumentCollection;
import org.bionlpst.corpus.source.ContentAndReferenceSource;
import org.bionlpst.corpus.source.PredictionSource;
import org.bionlpst.evaluation.AnnotationEvaluation;
import org.bionlpst.evaluation.BootstrapConfig;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMAliases;
import org.bionlpst.util.dom.DOMUtil;
import org.bionlpst.util.message.CheckLogger;
import org.w3c.dom.Element;

public class Task {
	private final String name;
	private final StringBuilder description = new StringBuilder();
	private Schema<Corpus> schema;
	private final List<AnnotationEvaluation> evaluations = new ArrayList<AnnotationEvaluation>();
	private ContentAndReferenceSource trainSource;
	private ContentAndReferenceSource devSource;
	private ContentAndReferenceSource testSource;
	private boolean testHasReferenceAnnotations;
	private CorpusPostprocessing corpusPostprocessing = NullPostprocessing.INSTANCE;


	public Task(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description.toString();
	}

	public Schema<Corpus> getSchema() {
		return schema;
	}

	public List<AnnotationEvaluation> getEvaluations() {
		return Collections.unmodifiableList(evaluations);
	}

	public ContentAndReferenceSource getTrainSource() {
		return trainSource;
	}

	public ContentAndReferenceSource getDevSource() {
		return devSource;
	}

	public ContentAndReferenceSource getTestSource() {
		return testSource;
	}

	public boolean isTestHasReferenceAnnotations() {
		return testHasReferenceAnnotations;
	}
	
	public boolean hasTest() {
		return testSource != null;
	}
	
	public CorpusPostprocessing getCorpusPostprocessing() {
		return corpusPostprocessing;
	}

	public void setCorpusPostprocessing(CorpusPostprocessing corpusPostprocessing) {
		this.corpusPostprocessing = corpusPostprocessing;
	}

	public void addDescription(CharSequence descr) {
		if (description.length() > 0) {
			description.append('\n');
		}
		description.append(descr);
	}
	
	public void addEvaluation(AnnotationEvaluation eval) {
		evaluations.add(eval);
	}

	public void setSchema(Schema<Corpus> schema) {
		this.schema = schema;
	}

	public void setTrainSource(ContentAndReferenceSource trainSource) {
		this.trainSource = trainSource;
	}

	public void setDevSource(ContentAndReferenceSource devSource) {
		this.devSource = devSource;
	}

	public void setTestSource(ContentAndReferenceSource testSource) {
		this.testSource = testSource;
	}

	public void setTestHasReferenceAnnotations(boolean testHasReferenceAnnotations) {
		this.testHasReferenceAnnotations = testHasReferenceAnnotations;
	}
	
	public Corpus getTrainCorpus(CheckLogger logger) throws BioNLPSTException, IOException {
		return trainSource.fillContentAndReference(logger, true);
	}
	
	public Corpus getDevCorpus(CheckLogger logger) throws BioNLPSTException, IOException {
		return devSource.fillContentAndReference(logger, true);
	}
	
	public Corpus getTrainAndDevCorpus(CheckLogger logger) throws BioNLPSTException, IOException {
		Corpus result = getTrainCorpus(logger);
		devSource.fillContentAndReference(logger, result, true);
		return result;
	}
	
	public Corpus getTestCorpus(CheckLogger logger) throws BioNLPSTException, IOException {
		if (testSource == null) {
			throw new BioNLPSTException("test set is not available for " + name);
		}
		return testSource.fillContentAndReference(logger, true);
	}
	
	public void checkSchema(CheckLogger logger, Corpus corpus) {
		schema.check(logger, corpus);
	}
	
	public static void checkParsedPredictions(CheckLogger logger, Corpus corpus, String source) {
		Collection<String> missing = new ArrayList<String>();
		for (Document doc : corpus.getDocuments()) {
			AnnotationSet aset = doc.getPredictionAnnotationSet();
			if (!aset.isParsed()) {
				missing.add(doc.getId());
			}
		}
		if (!missing.isEmpty()) {
			logger.serious(new Location(source, -1), "missing predictions for documents: " + Util.join(missing, ", "));
		}
	}

	public Map<String,EvaluationResult<Annotation>> evaluate(@SuppressWarnings("unused") CheckLogger logger, DocumentCollection documentCollection, boolean keepPairs, BootstrapConfig bootstrap) {
		Map<String,EvaluationResult<Annotation>> result = new LinkedHashMap<String,EvaluationResult<Annotation>>();
		for (AnnotationEvaluation eval : evaluations) {
			EvaluationResult<Annotation> er = eval.getResult(documentCollection, keepPairs, bootstrap);
			result.put(eval.getName(), er);
		}
		return result;
	}

	public EvaluationResult<Annotation> evaluateMain(@SuppressWarnings("unused") CheckLogger logger, DocumentCollection documentCollection, boolean keepPairs, BootstrapConfig bootstrap) {
		AnnotationEvaluation mainEvaluation = evaluations.get(0);
		return mainEvaluation.getMainResult(documentCollection, keepPairs, bootstrap);
	}

	public void loadPredictions(CheckLogger logger, Corpus corpus, PredictionSource predictionParser) throws BioNLPSTException, IOException {
		predictionParser.fillPredictions(logger, corpus);
		schema.check(logger, corpus);
	}
	
	public Map<String,EvaluationResult<Annotation>> evaluateTrain(CheckLogger logger, PredictionSource predictionParser, boolean keepPairs, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		Corpus corpus = getTrainCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluate(logger, corpus, keepPairs, bootstrap);
	}

	public Map<String,EvaluationResult<Annotation>> evaluateDev(CheckLogger logger, PredictionSource predictionParser, boolean keepPairs, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		Corpus corpus = getDevCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluate(logger, corpus, keepPairs, bootstrap);
	}	

	public Map<String,EvaluationResult<Annotation>> evaluateTrainAndDev(CheckLogger logger, PredictionSource predictionParser, boolean keepPairs, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		Corpus corpus = getTrainAndDevCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluate(logger, corpus, keepPairs, bootstrap);
	}	

	public Map<String,EvaluationResult<Annotation>> evaluateTest(CheckLogger logger, PredictionSource predictionParser, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		if (!testHasReferenceAnnotations) {
			logger.serious(new Location(predictionParser.getName(), 0), "evaluation for the test set is not available for " + name);
			return Collections.emptyMap();
		}
		Corpus corpus = getTestCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluate(logger, corpus, false, bootstrap);
	}
	
	public EvaluationResult<Annotation> evaluateMainTrain(CheckLogger logger, PredictionSource predictionParser, boolean keepPairs, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		Corpus corpus = getTrainCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluateMain(logger, corpus, keepPairs, bootstrap);
	}	

	public EvaluationResult<Annotation> evaluateMainDev(CheckLogger logger, PredictionSource predictionParser, boolean keepPairs, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		Corpus corpus = getDevCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluateMain(logger, corpus, keepPairs, bootstrap);
	}	

	public EvaluationResult<Annotation> evaluateMainTrainAndDev(CheckLogger logger, PredictionSource predictionParser, boolean keepPairs, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		Corpus corpus = getTrainAndDevCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluateMain(logger, corpus, keepPairs, bootstrap);
	}

	public EvaluationResult<Annotation> evaluateMainTest(CheckLogger logger, PredictionSource predictionParser, BootstrapConfig bootstrap) throws BioNLPSTException, IOException {
		if (!testHasReferenceAnnotations) {
			logger.serious(new Location(predictionParser.getName(), 0), "evaluation for the test set is not available for " + name);
			return new EvaluationResult<Annotation>(evaluations.get(0));
		}
		Corpus corpus = getTestCorpus(logger);
		loadPredictions(logger, corpus, predictionParser);
		return evaluateMain(logger, corpus, false, bootstrap);
	}
	
	public static final String TASK_DEFINITION_RESOURCE_NAME = "org/bionlpst/task-definition.xml";

	public static Map<String,Task> loadTasks() throws Exception {
		return loadTasks(Collections.<String>emptyList());
	}

	public static Map<String,Task> loadTasks(List<String> additionalTaskDefs) throws Exception {
		return loadTasks(Task.class.getClassLoader(), additionalTaskDefs);
	}

	public static Map<String,Task> loadTasks(ClassLoader classLoader) throws Exception {
		return loadTasks(classLoader, Collections.<String>emptyList());
	}

	public static Map<String,Task> loadTasks(ClassLoader classLoader, List<String> additionalTaskDefs) throws Exception {
		TaskMapConverter converter = new TaskMapConverter(classLoader);
		Enumeration<URL> urlEnum = getURLEnum(classLoader);
		while (urlEnum.hasMoreElements()) {
			URL url = urlEnum.nextElement();
			try (InputStream is = url.openStream()) {
				DOMUtil.convert(converter, is);
			}
		}
		for (String tdf : additionalTaskDefs) {
			try (InputStream is = Files.newInputStream(Paths.get(tdf))) {
				DOMUtil.convert(converter, is);
			}
		}
		return converter.getResult();
	}
	
	private static Enumeration<URL> getURLEnum(ClassLoader classLoader) throws IOException {
		if (classLoader == null) {
			return ClassLoader.getSystemResources(TASK_DEFINITION_RESOURCE_NAME);
		}
		return classLoader.getResources(TASK_DEFINITION_RESOURCE_NAME);
	}

	public static Task loadTask(ClassLoader classLoader, String taskName, InputStream is) throws Exception {
		DocumentBuilder docBuilder = DOMUtil.createDocumentBuilder();
		org.w3c.dom.Document doc = docBuilder.parse(is);
		DOMAliases aliases = DOMUtil.createAliases(doc);
		Element element = doc.getDocumentElement();
		aliases.replace(element);
		for (Element child : DOMUtil.getChildrenElements(element, false)) {
			String name = child.getAttribute("name");
			if (taskName.equals(name)) {
				TaskConverter converter = new TaskConverter(classLoader);
				return converter.convert(child);
			}
		}
		return null;
	}
	
	public static Task loadTask(ClassLoader classLoader, List<String> additionalTaskDefs, String taskName) throws Exception {
		Enumeration<URL> urlEnum = getURLEnum(classLoader);
		while (urlEnum.hasMoreElements()) {
			URL url = urlEnum.nextElement();
			try (InputStream is = url.openStream()) {
				Task result = loadTask(classLoader, taskName, is);
				if (result != null) {
					return result;
				}
			}
		}
		for (String tdf : additionalTaskDefs) {
			try (InputStream is = Files.newInputStream(Paths.get(tdf))) {
				Task result = loadTask(classLoader, taskName, is);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public static Task loadTask(List<String> additionalTaskDefs, String taskName) throws Exception {
		return loadTask(Task.class.getClassLoader(), additionalTaskDefs, taskName);
	}

	public static Task loadTask(ClassLoader classLoader, String taskName) throws Exception {
		return loadTask(classLoader, Collections.<String>emptyList(), taskName);
	}

	public static Task loadTask(String taskName) throws Exception {
		return loadTask(Task.class.getClassLoader(), taskName);
	}
}
