package com.equalexperts.cart.pricing;

import com.equalexperts.cart.common.PriceRetrievalException;
import com.equalexperts.cart.model.response.PriceResponse;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static com.equalexperts.cart.common.Constant.BASE_URL;

public class ApiPriceStrategy implements PriceStrategy{

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();


    @Override
    public BigDecimal getPrice(String productName) {
        try{

            String url = BASE_URL+ productName + ".json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new PriceRetrievalException(
                        "Failed to fetch price for product: " + productName + ". Status: " + response.statusCode()
                );
            }

            PriceResponse priceResponse =
                    gson.fromJson(response.body(), PriceResponse.class);

            if (priceResponse == null || priceResponse.getPrice() == null) {
                throw new PriceRetrievalException(
                        "Price not found in API response for product: " + productName
                );
            }

            return priceResponse.getPrice();

        }catch (PriceRetrievalException e) {
            throw e;
        } catch (Exception e) {
            throw new PriceRetrievalException(
                    "Failed to fetch price for product: " + productName, e
            );
        }
    }
}
