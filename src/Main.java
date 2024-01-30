import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        DBConnector dc = new DBConnector();
        ResultSet rs=dc.select("select users.*, plan from users\n" +
                "join subscription s on users.subscription_id = s.subscription_id\n" +
                "where plan='Diamond'");
        viewtablefromrs(rs);
        dc.connection.close();
    }
    //? gets json string data from the api
    public static String requestapidata(String param){
        ApiConnector ac = new ApiConnector();
        return ac.getdata(param);
    }
    //? maps the json data to the UserData class
    public static List<UserData> mapUsersfromJson(String json) throws Exception{
        return new ObjectMapper().readValue(json, new TypeReference<>(){});
    }
    //? combines the 2 function to generate a variable ammount of users in the database
    public static void insertGenUsers(DBConnector dc, int usercount) throws Exception {
        List<UserData> datalist = mapUsersfromJson(requestapidata("users?size="+usercount));
        for (UserData data: datalist) {
            dc.insertUserData(data);
        }
    }
    //? makes a jtable in a jframe to view tables (result sets) returned from select queries
    public static void viewtablefromrs(ResultSet rs) throws Exception{
        JFrame fr = new JFrame();
        fr.setLocationRelativeTo(null);
        fr.setSize(800,600);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ResultSetMetaData rsmd = rs.getMetaData();
        int numcol = rsmd.getColumnCount();
        rs.last();
        int numrow = rs.getRow();

        rs.beforeFirst();
        String[] nomecol = new String[numcol];
        Object[][] data = new Object[numrow][numcol];
        int idx = 0;
        while (rs.next()) {
            for (int i = 0; i < numcol; i++) {
                Object columnValue = rs.getObject(i + 1);
                String columnName = rsmd.getColumnName(i + 1);
                nomecol[i] = columnName;
                data[idx][i] = rs.getObject(columnName);
            }
            idx++;
            System.out.println();
        }
        JTable table = new JTable(data, nomecol);
        JScrollPane scrollPane = new JScrollPane(table);
        fr.add(scrollPane);

        fr.setVisible(true);
    }
}
