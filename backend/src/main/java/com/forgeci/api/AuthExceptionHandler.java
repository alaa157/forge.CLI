package com.forgeci.api;
import com.forgeci.application.auth.AuthException;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class AuthExceptionHandler {
 @ExceptionHandler(AuthException.class) ResponseEntity<ProblemDetail> auth(AuthException e){int status=switch(e.getCode()){case "DUPLICATE_EMAIL"->409;case "INVALID_EMAIL","WEAK_PASSWORD","INVALID_DISPLAY_NAME"->400;default->401;};var p=ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status),e.getMessage());p.setTitle(e.getCode());return ResponseEntity.status(HttpStatus.valueOf(status)).body(p);}
}
