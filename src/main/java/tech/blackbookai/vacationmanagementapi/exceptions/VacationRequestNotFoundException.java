package tech.blackbookai.vacationmanagementapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VacationRequestNotFoundException extends RuntimeException {

    public VacationRequestNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
