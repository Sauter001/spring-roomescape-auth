package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.annotation.LoginUser;
import roomescape.domain.User;
import roomescape.response.ReservationResponse;
import roomescape.response.ReservationTimeResponse;
import roomescape.response.ThemeResponse;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

@Controller
public class ManagerViewController {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ManagerViewController(ReservationService reservationService,
                                 ReservationTimeService reservationTimeService,
                                 ThemeService themeService) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @GetMapping("/manager/reservation")
    public String reservation(Model model, @LoginUser User manager) {
        model.addAttribute("reservations", ReservationResponse.from(reservationService.findReservationsToManage(manager.id())));
        model.addAttribute("themes", ThemeResponse.from(themeService.findThemesToManage(manager.id())));
        model.addAttribute("times", ReservationTimeResponse.from(reservationTimeService.findAllReservationTimes()));
        model.addAttribute("reservationApiPath", "/manager/reservations");
        return "admin/reservation";
    }
}
