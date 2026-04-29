/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@QuarkusTest
class VisitControllerTests {

	@Test
	void initNewVisitForm() {
		given().when()
			.get("/owners/1/pets/1/visits/new")
			.then()
			.statusCode(200)
			.body(containsString("Visit"));
	}

	@Test
	void processNewVisitFormSuccess() {
		given().redirects()
			.follow(false)
			.formParam("date", "2024-01-15")
			.formParam("description", "Visit Description")
			.when()
			.post("/owners/1/pets/1/visits/new")
			.then()
			.statusCode(303);
	}

}
