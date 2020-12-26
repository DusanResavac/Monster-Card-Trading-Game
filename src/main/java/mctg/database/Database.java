package mctg.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import mctg.UserRecord;

public class Database {
    public final String UNIQUE_CONSTRAINT_VIOLATION = "23505";
    public final String NO_DATA_SET_RETURNED = "02000";
    public final String NOT_NULL_CONSTRAINT_VIOLATION = "23502";

    private Connection connection = null;

    public static void main(String[] args) {
        Database db = new Database();
        db.openConnection("jdbc:postgresql://localhost:5432/mctg", "postgres", "password");
    }



    public void openConnection(String url, String user, String password) {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void closeConnection() throws SQLException {
        connection.close();
        connection = null;
    }

    public boolean checkTokenAndUsername(String token, String username) {
        try {
            // get latest creationDate of token, that matches the parameters
            var stmt = connection.prepareStatement("select createdAt from session join users u on session.user_id = u.id where token = ? and username = ? order by createdat desc limit 1");
            stmt.setString(1, token);
            stmt.setString(2, username);
            var res = stmt.executeQuery();
            if (res.next()) {
                Timestamp time = res.getTimestamp(1);
                // Currently the token is valid for one year, which can be easily adjusted by changing this value
                long threshold = 1000 * 60 * 60 * 24 * 365L;
                return Calendar.getInstance().getTimeInMillis() - time.getTime() < threshold;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        System.err.println(String.format("Unauthorized login attempt using token: '%s' and username: '%s'", token, username));
        return false;
    }


    public boolean insertUsers(UserRecord user) {
        try {
            var statement = connection.prepareStatement("insert into users (username, password, name, bio, image) values (?,?,?,?,?)");
            statement.setString(1, user.username());
            statement.setString(2, generateHash(user.password()));
            statement.setString(3, user.name());
            statement.setString(4, user.bio());
            statement.setString(5, user.image());

            statement.execute();
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }

    }

    public boolean deleteUsers(String token, String username) {
        try {
            // If there is a user with the stated constellation of token and username, it should have at least one record - take the latest record
            var statement = connection.prepareStatement("select user_id from session join users u on session.user_id = u.id where token = ? and username = ? order by createdat desc limit 1");
            statement.setString(1, token);
            statement.setString(2, username);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                var statement2 = connection.prepareStatement("delete from users where id = ?");
                statement2.setInt(1, res.getInt(1));
                statement2.execute();
                statement2.close();
                statement.close();
                return true;
            }
            // Token does not exist - at least not with specified account
            return false;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return false;
        }
    }

    public String loginUser (String username, String password) {
        try {
            var stmt = connection.prepareStatement("select id from users where username = ? and password = ?");
            stmt.setString(1, username);
            stmt.setString(2, generateHash(password));
            var res = stmt.executeQuery();
            if (res.next()) {
                var stmt2 = connection.prepareStatement("insert into session (user_id, token, createdAt) values (?, ?, ?)");
                stmt2.setInt(1, res.getInt(1));
                stmt2.setString(2, username + "-mtcgToken");
                stmt2.setTimestamp(3, new Timestamp(Calendar.getInstance().getTimeInMillis()));
                stmt2.execute();
                stmt2.close();
                stmt.close();
                return username + "-mtcgToken";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
     * Generiert nach SHA-512 Verfahren einen Hash für den angegebenen String
     * @param str der zu hashende String
     * @return hash
     */
    private String generateHash (String str) {
        String generatedHash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            /*SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            immer derselbe salt*/
            md.update(new byte[]{0,1,0,1,1,0,1,1,1,1,0,0,1,0,1,0});
            byte[] bytes = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedHash = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedHash;
    }

    public boolean updateUser(UserRecord user, String username, String token) {
        if (!checkTokenAndUsername(token, username)) {
            return false;
        }
        try {
            // May lord forgive for what's about to happen - looked even worse than it currently is
            HashMap<String, String> userProps = user.getProperties();
            for (String key : userProps.keySet()) {
                if (userProps.get(key) != null) {
                    var statement = connection.prepareStatement("update users set " + key + " = ? where username = ?");
                    statement.setString(1, userProps.get(key));
                    statement.setString(2, username);
                    statement.executeUpdate();
                    statement.close();
                }
            }

            return true;
        } catch (SQLException sqlException) {
            //sqlException.printStackTrace();
            return false;
        }
    }
}
