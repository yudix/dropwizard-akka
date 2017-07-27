package dwakka.test.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GeneratedResponse {

    @JsonProperty
    public List<String> entries;
    @JsonProperty
    public int numberOfLeads;
    @JsonProperty
    public long requestTime;



    public GeneratedResponse(int number, List<String> aggregated, long startTime) {
        entries = aggregated;
        numberOfLeads = number;
        requestTime = System.currentTimeMillis() - startTime;
    }


    @Override
    public String toString() {
        return "GeneratedResponse{" +
                "entries=" + entries +
                ", numberOfLeads=" + numberOfLeads +
                '}';
    }
}
