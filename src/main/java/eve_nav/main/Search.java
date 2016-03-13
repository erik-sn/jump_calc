package eve_nav.main;


import eve_nav.main.Functions;
import eve_nav.model.PriorityNode;
import eve_nav.model.Route;

import java.util.*;

public class Search {

    private List<Route> connections;

    public Search(List<Route> connections) {
        this.connections = connections;
    }

    /** Implementation of Dijkstra's algorithm ( https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm  )
     *
     *  Takes in a route that contains an origin and destination - these are Type IDs for solar systems from the eve
     *  universe static database dump ( https://www.fuzzwork.co.uk/dump/ ). Uses a Priority queue to hold nodes (vertices)
     *  and evaluates their jump connections (edges). Most solutions in the EVE universe are < 0.5ms.
     * @param route
     * @return
     */
    public Route DijkstraSearch(Route route) {
        Functions fn = new Functions();
        int origin = route.getOrigin();
        int destination = route.getDestination();

        Comparator<PriorityNode> comparator= new PriorityNodeComparator();
        PriorityQueue<PriorityNode> queue = new PriorityQueue<>(comparator);
        queue.add(new PriorityNode(0, origin));
        Map<Integer, Integer> source = new HashMap<>();
        Map<Integer, Integer> weights = new HashMap<>();
        source.put(origin, 0);
        weights.put(origin, 0);

        PriorityNode currentNode = null;
        while ((currentNode = queue.poll()) != null) {
            int current = currentNode.getNode();
            if(current == destination) {
                break; // if we find a solution break the loop, shortest was found
            }

            List<Integer> neighbors = fn.getNeighbors(current, connections); // find all neighbor systems
            for(Integer neighbor: neighbors) {
                int jumps = weights.get(current) + 1;
                // if we have not been to this system OR we have, but now the jumps are lower
                if(!weights.containsKey(neighbor) || jumps < weights.get(neighbor)) {
                    weights.put(neighbor, jumps);
                    int priority = jumps;
                    PriorityNode node = new PriorityNode(priority, neighbor);
                    queue.add(node);
                    source.put(neighbor, current); // where we are going, where we are now
                }
            }
        }
        List<Integer> path = getPath(origin, destination, source, new ArrayList<Integer>());
        // no solution was found, route does not exist (W-space, Jove space)
        if(path == null) {
            route.setJumps(-1);
            route.setRoute("No Route");
            return route;
        }
        route.setJumps(path.size());
        route.setRoute(path.toString());
        return route;
    }

    /**
     * Recursive function to retrieve a user-friendly path list from the source object.
     * @param origin - solar system type ID of starting point
     * @param destination - solar system type ID of destination
     * @param source - Map object that contains key/pairs representing a solar system and the solar system that was
     *               traveled from to get there
     * @param path - Recursively populated integer list representing the path taken
     * @return List<Integer> - user friendly list of solar system type IDs in the order they were traveled through
     */
    public List<Integer> getPath(int origin, int destination, Map<Integer, Integer> source, List<Integer> path) {
        try {
            if (origin == destination) {
                path.add(origin);
                Collections.reverse(path); // we are working destination to origin so must reverse list
                return path;
            }
            path.add(destination);
            return getPath(origin, source.get(destination), source, path);

        } catch(Exception e) {
            return null; // no solution found
        }
    }

    /**
     * Comparator used by priority queue to adjust queue position for Node objects based on their priority
     */
    public class PriorityNodeComparator implements Comparator<PriorityNode> {
        @Override
        public int compare(PriorityNode i, PriorityNode j) {
            return i.getPriority() - j.getPriority();
        }
    }


}

