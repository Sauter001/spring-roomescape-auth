package roomescape.exception;

import roomescape.exception.code.ErrorCode;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(ErrorCode code) {
        super(code);
    }
}
