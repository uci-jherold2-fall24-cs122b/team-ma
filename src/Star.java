public class Star {
    private String name;
    private String title;
    private int dob;
    private String director;
    private String genre;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getDob() {
        return dob;
    }
    public void setDob(int dob) {
        this.dob = dob;
    }


    @Override
    public String toString() {
        return "Star [name=" + name + ", dob=" + dob + "]";
    }
}
