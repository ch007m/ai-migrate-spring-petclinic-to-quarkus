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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Path("/owners")
public class OwnerController {

	@Inject
	OwnerRepository owners;

	@Inject
	Validator validator;

	@Inject
	Template createOrUpdateOwnerForm;

	@Inject
	Template findOwners;

	@Inject
	Template ownersList;

	@Inject
	Template ownerDetails;

	@GET
	@Path("new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm() {
		return createOrUpdateOwnerForm.data("owner", new Owner());
	}

	@POST
	@Path("new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processCreationForm(
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("address") String address,
			@FormParam("city") String city,
			@FormParam("telephone") String telephone) {

		Owner owner = new Owner();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			return createOrUpdateOwnerForm.data("owner", owner)
					.data("errors", violations);
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
	}

	@GET
	@Path("find")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initFindForm() {
		return findOwners.data("owner", new Owner());
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Object processFindForm(
			@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("lastName") @DefaultValue("") String lastName) {

		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			return findOwners.data("owner", new Owner())
					.data("notFound", true);
		}

		if (ownersResults.getTotalElements() == 1) {
			Owner owner = ownersResults.iterator().next();
			return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
		}

		List<Owner> listOwners = ownersResults.getContent();
		return ownersList.data("currentPage", page)
				.data("totalPages", ownersResults.getTotalPages())
				.data("totalItems", ownersResults.getTotalElements())
				.data("listOwners", listOwners);
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	@GET
	@Path("{ownerId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateOwnerForm(@PathParam("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return createOrUpdateOwnerForm.data("owner", owner);
	}

	@POST
	@Path("{ownerId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processUpdateOwnerForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("address") String address,
			@FormParam("city") String city,
			@FormParam("telephone") String telephone) {

		Owner owner = this.owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			return createOrUpdateOwnerForm.data("owner", owner)
					.data("errors", violations);
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("{ownerId}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showOwner(@PathParam("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		return ownerDetails.data("owner", owner);
	}

}
