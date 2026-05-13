package com.equalexperts.cart.pricing;

import com.equalexperts.cart.common.PriceRetrievalException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiPriceStrategyMockitoTest {

    private final Gson gson = new Gson();

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> response;

    private ApiPriceStrategy priceStrategy;

    @BeforeEach
    void setUp() throws Exception {
        priceStrategy = new ApiPriceStrategy();
        Field httpClientField = ApiPriceStrategy.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(priceStrategy, httpClient);
    }

    @ParameterizedTest
    @CsvSource({
            "cheerios, 8.43",
            "cornflakes, 2.52",
            "frosties, 4.99",
            "shreddies, 4.68",
            "weetabix, 9.98"
    })
    void shouldReturnPriceWhenApiResponseIsSuccessful(String productName, BigDecimal expectedPrice) throws Exception {
        Map<String, Object> apiResponse = Map.of(
                "title", productName,
                "price", expectedPrice
        );

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(gson.toJson(apiResponse));
        when(httpClient.send(
                any(HttpRequest.class),
                eq(HttpResponse.BodyHandlers.ofString())
        )).thenReturn(response);

        BigDecimal actualPrice = priceStrategy.getPrice(productName);

        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }

    @Test
    void shouldThrowExceptionWhenApiReturnsNonSuccessStatus() throws Exception {
        when(response.statusCode()).thenReturn(500);
        when(httpClient.send(
                any(HttpRequest.class),
                eq(HttpResponse.BodyHandlers.ofString())
        )).thenReturn(response);

        assertThrows(PriceRetrievalException.class,
                () -> priceStrategy.getPrice("cornflakes"));
    }

    @Test
    void shouldThrowExceptionWhenApiResponseDoesNotContainPrice() throws Exception {
        Map<String, Object> apiResponse = Map.of("title", "Cornflakes");

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(gson.toJson(apiResponse));
        when(httpClient.send(
                any(HttpRequest.class),
                eq(HttpResponse.BodyHandlers.ofString())
        )).thenReturn(response);

        assertThrows(PriceRetrievalException.class,
                () -> priceStrategy.getPrice("cornflakes"));
    }
}
