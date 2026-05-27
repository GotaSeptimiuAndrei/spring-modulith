package com.example.springmodulith;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class SpringModulithApplicationTests {

    ApplicationModules modules = ApplicationModules.of(SpringModulithApplication.class);

    @Test
    void verifyArchitecturalBoundaries() {
        // This will fail the test if any module violates the access rules
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        // This automatically generates the PlantUML and C4 diagrams for your report!
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
