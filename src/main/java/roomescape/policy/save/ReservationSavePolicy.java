package roomescape.policy.save;

import roomescape.domain.Reservation;

import java.time.LocalDateTime;

@FunctionalInterface
public interface ReservationSavePolicy {
    void validate(Reservation reservation, LocalDateTime now);
}
