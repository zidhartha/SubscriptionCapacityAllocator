package org.example.subscriptioncapacityallocator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_REQUEST_BODY = """
        {
          "maxCapacity": 15,
          "availableSubscriptions": [
            { "investorName": "Investor A", "requestedAmount": 5, "feeRevenue": 120 },
            { "investorName": "Investor B", "requestedAmount": 10, "feeRevenue": 200 },
            { "investorName": "Investor C", "requestedAmount": 3, "feeRevenue": 80 },
            { "investorName": "Investor D", "requestedAmount": 8, "feeRevenue": 160 }
          ]
        }
        """;

    @Test
    @DisplayName("POST /optimize - Returns 201 Created with optimal 0/1 knapsack allocation")
    void optimize_returnsCreatedWithOptimalAllocation() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.totalRequestedAmount").value(15))
                .andExpect(jsonPath("$.totalFeeRevenue").value(320))
                .andExpect(jsonPath("$.acceptedSubscriptions.length()").value(2));
    }

    @Test
    @DisplayName("POST /optimize - Returns 400 Bad Request when maxCapacity is negative")
    void optimize_withNegativeCapacity_returnsBadRequest() throws Exception {
        String invalidBody = """
            { "maxCapacity": -5, "availableSubscriptions": [] }
            """;

        mockMvc.perform(post("/api/v1/subscriptions/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /optimize - Returns 200 ok with empty list when no item fits capacity")
    void optimize_withNoValidCombination_returnsEmptyResultWith200() throws Exception {
        String body = """
            {
              "maxCapacity": 1,
              "availableSubscriptions": [
                { "investorName": "Investor A", "requestedAmount": 50, "feeRevenue": 100 }
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/subscriptions/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptedSubscriptions.length()").value(0))
                .andExpect(jsonPath("$.totalFeeRevenue").value(0));
    }

    @Test
    @DisplayName("POST /optimize - Empty subscriptions list safely returns 201 with 0 revenue")
    void optimize_withEmptySubscriptionsList_returnsCreatedWithZeroRevenue() throws Exception {
        String emptySubscriptionsBody = """
            {
              "maxCapacity": 10,
              "availableSubscriptions": []
            }
            """;

        mockMvc.perform(post("/api/v1/subscriptions/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptySubscriptionsBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.acceptedSubscriptions.length()").value(0))
                .andExpect(jsonPath("$.totalRequestedAmount").value(0))
                .andExpect(jsonPath("$.totalFeeRevenue").value(0));
    }

    @Test
    @DisplayName("POST /optimize - Chooses single high-revenue investor over multiple lower-revenue investors")
    void optimize_prioritizesHigherRevenueOverCapacityUtilization() throws Exception {
        String body = """
            {
              "maxCapacity": 10,
              "availableSubscriptions": [
                { "investorName": "Investor A", "requestedAmount": 5, "feeRevenue": 70 },
                { "investorName": "Investor B", "requestedAmount": 5, "feeRevenue": 80 },
                { "investorName": "Investor C", "requestedAmount": 10, "feeRevenue": 300 }
              ]
            }
            """;

        mockMvc.perform(post("/api/v1/subscriptions/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalFeeRevenue").value(300))
                .andExpect(jsonPath("$.totalRequestedAmount").value(10))
                .andExpect(jsonPath("$.acceptedSubscriptions.length()").value(1))
                .andExpect(jsonPath("$.acceptedSubscriptions[0].investorName").value("Investor C"));
    }

    @Test
    @DisplayName("POST /optimize - Submitting empty JSON triggers @Valid constraint error")
    void optimize_withEmptyJsonPayload_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{requestId} - Returns 200 OK when record exists")
    void getByRequestId_whenFound_returnsOk() throws Exception {
        String response = mockMvc.perform(post("/api/v1/subscriptions/optimize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_BODY))
                .andReturn().getResponse().getContentAsString();

        UUID requestId = UUID.fromString(
                objectMapper.readTree(response).get("requestId").asText());

        mockMvc.perform(get("/api/v1/subscriptions/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId.toString()));
    }

    @Test
    @DisplayName("GET /{requestId} - Returns 404 Not Found for non-existent ID")
    void getByRequestId_whenNotFound_returnsNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/subscriptions/{requestId}", randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET / - Returns 200 OK with paginated result list")
    void getAll_returnsPaginatedList() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY));

        mockMvc.perform(get("/api/v1/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }
}