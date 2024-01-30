import java.sql.*;
import java.util.Map;
import java.util.StringJoiner;

public class DBConnector {
    Connection connection;
    public DBConnector() throws SQLException, ClassNotFoundException {
        //? db credentials
        String url = "jdbc:mysql://localhost:3306/GeneratedUsersDB";
        String username = "root";
        String password = "";
        Class.forName("com.mysql.cj.jdbc.Driver");
        //? initialize db connection not using auto commit as failsafe (allows rollback) for insert
        try{
            connection = DriverManager.getConnection(url, username, password);
            connection.setAutoCommit(false);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void insertUserData(UserData data) throws SQLException {
        try {
            //? before inserting the user gets its auto generated id from the other tables while also inserting the data
            int employmentId = insertGetKeys(connection, "Employment",  Map.of("title",data.getEmployment().getTitle()));
            int addressId = insertGetKeys(connection, "Address", Map.of("country",data.getAddress().getCountry(),"city",data.getAddress().getCity()));
            int subscriptionId = insertGetKeys(connection, "subscription", Map.of("plan",data.getSubscription().getPlan(),"term",data.getSubscription().getTerm()));
            int creditCardId = insertGetKeys(connection, "creditcard", Map.of("cc_number",data.getCredit_card().getCc_number()));
            //? user insert using prepared statements to prevent sql injections (optional)
            String sql = "INSERT INTO Users (first_name, last_name, email, date_of_birth, employment_id, address_id, credit_card_id, subscription_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, data.getFirst_name());
                statement.setString(2, data.getLast_name());
                statement.setString(3, data.getEmail());
                statement.setDate(4, Date.valueOf(data.getDate_of_birth()));
                statement.setInt(5, employmentId);
                statement.setInt(6, addressId);
                statement.setInt(7, creditCardId);
                statement.setInt(8, subscriptionId);

                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }
    //? gets the row id (pk) of the newly inserted value
    private static int insertGetKeys(Connection connection, String tableName, Map<String, String> insertvalues) throws SQLException {
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        for (String column : insertvalues.keySet()) {
            columnNames.add(column);
            placeholders.add("?");
        }

        String sql = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (String value : insertvalues.values()) {
                statement.setString(index++, value);
            }
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Inserting data failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Retrieve the generated key
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
    //? standard select query (leaves result set open for further operations)
    public ResultSet select(String query) throws Exception{
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(query);
    }
}
