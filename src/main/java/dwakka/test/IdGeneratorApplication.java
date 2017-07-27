package dwakka.test;

import dwakka.test.config.ConfigurationApplication;
import dwakka.test.healthcheck.ApplicationHealthCheck;
import dwakka.test.resource.IdGeneratorResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class IdGeneratorApplication extends Application<ConfigurationApplication> {
    public static void main(String[] args) throws Exception {
        new IdGeneratorApplication().run(args);
    }

    @Override
    public String getName() {
        return "batch-application";
    }

    @Override
    public void initialize(Bootstrap<ConfigurationApplication> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(ConfigurationApplication configuration,
                    Environment environment) {

        final ApplicationHealthCheck healthCheck =
                new ApplicationHealthCheck ();
        environment.healthChecks().register("batch_health_check", healthCheck);

        environment.jersey().register(IdGeneratorResource.class);
    }
}