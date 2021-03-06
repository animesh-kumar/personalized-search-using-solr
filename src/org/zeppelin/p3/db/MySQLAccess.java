package org.zeppelin.p3.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zeppelin.p3.personalization.PreferredSourceWithCheckValue;
import org.zeppelin.p3.personalization.User;
import org.zeppelin.p3.personalization.UserLog;

// Copied from <br>
// http://www.vogella.com/tutorials/MySQLJava/article.html

public class MySQLAccess {
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public void readDataBase() throws Exception {
		try {

			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			resultSet = statement
					.executeQuery("select * from ub535p3.user_master");
			writeResultSet(resultSet);

			// // preparedStatements can use variables and are more efficient
			// preparedStatement = connect
			// .prepareStatement("insert into  FEEDBACK.COMMENTS values (default, ?, ?, ?, ? , ?, ?)");
			// //
			// "myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS");
			// // parameters start with 1
			// preparedStatement.setString(1, "Test");
			// preparedStatement.setString(2, "TestEmail");
			// preparedStatement.setString(3, "TestWebpage");
			// preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
			// preparedStatement.setString(5, "TestSummary");
			// preparedStatement.setString(6, "TestComment");
			// preparedStatement.executeUpdate();

			preparedStatement = connect
					.prepareStatement("SELECT user_id, name, category_id, tags, doc_id from ub535p3.user_master");
			resultSet = preparedStatement.executeQuery();
			writeResultSet(resultSet);

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public void createNewUser(int userId, String userName) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");

			preparedStatement = connect
					.prepareStatement("insert into  ub535p3.user_master values (?, ?)");
			preparedStatement.setInt(1, userId);
			preparedStatement.setString(2, userName);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public void createNewCategory(int categoryId, String categoryName)
			throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");

			preparedStatement = connect
					.prepareStatement("insert into  ub535p3.category_master values (?, ?)");
			preparedStatement.setInt(1, categoryId);
			preparedStatement.setString(2, categoryName);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public void updateUserCategoryPreferences(int userId, String categoryName,
			Integer likeScore) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// statements allow to issue SQL queries to the database
			// // preparedStatements can use variables and are more efficient
			preparedStatement = connect
					.prepareStatement("SELECT category_id from ub535p3.category_master where name=?");
			preparedStatement.setString(1, categoryName);
			resultSet = preparedStatement.executeQuery();
			int categoryId = 0;
			while (resultSet.next()) {
				categoryId = resultSet.getInt("category_id");
			}
			preparedStatement = connect
					.prepareStatement("update ub535p3.user_category_map set liking_count = ? where user_id = ? AND category_id = ?");
			preparedStatement.setInt(1, likeScore);
			preparedStatement.setInt(2, userId);
			preparedStatement.setInt(3, categoryId);
			int rowsAffected = preparedStatement.executeUpdate();

			if (rowsAffected == 0) {
				preparedStatement = connect
						.prepareStatement("insert into  ub535p3.user_category_map values (?, ?,?)");
				preparedStatement.setInt(1, userId);
				preparedStatement.setInt(2, categoryId);
				preparedStatement.setInt(3, likeScore);
				preparedStatement.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
	}

	public void updatePreferredSources(Integer loggedInUserId,
			String sourceName, Boolean checked) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// If already exists
			preparedStatement = connect
					.prepareStatement("update ub535p3.user_source_map set checked = ? where user_id = ? AND source_id = (select source_id from ub535p3.source_master where source_name = ?)");
			preparedStatement.setBoolean(1, checked);
			preparedStatement.setInt(2, loggedInUserId);
			preparedStatement.setString(3, sourceName);
			int rowsAffected = preparedStatement.executeUpdate();

			if (rowsAffected == 0) {
				// Fetch the source id
				int sourceId = 0;
				preparedStatement = connect
						.prepareStatement("select source_id from ub535p3.source_master where source_name = ?");
				preparedStatement.setString(1, sourceName);
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
					sourceId = resultSet.getInt("source_id");
				}
				preparedStatement = connect
						.prepareStatement("insert into  ub535p3.user_source_map values (?, ?,?)");
				preparedStatement.setInt(1, loggedInUserId);
				preparedStatement.setInt(2, sourceId);
				preparedStatement.setBoolean(3, checked);
				preparedStatement.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public ArrayList<String> fetchPreferredCategories(int userId)
			throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// statements allow to issue SQL queries to the database
			preparedStatement = connect
					.prepareStatement("SELECT cm.name from ub535p3.user_category_map ucm, ub535p3.category_master cm where user_id=? AND ucm.category_id=cm.category_id");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();
			ArrayList<String> preferredCartegories = new ArrayList<String>();
			while (resultSet.next()) {
				preferredCartegories.add("category:"
						+ resultSet.getString("name"));
			}
			return preferredCartegories;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public Map<String, Integer> fetchPreferredCategoriesWithTheirLikingScores(
			int userId) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			resultSet = statement
					.executeQuery("SELECT category_id,name from ub535p3.category_master ORDER BY category_id");
			ArrayList<String> allCategories = new ArrayList<String>();
			while (resultSet.next()) {
				allCategories.add(resultSet.getString("name"));
			}
			// statements allow to issue SQL queries to the database
			preparedStatement = connect
					.prepareStatement("SELECT cm.category_id,cm.name,ucm.liking_count from ub535p3.user_category_map ucm, ub535p3.category_master cm where user_id=? AND ucm.category_id=cm.category_id ORDER BY cm.category_id");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();

			Map<String, Integer> preferredCartegoriesMap = new HashMap<String, Integer>();
			Map<String, Integer> copyOfPreferredCartegoriesMap = new HashMap<String, Integer>();
			while (resultSet.next()) {
				copyOfPreferredCartegoriesMap.put(resultSet.getString("name"),
						resultSet.getInt("liking_count"));

			}
			for (int i = 0; i < allCategories.size(); i++) {
				String category = allCategories.get(i);
				Integer likingScore = copyOfPreferredCartegoriesMap
						.get(category);
				if (likingScore == null) {
					preferredCartegoriesMap.put(category, 0);
				} else {
					preferredCartegoriesMap.put(category, likingScore);
				}
			}
			return preferredCartegoriesMap;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public ArrayList<String> fetchAllCategories() throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// resultSet gets the result of the SQL query
			resultSet = statement
					.executeQuery("SELECT name from ub535p3.category_master");
			ArrayList<String> preferredCartegories = new ArrayList<String>();
			while (resultSet.next()) {
				preferredCartegories.add(resultSet.getString("name"));
			}
			// Add liking scores
			return preferredCartegories;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public List<UserLog> fetchLikeScoresAndClickCountsForAllDocuments(int userId)
			throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			ArrayList<UserLog> likeScoresAndClickCountsForAllDocuments = new ArrayList<UserLog>();
			// statements allow to issue SQL queries to the database
			preparedStatement = connect
					.prepareStatement("SELECT doc_id,like_score,click_count from ub535p3.user_relevance_feedback where user_id=?");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				UserLog log = new UserLog();
				log.setUserId(userId);
				log.setDocID(resultSet.getString("doc_id"));
				log.setLikingScore(resultSet.getInt("like_score"));
				log.setClickCount(resultSet.getInt("click_count"));
				likeScoresAndClickCountsForAllDocuments.add(log);
			}
			return likeScoresAndClickCountsForAllDocuments;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
	}

	public UserLog fetchLikeScoresAndClickCountsForGivenDocument(int userId,
			String docId) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// statements allow to issue SQL queries to the database
			preparedStatement = connect
					.prepareStatement("SELECT like_score,click_count from ub535p3.user_relevance_feedback where user_id=? AND doc_id=?");
			preparedStatement.setInt(1, userId);
			preparedStatement.setString(2, "doc_id");
			resultSet = preparedStatement.executeQuery();

			UserLog log = new UserLog();
			while (resultSet.next()) {
				log.setUserId(userId);
				log.setDocID(docId);
				log.setLikingScore(resultSet.getInt("like_score"));
				log.setClickCount(resultSet.getInt("click_count"));
			}
			return log;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
	}

	private void writeResultSet(ResultSet resultSet) throws SQLException {
		// resultSet is initialised before the first data set
		while (resultSet.next()) {
			// it is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g., resultSet.getSTring(2);
			int user = resultSet.getInt("user_id");
			String website = resultSet.getString("name");
			int summary = resultSet.getInt("category_id");
			String date = resultSet.getString("tags");
			String comment = resultSet.getString("doc_id");
			System.out.println("User: " + user);
			System.out.println("Website: " + website);
			System.out.println("Summary: " + summary);
			System.out.println("Date: " + date);
			System.out.println("Comment: " + comment);
		}
	}

	public List<User> getUsers() throws Exception {
		List<User> userList = new ArrayList<User>();

		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");

			preparedStatement = connect
					.prepareStatement("SELECT user_id, name from ub535p3.user_master");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				userList.add(new User(resultSet.getInt("user_id"), resultSet
						.getString("name")));
			}
			return userList;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
		return userList;
	}

	public void updateClickCountForDocument(Integer loggedInUserId, String docId)
			throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			preparedStatement = connect
					.prepareStatement("update ub535p3.user_relevance_feedback set click_count = click_count + 1 where user_id = ? AND doc_id = ?");
			// preparedStatement = connect.prepareStatement("INSERT");
			preparedStatement.setInt(1, loggedInUserId);
			preparedStatement.setString(2, docId);
			int affectedRows = preparedStatement.executeUpdate();
			// In case no rows were updated, means that there were no entry for
			// the given user and docid
			// Run an Insert command
			if (affectedRows == 0) {
				preparedStatement = connect
						.prepareStatement("insert into ub535p3.user_relevance_feedback values (?,?,?,?)");
				preparedStatement.setInt(1, loggedInUserId);
				preparedStatement.setString(2, docId);
				// Like Scores are handled separately and since this is the
				// first entry, no special handling is needed
				preparedStatement.setInt(3, 0);
				// First Click for the document by the user.
				preparedStatement.setInt(4, 1);
				preparedStatement.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
	}

	public Map<String, Integer> fetchLikeScoreForAllDocuments(Integer userId)
			throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// statements allow to issue SQL queries to the database
			preparedStatement = connect
					.prepareStatement("SELECT doc_id,like_score from ub535p3.user_relevance_feedback where user_id=?");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();

			Map<String, Integer> likeScoresForUser = new HashMap<String, Integer>();
			while (resultSet.next()) {
				likeScoresForUser.put(resultSet.getString("doc_id"),
						resultSet.getInt("like_score"));

			}
			return likeScoresForUser;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public ArrayList<PreferredSourceWithCheckValue> fetchPreferredSourcesWithCheckValue(
			Integer userId) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			// First Fetch all the Sources
			statement = connect.createStatement();
			resultSet = statement
					.executeQuery("SELECT source_id,source_name from ub535p3.source_master ORDER BY source_id");
			ArrayList<String> sourceSuperset = new ArrayList<String>();
			while (resultSet.next()) {
				sourceSuperset.add(resultSet.getString("source_name"));
			}
			// statements allow to issue SQL queries to the database
			preparedStatement = connect
					.prepareStatement("SELECT sm.source_id,sm.source_name,usm.checked from ub535p3.source_master sm, ub535p3.user_source_map usm where user_id=? AND sm.source_id=usm.source_id ORDER BY sm.source_id");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();

			ArrayList<PreferredSourceWithCheckValue> preferredSourceWithCheckedValue = new ArrayList<PreferredSourceWithCheckValue>();
			// Default all of them to false
			for (int i = 0; i < sourceSuperset.size(); i++) {
				String source = sourceSuperset.get(i);
				PreferredSourceWithCheckValue preferredSource = new PreferredSourceWithCheckValue();
				preferredSource.setUserId(userId);
				preferredSource.setSource(source);
				preferredSource.setChecked(false);
				preferredSourceWithCheckedValue.add(preferredSource);
			}
			HashMap<String, Boolean> map = new HashMap<String, Boolean>();
			while (resultSet.next()) {
				
				Boolean checked = resultSet.getBoolean("checked");
				if(checked==null){
				   checked = false;	
				}
				map.put(resultSet.getString("source_name"),
						checked);

			}
			// Set the values
			for (PreferredSourceWithCheckValue preferredSource : preferredSourceWithCheckedValue) {
				String source = preferredSource.getSource();
				Boolean checked= map.get(source);
				if(checked==null){
				   checked = false;	
				}
				// Update the check values
				preferredSource.setChecked(checked);
			}

			return preferredSourceWithCheckedValue;

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	public void updateLikingScoreForDocument(Integer loggedInUserId,
			String docId, Integer likingScore) throws Exception {
		try {
			// this will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// setup the connection with the DB.
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/ub535p3?"
							+ "user=mysqluser&password=justarandompassword");
			preparedStatement = connect
					.prepareStatement("update ub535p3.user_relevance_feedback set like_score = ? where user_id = ? AND doc_id = ?");
			preparedStatement.setInt(1, likingScore);
			preparedStatement.setInt(2, loggedInUserId);
			preparedStatement.setString(3, docId);
			int affectedRows = preparedStatement.executeUpdate();
			// In case no rows were updated, means that there were no entry for
			// the given user and docid
			// Run an Insert command
			if (affectedRows == 0) {
				// In case user likes the document without even clicking on the
				// document
				// then this part will be called before the creation of tuple
				// for click_count
				preparedStatement = connect
						.prepareStatement("insert into ub535p3.user_relevance_feedback values (?,?,?,?)");
				preparedStatement.setInt(1, loggedInUserId);
				preparedStatement.setString(2, docId);
				preparedStatement.setInt(3, likingScore);
				preparedStatement.setInt(4, 1);
				preparedStatement.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}

	}

	// you need to close all three to make sure
	private void close() {
		close(resultSet);
		close(statement);
		close(connect);
	}

	private void close(AutoCloseable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			// don't throw now as it might leave following closeables in
			// undefined state
		}
	}

}