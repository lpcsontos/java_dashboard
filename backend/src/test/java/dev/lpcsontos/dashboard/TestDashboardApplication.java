package dev.lpcsontos.dashboard;

import org.springframework.boot.SpringApplication;

public class TestDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.from(DashboardApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
