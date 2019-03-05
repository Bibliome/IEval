package org.bionlpst.app.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.app.Task;
import org.bionlpst.app.web.json.CheckMessageJsonConverter;
import org.bionlpst.app.web.json.EvaluationResultJsonConverter;
import org.bionlpst.app.web.json.JsonConverter;
import org.bionlpst.app.web.json.ListJsonConverter;
import org.bionlpst.app.web.json.TaskJsonConverter;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.DocumentCollection;
import org.bionlpst.corpus.parser.PredictionSource;
import org.bionlpst.corpus.parser.bionlpst.BioNLPSTSource;
import org.bionlpst.corpus.parser.bionlpst.InputStreamCollection;
import org.bionlpst.evaluation.BootstrapConfig;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.evaluation.Measure;
import org.bionlpst.evaluation.MeasureDirection;
import org.bionlpst.evaluation.MeasureResult;
import org.bionlpst.evaluation.ScoringResult;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;
import org.bionlpst.util.message.CheckMessageLevel;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.h2.tools.RunScript;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("")
public class BioNLPSTRest {
	private static final Location REST_URL_LOCATION = new Location("Request URL", 0);
	private static final String[] TEAM_PSEUDONYMS = new String[] {
		"A",
		"B",
		"C",
		"D",
		"E",
		"F",
		"G",
		"H",
		"I",
		"J",
		"K",
		"L",
		"M",
		"N",
	};
	
	private final Map<String,Task> taskMap;
	private final CheckLogger logger = new CheckLogger();
	private final String checkGoogleTokenURL;
	private final String databasePath;
	private final String superuser;
	private Task task = null;
	private Corpus corpus = null;
	private String dataset = null;
	private BootstrapConfig bootstrapConfig = null;
	private String userGID = null;
	private String username = null;
	private String email = null;

	public BioNLPSTRest() throws Exception {
		super();
		ClassLoader classLoader = getClass().getClassLoader();
		taskMap = Task.loadTasks(classLoader);
		try (InputStream is = getPropertiesResourcesAsStream(classLoader)) {
			Properties props = new Properties();
			props.load(is);
			checkGoogleTokenURL = props.getProperty("checkGoogleTokenURL");
			databasePath = props.getProperty("databasePath");
			superuser = props.getProperty("superuser");
		}
	}
	
	private static InputStream getPropertiesResourcesAsStream(ClassLoader classLoader) throws UnknownHostException {
		String user = System.getenv("USER");
		String host = System.getenv("HOSTNAME");
		if (host == null) {
			host = InetAddress.getLocalHost().getHostName();
		}
		for (String name : getPropertiesResourceNames(user, host)) {
			String resName = "org/bionlpst/app/web/" + name + ".properties";
			InputStream result = classLoader.getResourceAsStream(resName);
			if (result != null) {
				return result;
			}
		}
		throw new RuntimeException();
	}
	
	private static String[] getPropertiesResourceNames(String user, String host) {
		return new String[] {
			user + "@" + host,
			host,
			user,
			"default",
			"rest"
		};
	}
	
	@GET
	@Path("list-tasks")
	@Produces(MediaType.APPLICATION_JSON)
	public String listTasks() throws Exception {
		JSONArray result = ListJsonConverter.convert(TaskJsonConverter.INSTANCE, taskMap.values());
		return result.toString(4);
	}
	
	@POST
	@Path("task/{taskName}/{set:train|dev|traindev|test}/check")
	@Consumes({MediaType.MULTIPART_FORM_DATA, "application/zip"})
	@Produces(MediaType.APPLICATION_JSON)
	public String checkSubmission(
			@PathParam("taskName") String taskName,
			@PathParam("set") String set,
			@FormDataParam("zipfile") InputStream zipStream,
			@FormDataParam("zipfile") FormDataContentDisposition zipInfo
			) throws Exception {
		start(taskName, set, zipStream, zipInfo, null, null);
		return finish(new JSONObject());
	}
	
	private void start(String taskName, String set, InputStream zipStream, FormDataContentDisposition zipInfo, Integer resamples, Long seed) throws BioNLPSTException, IOException {
		task = selectTask(taskName);
		corpus = loadReference(set);
		dataset = set;
		loadAndCheckPredictions(zipStream, zipInfo);
		task.getCorpusPostprocessing().postprocess(corpus);
		bootstrapConfig = getBootstrapConfig(resamples, seed);
	}

	private static BootstrapConfig getBootstrapConfig(Integer resamples, Long seed) {
		if (resamples == null || resamples == 0) {
			return null;
		}
		Random random = seed == null ? new Random() : new Random(seed);
		return new BootstrapConfig(random, resamples);
	}
	
	private Task selectTask(String taskName) {
		if (taskMap.containsKey(taskName)) {
			return taskMap.get(taskName);
		}
		logger.serious(REST_URL_LOCATION, "unknown task: " +taskName);
		return null;
	}
	
	private Corpus loadReference(String set) throws BioNLPSTException, IOException {
		if (task == null) {
			return null;
		}
		switch (set) {
			case "train": return task.getTrainCorpus(logger);
			case "dev": return task.getDevCorpus(logger);
			case "train+dev": return task.getTrainAndDevCorpus(logger);
			case "test": {
				if (!task.hasTest()) {
					logger.serious(REST_URL_LOCATION, "test set is not available for " + task.getName());
					return null;
				}
				return task.getTestCorpus(logger);
			}
			default: {
				throw new RuntimeException("unknown set: " + set);
			}
		}
	}
	
	private void loadAndCheckPredictions(InputStream zipStream, FormDataContentDisposition zipInfo) throws BioNLPSTException, IOException {
		if (task == null || corpus == null) {
			return;
		}
		InputStreamCollection predictionInputStreamCollection = new ZipFileUploadInputStreamCollection(zipStream, zipInfo.getFileName());
		PredictionSource predictionParser = new BioNLPSTSource(predictionInputStreamCollection);
		predictionParser.getPredictions(logger, corpus);
		corpus.resolveReferences(logger);
		Task.checkParsedPredictions(logger, corpus, zipInfo.getFileName());
		task.checkSchema(logger, corpus);
	}
	
	private String finish(JSONObject result) throws Exception {
		result.put("messages", ListJsonConverter.convert(CheckMessageJsonConverter.INSTANCE, logger.getMessages()));
		CheckMessageLevel level = logger.getHighestLevel();
		result.put("highest-message-level", level);
		result.put("success", level == null || level == CheckMessageLevel.INFORMATION);
		return result.toString(4);
	}

	@POST
	@Path("task/{taskName}/{set:train|dev|traindev|test}/evaluate")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String evaluateSubmission(
			@PathParam("taskName") String taskName,
			@PathParam("set") String set,
			@FormDataParam("zipfile") InputStream zipStream,
			@FormDataParam("zipfile") FormDataContentDisposition zipInfo,
			@FormDataParam("detailed") @DefaultValue("false") Boolean detailed,
			@FormDataParam("alternate") @DefaultValue("false") Boolean alternate,
			@FormDataParam("resamples") @DefaultValue("") Integer resamples,
			@FormDataParam("token") @DefaultValue("") String token
			) throws Exception {
		checkToken(token);
		start(taskName, set, zipStream, zipInfo, resamples, null);
		JSONObject result = new JSONObject();
		if (task != null && corpus != null) {
			result.put("evaluation", doEvaluation(task, set, corpus, detailed, alternate));
		}
		return finish(result);
	}

	private void checkToken(String token) throws IOException, JSONException {
		if (token.isEmpty()) {
			return;
		}
//		System.err.println("token = " + token);
		URL url = new URL(checkGoogleTokenURL + "?id_token=" + URLEncoder.encode(token, "UTF-8"));
		URLConnection conn = url.openConnection();
		String enc = conn.getContentEncoding();
		if (enc == null) {
			enc = "UTF-8";
		}
		try (InputStream is = conn.getInputStream()) {
			Reader r = new InputStreamReader(is, enc);
			String s = Util.readWholeStream(r);
			JSONObject json = new JSONObject(s.toString());
			if (json.has("sub")) {
				userGID = json.getString("sub");
//				System.err.println("userGID = " + userGID);
				if (json.has("name")) {
					username = json.getString("name");
				}
				else {
					username = "Anonymous";
				}
				if (json.has("email")) {
					email = json.getString("email");
//					System.err.println("email = " + email);
				}
				else {
					email = "";
				}
			}
		}
	}
//				{
//					 "iss": "accounts.google.com",
//					 "at_hash": "q7sTltCilwkgoTtJhrVXhQ",
//					 "aud": "...",
//					 "sub": "...",
//					 "email_verified": "true",
//					 "azp": "...",
//					 "email": "robert.bossy.inra@gmail.com",
//					 "iat": "1455038528",
//					 "exp": "1455042128",
//					 "name": "Robert Bossy",
//					 "given_name": "Robert",
//					 "family_name": "Bossy",
//					 "locale": "en",
//					 "alg": "RS256",
//					 "kid": "..."
//					}
	
	private JSONObject doEvaluation(Task task, String set, Corpus corpus, boolean detailed, boolean alternate) throws Exception {
		if (set.equals("test") && !task.isTestHasReferenceAnnotations()) {
			logger.serious(REST_URL_LOCATION, "test set has no reference annotations for " + task.getName());
			return new JSONObject();
		}
		JSONObject result = new JSONObject();
		if (detailed) {
			DocumentJsonConverter converter = new DocumentJsonConverter(alternate);
			result.put("detail", ListJsonConverter.convert(converter, corpus.getDocuments()));
		}
		JsonConverter<EvaluationResult<Annotation>> converter = new EvaluationResultJsonConverter(false);
		List<EvaluationResult<Annotation>> evaluationResults = getEvaluationResults(task, alternate, corpus, false, bootstrapConfig);
		try (Connection conn = connectDatabase()) {
			long submissionId = storeSubmission(conn);
			storeSubmissionResults(conn, submissionId, evaluationResults);
			result.put("submission-id", submissionId);
		}
		result.put("global-evaluations", ListJsonConverter.convert(converter, evaluationResults));
		return result;
	}

	private class DocumentJsonConverter implements JsonConverter<Document> {
		private final boolean alternate;
		private final JsonConverter<EvaluationResult<Annotation>> converter = new EvaluationResultJsonConverter(true);
		
		private DocumentJsonConverter(boolean alternate) {
			super();
			this.alternate = alternate;
		}

		@Override
		public JSONObject convert(Document doc) throws Exception {
			JSONObject result = new JSONObject();
			result.put("document", doc.getId());
			result.put("evaluations", ListJsonConverter.convert(converter, getEvaluationResults(task, alternate, doc, true, bootstrapConfig)));
			return result;
		}
	}

	private List<EvaluationResult<Annotation>> getEvaluationResults(Task task, boolean alternate, DocumentCollection documentCollection, boolean pairs, BootstrapConfig bootstrap) {
		if (alternate) {
			Map<String,EvaluationResult<Annotation>> evaluationResultMap = task.evaluate(logger, documentCollection, pairs, bootstrap);
			return new ArrayList<EvaluationResult<Annotation>>(evaluationResultMap.values());
		}
		EvaluationResult<Annotation> evaluationResult = task.evaluateMain(logger, documentCollection, pairs, bootstrap);
		return Collections.singletonList(evaluationResult);
	}
	
	@POST
	@Path("run")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public String run(
			@FormDataParam("zipfile") InputStream zipStream,
			@FormDataParam("zipfile") FormDataContentDisposition zipInfo,
			@FormDataParam("taskName") String taskName,
			@FormDataParam("set") String set,
			@FormDataParam("detailed") @DefaultValue("false") Boolean detailed,
			@FormDataParam("alternate") @DefaultValue("false") Boolean alternate,
			@FormDataParam("action") @DefaultValue("evaluate") String action,
			@FormDataParam("resamples") @DefaultValue("") Integer resamples,
			@FormDataParam("token") @DefaultValue("") String token
			) throws Exception {
		switch (action) {
			case "check": return checkSubmission(taskName, set, zipStream, zipInfo);
			case "evaluate": return evaluateSubmission(taskName, set, zipStream, zipInfo, detailed, alternate, resamples, token);
			default: throw new BioNLPSTException("unknown action: " + action);
		}
	}

	@GET
	@Path("submission/{submission:\\d+}/set-owner")
	@Produces(MediaType.TEXT_HTML)
	public String setSubmissionOwner(
			@PathParam("submission") @DefaultValue("") long submissionId,
			@QueryParam("token") @DefaultValue("") String token
			) throws Exception {
		JSONObject result = new JSONObject();
		try (Connection conn = connectDatabase()) {
			checkToken(token);
			checkSubmissionOwnership(conn, submissionId);
			PreparedStatement stmt = conn.prepareStatement("UPDATE submission SET user_gid = ?, user_name = ?, email = ? WHERE id = ?");
			stmt.setString(1, userGID);
			stmt.setString(2, username);
			stmt.setString(3, email);
			stmt.setLong(4, submissionId);
			stmt.executeUpdate();
		}
		catch (Exception e) {
			logger.serious(REST_URL_LOCATION, "Server problem: " + e.getMessage());
		}
		return finish(result);
	}

	@GET
	@Path("submission/{submission:\\d+}/set-private/{priv}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String removeSubmissionTag(
			@PathParam("submission") Long submissionId,
			@PathParam("priv") Boolean priv,
			@QueryParam("token") @DefaultValue("") String token
			) throws Exception {
		JSONObject result = new JSONObject();
		try (Connection conn = connectDatabase()) {
			checkToken(token);
			checkSubmissionOwnership(conn, submissionId);
			PreparedStatement stmt = conn.prepareStatement("UPDATE submission SET private = ? WHERE id = ?");
			stmt.setBoolean(1, priv);
			stmt.setLong(2, submissionId);
			stmt.executeUpdate();
		}
		catch (Exception e) {
			logger.serious(REST_URL_LOCATION, "Server problem: " + e.getMessage());
		}
		return finish(result);
	}
	
	private static List<Long> getSubmissionIds(String submissionIdsString) {
		List<Long> result = new ArrayList<Long>();
		for (String s : Util.split(submissionIdsString, ',')) {
			result.add(Long.parseLong(s));
		}
		return result;
	}

	@GET
	@Path("delete-submissions")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteSubmissions(
			@QueryParam("id") @DefaultValue("") String submissionIdsString,
			@QueryParam("token") @DefaultValue("") String token
			) throws Exception {
		JSONObject result = new JSONObject();
		try (Connection conn = connectDatabase()) {
			checkToken(token);
			List<Long> submissionIds = getSubmissionIds(submissionIdsString);
			for (long id : submissionIds) {
				checkSubmissionOwnership(conn, id);
			}

			StringBuilder sqlIdsBuilder = new StringBuilder(3 * submissionIds.size());
			for (int i = 0; i < submissionIds.size(); ++i) {
				sqlIdsBuilder.append(sqlIdsBuilder.length() == 0 ? "?" : ", ?");
			}

			PreparedStatement stmt = conn.prepareStatement("DELETE FROM measure WHERE ref_submission IN (" + sqlIdsBuilder + ")");
			for (int i = 0; i < submissionIds.size(); ++i) {
				stmt.setLong(i+1, submissionIds.get(i));
			}
			stmt.executeUpdate();

			stmt = conn.prepareStatement("DELETE FROM submission WHERE id IN (" + sqlIdsBuilder + ")");
			for (int i = 0; i < submissionIds.size(); ++i) {
				stmt.setLong(i+1, submissionIds.get(i));
			}
			stmt.executeUpdate();
		}
		catch (Exception e) {
			logger.serious(REST_URL_LOCATION, "Server problem: " + e.getMessage());
		}
		return finish(result);
	}	

	@GET
	@Path("submission/{submission}/set-description")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String setSubmissionDescription(
			@PathParam("submission") @DefaultValue("") Long submissionId,
			@QueryParam("token") @DefaultValue("") String token,
			@QueryParam("description") @DefaultValue("") String description
			) throws Exception {
		JSONObject result = new JSONObject();
		try (Connection conn = connectDatabase()) {
			checkToken(token);
			checkSubmissionOwnership(conn, submissionId);
			PreparedStatement stmt = conn.prepareStatement("UPDATE submission SET description = ? WHERE id = ?");
			stmt.setString(1, description);
			stmt.setLong(2, submissionId);
			stmt.executeUpdate();
		}
		catch (Exception e) {
			logger.serious(REST_URL_LOCATION, "Server problem: " + e.getMessage());
		}
		return finish(result);
	}
	
	private void checkSubmissionOwnership(Connection conn, long submissionId) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM submission WHERE id = ? AND (user_gid = ? OR user_gid = '' OR user_gid IS NULL)");
		stmt.setLong(1, submissionId);
		stmt.setString(2, userGID);
		ResultSet rs = stmt.executeQuery();
		if (!rs.first()) {
			throw new BioNLPSTException("user is not owner");
		}
	}

	@GET
	@Path("task/{taskName}/submissions")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String getSubmissions(
			@PathParam("taskName") @DefaultValue("") String taskName,
			@QueryParam("token") @DefaultValue("") String token
			) throws Exception {
		JSONObject result = new JSONObject();
		try (Connection conn = connectDatabase()) {
			checkToken(token);
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM submission WHERE task = ?");
			stmt.setString(1, taskName);
			ResultSet rs = stmt.executeQuery();
			JSONArray submissions = new JSONArray();
			result.put("submissions", submissions);
			SubmissionJsonConverter converter = new SubmissionJsonConverter();
			while (rs.next()) {
				JSONObject sub = converter.convert(rs);
				long submissionId = sub.getLong("id");
				JSONObject evaluations = getSubmissionResults(conn, submissionId);
				sub.put("evaluations", evaluations);
				submissions.put(sub);
			}
		}
		catch (Exception e) {
			logger.serious(REST_URL_LOCATION, "Server problem: " + e.getMessage());
		}
		return finish(result);
	}

	private static JSONObject ensure(JSONObject obj, String key) throws JSONException {
		if (obj.has(key)) {
			return obj.getJSONObject(key);
		}
		JSONObject result = new JSONObject();
		obj.put(key, result);
		return result;
	}
	
	private static JSONObject getSubmissionResults(Connection conn, long submissionId) throws SQLException, JSONException {
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM measure WHERE ref_submission = ?");
		stmt.setLong(1, submissionId);
		ResultSet rs = stmt.executeQuery();
		JSONObject result = new JSONObject();
		while(rs.next()) {
			String evaluation = rs.getString("evaluation");
			JSONObject scorings = ensure(result, evaluation);
			String scoring = rs.getString("scoring");
			JSONObject measures = ensure(scorings, scoring);
			String name = rs.getString("name");
			double val = rs.getDouble("val");
			if (Double.isInfinite(val) || Double.isNaN(val)) {
				measures.put(name, JSONObject.NULL);
			}
			else {
				measures.put(name, val);
			}
		}
		return result;
	}
	
	private class SubmissionConverter {
		private final Map<String,String> pseudonyms = new HashMap<String,String>();
		private int nextPseudo = 0;
		
		protected String getOwnerName(ResultSet rs) throws SQLException {
			String subOwner = rs.getString("user_gid");
			if (subOwner == null) {
				return "Anonymous";
			}
			if (userGID != null && userGID.equals(subOwner)) {
				return "me";
			}
			if (userGID != null && userGID.equals(superuser)) {
				return rs.getString("user_name");
			}
			boolean priv = rs.getBoolean("private");
			if (!priv) {
				return rs.getString("user_name");
			}
			if (pseudonyms.containsKey(subOwner)) {
				return pseudonyms.get(subOwner);
			}
			String pseudo = "Team " + TEAM_PSEUDONYMS[nextPseudo];
			pseudonyms.put(subOwner, pseudo);
			nextPseudo = (nextPseudo + 1) % TEAM_PSEUDONYMS.length;
			return pseudo;
		}
		
		protected boolean isMe(ResultSet rs) throws SQLException {
			return (userGID != null && ((userGID.equals(superuser) && rs.getString("user_gid") == null) || userGID.equals(rs.getString("user_gid"))));
		}
	}

	private class SubmissionJsonConverter extends SubmissionConverter implements JsonConverter<ResultSet> {
		private SubmissionJsonConverter() {
			super();
		}

		@Override
		public JSONObject convert(ResultSet rs) throws Exception {
			JSONObject sub = new JSONObject();
			long id = rs.getLong("id");
			sub.put("id", id);
			sub.put("owner", getOwnerName(rs));
			sub.put("me", isMe(rs));
			sub.put("date", rs.getTimestamp("creation_date").toString());
			sub.put("set", rs.getString("data_set"));
			sub.put("private", rs.getBoolean("private"));
			sub.put("description", rs.getString("description"));
			return sub;
		}
	}

	private static void storeSubmissionResults(Connection conn, long submissionId, List<EvaluationResult<Annotation>> evaluationResults) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("INSERT INTO measure (ref_submission, evaluation, scoring, name, val, higher) VALUES (?, ?, ?, ?, ?, ?)");
		stmt.setLong(1, submissionId);
		for (EvaluationResult<Annotation> evalResult : evaluationResults) {
			stmt.setString(2, evalResult.getEvaluation().getName());
			for (ScoringResult<Annotation> scoringResult : evalResult.getScoringResults()) {
				stmt.setString(3, scoringResult.getScoring().getName());
				for (MeasureResult measureResult : scoringResult.getMeasureResults()) {
					Measure measure = measureResult.getMeasure();
					stmt.setString(4, measure.getName());
					stmt.setDouble(5, measureResult.getResult().doubleValue());
					stmt.setBoolean(6, measure.getMeasureDirection() == MeasureDirection.HIGHER_IS_BETTER);
					int n = stmt.executeUpdate();
					if (n == 0) {
						throw new BioNLPSTException("database operation failed");
					}
				}
			}
		}
	}

	private long storeSubmission(Connection conn) throws SQLException {
		PreparedStatement stmt;
		if (userGID != null && !userGID.isEmpty() && username != null && !username.isEmpty()) {
			stmt = conn.prepareStatement("INSERT INTO submission (creation_date, user_gid, user_name, email, private, task, data_set) VALUES (now(), ?, ?, ?, true, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, userGID);
			stmt.setString(2, username);
			stmt.setString(3, email);
			stmt.setString(4, task.getName());
			stmt.setString(5, dataset);
		}
		else {
			stmt = conn.prepareStatement("INSERT INTO submission (creation_date, private, task, data_set) VALUES (now(), true, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, task.getName());
			stmt.setString(2, dataset);
		}
		stmt.executeUpdate();
		ResultSet rs = stmt.getGeneratedKeys();
		if (rs.first()) {
			Long result = rs.getLong(1);
			if (result.longValue() == 0) {
				throw new BioNLPSTException("INSERT returned nothing");
			}
			return result;
		}
		throw new BioNLPSTException("INSERT returned nothing");
	}

	private Connection connectDatabase() throws SQLException, ClassNotFoundException, IOException {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:" + databasePath, "bionlp-st", "");
		Statement stmt = conn.createStatement();
		try {
			stmt.execute("SELECT id FROM submission LIMIT 1");
		}
		catch (SQLException e) {
			createDatabase(conn);
		}
		return conn;
	}

	private static void createDatabase(Connection conn) throws IOException, SQLException {
		try (InputStream is = BioNLPSTRest.class.getClassLoader().getResourceAsStream("org/bionlpst/app/web/createDB.sql")) {
			Reader r = new InputStreamReader(is);
			RunScript.execute(conn, r);
		}
	}
}
