package roomescape.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record LoginRequest(
        @NotBlank
        String uid,

        @NotBlank
        @Length(min = 8, message = "비밀번호 길이는 8자 이상이어야 합니다.")
        String password) {
}
