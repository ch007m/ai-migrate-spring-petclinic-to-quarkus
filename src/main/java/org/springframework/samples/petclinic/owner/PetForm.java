package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import jakarta.ws.rs.FormParam;

public class PetForm {

	@FormParam("name")
	public String name;

	@FormParam("birthDate")
	public LocalDate birthDate;

	@FormParam("type")
	public String type;

}
