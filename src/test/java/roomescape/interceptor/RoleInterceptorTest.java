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
class RoleInterceptorTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 비로그인으로_admin_API_접근시_401() {
        RestAssured.given()
                .when().get("/api/admin/themes")
                .then().statusCode(401)
                .body("code", equalTo("LOGIN_REQUIRED"));
    }

    @Test
    void ADMIN은_admin_API에_접근할_수_있다() {
        RestAssured.given().header("Authorization", login("admin", "admin123"))
                .when().get("/api/admin/reservations")
                .then().statusCode(200);
    }

    @Test
    void ADMIN이_manager_API에_접근하면_403() {
        RestAssured.given().header("Authorization", login("admin", "admin123"))
                .when().get("/api/manager/reservations")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void MANAGER는_manager_API에_접근할_수_있다() {
        RestAssured.given().header("Authorization", login("manager", "manager123"))
                .when().get("/api/manager/reservations")
                .then().statusCode(200);
    }

    @Test
    void MANAGER가_admin_예약_API에_접근하면_403() {
        RestAssured.given().header("Authorization", login("manager", "manager123"))
                .when().get("/api/admin/reservations")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void MANAGER가_admin_테마_API에_접근하면_403() {
        RestAssured.given().header("Authorization", login("manager", "manager123"))
                .when().get("/api/admin/themes")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void USER가_admin_API에_접근하면_403() {
        RestAssured.given().header("Authorization", login("user1", "password1"))
                .when().get("/api/admin/reservations")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void USER가_manager_API에_접근하면_403() {
        RestAssured.given().header("Authorization", login("user1", "password1"))
                .when().get("/api/manager/reservations")
                .then().statusCode(403)
                .body("code", equalTo("ACCESS_DENIED"));
    }

    @Test
    void MANAGER가_admin_페이지에_접근하면_홈으로_리다이렉트() {
        RestAssured.given().redirects().follow(false)
                .header("Authorization", login("manager", "manager123"))
                .when().get("/admin/reservation")
                .then().statusCode(302);
    }

    @Test
    void ADMIN이_manager_페이지에_접근하면_홈으로_리다이렉트() {
        RestAssured.given().redirects().follow(false)
                .header("Authorization", login("admin", "admin123"))
                .when().get("/manager/reservation")
                .then().statusCode(302);
    }

    @Test
    void USER가_admin_페이지에_접근하면_홈으로_리다이렉트() {
        RestAssured.given().redirects().follow(false)
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
