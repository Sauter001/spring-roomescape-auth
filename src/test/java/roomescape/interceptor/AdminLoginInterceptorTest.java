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

    @Test
    void 비로그인_상태에서_admin_엔드포인트_접근시_401() {
        RestAssured.given()
                .when().get("/api/admin/themes")
                .then().statusCode(401)
                .body("code", equalTo("LOGIN_REQUIRED"));
    }

    @Test
    void 로그인하면_admin_엔드포인트_접근_허용() {
        String token = login();

        RestAssured.given()
                .header("Authorization", token)
                .when().get("/api/admin/themes")
                .then().statusCode(200);
    }

    @Test
    void 로그인_없이_admin_시간_엔드포인트_접근시_401() {
        RestAssured.given()
                .when().get("/api/admin/times")
                .then().statusCode(401)
                .body("code", equalTo("LOGIN_REQUIRED"));
    }

    private String login() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("uid", "admin", "password", "admin123"))
                .when().post("/api/login")
                .then().statusCode(200)
                .extract().header("Authorization");
    }
}
