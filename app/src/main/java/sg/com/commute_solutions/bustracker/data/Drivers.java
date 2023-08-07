package sg.com.commute_solutions.bustracker.data;

/**
 * Created by Kyle on 27/4/17.
 */

public class Drivers {
    private String id;
    private String name;

    public Drivers(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
