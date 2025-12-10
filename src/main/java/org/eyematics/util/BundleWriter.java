package org.eyematics.util;


import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BundleWriter {

    private final FhirContext fhirContext;

    public BundleWriter() {
        this.fhirContext = FhirContext.forR4();
    }

    public boolean writeBundleToJSON(Bundle bundle, String folder, String filename) {
        if (bundle == null) return false;

        Path bundlePath = Path.of(folder);
        try {
            Files.createDirectories(bundlePath.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Folder for FHIR resources already existing.");
        }

        String resourceJSON = filename + ".json";
        bundlePath = bundlePath.resolve(resourceJSON);
        return this.writeFHIRResourceFile(bundle, bundlePath);
    }

    private boolean writeFHIRResourceFile(Bundle bundle, Path path) {
        try (FileOutputStream os = new FileOutputStream(path.toFile())) {
            String bundleJSON = this.fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
            os.write(bundleJSON.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
