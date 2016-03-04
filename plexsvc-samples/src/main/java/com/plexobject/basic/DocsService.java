package com.plexobject.basic;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;

import org.apache.log4j.Logger;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/docs", method = RequestMethod.GET, codec = CodecType.JSON)
// @Api(value = "/docs", description = "Documentation", authorizations = {
// @Authorization(value = "docs_auth", scopes = {
// @AuthorizationScope(scope = "write:docs", description = "modify"),
// @AuthorizationScope(scope = "read:docs", description = "read") }) }, tags =
// "docs")
public class DocsService implements RequestHandler {
    private static final Logger log = Logger.getLogger(ReverseService.class);

    public DocsService() {
        log.info("DocsService  Started");

        Info info = new Info()
                .title("Swagger Petstore")
                .description(
                        "This is a sample server Petstore server.  You can find out more about Swagger "
                                + "at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, "
                                + "you can use the api key `special-key` to test the authorization filters.")
                .termsOfService("http://swagger.io/terms/")
                .contact(new Contact().email("apiteam@swagger.io"))
                .license(
                        new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html"));

        Swagger swagger = new Swagger().info(info);
        swagger.securityDefinition("api_key", new ApiKeyAuthDefinition(
                "api_key", In.HEADER));
        swagger.securityDefinition(
                "petstore_auth",
                new OAuth2Definition()
                        .implicit("http://petstore.swagger.io/api/oauth/dialog")
                        .scope("read:pets", "read your pets")
                        .scope("write:pets", "modify pets in your account"));
        swagger.tag(new Tag()
                .name("pet")
                .description("Everything about your Pets")
                .externalDocs(
                        new ExternalDocs("Find out more", "http://swagger.io")));
        swagger.tag(new Tag().name("store").description(
                "Access to Petstore orders"));
        swagger.tag(new Tag()
                .name("user")
                .description("Operations about user")
                .externalDocs(
                        new ExternalDocs("Find out more about our store",
                                "http://swagger.io")));

        new SwaggerContextService().updateSwagger(swagger);
    }

    // @GET
    // @Path("/{docId}")
    // @ApiOperation(value = "Find by ID", notes = "Notes", response =
    // Person.class, authorizations = @Authorization(value = "api_key"))
    // @ApiResponses(value = {
    // @ApiResponse(code = 400, message = "Invalid ID supplied"),
    // @ApiResponse(code = 404, message = "not found") })
    // public Response getById(
    // @ApiParam(value = "ID", allowableValues = "range[1,10]", required = true)
    // @PathParam("id") Long petId)
    // throws NotFoundException {
    // }

    // @POST
    // @Path("/{id}")
    // @Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
    // @ApiOperation(value = "Updates", consumes =
    // MediaType.APPLICATION_FORM_URLENCODED)
    // @ApiResponses(value = { @ApiResponse(code = 405, message =
    // "Invalid input") })
    // public Response updatePetWithForm(
    // @ApiParam(value = "ID ", required = true) @PathParam("petId") Long petId,
    // @ApiParam(value = "name", required = false) @FormParam("name") String
    // name,
    // @ApiParam(value = "status", required = false) @FormParam("status") String
    // status) {
    // res.addHeader("Access-Control-Allow-Origin", "*");
    // res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
    // res.addHeader("Access-Control-Allow-Headers",
    // "Content-Type, api_key, Authorization");
    // }

    @Override
    public void handle(Request request) {
        Person person = request.getContentsAs();
        log.info("Received " + person);
        if (person == null) {
            person = new Person();
        }
        person.setId(System.currentTimeMillis());
        person.setName(person.getName() + System.currentTimeMillis());
        person.setEmail(person.getEmail() + System.currentTimeMillis()
                + "@gmail.com");
        request.getResponse().setContents(person);
    }
}