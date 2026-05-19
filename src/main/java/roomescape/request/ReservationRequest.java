package roomescape.request;

import jakarta.validation.constraints.NotNull;
import roomescape.command.ReservationSaveCommand;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId) {

    public ReservationSaveCommand toSaveCommand(Long userId) {
        return new ReservationSaveCommand(userId, date, timeId, themeId);
    }
}
