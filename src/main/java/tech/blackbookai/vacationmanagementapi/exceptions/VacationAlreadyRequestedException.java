package tech.blackbookai.vacationmanagementapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VacationAlreadyRequestedException extends RuntimeException {

    public VacationAlreadyRequestedException(String message, Exception e) {
        super(message, e);
    }
}
