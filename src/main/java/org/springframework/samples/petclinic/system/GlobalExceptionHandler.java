package org.springframework.samples.petclinic.system;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

public class GlobalExceptionHandler {
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<String> handleNotFound(NoResourceFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGeneralError(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body("An unexpected error occurred: " + ex.getMessage());
	}
}
