package roomescape.domain;

import roomescape.command.ReservationSaveCommand;
import roomescape.exception.BadRequestException;
import roomescape.exception.code.BadRequestCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record Reservation(
        Long id,
        User user,
        LocalDate date,
        ReservationTime time,
        Theme theme) {

    public Reservation {
        validateUser(user);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
    }

    private void validateUser(User user) {
        if (Objects.isNull(user)) {
            throw new BadRequestException(BadRequestCode.INVALID_RESERVATION_USER);
        }
    }

    private void validateTheme(Theme theme) {
        if (Objects.isNull(theme)) {
            throw new BadRequestException(BadRequestCode.INVALID_RESERVATION_THEME);
        }
    }

    private void validateTime(ReservationTime time) {
        if (Objects.isNull(time)) {
            throw new BadRequestException(BadRequestCode.INVALID_RESERVATION_TIME);
        }
    }

    private void validateDate(LocalDate date) {
        if (Objects.isNull(date)) {
            throw new BadRequestException(BadRequestCode.INVALID_RESERVATION_DATE);
        }
    }

    public static Reservation forSave(ReservationSaveCommand command, User user, ReservationTime reservationTime, Theme theme) {
        return new Reservation(null, user, command.date(), reservationTime, theme);
    }

    public long userId() {
        return user.id();
    }

    public long timeId() {
        return time.id();
    }

    public long themeId() {
        return theme.id();
    }

    public boolean isOwnedBy(User other) {
        return Objects.equals(user.id(), other.id());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean isDateTimeBefore(LocalDateTime dateTime) {
        return LocalDateTime.of(this.date, this.time.startAt()).isBefore(dateTime);
    }

    public boolean isDateBefore(LocalDate today) {
        return this.date.isBefore(today);
    }
}
