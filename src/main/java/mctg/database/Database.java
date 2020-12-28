package mctg.database;

import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import mctg.Card;
import mctg.Element;
import mctg.http.Jackson.UserRecord;
import mctg.http.Jackson.CardRecord;

public class Database {
    public final String UNIQUE_CONSTRAINT_VIOLATION = "23505";
    public final String NO_DATA_SET_RETURNED = "02000";
    public final String NOT_NULL_CONSTRAINT_VIOLATION = "23502";

    private Connection connection = null;

    public static void main(String[] args) throws ClassNotFoundException {
        Database db = new Database();
        db.openConnection("jdbc:postgresql://localhost:5432/mctg", "postgres", "password");
        db.insertUsers(new UserRecord("SmegmaHunterxTrashTaste", "never", null, null, null, 20, 100.0));
        System.out.println(db.getCards("kienboec-mtcgToken", false));
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

    public boolean isTokenValid (String token) {
        try {
            // get latest creationDate of token, that matches the parameters
            var stmt = connection.prepareStatement("select createdAt from session where token = ? order by createdat desc limit 1");
            stmt.setString(1, token);
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
        System.err.printf("Given token is not valid: %s%n", token);
        return false;
    }

    public boolean tokenMatchesUsername (String token, String username) {
        try {
            // get latest creationDate of token, that matches the parameters
            var stmt = connection.prepareStatement("select createdAt from session join users u on session.user_id = u.id where token = ? and username = ? order by createdat desc limit 1");
            stmt.setString(1, token);
            stmt.setString(2, username);
            var res = stmt.executeQuery();
            if (res.next()) {
                return true;
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        System.err.printf("Unauthorized login attempt using token: '%s' and Username: '%s'%n", token, username);
        return false;
    }

    public boolean insertPackage (ArrayList<CardRecord> cardRecords, String token) {
        if (!tokenMatchesUsername(token, "admin") || !isTokenValid(token)) {
            return false;
        }
        try {
            var stmt = connection.createStatement();
            stmt.executeUpdate("insert into package default values", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            int packageId = 0;
            if (rs.next()) {
                packageId = rs.getInt(1);
            } else {
                return false;
            }
            stmt.close();
            var stmtCard = connection.prepareStatement("insert into card (id, type, damage, element) values (?, ?, ?, ?)");
            var stmtPackageCard = connection.prepareStatement("insert into package_card (package_id, card_id) values (?, ?)");
            for (CardRecord cr: cardRecords) {
                // Since the curl tests contain a weird structure, we need to adjust the code accordingly.
                // For example: There is no element member of the package/card inserts, because it is present in the name attribute.
                // That makes everything more complicated than it had to be ...
                String nameLow = cr.Name().toLowerCase();
                Element el = Element.NORMAL;
                if (nameLow.contains("water")) {
                    el = Element.WATER;
                    nameLow = nameLow.replace("water", "");
                } else if (nameLow.contains("fire")) {
                    el = Element.FIRE;
                    nameLow = nameLow.replace("fire", "");
                } else if (nameLow.contains("regular")) {
                    nameLow = nameLow.replace("regular", "");
                }
                // capitalize first character
                nameLow = nameLow.toUpperCase().charAt(0) + nameLow.substring(1);

                stmtCard.setString(1, cr.Id());
                stmtCard.setString(2, nameLow);
                stmtCard.setDouble(3, cr.Damage());
                stmtCard.setString(4, el.toString());
                stmtCard.execute();
                stmtPackageCard.setInt(1, packageId);
                stmtPackageCard.setString(2, cr.Id());
                stmtPackageCard.execute();
            }
            stmtCard.close();
            stmtPackageCard.close();
            return true;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }

    public String buyPackage (String token) {
        if (!isTokenValid(token)) {
            return "token";
        }
        try {
            var stmt = connection.prepareStatement("select coins, user_id from users join session s on users.id = s.user_id where token = ?");
            stmt.setString(1, token);
            var res = stmt.executeQuery();
            // check amount of coins
            if (res.next() && res.getInt(1) >= 5) {
                // get a random package
                var stmt2 = connection.prepareStatement("select id from package order by random() limit 1");
                var resPackage = stmt2.executeQuery();
                // if no packages are available
                if (!resPackage.next()) {
                    return "package";
                }
                int packageId = resPackage.getInt(1);

                // adjust coins
                connection.createStatement().execute("update users set coins = " + (res.getInt(1) - 5) + " where id = " + res.getInt(2));
                int userId = res.getInt(2);
                stmt.close();

                stmt2.close();

                res = connection.createStatement().executeQuery("select card_id, damage, type, element from package_card join card c on c.id = package_card.card_id where package_id = " + packageId);
                stmt2 = connection.prepareStatement("insert into stack_card (card_id, user_id, locked, indeck) VALUES (?, ?, false, false)");
                StringBuilder rarityResult = new StringBuilder();
                rarityResult
                        .append("### Ranking ")
                        .append("\uD83D\uDFCA")
                        .append(" - ")
                        .append("\uD83D\uDFCA\uD83D\uDFCA\uD83D\uDFCA\uD83D\uDFCA\uD83D\uDFCA\uD83D\uDFCA ###")
                        .append(System.lineSeparator())
                        .append("-----------------------------")
                        .append(System.lineSeparator());
                while (res.next()) {
                    stmt2.setString(1, res.getString(1));
                    stmt2.setInt(2, userId);
                    stmt2.execute();
                    double damage = res.getDouble(2);
                    StringBuilder stars = new StringBuilder();
                    for (int boundary: Card.rarityDamage.get(res.getString(3))) {
                        if (damage >= boundary) {
                            stars.append("\uD83D\uDFCA");
                        }
                    }
                    stars.append("\uD83D\uDFCA");
                    String name = String.format("%-15s", res.getString(4) + "-" + res.getString(3));
                    rarityResult.append(name).append(" - ").append(stars).append(System.lineSeparator());
                }
                stmt2.close();
                // delete package after it has been acquired
                connection.createStatement().execute("delete from package where id = " + packageId);

                return rarityResult.toString();
            }
            return "coins";
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
    }


    public boolean insertUsers(UserRecord user) {
        try {
            var statement = connection.prepareStatement("insert into users (username, password, name, bio, image, coins, elo) values (?,?,?,?,?, 20, 100.0)");
            statement.setString(1, user.Username());
            statement.setString(2, generateHash(user.Password()));
            statement.setString(3, user.Name());
            statement.setString(4, user.Bio());
            statement.setString(5, user.Image());

            statement.execute();
            return true;
        } catch (SQLException exception) {
            //exception.printStackTrace();
            return false;
        }

    }

    public boolean deleteUsers(String username) {
        /*if (!tokenMatchesUsername(token, Username) || !isTokenValid(token)) {
            return false;
        }*/
        try {
            // If there is a user with the stated constellation of token and Username, it should have at least one record - take the latest record
            var statement = connection.prepareStatement("select id from users where username = ?");
            //statement.setString(1, token);
            statement.setString(1, username);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                var statement2 = connection.prepareStatement("delete from users where id = ?");
                statement2.setInt(1, res.getInt(1));
                statement2.execute();
                statement2.close();
                statement.close();
                return true;
            }
            statement.close();
            // Token does not exist - at least not with specified account
            return false;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return false;
        }
    }

    private Card getCorrectCard (String type, double damage, Element element, String id) {
        try {
            // A little bit of reflection in my life
            Class<?> cl = Class.forName("mctg." + type);
            return (Card) cl.getDeclaredConstructor(new Class[] {double.class, Element.class, String.class}).newInstance(damage, element, id);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            //e.printStackTrace();
            // If nothing's found: Look how they've massacred my boy :(  (Because of the obligatory curl scripts)
            e.printStackTrace();
        }
        return null;
    }

    public List<Card> getCards (String token, boolean onlyInDeck) {
        if (!isTokenValid(token)) {
            return null;
        }
        try {
            var statement = connection.prepareStatement("select type, damage, element, stack_card.card_id, locked, indeck from stack_card " +
                    "join card c on stack_card.card_id = c.id " +
                    "join users u on stack_card.user_id = u.id " +
                    "join session s on u.id = s.user_id where token = ?" + (onlyInDeck ? " and indeck = true" : ""));
            statement.setString(1, token);
            var res = statement.executeQuery();
            List<Card> result = new ArrayList<>();
            while (res.next()) {
                Card card = getCorrectCard(res.getString(1), res.getDouble(2), Element.valueOf(res.getString(3).toUpperCase()), res.getString(4));
                if (card != null) {
                    card.setLockedFromUsing(res.getBoolean(5));
                    card.setInDeck(res.getBoolean(6));
                    result.add(card);
                }
            }
            return result;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return null;
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
        if (!tokenMatchesUsername(token, username) || !isTokenValid(token)) {
            return false;
        }
        try {
            // May lord forgive for what's about to happen
            HashMap<String, Object> userProps = user.getProperties();
            for (String key : userProps.keySet()) {
                if (userProps.get(key) != null) {
                    var statement = connection.prepareStatement("update users set " + key + " = ? where username = ?");
                    statement.setString(1, key.equals("password") ? generateHash((String) userProps.get(key)) : (String) userProps.get(key));
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

    public boolean updateDeck(String token, List<String> cardIds) {
        if (!isTokenValid(token)) {
            return false;
        }

        try {
            // If an error is encountered anywhere in this transaction, we would like to restore the previous state of the deck without having to manually revert all the changes
            connection.setAutoCommit(false);

            // first we need to remove all cards from the deck
            var stmt = connection.prepareStatement("select user_id from session where token = ?");
            stmt.setString(1, token);
            var res = stmt.executeQuery();
            if (!res.next()) {
                return false;
            }
            int userId = res.getInt(1);
            // userId aus der Datenbank -> keine SQL Injection möglich
            connection.createStatement().executeUpdate("update stack_card set indeck = false where user_id = " + userId);

            // not enough or too many cards?
            if (cardIds.size() != 4) {
                throw new SQLException();
            }

            stmt.close();
            stmt = connection.prepareStatement("select id from stack_card where user_id = ? and card_id = ?");
            var updateStmt = connection.prepareStatement("update stack_card set indeck = true where user_id = ? and card_id = ?");

            for (String cardId: cardIds) {
                stmt.setInt(1, userId);
                stmt.setString(2, cardId);
                if (!stmt.execute()) {
                    throw new SQLException();
                }
                updateStmt.setInt(1, userId);
                updateStmt.setString(2, cardId);
                updateStmt.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }



        return false;
    }
}
