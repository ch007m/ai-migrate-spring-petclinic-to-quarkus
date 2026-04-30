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
package org.springframework.samples.petclinic.vet;

import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Path("/")
public class VetController {

	@Inject
	VetService vetService;

	@Inject
	Template vetList;

	@GET
	@Path("vets.html")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showVetList(@QueryParam("page") @DefaultValue("1") int page) {
		Page<Vet> paginated = findPaginated(page);
		List<Vet> listVets = paginated.getContent();
		return vetList.data("currentPage", page)
				.data("totalPages", paginated.getTotalPages())
				.data("totalItems", paginated.getTotalElements())
				.data("listVets", listVets);
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vetService.findAll(pageable);
	}

	@GET
	@Path("vets")
	@Produces(MediaType.APPLICATION_JSON)
	public Vets showResourcesVetList() {
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetService.findAll());
		return vets;
	}

}
