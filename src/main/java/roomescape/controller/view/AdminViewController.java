package roomescape.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.annotation.LoginUser;
import roomescape.annotation.RequireRole;
import roomescape.domain.Reservation;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.response.ReservationResponse;
import roomescape.response.ReservationTimeResponse;
import roomescape.response.ThemeResponse;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ThemeService;

import java.util.List;

@Controller
@RequireRole({Role.ADMIN, Role.MANAGER})
public class AdminViewController {

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public AdminViewController(ReservationService reservationService,
                               ReservationTimeService reservationTimeService,
                               ThemeService themeService) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @GetMapping("/admin")
    public String index() {
        return "admin/index";
    }

    @GetMapping("/admin/reservation")
    public String reservation(Model model, @LoginUser User user) {
        List<Reservation> reservations;
        List<Theme> themes;
        if (user.isAdmin()) {
            reservations = reservationService.findAllReservations();
            themes = themeService.getThemes();
        } else {
            reservations = reservationService.findReservationsToManage(user.id());
            themes = themeService.findThemesToManage(user.id());
        }
        model.addAttribute("reservations", ReservationResponse.from(reservations));
        model.addAttribute("themes", ThemeResponse.from(themes));
        model.addAttribute("times", ReservationTimeResponse.from(reservationTimeService.findAllReservationTimes()));
        return "admin/reservation";
    }

    @GetMapping("/admin/time")
    @RequireRole(Role.ADMIN)
    public String time(Model model) {
        model.addAttribute("times", ReservationTimeResponse.from(reservationTimeService.findAllReservationTimes()));
        return "admin/time";
    }

    @GetMapping("/admin/theme")
    @RequireRole(Role.ADMIN)
    public String theme(Model model) {
        model.addAttribute("themes", ThemeResponse.from(themeService.getThemes()));
        return "admin/theme";
    }
}
