
package org.fhi360.ddd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {


    @Bean
    public Docket api() {
        final List<ResponseMessage> globalResponses = Arrays.asList(
            new ResponseMessageBuilder()
                .code(200)
                .message("Request processed successfully")
                .build(),
            new ResponseMessageBuilder()
                .code(400)
                .message("Bad Request, Check request details")
                .build(),
            new ResponseMessageBuilder()
                .code(401)
                .message("Invalid request credential")
                .build(),
            new ResponseMessageBuilder()
                .code(500)
                .message("Oops, Internal Server Error")
                .build());

        return new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .globalResponseMessage(RequestMethod.GET, globalResponses)
            .globalResponseMessage(RequestMethod.POST, globalResponses)
            .globalResponseMessage(RequestMethod.DELETE, globalResponses)
            .globalResponseMessage(RequestMethod.PATCH, globalResponses)
            .select().apis(RequestHandlerSelectors.basePackage("org.fhi360.ddd.web"))
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build()
            .apiInfo(metaData());
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
            .title("DDD API")
            .description("Api Documentation")
            .version("1.0.0")
            .build();
    }
}
