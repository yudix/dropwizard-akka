package dwakka.test.resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.PatternsCS;
import com.codahale.metrics.annotation.Timed;
import dwakka.test.actor.MyActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Path("v3")
public class IdGeneratorResource {

    private final static Logger logger = LoggerFactory.getLogger(IdGeneratorResource.class);

    private final static ExecutorService executorService = Executors.newFixedThreadPool(8);
    private final static ActorSystem system = ActorSystem.create("akka-system");

    private Set<String> repository = new HashSet<>();

    @GET
    @Timed(name = "generateIds")
    @Path("generate/{number}")
    @Produces(MediaType.APPLICATION_JSON)
    public void generateIds (@PathParam("number") int number, @Suspended final AsyncResponse asyncResponse) {

        setTimeout(number, asyncResponse);

        registerOnComplete(asyncResponse);

        submitJob(number, asyncResponse);

    }

    private void submitJob(int number, AsyncResponse asyncResponse) {
        executorService.submit(() -> {

            ActorRef worker = system.actorOf(Props.create(MyActor.class));
            PatternsCS.ask(worker, Integer.valueOf(number), 5000)
                    .toCompletableFuture()
            .thenAccept((response) -> asyncResponse.resume(response));
        });
    }

    private void registerOnComplete(@Suspended AsyncResponse asyncResponse) {
        asyncResponse.register(
                (CompletionCallback) throwable -> {
                    if (throwable == null) {
                        logger.debug("dispatched to client thread id {}", Thread.currentThread().getId());
                    } else {
                        logger.error("An error has occurred during request processing");
                        asyncResponse.resume(throwable);
                    }
                },
                (ConnectionCallback) disconnected -> {
                    logger.error("Connection lost or closed by the client!");
                }
        );
    }

    private void setTimeout(int numberOfRequests, AsyncResponse asyncResponse) {
        asyncResponse.setTimeoutHandler(asyncResponse1 -> {
            logger.error("Timeout on request with {} ", numberOfRequests);
            asyncResponse1.resume(Response.Status.SERVICE_UNAVAILABLE);
        });
        asyncResponse.setTimeout(10, TimeUnit.SECONDS);
    }


    @POST
    @Path("ids")
    public Response saveIds (@Context HttpServletRequest request, Set<String> ids) throws Exception {

        if(repository.size() != 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        repository = ids;

        return Response.created(new URI(request.getRequestURI())).build();
    }

    @PUT
    @Path("ids")
    public Response update (@Context HttpServletRequest request, Set<String> ids) throws Exception {

        if(repository.size() == 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        repository.clear();
        repository.addAll(ids);

        return Response.accepted(new URI(request.getRequestURI())).build();
    }


    @DELETE
    public Response delete (String id) {

        boolean remove = repository.remove(id);

        return Response.ok(remove).build();

    }

}
