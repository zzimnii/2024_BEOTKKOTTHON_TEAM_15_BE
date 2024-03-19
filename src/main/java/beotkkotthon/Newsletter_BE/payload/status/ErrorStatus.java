package beotkkotthon.Newsletter_BE.payload.status;

import beotkkotthon.Newsletter_BE.payload.BaseErrorCode;
import beotkkotthon.Newsletter_BE.payload.dto.ErrorReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // Common_Error
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON4000", "Bad request"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON4001",  "Validation error"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON4002", "Requested resource not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5000",  "Internal error"),
    DATA_ACCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON5001",  "Data access error"),

    // Member_Error
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER4000", "Email already exists"),
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001","MEMBER not found"),

    // Team Error
    TEAM_NOT_FOUND(HttpStatus.BAD_REQUEST, "TEAM4000", "TEAM not found"),

    //
    NEWS_NOT_FOUND(HttpStatus.BAD_REQUEST, "NEWS4000", "News not found"),
    // Token Error
    INVALID_TOKEN(HttpStatus.BAD_REQUEST,"TOKEN4000", "Invalid token"),

    // Security Error
    NOT_FOUND_CONTEXT(HttpStatus.NOT_FOUND,"Security4000", "SecurityContext not found");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    public String getMessage(String message) {
        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.getMessage());
    }

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
            .message(message)
            .code(code)
            .isSuccess(false)
            .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
            .message(message)
            .code(code)
            .isSuccess(false)
            .httpStatus(httpStatus)
            .build();
    }
}
