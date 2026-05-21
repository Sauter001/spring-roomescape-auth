package roomescape.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import roomescape.domain.Theme;

public record ThemeRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 1000) String description,
        @NotBlank @Size(max = 500) String thumbnailUrl,
        @NotNull Long branchId) {

    public Theme toDomainForSave() {
        return new Theme(null, branchId, name, description, thumbnailUrl);
    }
}
