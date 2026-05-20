package roomescape.controller.user;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/test-truncate.sql", "/test-user.sql"})
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 올바른_자격증명이면_200을_반환한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("uid", "admin", "password", "admin123"))
                .when().post("/api/login")
                .then().statusCode(200);
    }

    @Test
    void 비밀번호가_틀리면_401을_반환한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("uid", "admin", "password", "wrong"))
                .when().post("/api/login")
                .then().statusCode(401)
                .body("code", equalTo("LOGIN_FAILED"));
    }

    @Test
    void 존재하지_않는_아이디면_401을_반환한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("uid", "ghost", "password", "any"))
                .when().post("/api/login")
                .then().statusCode(401)
                .body("code", equalTo("LOGIN_FAILED"));
    }

    @Test
    void 로그인_성공시_토큰이_발급된다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("uid", "admin", "password", "admin123"))
                .when().post("/api/login")
                .then().statusCode(200)
                .header("Authorization", startsWith("Bearer "));
    }
}
