package io.micronaut.http.server.netty.cors

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.CollectionUtils
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.HttpClient
import io.micronaut.http.server.cors.CorsOriginConfiguration
import io.micronaut.http.server.cors.CrossOrigin
import io.micronaut.http.server.cors.CrossOriginUtil
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class CrossOriginUtilSpec extends Specification {

    private static final String SPECNAME = "CrossOriginUtilSpec"

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, ["spec.name": SPECNAME])

    @Shared
    @AutoCleanup
    HttpClient client = embeddedServer.applicationContext.createBean(HttpClient, embeddedServer.URL)

    void "test CrossOrigin on method annotation maps to CorsOriginConfiguration"() {
        when:
        HttpRequest req = HttpRequest.GET("/methodexample").accept(MediaType.TEXT_PLAIN)
        client.toBlocking().retrieve(req, String.class)
        CorsOriginConfiguration config = embeddedServer.applicationContext.getBean(TestMethodController).config

        then:
        config
        config.allowedOrigins == [ "https://foo.com" ]
        !config.allowedOriginsRegex.isPresent()
        config.allowedHeaders == [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ]
        config.exposedHeaders == [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ]
        config.allowedMethods == [ HttpMethod.GET, HttpMethod.POST ]
        !config.allowCredentials
        config.maxAge == -1L

        cleanup:
        embeddedServer.applicationContext.getBean(TestMethodController).config = null
    }

    void "test CrossOrigin with value on method annotation maps to CorsOriginConfiguration allowedOrigin"() {
        when:
        HttpRequest req = HttpRequest.GET("/anothermethod").accept(MediaType.TEXT_PLAIN)
        client.toBlocking().retrieve(req, String.class)
        CorsOriginConfiguration config = embeddedServer.applicationContext.getBean(TestMethodController).config

        then:
        config
        config.allowedOrigins == [ "https://foo.com" ]
        !config.allowedOriginsRegex
        config.allowedHeaders == CorsOriginConfiguration.ANY
        CollectionUtils.isEmpty(config.exposedHeaders)
        config.allowedMethods == CorsOriginConfiguration.ANY_METHOD
        config.allowCredentials
        config.maxAge == 1800L

        cleanup:
        embeddedServer.applicationContext.getBean(TestMethodController).config = null
    }

    void "test CrossOrigin on class annotation maps to CorsOriginConfiguration"() {
        when:
        HttpRequest req = HttpRequest.GET("/classexample").accept(MediaType.TEXT_PLAIN)
        client.toBlocking().retrieve(req, String.class)
        CorsOriginConfiguration config = embeddedServer.applicationContext.getBean(TestClassController).config

        then:
        config
        config.allowedOrigins == [ "https://bar.com" ]
        config.allowedHeaders == [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ]
        config.exposedHeaders == [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ]
        config.allowedMethods == [ HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE ]
        !config.allowCredentials
        config.maxAge == 3600L

        cleanup:
        embeddedServer.applicationContext.getBean(TestMethodController).config = null
    }

    void "test CrossOrigin with value on class annotation maps to CorsOriginConfiguration allowedOrigin"() {
        when:
        HttpRequest req = HttpRequest.GET("/anotherclass").accept(MediaType.TEXT_PLAIN)
        client.toBlocking().retrieve(req, String.class)
        CorsOriginConfiguration config = embeddedServer.applicationContext.getBean(TestAnotherClassController).config

        then:
        config
        config.allowedOrigins == [ "https://bar.com" ]
        config.allowedHeaders == CorsOriginConfiguration.ANY
        CollectionUtils.isEmpty(config.exposedHeaders)
        config.allowedMethods == CorsOriginConfiguration.ANY_METHOD
        config.allowCredentials
        config.maxAge == 1800L

        cleanup:
        embeddedServer.applicationContext.getBean(TestMethodController).config = null
    }

    @Requires(property = 'spec.name', value = SPECNAME)
    @Controller
    static class TestMethodController {
        CorsOriginConfiguration config

        @CrossOrigin(
                allowedOrigins = "https://foo.com",
                allowedHeaders = [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ],
                exposedHeaders = [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ],
                allowedMethods = [ HttpMethod.GET, HttpMethod.POST ],
                allowCredentials = false,
                maxAge = -1L
        )
        @Produces(MediaType.TEXT_PLAIN)
        @Get("/methodexample")
        String method(HttpRequest req) {
            this.config = CrossOriginUtil.getCorsOriginConfigurationForRequest(req).orElse(null)
            return "method"
        }

        @CrossOrigin(
                "https://foo.com"
                // allowedOriginsRegex = false - is the default
        )
        @Produces(MediaType.TEXT_PLAIN)
        @Get("/anothermethod")
        String example(HttpRequest req) {
            this.config = CrossOriginUtil.getCorsOriginConfigurationForRequest(req).orElse(null)
            return "anothermethod"
        }
    }

    @Requires(property = 'spec.name', value = SPECNAME)
    @Controller
    @CrossOrigin(
            allowedOrigins = "https://bar.com",
            allowedHeaders = [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ],
            exposedHeaders = [ HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION ],
            allowedMethods = [ HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE ],
            allowCredentials = false,
            maxAge = 3600L
    )
    static class TestClassController{
        CorsOriginConfiguration config

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/classexample")
        String example(HttpRequest req) {
            this.config = CrossOriginUtil.getCorsOriginConfigurationForRequest(req).orElse(null)
            return "class"
        }
    }

    @Requires(property = 'spec.name', value = SPECNAME)
    @Controller
    @CrossOrigin("https://bar.com")
    static class TestAnotherClassController{
        CorsOriginConfiguration config

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/anotherclass")
        String example(HttpRequest req) {
            this.config = CrossOriginUtil.getCorsOriginConfigurationForRequest(req).orElse(null)
            return "class"
        }
    }
}
