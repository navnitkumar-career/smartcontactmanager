package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmartcontactmanagerApplicationTests {

	private Calculator calculator = new Calculator();
	@Test
	void contextLoads() {
	}

	@Test
	void testSum()
	{
		int expectedResult = 25;
		int actualResult = calculator.doSum(12, 5, 3);
		
		assertThat(actualResult).isEqualTo(expectedResult);
	}
}
