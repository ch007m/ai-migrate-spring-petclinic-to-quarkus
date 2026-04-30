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
package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the clinic service (repository layer).
 */
@QuarkusTest
class ClinicServiceTests {

	@Inject
	OwnerRepository owners;

	@Inject
	PetTypeRepository petTypes;

	@Inject
	VetRepository vets;

	@Test
	void shouldFindOwnersByLastName() {
		Pageable pageable = PageRequest.of(0, 5);
		Page<Owner> result = this.owners.findByLastNameStartingWith("Davis", pageable);
		assertThat(result.getTotalElements()).isEqualTo(2);
	}

	@Test
	void shouldFindSingleOwnerWithPet() {
		Optional<Owner> optOwner = this.owners.findById(1);
		assertThat(optOwner).isPresent();
		Owner owner = optOwner.get();
		assertThat(owner.getLastName()).startsWith("Franklin");
		assertThat(owner.getPets()).hasSize(1);
		assertThat(owner.getPets().get(0).getType()).isNotNull();
		assertThat(owner.getPets().get(0).getType().getName()).isEqualTo("cat");
	}

	@Test
	@Transactional
	void shouldInsertOwner() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<Owner> ownersBefore = this.owners.findByLastNameStartingWith("Schultz", pageable);
		int found = (int) ownersBefore.getTotalElements();

		Owner owner = new Owner();
		owner.setFirstName("Sam");
		owner.setLastName("Schultz");
		owner.setAddress("4, Evans Street");
		owner.setCity("Wollongong");
		owner.setTelephone("4444444444");
		this.owners.save(owner);
		assertThat(owner.getId()).isNotNull();

		Page<Owner> ownersAfter = this.owners.findByLastNameStartingWith("Schultz", pageable);
		assertThat(ownersAfter.getTotalElements()).isEqualTo(found + 1);
	}

	@Test
	@Transactional
	void shouldUpdateOwner() {
		Optional<Owner> optOwner = this.owners.findById(1);
		assertThat(optOwner).isPresent();
		Owner owner = optOwner.get();

		String oldLastName = owner.getLastName();
		String newLastName = oldLastName + "X";
		owner.setLastName(newLastName);
		this.owners.save(owner);

		Optional<Owner> optUpdated = this.owners.findById(1);
		assertThat(optUpdated).isPresent();
		assertThat(optUpdated.get().getLastName()).isEqualTo(newLastName);
	}

	@Test
	void shouldFindAllPetTypes() {
		List<PetType> petTypesList = this.petTypes.findPetTypes();
		assertThat(petTypesList).hasSizeGreaterThan(0);
		PetType petType1 = petTypesList.get(0);
		assertThat(petType1.getName()).isNotNull();
	}

	@Test
	@Transactional
	void shouldInsertPetIntoDatabaseAndGenerateId() {
		Optional<Owner> optOwner = this.owners.findById(6);
		assertThat(optOwner).isPresent();
		Owner owner6 = optOwner.get();
		int found = owner6.getPets().size();

		Pet pet = new Pet();
		pet.setName("bowser");
		Collection<PetType> allTypes = this.petTypes.findPetTypes();
		pet.setType(allTypes.iterator().next());
		pet.setBirthDate(LocalDate.now());
		owner6.addPet(pet);
		assertThat(owner6.getPets()).hasSize(found + 1);

		this.owners.saveAndFlush(owner6);

		// Re-fetch to verify pet was persisted
		Optional<Owner> refreshed = this.owners.findById(6);
		assertThat(refreshed).isPresent();
		assertThat(refreshed.get().getPets()).hasSize(found + 1);
	}

	@Test
	void shouldFindVets() {
		List<Vet> vetList = this.vets.findAll();
		assertThat(vetList).isNotEmpty();
		Vet vet = vetList.get(0);
		assertThat(vet.getLastName()).isNotNull();
	}

	@Test
	@Transactional
	void shouldAddNewVisitForPet() {
		Optional<Owner> optOwner = this.owners.findById(1);
		assertThat(optOwner).isPresent();
		Owner owner = optOwner.get();
		assertThat(owner.getPets()).isNotEmpty();
		Pet pet = owner.getPets().get(0);
		int found = pet.getVisits().size();

		Visit visit = new Visit();
		visit.setDescription("test visit");
		pet.addVisit(visit);
		this.owners.saveAndFlush(owner);

		// Re-fetch to verify visit was persisted
		Optional<Owner> refreshed = this.owners.findById(1);
		assertThat(refreshed).isPresent();
		Pet refreshedPet = refreshed.get().getPets().get(0);
		assertThat(refreshedPet.getVisits()).hasSize(found + 1);
	}

}
