package eve_nav.main;

import eve_nav.info.Info;
import eve_nav.model.Route;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Functions {

    /**
     * Return a List of routes where origin and destination are solar system type IDs. Each route represents
     * a star gate in the eve universe
     * @return
     */
    public List<Route> getJumpConnections() {
        Connection conn = getUniverseConnection();
        PreparedStatement pstmt = null;
        List<Route> jumpConnections = new ArrayList<>();
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("SELECT fromSolarSystemID, toSolarSystemID FROM mapSolarSystemJumps ORDER BY fromSolarSystemID");
            rs = pstmt.executeQuery();
            while(rs.next()) {
                Route route = new Route();
                route.setOrigin(rs.getInt(1));
                route.setDestination(rs.getInt(2));
                jumpConnections.add(route);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pstmt);
        }
        return jumpConnections;
    }

    /**
     * Find a list of all solar systems that neighbor the origin system
     * @param origin - solar system type ID
     * @param jumpConnections - list of solar system type IDs that have a connecting stargate to the origin
     * @return list of solar system type IDs
     */
    public List<Integer> getNeighbors(int origin, List<Route> jumpConnections) {
        List<Integer> connections = new ArrayList<>();
        for(Route route: jumpConnections) {
            if(route.getOrigin() == origin) {
                connections.add(route.getDestination());
            }
        }
        return connections;
    }

    /**
     * Find all K-Space solar systems that are NOT in Jove space
     * @return list of solar system type IDs
     */
    public List<Integer> getKspaceSystems() {
        Connection conn = getUniverseConnection();
        PreparedStatement pstmt = null;
        List<Integer> systems = new ArrayList<>();
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement("SELECT solarSystemID FROM mapSolarSystems WHERE solarSystemID <= 31000000 " +
                    "AND regionID <> 10000004 AND regionID <> 10000017 AND regionID <> 10000019 ORDER BY solarSystemID ASC");
            rs = pstmt.executeQuery();
            while(rs.next()) {
                systems.add(rs.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(pstmt);
        }
        return systems;
    }

    /**
     * Get a connection object to the static sqllite database
     * @return SQL Connection
     */
    public Connection getUniverseConnection() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(Info.universeUrl);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Get a connection object to the postgres database
     * @return SQL connection
     */
    public Connection getPostgresConnection() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(Info.postgresUrl, Info.postgresUser, Info.postgresPass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
