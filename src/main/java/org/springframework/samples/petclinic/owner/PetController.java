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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
 * @author Wick Dynex
 */
@Path("/owners/{ownerId}/pets")
public class PetController {

	@Inject
	OwnerRepository owners;

	@Inject
	PetTypeRepository types;

	@Inject
	Template createOrUpdatePetForm;

	@GET
	@Path("new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm(@PathParam("ownerId") int ownerId) {
		Owner owner = findOwner(ownerId);
		Pet pet = new Pet();
		owner.addPet(pet);
		Collection<PetType> petTypes = this.types.findPetTypes();
		return createOrUpdatePetForm.data("pet", pet)
				.data("owner", owner)
				.data("types", petTypes);
	}

	@POST
	@Path("new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processCreationForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDateStr,
			@FormParam("type") String typeName) {

		Owner owner = findOwner(ownerId);
		Collection<PetType> petTypes = this.types.findPetTypes();
		List<String> errors = new ArrayList<>();

		Pet pet = new Pet();
		pet.setName(name);

		if (birthDateStr != null && !birthDateStr.isBlank()) {
			try {
				LocalDate birthDate = LocalDate.parse(birthDateStr);
				pet.setBirthDate(birthDate);
				if (birthDate.isAfter(LocalDate.now())) {
					errors.add("Birth date cannot be in the future");
				}
			}
			catch (Exception e) {
				errors.add("Invalid birth date format");
			}
		}
		else {
			errors.add("Birth date is required");
		}

		// Resolve pet type
		if (typeName != null && !typeName.isBlank()) {
			PetType resolvedType = resolvePetType(typeName, petTypes);
			pet.setType(resolvedType);
		}
		else {
			errors.add("Type is required");
		}

		if (name == null || name.isBlank()) {
			errors.add("Name is required");
		}

		if (name != null && !name.isBlank() && pet.isNew() && owner.getPet(name, true) != null) {
			errors.add("Name already exists");
		}

		if (!errors.isEmpty()) {
			return createOrUpdatePetForm.data("pet", pet)
					.data("owner", owner)
					.data("types", petTypes)
					.data("errors", errors);
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("{petId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		Collection<PetType> petTypes = this.types.findPetTypes();
		return createOrUpdatePetForm.data("pet", pet)
				.data("owner", owner)
				.data("types", petTypes);
	}

	@POST
	@Path("{petId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processUpdateForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDateStr,
			@FormParam("type") String typeName) {

		Owner owner = findOwner(ownerId);
		Collection<PetType> petTypes = this.types.findPetTypes();
		List<String> errors = new ArrayList<>();

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		pet.setName(name);

		if (birthDateStr != null && !birthDateStr.isBlank()) {
			try {
				LocalDate birthDate = LocalDate.parse(birthDateStr);
				pet.setBirthDate(birthDate);
				if (birthDate.isAfter(LocalDate.now())) {
					errors.add("Birth date cannot be in the future");
				}
			}
			catch (Exception e) {
				errors.add("Invalid birth date format");
			}
		}
		else {
			errors.add("Birth date is required");
		}

		if (typeName != null && !typeName.isBlank()) {
			PetType resolvedType = resolvePetType(typeName, petTypes);
			pet.setType(resolvedType);
		}

		if (name == null || name.isBlank()) {
			errors.add("Name is required");
		}

		// checking if the pet name already exists for the owner
		if (name != null && !name.isBlank()) {
			Pet existingPet = owner.getPet(name, false);
			if (existingPet != null && !Objects.equals(existingPet.getId(), pet.getId())) {
				errors.add("Name already exists");
			}
		}

		if (!errors.isEmpty()) {
			return createOrUpdatePetForm.data("pet", pet)
					.data("owner", owner)
					.data("types", petTypes)
					.data("errors", errors);
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private Owner findOwner(int ownerId) {
		return this.owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

	private PetType resolvePetType(String typeName, Collection<PetType> petTypes) {
		for (PetType type : petTypes) {
			if (Objects.equals(type.getName(), typeName)) {
				return type;
			}
		}
		return null;
	}

}
