package eve_nav.main;

import eve_nav.model.Route;
import eve_nav.model.Search;
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

    public static void main(String[] args) {
        Functions fn = new Functions();

        Connection eveNavConn = fn.getUniverseConnection();
        List<Integer> systems = fn.getKspaceSystems(eveNavConn);

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

        List<Route> jumpConnections = fn.getJumpConnections();
        Search search = new Search(jumpConnections);
        long start = System.currentTimeMillis();
        processRoutes(routes, search);
        System.out.println("Routes Analyzed In: " + (System.currentTimeMillis() - start));

    }

    private static void processRoutes(List<Route> routes, Search search) {
        if(routes != null && routes.size() != 0) {
            Functions fn = new Functions();
            Queue<Route> queue = new ConcurrentLinkedQueue<>();
            queue.addAll(routes);

            // submit jobs, limit to 20 threads
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                Connection conn = fn.getPostgresConnection();
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
            DbUtils.closeQuietly(conn);
        }
    }

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


