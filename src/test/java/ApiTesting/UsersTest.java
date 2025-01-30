package ApiTesting;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class UsersTest extends ApiTestBase{

    @Test
    public void testGetUsers() {
        Response response = getRequestSpecification()
                .when()
                .get("/users");

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.jsonPath().getList(""), "User list should not be empty");

        System.out.println(response.prettyPrint());
    }

    @Test
    public void testCreateUser() {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Juan Pérez");
        userData.put("gender", "male");
        userData.put("email", "juan.perez" + System.currentTimeMillis() + "@gmail.com");
        userData.put("status", "active");

        Response response = getRequestSpecification()
                .body(userData)
                .post("/users");

        System.out.println("Response Body: " + response.prettyPrint());

        Assert.assertEquals(response.statusCode(), 201);
        Assert.assertNotNull(response.jsonPath().getString("id"), "User ID should not be null");
        Assert.assertEquals(response.jsonPath().getString("name"), userData.get("name"));
    }

    @Test
    public void testGetUserById() {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Carlos López");
        userData.put("gender", "male");
        userData.put("email", "carlos.lopez" + System.currentTimeMillis() + "@gmail.com");
        userData.put("status", "active");

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

    @Test
    public void testUpdateUser() {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Pedro Martinez");
        userData.put("gender", "male");
        userData.put("email", "pedro.martinez" + System.currentTimeMillis() + "@gmail.com");
        userData.put("status", "active");

        Response createResponse = getRequestSpecification()
                .body(userData)
                .post("/users");

        Assert.assertEquals(createResponse.statusCode(), 201);
        String userId = createResponse.jsonPath().getString("id");

        Map<String, String> updatedUserData = new HashMap<>();
        updatedUserData.put("name", "Pedro M. Updated");
        updatedUserData.put("email", "pedro.updated" + System.currentTimeMillis() + "@gmail.com");
        updatedUserData.put("status", "inactive");

        Response updateResponse = getRequestSpecification()
                .body(updatedUserData)
                .put("/users/" + userId);

        System.out.println("Response Body: " + updateResponse.prettyPrint());

        Assert.assertEquals(updateResponse.statusCode(), 200);
        Assert.assertEquals(updateResponse.jsonPath().getString("name"), updatedUserData.get("name"));
        Assert.assertEquals(updateResponse.jsonPath().getString("status"), updatedUserData.get("status"));

        Response getResponse = getRequestSpecification()
                .when()
                .get("/users/" + userId);

        Assert.assertEquals(getResponse.statusCode(), 200);
        Assert.assertEquals(getResponse.jsonPath().getString("name"), updatedUserData.get("name"));
        Assert.assertEquals(getResponse.jsonPath().getString("status"), updatedUserData.get("status"));
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

    @Test
    public void testGetUserByIdGraphQL() {
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Lucas Ortega");
        userData.put("gender", "male");
        userData.put("email", "lucas.ortega" + System.currentTimeMillis() + "@gmail.com");
        userData.put("status", "active");

        Response createResponse = getRequestSpecification()
                .body(userData)
                .post("/users");

        Assert.assertEquals(createResponse.statusCode(), 201);
        String userId = createResponse.jsonPath().getString("id");

        String query = "{ \"query\": \"query { user(id: " + userId + ") { id name email gender status } }\" }";

        Response response = getRequestSpecification()
                .body(query)
                .post("/graphql");

        System.out.println("Response Body: " + response.prettyPrint());

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("data.user.id"), userId);
        Assert.assertEquals(response.jsonPath().getString("data.user.name"), userData.get("name"));
    }

    @Test
    public void testCreateUserGraphQL() {
        String mutation = "{ \"query\": \"mutation { createUser(input: { name: \\\"María González\\\", gender: \\\"female\\\", email: \\\"maria.gonzalez"
                + System.currentTimeMillis() + "@gmail.com\\\", status: \\\"active\\\" }) { user { id name email gender status } } }\" }";

        Response response = getRequestSpecification()
                .body(mutation)
                .post("/graphql");

        System.out.println("Response Body: " + response.prettyPrint());

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertNotNull(response.jsonPath().getString("data.createUser.user.id"), "User ID should be null");
        Assert.assertEquals(response.jsonPath().getString("data.createUser.user.name"), "María González");
        Assert.assertEquals(response.jsonPath().getString("data.createUser.user.status"), "active");
    }

    @Test
    public void testUpdateUserGraphQL() {
        String mutationCreate = "{ \"query\": \"mutation { createUser(input: { name: \\\"Lucas Ramírez\\\", gender: \\\"male\\\", email: \\\"lucas.ramirez"
                + System.currentTimeMillis() + "@gmail.com\\\", status: \\\"active\\\" }) { user { id name email gender status } } }\" }";

        Response createResponse = getRequestSpecification()
                .body(mutationCreate)
                .post("/graphql");

        Assert.assertEquals(createResponse.statusCode(), 200);
        String userId = createResponse.jsonPath().getString("data.createUser.user.id");

        String mutationUpdate = "{ \"query\": \"mutation { updateUser(input: { id: " + userId + ", name: \\\"Lucas R. updated\\\", status: \\\"inactive\\\" }) { user { id name status } } }\" }";

        Response updateResponse = getRequestSpecification()
                .body(mutationUpdate)
                .post("/graphql");

        System.out.println("Response Body: " + updateResponse.prettyPrint());

        Assert.assertEquals(updateResponse.statusCode(), 200);
        Assert.assertEquals(updateResponse.jsonPath().getString("data.updateUser.user.name"), "Lucas R. updated");
        Assert.assertEquals(updateResponse.jsonPath().getString("data.updateUser.user.status"), "inactive");
    }

    @Test
    public void testDeleteUserGraphQL() {
        String mutationCreate = "{ \"query\": \"mutation { createUser(input: { name: \\\"Diego Torres\\\", gender: \\\"male\\\", email: \\\"diego.torres"
                + System.currentTimeMillis() + "@gmail.com\\\", status: \\\"active\\\" }) { user { id name email gender status } } }\" }";

        Response createResponse = getRequestSpecification()
                .body(mutationCreate)
                .post("/graphql");

        Assert.assertEquals(createResponse.statusCode(), 200);
        String userId = createResponse.jsonPath().getString("data.createUser.user.id");

        String mutationDelete = "{ \"query\": \"mutation { deleteUser(input: { id: " + userId + " }) { user { id name email gender status } } }\" }";

        Response deleteResponse = getRequestSpecification()
                .body(mutationDelete)
                .post("/graphql");

        System.out.println("Response Body: " + deleteResponse.prettyPrint());

        Assert.assertEquals(deleteResponse.statusCode(), 200);
        Assert.assertEquals(deleteResponse.jsonPath().getString("data.deleteUser.user.id"), userId);

        String queryGet = "{ \"query\": \"query { user(id: " + userId + ") { id name email gender status } }\" }";

        Response getResponse = getRequestSpecification()
                .body(queryGet)
                .post("/graphql");

        Assert.assertEquals(getResponse.statusCode(), 200);
        Assert.assertNull(getResponse.jsonPath().get("data.user"), "Deleted user should not exist");
    }


}
