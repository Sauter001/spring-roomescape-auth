package roomescape.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.command.ReservationEditCommand;
import roomescape.command.ReservationSaveCommand;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.exception.ConflictException;
import roomescape.exception.ForbiddenException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnprocessableException;
import roomescape.exception.code.ConflictCode;
import roomescape.exception.code.ForbiddenCode;
import roomescape.exception.code.NotFoundCode;
import roomescape.exception.code.UnprocessableCode;
import roomescape.policy.ReservationCancelPolicy;
import roomescape.policy.ReservationSavePolicy;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAllReservations();
    }

    @Transactional
    public void updateCanceled(Long id, LocalDateTime now, ReservationCancelPolicy policy) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundCode.RESERVATION_NOT_FOUND));
        policy.validate(reservation, now);
        int archived = reservationRepository.relocateToCanceledReservation(id);
        if (archived == 0) {
            throw new NotFoundException(NotFoundCode.RESERVATION_NOT_FOUND);
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelByOwner(Long id, User owner, LocalDateTime now, ReservationCancelPolicy policy) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundCode.RESERVATION_NOT_FOUND));
        if (!reservation.isOwnedBy(owner)) {
            throw new ForbiddenException(ForbiddenCode.NOT_RESERVATION_OWNER);
        }
        policy.validate(reservation, now);
        int archived = reservationRepository.relocateToCanceledReservation(id);
        if (archived == 0) {
            throw new NotFoundException(NotFoundCode.RESERVATION_NOT_FOUND);
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public Reservation saveReservation(ReservationSaveCommand command, LocalDateTime now, ReservationSavePolicy policy) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException(NotFoundCode.USER_NOT_FOUND));
        ReservationTime reservationTime = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new NotFoundException(NotFoundCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(command.themeId())
                .orElseThrow(() -> new NotFoundException(NotFoundCode.THEME_NOT_FOUND));
        Reservation reservation = Reservation.forSave(command, user, reservationTime, theme);
        policy.validate(reservation, now);

        return reservationRepository.addReservation(reservation);
    }

    public List<Reservation> findReservationsByUserId(Long userId) {
        return reservationRepository.findReservationsByUserId(userId);
    }

    @Transactional
    public Reservation editReservation(Long id, ReservationEditCommand command, LocalDateTime now) {
        Reservation reservation = getValidReservation(id, now);
        int reservationCount = reservationRepository.countReservationsOf(command.date(), command.timeId(),
                reservation.themeId());
        checkReservationDuplication(reservationCount);
        validateEditedDateTime(command, now);

        try {
            reservationRepository.updateReservation(id, command.date(), command.timeId());
            return reservationRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException(NotFoundCode.RESERVATION_NOT_FOUND));
        } catch (IllegalStateException e) {
            throw new NotFoundException(NotFoundCode.RESERVATION_TIME_NOT_FOUND);
        }
    }

    @Transactional
    public Reservation editReservationByOwner(Long id, User owner, ReservationEditCommand command, LocalDateTime now) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundCode.RESERVATION_NOT_FOUND));
        if (!reservation.isOwnedBy(owner)) {
            throw new ForbiddenException(ForbiddenCode.NOT_RESERVATION_OWNER);
        }
        return editReservation(id, command, now);
    }

    @NonNull
    private Reservation getValidReservation(Long id, LocalDateTime now) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(NotFoundCode.RESERVATION_NOT_FOUND));
        if (reservation.isDateTimeBefore(now)) {
            throw new UnprocessableException(UnprocessableCode.RESERVATION_ALREADY_STARTED);
        }
        return reservation;
    }

    private void validateEditedDateTime(ReservationEditCommand command, LocalDateTime now) {
        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new NotFoundException(NotFoundCode.RESERVATION_TIME_NOT_FOUND));
        command.validateNow(time, now);
    }

    private static void checkReservationDuplication(int reservationCount) {
        if (reservationCount > 0) {
            throw new ConflictException(ConflictCode.RESERVATION_DUPLICATED);
        }
    }

    @Transactional
    public List<Reservation> findReservationsToManage(Long id) {
        return reservationRepository.findReservationsByUserId(id);
    }
}
