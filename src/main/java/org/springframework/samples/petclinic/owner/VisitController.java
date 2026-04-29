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

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
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
@jakarta.enterprise.context.ApplicationScoped
public class VisitController {

	@CheckedTemplate
	public static class Templates {

		public static native TemplateInstance createOrUpdateVisitForm(Owner owner, Pet pet, Visit visit, String error,
				String menu);

	}

	private final OwnerRepository owners;

	@Inject
	public VisitController(OwnerRepository owners) {
		this.owners = owners;
	}

	private Owner findOwner(int ownerId) {
		return this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
	}

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
		return Templates.createOrUpdateVisitForm(owner, pet, visit, null, "owners");
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processNewVisitForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId,
			@BeanParam VisitForm form) {
		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}

		Visit visit = new Visit();
		visit.setDate(form.date);
		visit.setDescription(form.description);

		// Basic validation
		if (visit.getDescription() == null || visit.getDescription().isBlank()) {
			return Response.ok(Templates.createOrUpdateVisitForm(owner, pet, visit, "Description is required", "owners")).build();
		}

		pet.addVisit(visit);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

}
