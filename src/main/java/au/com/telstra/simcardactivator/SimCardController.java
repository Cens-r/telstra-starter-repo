package au.com.telstra.simcardactivator;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;

import au.com.telstra.simcardactivator.model.ActivateRequest;
import au.com.telstra.simcardactivator.model.ActuateRequest;
import au.com.telstra.simcardactivator.model.TaskResponse;

@RestController
public class SimCardController {
    private static final String ACTUATE_URL = "http://localhost:8444/actuate";

    @PostMapping("/activate")
    public ResponseEntity<String> activate(@RequestBody ActivateRequest activateRequest) {
        String iccid = activateRequest.getIccid();
        String customerEmail = activateRequest.getCustomerEmail();

        // Validate the request
        if (iccid == null || iccid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Activation request missing an ICCID!"
            );
        }
        if (customerEmail == null || customerEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Activation request missing a Customer Email!"
            );
        }

        // Generate actuate request
        ActuateRequest actuateRequest = new ActuateRequest(iccid);

        RestTemplate template = new RestTemplate();
        ResponseEntity<TaskResponse> response;

        // Actuate the SIM card via its ICCID
        try {
            response = template.postForEntity(ACTUATE_URL, actuateRequest, TaskResponse.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    "Actuator Service is currently unavailable!"
            );
        }

        // Handle the response result
        TaskResponse result = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && result != null) {
            if (result.getSuccess()) {
                return ResponseEntity.ok(
                        String.format("Successfully activated SIM card (ICCID = %s)!", iccid)
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        String.format("Failed to activate SIM card (ICCID = %s)!", iccid)
                );
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    "Failed to retrieve a valid response from Actuator Service!"
            );
        }
    }
}
