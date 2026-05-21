package roomescape.controller.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.annotation.LoginUser;
import roomescape.domain.Reservation;
import roomescape.domain.User;
import roomescape.policy.cancel.UserReservationCancelPolicy;
import roomescape.policy.save.UserReservationSavePolicy;
import roomescape.request.ReservationEditRequest;
import roomescape.request.ReservationRequest;
import roomescape.response.ReservationResponse;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private static final String DEFAULT_PATH = "/api/reservations/";
    private static final UserReservationSavePolicy SAVE_POLICY = new UserReservationSavePolicy();
    private static final UserReservationCancelPolicy CANCEL_POLICY = new UserReservationCancelPolicy();
    private final ReservationService reservationService;
    private final Clock clock;

    public ReservationController(
            ReservationService reservationService,
            Clock clock) {
        this.reservationService = reservationService;
        this.clock = clock;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations(@LoginUser User user) {
        List<ReservationResponse> responses = ReservationResponse.from(reservationService.findReservationsByUserId(user.id()));
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> saveReservation(
            @Valid @RequestBody ReservationRequest request,
            @LoginUser User user) {
        LocalDateTime now = LocalDateTime.now(clock);
        Reservation reservationReturned = reservationService.saveReservation(request.toSaveCommand(user.id()), now,
                SAVE_POLICY);
        ReservationResponse reservationResponse = ReservationResponse.from(reservationReturned);

        return ResponseEntity.created(getLocation(reservationResponse.id())).body(reservationResponse);
    }

    @NonNull
    private static URI getLocation(Long id) {
        return URI.create(DEFAULT_PATH + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, @LoginUser User user) {
        reservationService.cancelByOwner(id, user, LocalDateTime.now(clock), CANCEL_POLICY);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> editReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationEditRequest request,
            @LoginUser User user) {
        LocalDateTime now = LocalDateTime.now(clock);
        Reservation reservation = reservationService.editReservationByOwner(id, user, request.toCommand(), now);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }
}
