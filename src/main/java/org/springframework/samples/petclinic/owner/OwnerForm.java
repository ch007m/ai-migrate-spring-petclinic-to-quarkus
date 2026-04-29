package org.springframework.samples.petclinic.owner;

import jakarta.ws.rs.FormParam;

public class OwnerForm {

	@FormParam("firstName")
	public String firstName;

	@FormParam("lastName")
	public String lastName;

	@FormParam("address")
	public String address;

	@FormParam("city")
	public String city;

	@FormParam("telephone")
	public String telephone;

}
