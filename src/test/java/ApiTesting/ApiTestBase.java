package ApiTesting;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class ApiTestBase {
    protected static final String BASE_URL = "https://gorest.co.in/public/v2";
    protected static final String TOKEN = "2be7fe4f0af57cdb4a623de4db7c5c1ab004c805b0680c61537125a95abae256"; // Reemplaza con tu token real

    protected RequestSpecification getRequestSpecification() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + TOKEN)
                .header("Content-Type", "application/json");
    }
}
