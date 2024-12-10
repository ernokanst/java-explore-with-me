package ru.practicum.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StatsServer {
    public static void main(String[] args) {
        System.out.println("Stats server is on");
        SpringApplication.run(StatsServer.class, args);
    }
}