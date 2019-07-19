/**
 * 
 */
package com.flow;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author sunnan
 *
 */
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class WorkFlowApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(WorkFlowApplication.class, args);
	}

}
