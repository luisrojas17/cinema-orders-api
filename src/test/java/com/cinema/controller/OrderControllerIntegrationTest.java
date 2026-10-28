package com.cinema.controller;

import com.cinema.constant.ValidationMessages;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    private static final String BASE = "/v1/countries/MX/orders";
    private static final String VALID_BODY = """
            {"cinema_id":"472","user_id":"user-123","items":[
              {"type":"TICKET","name":"Adulto","price":85},
              {"type":"FOOD","name":"Combo 1","price":120}]}""";

    @Autowired
    private MockMvc mvc;

    private String createOrder() throws Exception {
        String json = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.order_id");
    }

    @Test
    void createReturns201WithLocationAndComputedFields() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.order_id").isNotEmpty())
                .andExpect(jsonPath("$.country").value("MX"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(205.0))
                .andExpect(jsonPath("$.created_at").isNotEmpty());
    }

    @Test
    void createWithBlankFieldsReturns400WithDetails() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cinema_id\":0,\"user_id\":null,\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.length()").value(3));
    }

    @Test
    void createWithNonPositiveCinemaIdReturns400WithDescriptiveMessage() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cinema_id\":-5,\"user_id\":\"u\",\"items\":[{\"type\":\"TICKET\",\"name\":\"Adulto\",\"price\":85}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("cinema_id"))
                .andExpect(jsonPath("$.details[0].message").value(ValidationMessages.CINEMA_ID_MESSAGE));
    }

    @Test
    void createWithoutTicketReturns422() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cinema_id\":\"472\",\"user_id\":\"u\",\"items\":[{\"type\":\"FOOD\",\"name\":\"Combo 1\",\"price\":120}]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER"));
    }

    @Test
    void getReturns200AndUnknownOrderReturns404() throws Exception {
        String id = createOrder();
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.order_id").value(id));
        mvc.perform(get("/v1/countries/CO/orders/" + id)).andExpect(status().isNotFound());
        mvc.perform(get(BASE + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void getWithMalformedIdReturns400() throws Exception {
        mvc.perform(get(BASE + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("order_id"));
    }

    @Test
    void patchWithEmptyBodyReturns400() throws Exception {
        String id = createOrder();
        mvc.perform(patch(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void patchItemsRecalculatesTotal() throws Exception {
        String id = createOrder();
        mvc.perform(patch(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"type\":\"TICKET\",\"name\":\"Adulto\",\"price\":85}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(85.0));
    }

    @Test
    void patchWithInvalidStatusReturns400ListingAllowedValues() throws Exception {
        String id = createOrder();
        mvc.perform(patch(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString("Allowed values")));
    }

    @Test
    void deletePendingReturns204ThenGetReturns404() throws Exception {
        String id = createOrder();
        mvc.perform(delete(BASE + "/" + id)).andExpect(status().isNoContent());
        mvc.perform(get(BASE + "/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void deleteNonPendingReturns422() throws Exception {
        String id = createOrder();
        mvc.perform(patch(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_DELETABLE"));
    }
}
