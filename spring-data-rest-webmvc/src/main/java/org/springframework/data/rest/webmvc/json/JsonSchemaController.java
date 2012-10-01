package org.springframework.data.rest.webmvc.json;

import java.io.IOException;
import java.net.URI;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.schema.JsonSchema;
import org.springframework.data.rest.repository.RepositoryExporterSupport;
import org.springframework.data.rest.repository.RepositoryMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A controller to output JSON schema based on Jackson's schema generator.
 *
 * @author Jon Brisbin
 */
public class JsonSchemaController extends RepositoryExporterSupport<JsonSchemaController> {

  private ObjectMapper mapper = new ObjectMapper();

  {
    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
  }

  @RequestMapping(
      value = "/{repository}/schema",
      method = RequestMethod.GET,
      produces = "application/json"
  )
  @ResponseBody
  public ResponseEntity<?> schemaForRepository(ServletServerHttpRequest request,
                                               URI baseUri,
                                               @PathVariable String repository) throws IOException {
    RepositoryMetadata repoMeta = repositoryMetadataFor(repository);
    if(null == repoMeta) {
      throw new IllegalArgumentException("Resource /" + repository + "/schema not found.");
    }

    JsonSchema schema = mapper.generateJsonSchema(repoMeta.domainType());

    URI requestUri = UriComponentsBuilder.fromUri(baseUri).path(repository).build().toUri();
    Resource<JsonSchema> resource = new Resource<JsonSchema>(schema,
                                                             new Link(request.getURI().toString(), "self"),
                                                             new Link(requestUri.toString(), repoMeta.rel()));

    String output = mapper.writeValueAsString(resource);

    return new ResponseEntity<String>(output, HttpStatus.OK);
  }

}
