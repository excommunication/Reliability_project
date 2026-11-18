package app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Application {

    public static void main(String[] args) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();

        System.out.println("Max Heap: " + (maxMemory / 1024 / 1024) + " MB");
        System.out.println("Total Heap: " + (totalMemory / 1024 / 1024) + " MB");
        System.out.println("Free Heap: " + (freeMemory / 1024 / 1024) + " MB");

        SpringApplication.run(Application.class, args);
    }
}
