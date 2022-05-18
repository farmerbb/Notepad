package Branch;

public class new_branch_br {
    String name;
    int id;
    int roll;
    double cgpa;
    int position;


    public new_branch_br(String name, int id, int roll, double cgpa, int position) {
        this.name = name;
        this.id = id;
        this.roll = roll;
        this.cgpa = cgpa;
        this.position = position;
    }
    public new_branch_br(String name, int id, int roll) {
        this.name = name;
        this.id = id;
        this.roll = roll;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }
}
