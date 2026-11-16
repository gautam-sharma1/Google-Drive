package com.cfs.Api_GateWay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGateWayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGateWayApplication.class, args);
	}
// toh ab koi bhi service direct baat nahi karegi order gatway se baat karega gateway eureka server se  then oreder service ko user service ka stuff dega
}
//now in  pom  <dependency>
//            <groupId>org.springframework.cloud</groupId>
//            <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
//        </dependency>
//         this dependency work in api gateway for api gateway
//which is api reactive