package ApiTesting;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;

public class UsersTest extends ApiTestBase {

    @DataProvider(name = "userDataProvider")
    public static Object[][] userData() {
        return new Object[][]{
                {"Carlos López", "male", "carlos.lopez@test.com", "active"},
                {"Ana Gómez", "female", "ana.gomez@test.com", "inactive"},
                {"Pedro Martínez", "male", "pedro.martinez@test.com", "active"}
        };
    }

    @Test
    public void testGetUsers() {
        Response response = getRequestSpecification()
                .when()
                .get("/users");

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.jsonPath().getList(""), "User list should not be empty");

        System.out.println(response.prettyPrint());
    }

    @Test(dataProvider = "userDataProvider")
    public void testCreateUser(String name, String gender, String email, String status) {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("gender", gender);
        userData.put("email", email.replace("@", System.currentTimeMillis() + "@"));
        userData.put("status", status);

        Response response = getRequestSpecification()
                .body(userData)
                .post("/users");

        System.out.println("Response Body: " + response.prettyPrint());

        Assert.assertEquals(response.statusCode(), 201);
        Assert.assertNotNull(response.jsonPath().getString("id"), "User ID should not be null");
        Assert.assertEquals(response.jsonPath().getString("name"), userData.get("name"));
    }

    @Test(dataProvider = "userDataProvider")
    public void testGetUserById(String name, String gender, String email, String status) {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("gender", gender);
        userData.put("email", email.replace("@", System.currentTimeMillis() + "@"));
        userData.put("status", status);

        Response createResponse = getRequestSpecification()
                .body(userData)
                .post("/users");

        Assert.assertEquals(createResponse.statusCode(), 201);
        String userId = createResponse.jsonPath().getString("id");

        Response getResponse = getRequestSpecification()
                .when()
                .get("/users/" + userId);

        System.out.println("Response Body: " + getResponse.prettyPrint());

        Assert.assertEquals(getResponse.statusCode(), 200);
        Assert.assertEquals(getResponse.jsonPath().getString("id"), userId);
        Assert.assertEquals(getResponse.jsonPath().getString("name"), userData.get("name"));
    }

    @Test(dataProvider = "userDataProvider")
    public void testUpdateUser(String name, String gender, String email, String status) {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("gender", gender);
        userData.put("email", email.replace("@", System.currentTimeMillis() + "@"));
        userData.put("status", status);

        Response createResponse = getRequestSpecification()
                .body(userData)
                .post("/users");

        Assert.assertEquals(createResponse.statusCode(), 201);
        String userId = createResponse.jsonPath().getString("id");

        Map<String, String> updatedUserData = new HashMap<>();
        updatedUserData.put("name", name + " Updated");
        updatedUserData.put("status", "inactive");

        Response updateResponse = getRequestSpecification()
                .body(updatedUserData)
                .put("/users/" + userId);

        System.out.println("Response Body: " + updateResponse.prettyPrint());

        Assert.assertEquals(updateResponse.statusCode(), 200);
        Assert.assertEquals(updateResponse.jsonPath().getString("name"), updatedUserData.get("name"));
        Assert.assertEquals(updateResponse.jsonPath().getString("status"), updatedUserData.get("status"));
    }

    @Test
    public void testPartialUpdateUser() {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Ana Gómez");
        userData.put("gender", "female");
        userData.put("email", "ana.gomez" + System.currentTimeMillis() + "@test.com");
        userData.put("status", "active");

        Response createResponse = getRequestSpecification()
                .body(userData)
                .post("/users");

        Assert.assertEquals(createResponse.statusCode(), 201);
        String userId = createResponse.jsonPath().getString("id");

        Map<String, String> partialUpdateData = new HashMap<>();
        partialUpdateData.put("status", "inactive");

        Response patchResponse = getRequestSpecification()
                .body(partialUpdateData)
                .patch("/users/" + userId);

        System.out.println("Response Body: " + patchResponse.prettyPrint());

        Assert.assertEquals(patchResponse.statusCode(), 200);
        Assert.assertEquals(patchResponse.jsonPath().getString("status"), partialUpdateData.get("status"));

        Response getResponse = getRequestSpecification()
                .when()
                .get("/users/" + userId);

        Assert.assertEquals(getResponse.statusCode(), 200);
        Assert.assertEquals(getResponse.jsonPath().getString("status"), partialUpdateData.get("status"));
        Assert.assertEquals(getResponse.jsonPath().getString("name"), userData.get("name"));
    }

    @Test
    public void testDeleteUser() {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Sofía Fernández");
        userData.put("gender", "female");
        userData.put("email", "sofia.fernandez" + System.currentTimeMillis() + "@gmail.com");
        userData.put("status", "active");

        Response createResponse = getRequestSpecification()
                .body(userData)
                .post("/users");

        Assert.assertEquals(createResponse.statusCode(), 201);
        String userId = createResponse.jsonPath().getString("id");

        Response deleteResponse = getRequestSpecification()
                .delete("/users/" + userId);

        System.out.println("Response Body: " + deleteResponse.prettyPrint());

        Assert.assertEquals(deleteResponse.statusCode(), 204); // No content

        Response getResponse = getRequestSpecification()
                .when()
                .get("/users/" + userId);

        Assert.assertEquals(getResponse.statusCode(), 404); // Not found
    }

    @Test
    public void testGetUsersWithPagination() {
        Response response = getRequestSpecification()
                .when()
                .get("/users?page=1&per_page=5");

        System.out.println("Response Body: " + response.prettyPrint());

        Assert.assertEquals(response.statusCode(), 200);
        int totalUsers = response.jsonPath().getList("").size();
        Assert.assertTrue(totalUsers <= 5, "User ammount given should be 5 maximum");
    }

}
