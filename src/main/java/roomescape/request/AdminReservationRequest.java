package roomescape.request;

import jakarta.validation.constraints.NotNull;
import roomescape.command.ReservationSaveCommand;

import java.time.LocalDate;

public record AdminReservationRequest(
        @NotNull Long userId,
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId) {

    public ReservationSaveCommand toSaveCommand() {
        return new ReservationSaveCommand(userId, date, timeId, themeId);
    }
}
