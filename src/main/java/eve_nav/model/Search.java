package eve_nav.model;


import eve_nav.main.Functions;

import java.util.*;

public class Search {

    private List<Route> connections;

    public Search(List<Route> connections) {
        this.connections = connections;
    }

    public Route DijkstraSearch(Route route) {
        long start = System.currentTimeMillis();
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
                break;
            }

            List<Integer> neighbors = fn.getNeighbors(current, connections);
            for(Integer neighbor: neighbors) {
                int cost = weights.get(current) + 1;
                if(!weights.containsKey(neighbor) || cost < weights.get(neighbor)) {
                    weights.put(neighbor, cost);
                    int priority = cost;
                    PriorityNode node = new PriorityNode(priority, neighbor);
                    queue.add(node);
                    source.put(neighbor, current);
                }
            }
        }
        List<Integer> path = getPath(origin, destination, source, new ArrayList<Integer>());
        if(path == null) {
            route.setJumps(-1);
            route.setRoute("No Route");
            return route;
        }
        route.setJumps(path.size());
        route.setRoute(path.toString());
        long end = System.currentTimeMillis();
//        System.out.println("Origin: " + origin + " Destination: " + destination + " Jumps: " + path.size() + " Time: " + (end - start));
        return route;
    }

    public List<Integer> getPath(int origin, int destination, Map<Integer, Integer> source, List<Integer> path) {

            if (origin == destination) {
                path.add(origin);
                Collections.reverse(path);
                return path;
            }
            path.add(destination);

        try {
            return getPath(origin, source.get(destination), source, path);
        } catch(Exception e) {
            return null;
        }
    }

    public class PriorityNodeComparator implements Comparator<PriorityNode> {

        @Override
        public int compare(PriorityNode i, PriorityNode j) {
            return i.getPriority() - j.getPriority();
        }
    }


}

