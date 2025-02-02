package au.com.telstra.simcardactivator;

import au.com.telstra.simcardactivator.model.ActivateRequest;
import au.com.telstra.simcardactivator.model.SimCard;
import au.com.telstra.simcardactivator.repository.SimCardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

import org.springframework.web.client.RestTemplate;

import au.com.telstra.simcardactivator.model.ActuateRequest;
import au.com.telstra.simcardactivator.model.TaskResponse;

import java.util.Optional;

@RestController
public class SimCardController {
    private final String actuateUrl;
    private final SimCardRepository repository;

    public SimCardController(SimCardRepository repository) {
        this.actuateUrl = "http://localhost:8444/actuate";
        this.repository = repository;
    }

    @GetMapping("/query")
    public ResponseEntity<SimCard> query(@RequestParam long id) {
        Optional<SimCard> simCard = repository.findById(id);
        return simCard.map(
                ResponseEntity::ok
        ).orElseGet(
                () -> ResponseEntity.notFound().build()
        );
    }

    @PostMapping("/activate")
    public ResponseEntity<String> activate(@RequestBody ActivateRequest activateRequest) {
        String iccid = activateRequest.getIccid();
        String customerEmail = activateRequest.getCustomerEmail();

        // Validate the request
        if (iccid == null || iccid.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Activation request missing an ICCID!"
            );
        } else if (customerEmail == null || customerEmail.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    "Activation request missing a Customer Email!"
            );
        }

        // Get the existing SIM Card or create a new one
        Optional<SimCard> entry = repository.findByIccid(iccid);
        SimCard simCard = entry.orElse(null);

        if (simCard != null && simCard.getActive()) {
            return ResponseEntity.ok(
                    String.format("SIM Card (iccid=%s) already activated!", iccid)
            );
        } else {
            simCard = new SimCard(iccid, customerEmail, false);
        }

        // Generate actuate request
        ActuateRequest actuateRequest = new ActuateRequest(iccid);

        // Actuate the SIM card via its ICCID
        RestTemplate template = new RestTemplate();
        ResponseEntity<TaskResponse> response;
        try {
            response = template.postForEntity(actuateUrl, actuateRequest, TaskResponse.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    "Actuator Service is currently unavailable!"
            );
        }

        // Handle the response result
        TaskResponse result = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && result != null) {
            boolean active = result.getSuccess();
            simCard.setActive(active);

            repository.save(simCard);
            if (active) {
                return ResponseEntity.ok(
                        String.format("Successfully activated SIM card (iccid=%s)!", iccid)
                );
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        String.format("Failed activated SIM card (iccid=%s)!", iccid)
                );
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    "Failed to retrieve a valid response from Actuator Service!"
            );
        }
    }
}
