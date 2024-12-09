package com.hmdrinks.Exception;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends RuntimeException {
    private String message;

    public NotFoundException(String e) {
        super(e);
        this.message = e;
    }
}
