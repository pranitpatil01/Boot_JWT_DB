package com.secure.be;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
* http://www.baeldung.com/swagger-2-documentation-for-spring-rest-api
*/
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
	@Bean
	public Docket api() {
//		 ParameterBuilder aParameterBuilder = new ParameterBuilder();
//	        aParameterBuilder.name("Authorization")                 // name of header
//	                         .modelRef(new ModelRef("string"))
//	                         .parameterType("header")               // type - header
//	                         .required(true)                // for compulsory
//	                         .build();
//	        List<Parameter> aParameters = new ArrayList<>();
//	        aParameters.add(aParameterBuilder.build()); 
		
		return new Docket(DocumentationType.SWAGGER_2).select() //
				.apis(RequestHandlerSelectors.any()) //
				.paths(PathSelectors.any()) //
				.build().pathMapping("")
				.apiInfo(apiInfo())
				.securitySchemes(Arrays.asList(apiKey()));
	}

	private ApiInfo apiInfo() {
		final String version = BootJwtDbApplication.class.getPackage().getImplementationVersion();
		ApiInfo apiInfo = new ApiInfo("BOOT JWT SECURITY WITH DB", 
				"For Authentication and Authorization of boot API's .", // description
				version == null ? "version not available" : version, // version
				"Terms of service", // TOS
				new Contact("Boot Secure Dev", "http://bootsecure.org", "devteam@bootsecure.org"), // Contact
				"Apache 2.0 License", // License
				"http://www.apache.org/licenses/LICENSE-2.0"); // License URL
		return apiInfo;
	}
	
	private ApiKey apiKey() {
		final String swaggerToken = "Bearer";
	    return new ApiKey("Authorization", "Authorization", "header");
	}
}

