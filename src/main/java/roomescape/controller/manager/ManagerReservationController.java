package roomescape.controller.manager;

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
@RequestMapping("/api/manager/reservations")
public class ManagerReservationController {
    private static final AdminReservationSavePolicy SAVE_POLICY = new AdminReservationSavePolicy();
    private static final AdminReservationCancelPolicy CANCEL_POLICY = new AdminReservationCancelPolicy();
    private static final String DEFAULT_PATH = "/api/reservations/";
    private final ReservationService reservationService;
    private final Clock clock;

    public ManagerReservationController(ReservationService reservationService, Clock clock) {
        this.reservationService = reservationService;
        this.clock = clock;
    }

    @GetMapping
    public List<ReservationResponse> getReservations(@LoginUser User manager) {
        return ReservationResponse.from(reservationService.findReservationsToManage(manager.id()));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> saveReservation(
            @Valid @RequestBody AdminReservationRequest request, @LoginUser User manager) {
        LocalDateTime now = LocalDateTime.now(clock);
        Reservation saved = reservationService.saveReservationByManager(manager.id(), request.toSaveCommand(), now, SAVE_POLICY);

        return ResponseEntity.created(getLocation(saved.id())).body(ReservationResponse.from(saved));
    }

    @NonNull
    private static URI getLocation(Long id) {
        return URI.create(DEFAULT_PATH + id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> editReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationEditRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        Reservation reservation = reservationService.editReservation(id, request.toCommand(), now);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id, @LoginUser User manager) {
        reservationService.cancelByManager(manager.id(), id, LocalDateTime.now(clock), CANCEL_POLICY);

        return ResponseEntity.noContent().build();
    }
}
