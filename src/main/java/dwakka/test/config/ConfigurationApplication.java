package dwakka.test.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;


public class ConfigurationApplication extends Configuration {



    @NotNull
    private String metricsHost;

    @JsonProperty
    public String getMetricsHost() {
        return metricsHost;
    }

    @JsonProperty
    public void setMetricsHost(String metricsHost) {
        this.metricsHost = metricsHost;
    }
}
