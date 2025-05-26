package roomescape.controller;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(RestDocumentationExtension.class)
@ActiveProfiles("test")
public abstract class AbstractRestDocsTest {

    @LocalServerPort
    int port;

    protected RequestSpecification documentationSpec;

    @BeforeEach
    void setUpRestDocs(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;

        this.documentationSpec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withRequestDefaults(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("Content-Length")
                        )
                        .withResponseDefaults(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("Transfer-Encoding")
                                        .remove("Date")
                                        .remove("Keep-Alive")
                                        .remove("Connection")
                                        .remove("Content-Length")
                        )
                ).build();
    }

    protected RequestSpecification givenWithDocs(String identifier) {
        return RestAssured.given(documentationSpec)
                .filter(document(identifier))
                .log().all();
    }
}
