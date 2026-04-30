package org.springframework.samples.petclinic.vet;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service layer for Vet operations with caching support.
 */
@ApplicationScoped
public class VetService {

	@Inject
	VetRepository vetRepository;

	@Cacheable("vets")
	public List<Vet> findAll() {
		return vetRepository.findAll();
	}

	@Cacheable("vets")
	public Page<Vet> findAll(Pageable pageable) {
		return vetRepository.findAll(pageable);
	}

}
