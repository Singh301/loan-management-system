package com.sudhanshu.loanmanagement;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full application context test.
 * Disabled by default – needs a running MySQL with correct credentials.
 * Prefer unit tests or Testcontainers integration tests.
 */
@SpringBootTest
@Disabled("Requires running MySQL. Use unit tests or Testcontainers instead.")
class LoanManagementSystemApplicationTests {

	@Test
	void contextLoads() {
	}
}