package roomescape.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import roomescape.annotation.LoginUser;
import roomescape.domain.Reservation;
import roomescape.domain.User;
import roomescape.policy.cancel.AdminReservationCancelPolicy;
import roomescape.policy.save.AdminReservationSavePolicy;
import roomescape.request.AdminReservationRequest;
import roomescape.request.ReservationEditRequest;
import roomescape.response.ReservationResponse;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
    private static final AdminReservationSavePolicy SAVE_POLICY = new AdminReservationSavePolicy();
    private static final AdminReservationCancelPolicy CANCEL_POLICY = new AdminReservationCancelPolicy();
    private static final String DEFAULT_PATH = "/api/reservations/";
    private final ReservationService reservationService;
    private final Clock clock;

    public AdminReservationController(
            ReservationService reservationService,
            Clock clock) {
        this.reservationService = reservationService;
        this.clock = clock;
    }

    @GetMapping
    public List<ReservationResponse> getReservations() {
        return ReservationResponse.from(reservationService.findAllReservations());
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> saveReservation(
            @Valid @RequestBody AdminReservationRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        Reservation reservationReturned = reservationService.saveReservation(request.toSaveCommand(), now,
                SAVE_POLICY);
        ReservationResponse reservationResponse = ReservationResponse.from(reservationReturned);

        return ResponseEntity.created(getLocation(reservationResponse.id())).body(reservationResponse);
    }

    @NonNull
    private static URI getLocation(Long id) {
        return URI.create(DEFAULT_PATH + id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.updateCanceled(id, LocalDateTime.now(clock), CANCEL_POLICY);

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
