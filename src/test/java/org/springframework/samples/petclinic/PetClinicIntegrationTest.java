package org.springframework.samples.petclinic;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class PetClinicIntegrationTest {

    @Test
    void testWelcomePage() {
        given()
            .when().get("/")
            .then()
            .statusCode(200)
            .body(containsString("Welcome"));
    }

    @Test
    void testFindOwnersPage() {
        given()
            .when().get("/owners/find")
            .then()
            .statusCode(200)
            .body(containsString("Find Owners"));
    }

    @Test
    void testVetsJsonEndpoint() {
        given()
            .accept("application/json")
            .when().get("/vets")
            .then()
            .statusCode(200)
            .body("vetList", is(notNullValue()));
    }

    @Test
    void testVetsHtmlPage() {
        given()
            .when().get("/vets.html")
            .then()
            .statusCode(200)
            .body(containsString("Veterinarians"));
    }

    @Test
    void testOwnerDetails() {
        given()
            .when().get("/owners/1")
            .then()
            .statusCode(200)
            .body(containsString("George"))
            .body(containsString("Franklin"));
    }

    @Test
    void testFindOwnersByLastName() {
        // Should redirect to owner details when single result
        given()
            .redirects().follow(false)
            .when().get("/owners?lastName=Franklin")
            .then()
            .statusCode(303);
    }

    @Test
    void testFindOwnersMultipleResults() {
        given()
            .when().get("/owners?lastName=Davis")
            .then()
            .statusCode(200)
            .body(containsString("Davis"));
    }

    @Test
    void testNewOwnerForm() {
        given()
            .when().get("/owners/new")
            .then()
            .statusCode(200)
            .body(containsString("Owner"));
    }

    @Test
    void testOupsEndpoint() {
        given()
            .when().get("/oups")
            .then()
            .statusCode(500);
    }

    @Test
    void testHealthEndpoint() {
        given()
            .when().get("/q/health")
            .then()
            .statusCode(200)
            .body(containsString("UP"));
    }
}
