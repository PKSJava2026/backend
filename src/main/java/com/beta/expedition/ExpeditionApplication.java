package com.beta.expedition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

import java.util.Scanner;

@SpringBootApplication
public class ExpeditionApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(ExpeditionApplication.class, args);
	}

	@Override 
	public void run(String... args) {
		Scanner scanner = new Scanner(System.in);
		boolean running = true;

		System.out.println("Chao! How are you today?");
		while(running) {
			System.out.println();
			System.out.println("Please, chose any option of the following");
			System.out.println("1. Users");
			System.out.println("2. Shipments");
			System.out.println("0. Exit");

			String input = scanner.nextLine().trim();
			System.out.println();

			switch(input) {
				case "0" -> running = false;
				case "1" -> System.out.println("Users: nothing so far");
				case "2" -> System.out.println("Shipments: nothing so far");
				default -> System.out.println("Incorrect choice");
			}
		}
		scanner.close();
	}

}
