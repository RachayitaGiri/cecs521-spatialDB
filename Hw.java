import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.System.exit;

public class Hw {

    // method to run the first query
    // QUERY: list all incidents that occurred within the given polygon
    private static void runQuery1(Connection conn, String[] args) {
        try {
            int vertex_count = Integer.parseInt(args[2]);
            if (args.length-3 != 2*vertex_count) {
                System.out.println("Error: Incorrect number of arguments passed to the query. Please check and try again.");
            }

            // constructing the polygon string
            StringBuilder temp_str = new StringBuilder("'POLYGON((");

            for (int i = 0; i<2*vertex_count; i++) {
                if (args[i+3]!=null) {
                    temp_str.append(args[i+3]);
                    if (i%2==0)
                        temp_str.append(" ");
                    else
                        temp_str.append(",");
                }
                else {
                    System.out.println("Error: Null value passed to the query. Please check and try again.");
                }
            }
            String lalala = args[3] + " " + args[4] + "))'";
            temp_str.append(lalala);

            String query = "SELECT incident_id, ST_X(incident_location) AS `lat`, ST_Y(incident_location) AS `lng`, " +
                    "incident_type FROM incident WHERE ST_Contains(ST_GeomFromText("+temp_str+"), incident_location) " +
                    "ORDER BY incident_id;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Print message if result set is empty
            if (!rs.isBeforeFirst() ) {
                System.out.println("No data returned. Empty result set object.");
            }

            // Extract the data from the ResultSet
            while (rs.next()) {

                // Retrieve by column name
                int iid = rs.getInt("incident_id");
                String lat = rs.getString("lat");
                String lng = rs.getString("lng");
                String name = rs.getString("incident_type");

                // Display values
                System.out.print(iid + "\t");
                System.out.print(lng + ", ");
                System.out.print(lat + "\t");
                System.out.println(name);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }

    }

    // method to run the second query
    // QUERY: find all officers who are within the given distance (in metres) from the given incident
    private static void runQuery2(Connection conn, String[] args) {
        try {
            String iid = args[2];
            String radius = args[3];
            String query = "SELECT o.badge_id, ROUND(ST_Distance_Sphere(i.incident_location, o.location)) AS `dist`, o.officer_name " +
                    "FROM incident i, officer o WHERE i.incident_id="+iid+" AND ST_Distance_Sphere(i.incident_location, o.location) <= "+radius+" ORDER BY `dist`;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Print message if result set is empty
            if (!rs.isBeforeFirst() ) {
                System.out.println("No data returned. Empty result set object.");
            }

            // Extract the data from the ResultSet
            while (rs.next()) {

                // Retrieve by column name
                int id = rs.getInt("badge_id");
                String dist = rs.getString("dist");
                String name = rs.getString("officer_name");

                // Display values
                System.out.print(id + "\t");
                System.out.print(dist + "m\t");
                System.out.println(name);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    // method to run the third query
    // QUERY: display the squad number assigned to the given zone and list all the officers in that squad (IN and OUT)
    private static void runQuery3(Connection conn, String[] args) {
       try {
           String sid = args[2];
           String query = "SELECT o.badge_id, IF(ST_Contains(z.zone_area, o.location),\"IN\", \"OUT\") AS `position`, o.officer_name " +
                          "FROM officer o, zone z WHERE o.squad_id="+sid+" AND o.squad_id = z.squad_id ORDER BY o.badge_id;";
           String query2 = "SELECT zone_name FROM zone WHERE squad_id="+sid+";";

           Statement stmt1 = conn.createStatement();
           Statement stmt2 = conn.createStatement();

           ResultSet rs = stmt1.executeQuery(query);
           ResultSet rs2 = stmt2.executeQuery(query2);

           // Print message if result set is empty
           if (!rs.isBeforeFirst() ||  !rs2.isBeforeFirst()) {
               System.out.println("No data returned. Empty result set object.");
           }

           //
           while (rs2.next()) {
               String squadName = rs2.getString("zone_name");
               System.out.println("Squad " + args[2] + " is now patrolling: " + squadName);
           }
           // Extract the data from the ResultSet
           while (rs.next()) {

               // Retrieve by column name
               String id = rs.getString("badge_id");
               String position = rs.getString("position");
               String name = rs.getString("officer_name");

               // Display values
               System.out.print(id+"\t");
               System.out.print(position+"\t");
               System.out.println(name);
           }


       } catch (SQLException se) {
            se.printStackTrace();
       }
    }

    // method to run the fourth query
    // QUERY: given a route number, list the zones that it passes through
    private static void runQuery4(Connection conn, String[] args) {
        try {
            String rid = args[2];
            String query = "SELECT DISTINCT z.zone_id, z.zone_name FROM zone z, route r WHERE r.route_id="+rid+" AND ST_Intersects(r.route, z.zone_area) ORDER BY z.zone_id;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Print message if result set is empty
            if (!rs.isBeforeFirst() ) {
                System.out.println("No data returned. Empty result set object.");
            }

            // Extract the data from the ResultSet
            while (rs.next()) {

                // Retrieve by column name
                String id = rs.getString("z.zone_id");
                String name = rs.getString("z.zone_name");

                // Display values
                System.out.print(id + "\t");
                System.out.println(name);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static void main(String[] args) {

        if (args.length<3) {
            System.out.println("Error: Insufficient number of arguments passed to the query. Please check and try again.");
            exit(0);
        }
        Populate db = new Populate();
        String dbFileName = args[0];

        // set the database properties and return a connection to the database
        db.setDatabaseProperties(dbFileName);
        Connection conn = db.getConnectionToDatabase();

        // read the query number and run the appropriate query
        switch (args[1]) {
            case "q1":
                runQuery1(conn, args);
                break;
            case "q2":
                runQuery2(conn, args);
                break;
            case "q3":
                runQuery3(conn, args);
                break;
            case "q4":
                runQuery4(conn, args);
                break;
            default:
                System.out.println("Invalid query number. Please check and rerun.");
                break;
        }
    }
}


