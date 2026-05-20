package roomescape.exception.code;

import org.springframework.http.HttpStatus;

public enum UnauthorizedCode implements ErrorCode {
    LOGIN_FAILED("로그인 실패. 아이디 혹은 비밀번호가 일치하지 않습니다."),
    LOGIN_REQUIRED("로그인이 필요합니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다.");

    private final String message;

    UnauthorizedCode(String message) {
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
