//package com.cherniva.apigateway.config;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@ConfigurationProperties(prefix = "spring.cloud.gateway.server.webmvc")
//@RefreshScope
//@Getter
//@Setter
//public class GatewayConfigProperties {
//    private List<Route> routes;
//
//    @Getter
//    @Setter
//    public static class Route {
//        private String id;
//        private String uri;
//        private List<String> predicates;
//        private List<String> filters;
//    }
//}
