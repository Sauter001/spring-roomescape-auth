package roomescape.exception.code;

import org.springframework.http.HttpStatus;

public enum ForbiddenCode implements ErrorCode {
    NOT_RESERVATION_OWNER("본인의 예약만 수정/취소할 수 있습니다."),
    ACCESS_DENIED("접근 권한이 없습니다.");

    private final String message;

    ForbiddenCode(String message) {
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
