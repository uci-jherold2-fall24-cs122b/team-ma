public class StarInMovie {
    private String name;
    private String movieId;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    @Override
    public String toString() {
        return "Star [name=" + name + ", movie id=" + movieId + "]";
    }
}
