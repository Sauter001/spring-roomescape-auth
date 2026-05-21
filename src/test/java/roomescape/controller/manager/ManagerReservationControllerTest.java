package roomescape.controller.manager;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-branch-manager.sql",
        "/test-reservation-time.sql", "/test-reservation.sql"})
class ManagerReservationControllerTest {

    private static final long OWN_BRANCH_RESERVATION_ID = 1L;   // 테마1 → branch 1 (매니저 담당)
    private static final long OWN_BRANCH_THEME_ID = 1L;
    private static final long OTHER_BRANCH_THEME_ID = 6L;       // 테마6 → branch 2

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long otherBranchReservationId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update(
                "INSERT INTO reservation (user_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                2, "2021-01-01", 3, OTHER_BRANCH_THEME_ID);
        otherBranchReservationId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE theme_id = ?", Long.class, OTHER_BRANCH_THEME_ID);
    }

    @Test
    void 매니저는_담당_매장의_예약을_삭제할_수_있다() {
        RestAssured.given().header("Authorization", login("manager", "manager123"))
                .when().delete("/api/manager/reservations/" + OWN_BRANCH_RESERVATION_ID)
                .then().statusCode(204);
    }

    @Test
    void 매니저가_다른_매장의_예약을_삭제하면_403() {
        RestAssured.given().header("Authorization", login("manager", "manager123"))
                .when().delete("/api/manager/reservations/" + otherBranchReservationId)
                .then().statusCode(403)
                .body("code", equalTo("NOT_BRANCH_MANAGER"));
    }

    @Test
    void 매니저는_담당_매장_테마로_예약을_생성할_수_있다() {
        RestAssured.given().header("Authorization", login("manager", "manager123"))
                .contentType(ContentType.JSON)
                .body(Map.of("userId", 2, "date", "2099-12-31", "timeId", 1, "themeId", OWN_BRANCH_THEME_ID))
                .when().post("/api/manager/reservations")
                .then().statusCode(201);
    }

    @Test
    void 매니저가_다른_매장_테마로_예약을_생성하면_403() {
        RestAssured.given().header("Authorization", login("manager", "manager123"))
                .contentType(ContentType.JSON)
                .body(Map.of("userId", 2, "date", "2099-12-31", "timeId", 1, "themeId", OTHER_BRANCH_THEME_ID))
                .when().post("/api/manager/reservations")
                .then().statusCode(403)
                .body("code", equalTo("NOT_BRANCH_MANAGER"));
    }

    private String login(String uid, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("uid", uid, "password", password))
                .when().post("/api/login")
                .then().statusCode(200)
                .extract().header("Authorization");
    }
}
