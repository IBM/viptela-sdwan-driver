package com.ibm.sdwan.viptela.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;


@Configuration
public class SwaggerConfiguration {

    @Value("${info.app.name:Viptela SDWAN Driver}")
    private String appName;

    @Value("${info.app.description:CP4NA driver to communicate with CISCO Viptela vManage}")
    private String appDescription;

    @Value("${info.app.version:0.0.1}")
    private String appVersion;

    @Value("${info.contact.name:IBM}")
    private String contactName;

   // @Value("${swagger.server-url}")
    private String serverUrl;

    @Bean
  public OpenAPI springShopOpenAPI() {
      return new OpenAPI()
              .info(new Info().title(appName)
              .description(appDescription)
              .version(appVersion));
              
  }
}
