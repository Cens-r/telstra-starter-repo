package stepDefinitions;

import au.com.telstra.simcardactivator.SimCardActivator;
import au.com.telstra.simcardactivator.model.ActivateRequest;
import au.com.telstra.simcardactivator.model.SimCard;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = SimCardActivator.class, loader = SpringBootContextLoader.class)
public class SimCardActivatorStepDefinitions {
    public static final String ACTIVATE_URL = "http://localhost:8080/activate";
    public static final String QUERY_URL = "http://localhost:8080/query";

    @Autowired
    private TestRestTemplate restTemplate;
    private ActivateRequest payload;

    @Given("a valid ICCID for a SIM Card")
    public void GenerateValidRequest() {
        this.payload = new ActivateRequest("1255789453849037777", "test@email.com");
    }

    @Given("an invalid ICCID for a SIM Card")
    public void GenerateInvalidRequest() {
        this.payload = new ActivateRequest("8944500102198304826", "test@email.com");
    }

    @When("attempting to activate said SIM Card")
    public void AttemptActivation() {
        String result = this.restTemplate.postForObject(ACTIVATE_URL, payload, String.class);
        System.out.println(result);
    }

    public SimCard QuerySimCard(long id) {
        ResponseEntity<SimCard> response = this.restTemplate.getForEntity(QUERY_URL + "?id=" + id, SimCard.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        SimCard result = response.getBody();
        System.out.println(result);
        return result;
    }

    @Then("the SIM Card should be activated")
    public void EnsureActivationSuccess() {
        SimCard result = QuerySimCard(1);
        assertNotNull(result);
        assertTrue(result.getActive());
    }

    @Then("the SIM Card should not be activated")
    public void EnsureActivationFailure() {
        SimCard result = QuerySimCard(2);
        assertNotNull(result);
        assertFalse(result.getActive());
    }
}