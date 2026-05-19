package roomescape.exception;

import roomescape.exception.code.ErrorCode;

public class ForbiddenException extends BaseException {
    public ForbiddenException(ErrorCode code) {
        super(code);
    }
}
