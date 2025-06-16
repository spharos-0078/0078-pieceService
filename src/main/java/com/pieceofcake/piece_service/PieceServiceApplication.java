package com.pieceofcake.piece_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.pieceofcake.piece_service.trade.infrastructure.feign.client")
@EnableDiscoveryClient
@SpringBootApplication
public class PieceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PieceServiceApplication.class, args);
	}

}
