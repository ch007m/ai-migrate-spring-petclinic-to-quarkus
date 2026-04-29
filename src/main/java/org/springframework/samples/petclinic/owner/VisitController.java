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

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
@ApplicationScoped
public class VisitController {

	@CheckedTemplate(requireTypeSafeExpressions = false)
	public static class Templates {

		public static native TemplateInstance createOrUpdateVisitForm(Owner owner, Pet pet, Visit visit, String error);

	}

	@Inject
	OwnerRepository owners;

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initNewVisitForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}
		Visit visit = new Visit();
		return Templates.createOrUpdateVisitForm(owner, pet, visit, null);
	}

	@POST
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processNewVisitForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId,
			@FormParam("date") String dateStr, @FormParam("description") String description) {

		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}

		Visit visit = new Visit();
		if (dateStr != null && !dateStr.isBlank()) {
			visit.setDate(LocalDate.parse(dateStr));
		}
		visit.setDescription(description);

		if (description == null || description.isBlank()) {
			return Response.ok(Templates.createOrUpdateVisitForm(owner, pet, visit, "Description is required")).build();
		}

		pet.addVisit(visit);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Owner findOwner(int ownerId) {
		return owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

}
