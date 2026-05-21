package roomescape.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({JdbcTemplateReservationRepository.class, JdbcTemplateThemeRepository.class, JdbcTemplateUserRepository.class})
class JdbcTemplateReservationRepositoryTest {

    private static final long TIME_ID = 1L;
    private static final long THEME_ID = 1L;
    private static final long ADMIN_ID = 1L;
    private static final long USER1_ID = 2L;
    private static final long MANAGER_ID = 3L;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-reservation-time.sql"})
    void 예약을_저장하면_id가_채워진_도메인을_반환한다() {
        Reservation saved = addReservation(USER1_ID, LocalDate.of(2026, 5, 3));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.user().id()).isEqualTo(USER1_ID);
        assertThat(saved.date()).isEqualTo(LocalDate.of(2026, 5, 3));
        assertThat(saved.time().id()).isEqualTo(TIME_ID);
    }

    private Reservation addReservation(long userId, LocalDate date) {
        return addReservationOnTheme(userId, date, THEME_ID);
    }

    private Reservation addReservationOnTheme(long userId, LocalDate date, long themeId) {
        ReservationTime time = new ReservationTime(TIME_ID, LocalTime.of(10, 0));
        Theme theme = themeRepository.findById(themeId).get();
        User user = userRepository.findById(userId).get();
        return reservationRepository.addReservation(new Reservation(null, user, date, time, theme));
    }

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-branch-manager.sql",
            "/test-reservation-time.sql"})
    void 매니저는_담당_매장의_예약만_조회한다() {
        addReservationOnTheme(USER1_ID, LocalDate.of(2026, 5, 3), 1L);  // 테마1 → branch 1 (매니저 담당)
        addReservationOnTheme(USER1_ID, LocalDate.of(2026, 5, 3), 6L);  // 테마6 → branch 2

        List<Reservation> managed = reservationRepository.findReservationsToManage(MANAGER_ID);

        assertThat(managed).hasSize(1);
        assertThat(managed.get(0).theme().branchId()).isEqualTo(1L);
    }

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-reservation-time.sql"})
    void 모든_예약을_조인_조회한다() {
        addReservation(USER1_ID, LocalDate.of(2026, 5, 3));
        addReservation(ADMIN_ID, LocalDate.of(2026, 5, 4));

        List<Reservation> reservations = reservationRepository.findAllReservations();

        assertThat(reservations).hasSize(2);
        assertThat(reservations.get(0).time().startAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-reservation-time.sql"})
    void 특정_사용자의_예약을_조회한다() {
        addReservation(USER1_ID, LocalDate.of(2026, 5, 3));
        addReservation(USER1_ID, LocalDate.of(2026, 5, 4));
        addReservation(ADMIN_ID, LocalDate.of(2026, 5, 5));

        List<Reservation> reservations = reservationRepository.findReservationsByUserId(USER1_ID);

        assertThat(reservations).hasSize(2);
    }

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-reservation-time.sql"})
    void 예약이_없으면_빈_리스트를_반환한다() {
        List<Reservation> reservations = reservationRepository.findAllReservations();

        assertThat(reservations).isEmpty();
    }

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-reservation-time.sql"})
    void id로_예약을_삭제한다() {
        long reservationId = addReservation(USER1_ID, LocalDate.of(2026, 5, 3)).id();

        reservationRepository.deleteById(reservationId);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM reservation", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-reservation-time.sql"})
    void 같은_날짜_시간_테마에_이미_예약이_있는지_카운트() {
        addReservation(USER1_ID, LocalDate.of(2026, 5, 3));
        int count = reservationRepository.countReservationsOf(LocalDate.of(2026, 5, 3), TIME_ID, THEME_ID);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-reservation-time.sql"})
    void 날짜_시간_테마_중_하나라도_다르면_0건() {
        addReservation(USER1_ID, LocalDate.of(2026, 5, 3));

        int count = reservationRepository.countReservationsOf(LocalDate.of(2026, 5, 3), 2L, THEME_ID);
        assertThat(count).isEqualTo(0);

        count = reservationRepository.countReservationsOf(LocalDate.of(2026, 5, 3), TIME_ID, 2L);
        assertThat(count).isEqualTo(0);

        count = reservationRepository.countReservationsOf(LocalDate.of(2026, 5, 2), TIME_ID, THEME_ID);
        assertThat(count).isEqualTo(0);
    }


}
