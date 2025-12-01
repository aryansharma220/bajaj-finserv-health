package com.example.bajajFinserv;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Runner implements CommandLineRunner {

    private final RestTemplate restTemplate;
    private final Config config;

    public Runner(RestTemplate restTemplate, Config config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Override
    public void run(String... args) {
        GenerateWebhookResponse response = callGenerateWebhook();
        if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
            System.out.println("Failed to get webhook or accessToken. Exiting.");
            return;
        }

        String finalQuery = buildFinalQuery();
        submitFinalQuery(response.getWebhook(), response.getAccessToken(), finalQuery);
    }

    private GenerateWebhookResponse callGenerateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        System.out.println("URL I'm calling: '" + url + "'");

        GenerateWebhookRequest body = new GenerateWebhookRequest();
        body.setName(config.getName());
        body.setRegNo(config.getRegNo());
        body.setEmail(config.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GenerateWebhookResponse> response =
                    restTemplate.postForEntity(url, entity, GenerateWebhookResponse.class);

            System.out.println("generateWebhook status: " + response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String buildFinalQuery() {
        // Here you convert your SQL into one Java string
        return "WITH high_earners AS ( " +
                "SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB, e.DEPARTMENT " +
                "FROM EMPLOYEE e " +
                "JOIN PAYMENTS p ON p.EMP_ID = e.EMP_ID " +
                "GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB, e.DEPARTMENT " +
                "HAVING MAX(p.AMOUNT) > 70000 " +
                "), ranked AS ( " +
                "SELECT d.DEPARTMENT_ID, d.DEPARTMENT_NAME, " +
                "EXTRACT(YEAR FROM AGE(CURRENT_DATE, h.DOB)) AS age, " +
                "(h.FIRST_NAME || ' ' || h.LAST_NAME) AS full_name, " +
                "ROW_NUMBER() OVER (PARTITION BY d.DEPARTMENT_ID ORDER BY h.EMP_ID) AS rn " +
                "FROM high_earners h " +
                "JOIN DEPARTMENT d ON d.DEPARTMENT_ID = h.DEPARTMENT " +
                ") " +
                "SELECT r.DEPARTMENT_NAME, " +
                "AVG(r.age) AS AVERAGE_AGE, " +
                "STRING_AGG(r.full_name, ', ') AS EMPLOYEE_LIST " +
                "FROM ranked r " +
                "WHERE r.rn <= 10 " +
                "GROUP BY r.DEPARTMENT_ID, r.DEPARTMENT_NAME " +
                "ORDER BY r.DEPARTMENT_ID DESC";
    }

    private void submitFinalQuery(String webhookUrl, String accessToken, String finalQuery) {
        FinalRequest body = new FinalRequest(); // use existing FinalRequest class
        body.setFinalQuery(finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken); // as per problem

        HttpEntity<FinalRequest> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(webhookUrl, entity, String.class);

            System.out.println("submitFinalQuery status: " + response.getStatusCode());
            System.out.println("submitFinalQuery body: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
