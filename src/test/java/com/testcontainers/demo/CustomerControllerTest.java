package com.testcontainers.demo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import com.intuit.karate.junit5.Karate;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class CustomerControllerTest {

  @LocalServerPort
  private Integer port;

  //  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
//    "postgres:16-alpine"
//  );
  static final DockerComposeContainer<?> postgres =
          new DockerComposeContainer<>(new File("docker-compose.yml"))
                  .withExposedService("postgres", 5432);
  @BeforeAll
  static void beforeAll() {
    postgres.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", ()->"jdbc:postgresql://localhost:5432/testdb");
    registry.add("spring.datasource.username", ()-> "testuser");
    registry.add("spring.datasource.password", ()-> "testpass");
  }

  @Autowired
  CustomerRepository customerRepository;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost:" + port;
    customerRepository.deleteAll();
  }

  @Test
  void shouldGetAllCustomers() {
    List<Customer> customers = List.of(
      new Customer(null, "John", "john@mail.com"),
      new Customer(null, "Dennis", "dennis@mail.com")
    );
    customerRepository.saveAll(customers);

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/api/customers")
      .then()
      .statusCode(200)
      .body(".", hasSize(2));
  }
  @Karate.Test
  public void testParallel() {
    Karate.run().relativeTo(getClass());;
  }
}
