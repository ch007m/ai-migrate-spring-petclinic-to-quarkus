package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import jakarta.ws.rs.FormParam;

public class VisitForm {

	@FormParam("date")
	public LocalDate date;

	@FormParam("description")
	public String description;

}
