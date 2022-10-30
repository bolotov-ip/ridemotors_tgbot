package com.ridemotors.tgbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;

@SpringBootApplication
public class TelegramBotApplication {


	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		SpringApplication.run(TelegramBotApplication.class, args);
		System.out.println("running");

	}
}
