package com.amped.helloworld.resources;

import com.amped.helloworld.ManagedHazelcast;

import com.codahale.metrics.annotation.Timed;
import com.amped.helloworld.core.Saying;
import com.amped.helloworld.core.Template;
import com.google.common.base.Optional;
import io.dropwizard.jersey.caching.CacheControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// camel
import org.apache.camel.ProducerTemplate;

// shiro
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;;
import org.apache.shiro.authc.UsernamePasswordToken;;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldResource.class);

    private final ManagedHazelcast hazelcast;
    private final Template template;
    private final AtomicLong counter;

    private ProducerTemplate producer;

    public HelloWorldResource(Template template, ProducerTemplate producer, ManagedHazelcast hazelcast) {
        this.hazelcast = hazelcast;
        this.template = template;
        this.counter = new AtomicLong();
	this.producer = producer;
    }

    @GET
    @Timed(name = "get-requests")
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    public Saying sayHello(@QueryParam("name") Optional<String> name) {

	/** Hello Shiro
	Subject currentUser = SecurityUtils.getSubject();
	if ( !currentUser.isAuthenticated() ) {
	    logger.info("Logging in");
	    UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
	    token.setRememberMe(true);
	    currentUser.login(token);
	} else {
	    logger.info("Already Logged in");
	}
	**/

	String message = template.render(name);

	// hello camel example
	producer.asyncSendBody("direct:start", message);

	// DropWizard example
        return new Saying(counter.incrementAndGet(), message);
    }

    @POST
    public void receiveHello(@Valid Saying saying) {
        logger.info("Received a saying: {}", saying);
    }
}
