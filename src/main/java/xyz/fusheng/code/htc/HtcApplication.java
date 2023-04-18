package xyz.fusheng.code.htc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author code-fusheng
 */
@SpringBootApplication
@ComponentScan(basePackages = "xyz.fusheng")
@MapperScan(basePackages = "xyz.fusheng.code.htc.core.mapper")
@EnableScheduling
public class HtcApplication {

    public static void main(String[] args) {
        SpringApplication.run(HtcApplication.class, args);
    }

}
