package eve_nav.model;

public class PriorityNode {
    int priority;
    int node;

    public PriorityNode() {

    }

    public PriorityNode(int priority, int node) {
        this.priority = priority;
        this.node = node;
    }

    @Override
    public String toString() {
        return "PriorityNode{" +
                "priority=" + priority +
                ", node=" + node +
                '}';
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }
}
