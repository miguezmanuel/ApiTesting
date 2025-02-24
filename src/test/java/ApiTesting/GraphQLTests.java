package ApiTesting;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;

public class GraphQLTests extends ApiTestBase {

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
        Assert.assertNotNull(response.jsonPath().getString("data.createUser.user.id"), "User ID should not be null");
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
