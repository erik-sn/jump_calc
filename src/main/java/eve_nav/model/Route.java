package eve_nav.model;

public class Route {

    private int origin;
    private int destination;
    private int jumps;
    private String route;
    private int priority;

    public Route() {

    }

    public Route(int origin, int destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Route{" +
                "origin=" + origin +
                ", destination=" + destination +
                ", jumps=" + jumps +
                ", route='" + route + '\'' +
                '}';
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getJumps() {
        return jumps;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
