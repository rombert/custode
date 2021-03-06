package nu.muntea.custode.storage.api;

public final class Measurement {

    private final String name;
    private final long degreesCelsius;

    public Measurement(String name, long degreesCelsius) {
        this.name = name;
        this.degreesCelsius = degreesCelsius;
    }

    public String getName() {
        return name;
    }

    public long getDegreesCelsius() {
        return degreesCelsius;
    }

}
