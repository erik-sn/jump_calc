package eve_nav.main;

import eve_nav.model.Route;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int threadCount = 10;

    public static void main(String[] args) {
        Functions fn = new Functions();

        List<Integer> systems = fn.getKspaceSystems();

        // Compute all possible combinations of origin and destination
        List<Route> routes = new ArrayList<>();
        int systemCount = 1;
        for(Integer origin: systems) {
            if(systemCount < systems.size() -1 ) {
                List<Integer> newSystems = systems.subList(systemCount, systems.size() - 1);
                for (Integer destination : newSystems) {
                    Route route = new Route(origin, destination);
                    routes.add(route);
                }
                systemCount++;
            }
        }
        System.out.println("Analyzing " + routes.size() + " Routes.");

        // Get all stargate connections from static database
        List<Route> jumpConnections = fn.getJumpConnections();
        Search search = new Search(jumpConnections); // pass stargate connections to Search object for algorithm to utilize
        long start = System.currentTimeMillis();
        processRoutes(routes, search); // Begin analysis
        System.out.println("Routes Analyzed In: " + (System.currentTimeMillis() - start));

    }

    /**
     * Take in a list of routes and search object and generate threads to computer routes.
     * @param routes - List of Route objects that contain an origin and destination
     * @param search - Search object with jump connections pre-populated
     */
    private static void processRoutes(List<Route> routes, Search search) {
        if(routes != null && routes.size() != 0) {
            Functions fn = new Functions();
            Queue<Route> queue = new ConcurrentLinkedQueue<>(); // thread safe queue
            queue.addAll(routes);

            // submit jobs, limit to 10 threads
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                Connection conn = fn.getPostgresConnection(); // generate connection for each thread to use
                executorService.submit(new ProcessRoute(queue, search, conn));
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

/**
 * Multithreading runnable to compute routes and insert solutions into the database
 */
class ProcessRoute implements Runnable {
    private final Queue<Route> routes;
    private final Search search;
    private final Connection conn;

    public ProcessRoute(Queue<Route> routes, Search search, Connection conn) {
        this.routes = routes;
        this.search = search;
        this.conn = conn;
    }

    public void run() {
        Connection conn = this.conn;
        try {
            Route route = null;
            while((route = routes.poll()) != null) {
                route = search.DijkstraSearch(route);
                insertRoute(route, conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn); // use same connection object until queue is empty
        }
    }

    /** Insert a Route object into the database
     *
     * @param route - Fully populated Route object (with path and jumps)
     * @param conn - Postgres connection object
     */
    private void insertRoute(Route route, Connection conn) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("INSERT INTO jumpdistance (system1, system2, route, jumps) VALUES(?, ?, ?, ?)");
            st.setInt(1, route.getOrigin());
            st.setInt(2, route.getDestination());
            st.setString(3, route.getRoute());
            st.setInt(4, route.getJumps());
            st.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(st);
        }
    }
}


