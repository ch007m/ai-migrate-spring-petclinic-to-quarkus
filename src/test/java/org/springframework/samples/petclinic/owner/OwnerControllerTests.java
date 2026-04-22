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
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@QuarkusTest
class OwnerControllerTests {

	@Test
	void initCreationForm() {
		given().when().get("/owners/new").then().statusCode(200).body(containsString("Owner"));
	}

	@Test
	void processCreationFormSuccess() {
		given().redirects()
			.follow(false)
			.formParam("firstName", "Joe")
			.formParam("lastName", "Bloggs")
			.formParam("address", "123 Caramel Street")
			.formParam("city", "London")
			.formParam("telephone", "1316761638")
			.when()
			.post("/owners/new")
			.then()
			.statusCode(303);
	}

	@Test
	void initFindForm() {
		given().when().get("/owners/find").then().statusCode(200).body(containsString("Find Owners"));
	}

	@Test
	void initUpdateOwnerForm() {
		given().when()
			.get("/owners/1/edit")
			.then()
			.statusCode(200)
			.body(containsString("Owner"))
			.body(containsString("Franklin"));
	}

	@Test
	void showOwner() {
		given().when()
			.get("/owners/1")
			.then()
			.statusCode(200)
			.body(containsString("Owner Information"))
			.body(containsString("Franklin"));
	}

}
