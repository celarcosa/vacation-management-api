package tech.blackbookai.vacationmanagementapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VacationInvalidRequestException extends RuntimeException {

    public VacationInvalidRequestException(String message, Exception e) {
        super(message, e);
    }
}
