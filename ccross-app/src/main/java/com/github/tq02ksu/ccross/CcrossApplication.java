package com.github.tq02ksu.ccross;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CcrossApplication {

	public static void main(String[] args) {
		SpringApplication.run(CcrossApplication.class, args);
	}

	@Bean
	public CommandLineParser commandLineParser() {
		return DefaultParser.builder()
				.setAllowPartialMatching(false)
				.setStripLeadingAndTrailingQuotes(false)
				.build();
	}
}
