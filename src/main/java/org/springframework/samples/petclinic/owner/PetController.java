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
import java.util.Collection;
import java.util.Objects;

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
 * @author Wick Dynex
 */
@Path("/owners/{ownerId}/pets")
@ApplicationScoped
public class PetController {

	@CheckedTemplate(requireTypeSafeExpressions = false)
	public static class Templates {

		public static native TemplateInstance createOrUpdatePetForm(Owner owner, Pet pet,
				Collection<PetType> types, String error);

	}

	@Inject
	OwnerRepository owners;

	@Inject
	PetTypeRepository petTypeRepository;

	private Owner findOwner(int ownerId) {
		return this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm(@PathParam("ownerId") int ownerId) {
		Owner owner = findOwner(ownerId);
		Pet pet = new Pet();
		owner.addPet(pet);
		return Templates.createOrUpdatePetForm(owner, pet, petTypeRepository.findPetTypes(), null);
	}

	@POST
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processCreationForm(@PathParam("ownerId") int ownerId, @FormParam("name") String name,
			@FormParam("birthDate") String birthDateStr, @FormParam("type") String typeName) {

		Owner owner = findOwner(ownerId);
		Pet pet = new Pet();
		pet.setName(name);

		if (birthDateStr != null && !birthDateStr.isBlank()) {
			pet.setBirthDate(LocalDate.parse(birthDateStr));
		}

		if (typeName != null && !typeName.isBlank()) {
			PetType type = findPetType(typeName);
			pet.setType(type);
		}

		String error = validatePet(owner, pet, true);
		if (error != null) {
			return Response.ok(Templates.createOrUpdatePetForm(owner, pet, petTypeRepository.findPetTypes(), error))
				.build();
		}

		owner.addPet(pet);
		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{petId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);
		return Templates.createOrUpdatePetForm(owner, pet, petTypeRepository.findPetTypes(), null);
	}

	@POST
	@Path("/{petId}/edit")
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processUpdateForm(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId,
			@FormParam("name") String name, @FormParam("birthDate") String birthDateStr,
			@FormParam("type") String typeName) {

		Owner owner = findOwner(ownerId);
		Pet pet = owner.getPet(petId);

		if (pet == null) {
			pet = new Pet();
		}
		pet.setName(name);

		if (birthDateStr != null && !birthDateStr.isBlank()) {
			pet.setBirthDate(LocalDate.parse(birthDateStr));
		}

		if (typeName != null && !typeName.isBlank()) {
			PetType type = findPetType(typeName);
			pet.setType(type);
		}

		String error = validatePetUpdate(owner, pet);
		if (error != null) {
			return Response.ok(Templates.createOrUpdatePetForm(owner, pet, petTypeRepository.findPetTypes(), error))
				.build();
		}

		this.owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	private PetType findPetType(String typeName) {
		for (PetType type : petTypeRepository.findPetTypes()) {
			if (Objects.equals(type.getName(), typeName)) {
				return type;
			}
		}
		return null;
	}

	private String validatePet(Owner owner, Pet pet, boolean isNew) {
		if (pet.getName() == null || pet.getName().isBlank()) {
			return "Name is required";
		}
		if (isNew && pet.getType() == null) {
			return "Type is required";
		}
		if (pet.getBirthDate() == null) {
			return "Birth date is required";
		}
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(LocalDate.now())) {
			return "Birth date cannot be in the future";
		}
		if (pet.getName() != null && isNew && owner.getPet(pet.getName(), true) != null) {
			return "Pet name already exists for this owner";
		}
		return null;
	}

	private String validatePetUpdate(Owner owner, Pet pet) {
		if (pet.getName() == null || pet.getName().isBlank()) {
			return "Name is required";
		}
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(LocalDate.now())) {
			return "Birth date cannot be in the future";
		}
		if (pet.getName() != null) {
			Pet existingPet = owner.getPet(pet.getName(), false);
			if (existingPet != null && !Objects.equals(existingPet.getId(), pet.getId())) {
				return "Pet name already exists for this owner";
			}
		}
		return null;
	}

}
