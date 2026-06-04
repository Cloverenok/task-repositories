package com.github.repositories;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubControllerIntegrationTest {

    static WireMockServer wireMockServer = new WireMockServer(8081);

    @LocalServerPort
    private int port;

    private RestClient restClient;

    private static final String TEST_USER = "testuser";
    private static final String NON_EXISTENT_USER = "nonexistentuser12345";

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
        configureFor("localhost", 8081);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.url", () -> "http://localhost:8081");
    }

    @Test
    void shouldReturnRepositoriesWithoutForks() {
        stubFor(get(urlEqualTo("/users/" + TEST_USER + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {"name": "repo1", "fork": false, "owner": {"login": "%s"}},
                                    {"name": "forked-repo", "fork": true, "owner": {"login": "%s"}},
                                    {"name": "repo2", "fork": false, "owner": {"login": "%s"}}
                                ]
                                """.formatted(TEST_USER, TEST_USER, TEST_USER))));

        stubFor(get(urlEqualTo("/repos/" + TEST_USER + "/repo1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {"name": "main", "commit": {"sha": "abc123"}},
                                    {"name": "develop", "commit": {"sha": "def456"}}
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USER + "/repo2/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {"name": "master", "commit": {"sha": "xyz789"}}
                                ]
                                """)));

        ResponseEntity<RepositoryDto[]> response = restClient.get()
                .uri("/api/repositories/" + TEST_USER)
                .retrieve()
                .toEntity(RepositoryDto[].class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);

        RepositoryDto firstRepo = response.getBody()[0];
        assertThat(firstRepo.getRepositoryName()).isEqualTo("repo1");
        assertThat(firstRepo.getOwnerLogin()).isEqualTo(TEST_USER);
        assertThat(firstRepo.getBranches()).hasSize(2);

        RepositoryDto secondRepo = response.getBody()[1];
        assertThat(secondRepo.getRepositoryName()).isEqualTo("repo2");
        assertThat(secondRepo.getBranches()).hasSize(1);
    }

    @Test
    void shouldReturn404WhenUserNotFound() {
        stubFor(get(urlEqualTo("/users/" + NON_EXISTENT_USER + "/repos"))
                .willReturn(aResponse().withStatus(404)));

        ResponseEntity<ErrorResponse> response = restClient.get()
                .uri("/api/repositories/" + NON_EXISTENT_USER)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {})
                .toEntity(ErrorResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("User not found: " + NON_EXISTENT_USER);
    }
}
