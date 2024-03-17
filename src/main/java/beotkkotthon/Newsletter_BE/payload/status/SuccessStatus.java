package beotkkotthon.Newsletter_BE.payload.status;

import beotkkotthon.Newsletter_BE.payload.BaseCode;
import beotkkotthon.Newsletter_BE.payload.dto.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    OK(HttpStatus.OK, "COMMON200", "Your request has been successfully performed."),
    CREATED(HttpStatus.CREATED, "COMMON201", "Resource creation successfully performed.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
            .httpStatus(httpStatus)
            .code(code)
            .message(message)
            .isSuccess(true)
            .build();

    }
}
