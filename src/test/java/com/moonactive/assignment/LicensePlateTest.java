package com.moonactive.assignment;

import com.moonactive.assignment.controller.LicensePlateController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class LicensePlateTest {

    @LocalServerPort
    private int port = 3000;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testProhibited() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/41Q5wj7/01234589.jpg",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("ProhibitedDigits vehicle is NOT allowed to enter the parking lot.");
    }

    @Test
    public void testProhibited2() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/zsmpbmL/7722286.jpg",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("ProhibitedDigits vehicle is NOT allowed to enter the parking lot.");
    }

    @Test
    public void testProhibited3() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/qCPGnQm/00.png",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("ProhibitedDigits vehicle is NOT allowed to enter the parking lot.");
    }

    @Test
    public void testAllowed() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/xSX7w0j/12.jpg",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("Vehicle number 1674123 is allowed to enter the parking lot.");
    }

    @Test
    public void testAllowed2() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/SmqQrjF/2952165.jpg",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("Vehicle number 2952165 is allowed to enter the parking lot.");
    }


    @Test
    public void testGas() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/tcr0S7n/2026022.png",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("Gas vehicle is NOT allowed to enter the parking lot.");
    }

    @Test
    public void testMilitary() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/gR9jkg6/cali.jpg",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("Military vehicle is NOT allowed to enter the parking lot.");
    }

    @Test
    public void testMilitary2() {
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                        "/plates/check?image_url=https://i.ibb.co/Q8RPWBc/9.jpg",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });
        String result = response.getBody();
        assertThat(result).isEqualTo("Military vehicle is NOT allowed to enter the parking lot.");
    }



    @Test
    public void testSendPost() throws Exception{
        String result = LicensePlateController.postRequest("https://i.ibb.co/SmqQrjF/2952165.jpg");
        boolean doesContain = result.contains("\"ParsedText\":\"29:521 65\\t\\r\\n\"");
        assertTrue(doesContain);
    }

}