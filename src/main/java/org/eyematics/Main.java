package org.eyematics;

import org.eyematics.builder.BundleResource;
import org.eyematics.builder.PatientResource;
import org.eyematics.util.BundleWriter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.util.List;

public class Main {

    private static final List<String> DIC = List.of("AC", "GW", "MS", "TB");
    private static final String BASE_PATH = "output";
    private static final int MAX_PATIENTS = 9;

    public static void main(String[] args) {
        BundleWriter bundleWriter = new BundleWriter();
        System.out.print("Creating one shared Patient...");
        Patient sharedPatient = new PatientResource()
                .setPatientID(MAX_PATIENTS + 1)
                .setSource("shared")
                .build();
        System.out.println("DONE");
        for (String d : DIC) {
            int consentId = 1;
            System.out.print("Creating and Preparing Bundle for " + d + "...");
            Bundle dicBundle = new BundleResource()
                    .setBundleType(Bundle.BundleType.TRANSACTION)
                    .setHttpVerb(Bundle.HTTPVerb.PUT)
                    .build();
            dicBundle.getEntry().clear();
            System.out.println("DONE");
            System.out.print("Creating patients for " + d + "...");
            for (int j = 0; j < MAX_PATIENTS; j++) {
                BundleResource randomPatientBundle = new BundleResource();
                Bundle ramdomPatient = randomPatientBundle.randomize()
                        .setBundleType(Bundle.BundleType.TRANSACTION)
                        .setHttpVerb(Bundle.HTTPVerb.PUT)
                        .setPatientId(j + 1)
                        .setSource(d.toLowerCase())
                        .setConsentId(consentId)
                        .build();
                ramdomPatient.getEntry().forEach(dicBundle::addEntry);
                consentId = randomPatientBundle.getConsentId();
            }
            System.out.println("DONE");
            System.out.print("Adding shared patient into Bundle for " + d + "...");
            Bundle sharedPatientResources = new BundleResource()
                    .setBundleType(Bundle.BundleType.TRANSACTION)
                    .setHttpVerb(Bundle.HTTPVerb.PUT)
                    .setPatientId(MAX_PATIENTS + 1)
                    .setConsentId(consentId)
                    .build();
            sharedPatient.getMeta().setSource("mailto:medic@" + d.toLowerCase() + ".org");
            sharedPatientResources.getEntry().get(0).setResource(sharedPatient);
            sharedPatientResources.getEntry().forEach(dicBundle::addEntry);
            bundleWriter.writeBundleToJSON(dicBundle, BASE_PATH, "testdatensatz_" + d.toLowerCase());
            System.out.println("DONE");
        }
    }
}