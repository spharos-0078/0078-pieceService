package com.pieceofcake.piece_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
//@EnableDiscoveryClient
@SpringBootApplication
public class PieceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PieceServiceApplication.class, args);
	}

}
