package roomescape.interceptor;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/test-truncate.sql", "/test-user.sql"})
class AdminLoginInterceptorTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    // ---------- 인증(로그인) ----------

    @Test
    void 비로그인_상태로_admin_API_접근시_401() {
        RestAssured.given()
                .when().get("/api/admin/themes")
                .then().statusCode(401)
                .body("code", equalTo("LOGIN_REQUIRED"));
    }

    // ---------- ADMIN: 전체 허용 ----------

    @Test
    void ADMIN은_테마_API에_접근할_수_있다() {
        RestAssured.given()
                .header("Authorization", login("admin", "admin123"))
                .when().get("/api/admin/themes")
                .then().statusCode(200);
    }

    @Test
    void ADMIN은_테마_관리_페이지에_접근할_수_있다() {
        RestAssured.given()
                .redirects().follow(false)
                .header("Authorization", login("admin", "admin123"))
                .when().get("/admin/theme")
                .then().statusCode(200);
    }

    // ---------- MANAGER: 예약은 허용, 테마/타임은 거부 ----------

    @Test
    void MANAGER는_예약_관리_페이지에_접근할_수_있다() {
        RestAssured.given()
                .redirects().follow(false)
                .header("Authorization", login("manager", "manager123"))
                .when().get("/admin/reservation")
                .then().statusCode(200);
    }

    @Test
    void MANAGER는_테마_API에_접근하면_403() {
        RestAssured.given()
                .header("Authorization", login("manager", "manager123"))
                .when().get("/api/admin/themes")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void MANAGER는_타임_API에_접근하면_403() {
        RestAssured.given()
                .header("Authorization", login("manager", "manager123"))
                .when().get("/api/admin/times")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void MANAGER가_테마_관리_페이지에_접근하면_홈으로_리다이렉트() {
        RestAssured.given()
                .redirects().follow(false)
                .header("Authorization", login("manager", "manager123"))
                .when().get("/admin/theme")
                .then().statusCode(302);
    }

    // ---------- USER: 관리자 영역 전체 거부 ----------

    @Test
    void USER가_테마_API에_접근하면_403() {
        RestAssured.given()
                .header("Authorization", login("user1", "password1"))
                .when().get("/api/admin/themes")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void USER가_예약_API에_접근하면_403() {
        RestAssured.given()
                .header("Authorization", login("user1", "password1"))
                .when().get("/api/admin/reservations")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void USER가_관리자_페이지에_접근하면_홈으로_리다이렉트() {
        RestAssured.given()
                .redirects().follow(false)
                .header("Authorization", login("user1", "password1"))
                .when().get("/admin/reservation")
                .then().statusCode(302);
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
