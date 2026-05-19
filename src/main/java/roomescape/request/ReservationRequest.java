package roomescape.request;

import jakarta.validation.constraints.NotNull;
import roomescape.command.ReservationSaveCommand;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId) {

    public ReservationSaveCommand toSaveCommand(String username) {
        return new ReservationSaveCommand(username, date, timeId, themeId);
    }
}
