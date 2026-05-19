package roomescape.command;

import java.time.LocalDate;

public record ReservationSaveCommand(Long userId, LocalDate date, Long timeId, Long themeId) {

}
