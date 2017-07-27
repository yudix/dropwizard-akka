package dwakka.test.actor;


import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import dwakka.test.model.GeneratedResponse;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MyActor extends AbstractActor {

    static class JobComplete {

        private final String result;
        JobComplete(String result) {
            this.result = result;
        }
    }

    private ActorRef originalSender = null;
    private int expectedNumberOfMessages = 0;

    private List<String> results = new ArrayList<>();

    private long startTime = System.currentTimeMillis();

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(Integer.class, s -> {
                    originalSender = sender();
                    expectedNumberOfMessages = s;
                    ActorRef self = self();

                    ActorSystem system = getContext().system();

                    for (int i = 0; i < s; i++) {
                        scheduleJobComplete(self, system);
                    }

                })
                .match(JobComplete.class, (jobComplete) -> {
                            results.add(jobComplete.result);

                            if (results.size() == expectedNumberOfMessages) {
                                originalSender.tell(prepareResponse(), originalSender);
                                self().tell(PoisonPill.getInstance(),self());
                            }
                        }
                )
                .matchAny(o -> System.err.println("received unknown message"))
                .build();
    }

    private void scheduleJobComplete(ActorRef self, ActorSystem system) {
        system.scheduler().scheduleOnce(Duration.create(1000, TimeUnit.MILLISECONDS),
                () -> {
                    JobComplete complete = new JobComplete(UUID.randomUUID().toString());
                    self.tell(complete, ActorRef.noSender());
                }, system.dispatcher());
    }


    private GeneratedResponse prepareResponse() {
        return new GeneratedResponse(expectedNumberOfMessages, results, startTime);
    }
}
