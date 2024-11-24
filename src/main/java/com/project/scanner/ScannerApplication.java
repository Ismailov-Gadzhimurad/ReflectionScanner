package com.project.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ScannerApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(ScannerApplication.class, args);

		AnnotationScanner.scan();


	}

}
