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
import java.util.Set;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
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
@jakarta.enterprise.context.ApplicationScoped
public class OwnerController {

	@CheckedTemplate
	public static class Templates {

		public static native TemplateInstance createOrUpdateOwnerForm(Owner owner, boolean isNew, String menu);

		public static native TemplateInstance findOwners(Owner owner, String error, String menu);

		public static native TemplateInstance ownersList(List<Owner> listOwners, int currentPage, int totalPages,
				long totalItems, String menu);

		public static native TemplateInstance ownerDetails(Owner owner, String message, String error, String menu);

	}

	private final OwnerRepository owners;

	private final Validator validator;

	@Inject
	public OwnerController(OwnerRepository owners, Validator validator) {
		this.owners = owners;
		this.validator = validator;
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm() {
		return Templates.createOrUpdateOwnerForm(new Owner(), true, "owners");
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processCreationForm(@BeanParam OwnerForm form) {
		Owner owner = new Owner();
		owner.setFirstName(form.firstName);
		owner.setLastName(form.lastName);
		owner.setAddress(form.address);
		owner.setCity(form.city);
		owner.setTelephone(form.telephone);

		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			return Response.ok(Templates.createOrUpdateOwnerForm(owner, true, "owners")).build();
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
	}

	@GET
	@Path("/find")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initFindForm() {
		return Templates.findOwners(new Owner(), null, "owners");
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response processFindForm(@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("lastName") @DefaultValue("") String lastName) {
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			Owner owner = new Owner();
			owner.setLastName(lastName);
			return Response.ok(Templates.findOwners(owner, "not found", "owners")).build();
		}

		if (ownersResults.getTotalElements() == 1) {
			Owner owner = ownersResults.iterator().next();
			return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
		}

		List<Owner> listOwners = ownersResults.getContent();
		return Response.ok(Templates.ownersList(listOwners, page, ownersResults.getTotalPages(),
				ownersResults.getTotalElements(), "owners")).build();
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	@GET
	@Path("/{ownerId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateOwnerForm(@PathParam("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return Templates.createOrUpdateOwnerForm(owner, false, "owners");
	}

	@POST
	@Path("/{ownerId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processUpdateOwnerForm(@PathParam("ownerId") int ownerId, @BeanParam OwnerForm form) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		owner.setFirstName(form.firstName);
		owner.setLastName(form.lastName);
		owner.setAddress(form.address);
		owner.setCity(form.city);
		owner.setTelephone(form.telephone);

		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			return Response.ok(Templates.createOrUpdateOwnerForm(owner, false, "owners")).build();
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{ownerId}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showOwner(@PathParam("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return Templates.ownerDetails(owner, null, null, "owners");
	}

}
