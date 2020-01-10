import java.io.*;
import java.sql.*;

public class Populate {

    // Database property parameters
    private static String HOST = "";
    private static String PORT = "";
    private static String DATABASE = "";
    private static String USER = "";
    private static String PASS = "";

    // Method to set the database properties as read from the given file
    static void setDatabaseProperties(String dbFileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(dbFileName));
            String[] dbprops = new String[5];
            int i = 0;
            String line;
            while( (line = br.readLine()) != null) {
                dbprops[i] = line;
                //System.out.println(dbprops[i]);
                i++;
            }
            HOST = dbprops[0];
            PORT = dbprops[1];
            DATABASE = dbprops[2];
            USER = dbprops[3];
            PASS = dbprops[4];

        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("Successfully set database configuration!");
    }

    // Method to open connection with the set database properties
    static Connection getConnectionToDatabase() {
        Connection conn = null;

        try {
            // JDBC Driver name and database URL
            final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
            final String DB_URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false";

            // Register the driver
            Class.forName(JDBC_DRIVER);

            // Open a connection
            //System.out.println("Connecting to the database: " + DATABASE + " ...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            //System.out.println("Successfully connected to database!");

        } catch (ClassNotFoundException | SQLException e) {
            // handle errors for Class.forName() and getConnection()
            e.printStackTrace();
        }

        // Return the connection
        return conn;
    }

    // Method to populate the Zone table
    private static void populateZone(Connection conn, String[] tmp) {

        String sql = null;
        PreparedStatement ps = null;
        try {
            int zone_id_src = Integer.parseInt(tmp[0]);
            String zone_name_src = tmp[1];
            int squad_id_src = Integer.parseInt(tmp[2]);
            int area_vertex_cnt_src = Integer.parseInt(tmp[3]);
            float[] coordinates = new float[2*area_vertex_cnt_src];
            for (int i=0; i<2*area_vertex_cnt_src; i++) {
                coordinates[i] = Float.valueOf(tmp[i+4]);
            }

            // create the number of question marks needed
            StringBuilder temp_str = new StringBuilder("'POLYGON((");
            for (int i = 0; i<2*area_vertex_cnt_src; i++) {
                temp_str.append(Float.toString(coordinates[i]));
                if (i%2==0)
                    temp_str.append(" ");
                else
                    temp_str.append(",");
            }
            String lalala = coordinates[0] + " " + coordinates[1] + "))'))";
            temp_str.append(lalala);

            sql = "INSERT INTO zone VALUES (?,?,?,?,"+ "ST_GeomFromText(" + temp_str;

            ps = conn.prepareStatement(sql);

            ps.setInt(1, zone_id_src);
            ps.setString(2, zone_name_src.substring(0));
            ps.setInt(3, squad_id_src);
            ps.setInt(4, area_vertex_cnt_src);
            ps.executeUpdate();

        } catch (Exception e) {
            // handle errors for Class.forName
            e.printStackTrace();
        }
    }

    // Method to populate the Officer table
    private static void populateOfficer(Connection conn, String[] tmp) {

        String sql = null;
        PreparedStatement ps = null;
        try {
            int badge_id_src = Integer.parseInt(tmp[0]);
            String name_src = tmp[1];
            int squad_id_src = Integer.parseInt(tmp[2]);
            float long_src = Float.valueOf(tmp[3]);
            float lat_src = Float.valueOf(tmp[4]);

            sql = "INSERT INTO officer VALUES (?,?,?,POINT(?,?))";

            ps = conn.prepareStatement(sql);

            ps.setInt(1, badge_id_src);
            ps.setString(2, name_src);
            ps.setInt(3, squad_id_src);
            ps.setFloat(4, long_src);
            ps.setFloat(5, lat_src);
            ps.executeUpdate();

        } catch (Exception e) {
            // handle errors for Class.forName
            e.printStackTrace();
        }
    }

    // Method to populate the Route table
    private static void populateRoute(Connection conn, String[] tmp) {
        String sql = null;
        PreparedStatement ps = null;
        try {
            int route_id_src = Integer.parseInt(tmp[0]);
            int route_vertex_cnt_src = Integer.parseInt(tmp[1]);
            float[] coordinates = new float[2*route_vertex_cnt_src];
            for (int i=0; i<2*route_vertex_cnt_src; i++) {
                coordinates[i] = Float.valueOf(tmp[i+2]);
            }

            // create the number of question marks needed
            StringBuilder temp_str = new StringBuilder("'LINESTRING(");
            for (int i = 0; i<2*route_vertex_cnt_src; i++) {
                temp_str.append(Float.toString(coordinates[i]));
                if (i%2==0)
                    temp_str.append(" ");
                else
                    temp_str.append(",");
            }
            temp_str.deleteCharAt(temp_str.length()-1);
            temp_str.append(")'))");

            sql = "INSERT INTO route VALUES (?,?,"+ "ST_GeomFromText("+ temp_str;

            ps = conn.prepareStatement(sql);

            ps.setInt(1, route_id_src);
            ps.setInt(2, route_vertex_cnt_src);
            ps.executeUpdate();

        } catch (Exception e) {
            // handle errors for Class.forName
            e.printStackTrace();
        }
    }


    // Method to populate the Incident table
    private static void populateIncident(Connection conn, String[] tmp) {

        String sql = null;
        PreparedStatement ps = null;
        try {
            int incident_id_src = Integer.parseInt(tmp[0]);
            String incident_type_src = tmp[1];
            String long_src = tmp[2];
            String lat_src = tmp[3];

            sql = "INSERT INTO incident VALUES (?,?,POINT(?,?))";

            ps = conn.prepareStatement(sql);

            ps.setInt(1, incident_id_src);
            ps.setString(2, incident_type_src);
            ps.setString(3, long_src);
            ps.setString(4, lat_src);
            ps.executeUpdate();

        } catch (Exception e) {
            // handle errors for Class.forName
            e.printStackTrace();
        }
    }

    // Main method
    public static void main(String[] args) throws Exception {

        setDatabaseProperties(args[0]);
        Connection conn = getConnectionToDatabase();

        // Read the number of tables to be populated
        int tableCount = args.length - 1;
        //String[] tables = new String[tableCount];

        String query = "DELETE FROM zone";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);

        query = "DELETE FROM officer";
        stmt = conn.createStatement();
        stmt.executeUpdate(query);

        query = "DELETE FROM route";
        stmt = conn.createStatement();
        stmt.executeUpdate(query);

        query = "DELETE FROM incident";
        stmt = conn.createStatement();
        stmt.executeUpdate(query);

        for (int i = 1; i <= tableCount; i++) {
            try {
                // Read the filename at args[i]
                BufferedReader br = new BufferedReader(new FileReader(args[i]));

                // while the end of file is not reached, read the data line by line and populate
                String line = null;
                while ((line = br.readLine()) != null) {
                    String tmp[] = line.split(", ");

                    if (args[i].equals("zone.txt")) {
                        populateZone(conn, tmp);
                    }

                    if (args[i].equals("officer.txt")) {
                        populateOfficer(conn, tmp);
                    }

                    if (args[i].equals("route.txt")) {
                        populateRoute(conn, tmp);
                    }

                    if (args[i].equals("incident.txt")) {
                        populateIncident(conn, tmp);
                    }

                }

            } catch (Exception e) {
                // handle errors for Class.forName
                e.printStackTrace();
            } 
        }

    }
}
