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

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for {@link Pet} forms. No longer implements Spring's Validator interface.
 * Instead, provides a simple validate method that returns a list of error messages.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class PetValidator {

	/**
	 * Validate a Pet and return a list of error messages. An empty list means valid.
	 * @param pet the pet to validate
	 * @return list of error messages
	 */
	public List<String> validate(Pet pet) {
		List<String> errors = new ArrayList<>();
		String name = pet.getName();

		// name validation
		if (name == null || name.isBlank()) {
			errors.add("Name is required");
		}

		// type validation
		if (pet.isNew() && pet.getType() == null) {
			errors.add("Type is required");
		}

		// birth date validation
		if (pet.getBirthDate() == null) {
			errors.add("Birth date is required");
		}

		return errors;
	}

}
