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

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 */
@Path("/owners/{ownerId}/pets/{petId}/visits")
public class VisitController {

	@Inject
	OwnerRepository owners;

	@Inject
	Template createOrUpdateVisitForm;

	@GET
	@Path("new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initNewVisitForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}
		Visit visit = new Visit();
		return createOrUpdateVisitForm.data("pet", pet)
				.data("owner", owner)
				.data("visit", visit);
	}

	@POST
	@Path("new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("date") String dateStr,
			@FormParam("description") String description) {

		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}

		Visit visit = new Visit();
		List<String> errors = new ArrayList<>();

		if (dateStr != null && !dateStr.isBlank()) {
			try {
				visit.setDate(LocalDate.parse(dateStr));
			}
			catch (Exception e) {
				errors.add("Invalid date format");
			}
		}

		visit.setDescription(description);

		if (description == null || description.isBlank()) {
			errors.add("Description is required");
		}

		if (!errors.isEmpty()) {
			return createOrUpdateVisitForm.data("pet", pet)
					.data("owner", owner)
					.data("visit", visit)
					.data("errors", errors);
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Owner findOwner(int ownerId) {
		return this.owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

}
