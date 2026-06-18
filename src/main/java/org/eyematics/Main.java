package org.eyematics;

import org.eyematics.builder.BundleResource;
import org.eyematics.builder.MeasureReportResource;
import org.eyematics.builder.MedicationResource;
import org.eyematics.builder.PatientResource;
import org.eyematics.util.BundleCompressor;
import org.eyematics.util.BundleWriter;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Main {

    private static final List<String> DIC = List.of("AC", "CN", "GW", "LZ", "MS", "TU");
    private static final String BASE_PATH = "output";
    private static final int AMOUNT_PATIENT_BUNDLES = 50;
    private static final int AMOUNT_VALID_PATIENT_PER_BUNDLE = 10;
    private static final int AMOUNT_INVALID_PATIENT_PER_BUNDLE = 10;
    private static final int AMOUNT_MEASURE_REPORTS = 3;
    private static final int AMOUNT_SHARED_MEDICATIONS = 5;
    private static final boolean READABLE = false;
    private static final boolean COMPRESSED = true;

    public static void main(String[] args) {
        Date startDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.now().plusYears(5).atZone(ZoneId.systemDefault()).toInstant());
        System.out.print("Creating one shared Patient...");
        Patient sharedPatient = new PatientResource()
                .randomize()
                .setSource("shared")
                .build();
        System.out.println("DONE");
        System.out.print("Creating shared Medications...");
        List<Medication> medicationList = new ArrayList<>();
        MedicationResource mr = new MedicationResource();
        for (int i = 0; i < AMOUNT_SHARED_MEDICATIONS; i++) {
            medicationList.add(mr.randomize().build());
        }
        System.out.println("DONE");
        for (String d : DIC) {
            System.out.print("Creating and Preparing Bundle for " + d + "...");
            boolean areAdditionalAdded = false;
            Bundle dicBundle = new BundleResource()
                    .setBundleType(Bundle.BundleType.TRANSACTION)
                    .setHttpVerb(Bundle.HTTPVerb.PUT)
                    .build();
            Bundle.BundleEntryRequestComponent brc = new Bundle.BundleEntryRequestComponent();
            brc.setMethod(Bundle.HTTPVerb.PUT);
            System.out.println("DONE");
            int bundleCounter = 1;
            for (int j = 0; j < AMOUNT_PATIENT_BUNDLES; j++) {
                dicBundle.getEntry().clear();
                if (!areAdditionalAdded) {
                    System.out.print("Adding shared Medications into Bundle for " + d + "...");
                    medicationList.forEach(m -> dicBundle.addEntry()
                            .setResource(m)
                            .setRequest(brc.copy().setUrl(m.getResourceType().name() + "/" + m.getId()))
                            .setFullUrl(m.getResourceType().name() + "/" + m.getId())
                    );
                    System.out.println("DONE");
                    System.out.print("Adding shared Patient and MDAT into Bundle for " + d + "...");
                    Bundle sharedPatientResources = new BundleResource().randomize()
                            .setBundleType(Bundle.BundleType.TRANSACTION)
                            .setHttpVerb(Bundle.HTTPVerb.PUT)
                            .setPatient(sharedPatient)
                            .setPeriod(startDate, endDate)
                            .setAmountValidObservation(5)
                            .setAmountInvalidObservation(5)
                            .setAmountValidDiagnosticReport(5)
                            .setAmountInvalidDiagnosticReport(5)
                            .setMedication(medicationList)
                            .setAmountValidMedicationRequest(5)
                            .setAmountInvalidMedicationRequest(5)
                            .setAmountValidMedicationAdministration(5)
                            .setAmountInvalidMedicationAdministration(5)
                            .setAmountValidConsent(5)
                            .setAmountInvalidConsent(5)
                            .build();
                    sharedPatientResources.getEntry().forEach(dicBundle::addEntry);
                    System.out.println("DONE");
                    System.out.print("Creating MeasureReports for " + d + "...");
                    int count = 0;
                    for (int i = 0; i < AMOUNT_MEASURE_REPORTS; i++) {
                        count = ThreadLocalRandom.current().nextInt(count, 1000);
                        MeasureReport measureReport = new MeasureReportResource()
                                .randomize()
                                .setPeriod(startDate, endDate)
                                .setCount(count)
                                .build();
                        String measureReportName = measureReport.getResourceType().name() + "/" + measureReport.getId();
                        dicBundle.addEntry()
                                .setResource(measureReport)
                                .setRequest(brc.copy().setUrl(measureReportName))
                                .setFullUrl(measureReportName);
                    }
                    System.out.println("DONE");
                    areAdditionalAdded = true;
                }
                System.out.print("Creating patients and mdat for " + d + "...");
                for (int i = 0; i < AMOUNT_VALID_PATIENT_PER_BUNDLE; i++) {
                    BundleResource randomValidPatientBundle = new BundleResource();
                    Bundle ramdomValidPatient = randomValidPatientBundle.randomize()
                            .setBundleType(Bundle.BundleType.TRANSACTION)
                            .setHttpVerb(Bundle.HTTPVerb.PUT)
                            .setPeriod(startDate, endDate)
                            .setAmountValidObservation(5)
                            .setAmountInvalidObservation(5)
                            .setAmountValidDiagnosticReport(5)
                            .setAmountInvalidDiagnosticReport(5)
                            .setMedication(medicationList)
                            .setAmountValidMedicationRequest(5)
                            .setAmountInvalidMedicationRequest(5)
                            .setAmountValidMedicationAdministration(5)
                            .setAmountInvalidMedicationAdministration(5)
                            .setAmountValidConsent(5)
                            .setAmountInvalidConsent(5)
                            .setSource(d.toLowerCase())
                            .build();
                    ramdomValidPatient.getEntry().forEach(dicBundle::addEntry);
                }
                for (int i = 0; i < AMOUNT_INVALID_PATIENT_PER_BUNDLE; i++) {
                    BundleResource randomInvalidPatientBundle = new BundleResource();
                    Bundle ramdomInvalidPatient = randomInvalidPatientBundle.randomize()
                            .setBundleType(Bundle.BundleType.TRANSACTION)
                            .setHttpVerb(Bundle.HTTPVerb.PUT)
                            .setPeriod(startDate, endDate)
                            .setAmountValidObservation(5)
                            .setAmountInvalidObservation(5)
                            .setAmountValidDiagnosticReport(5)
                            .setAmountInvalidDiagnosticReport(5)
                            .setMedication(medicationList)
                            .setAmountValidMedicationRequest(5)
                            .setAmountInvalidMedicationRequest(5)
                            .setAmountValidMedicationAdministration(5)
                            .setAmountInvalidMedicationAdministration(5)
                            .setAmountValidConsent(0)
                            .setAmountInvalidConsent(5)
                            .setSource(d.toLowerCase())
                            .build();
                    ramdomInvalidPatient.getEntry().forEach(dicBundle::addEntry);
                }
                System.out.println("DONE");
                System.out.print("Writing Bundle #" + bundleCounter +" for " + d + "...");
                BundleWriter.writeBundleToJSON(dicBundle,
                        BASE_PATH + "/" + d, Main.getFileNumber(AMOUNT_PATIENT_BUNDLES, bundleCounter)
                                + "_test_data_" + d.toLowerCase(),
                        READABLE);
                bundleCounter++;
                System.out.println("DONE");
            }
            if (COMPRESSED) BundleCompressor.compress(BASE_PATH + "/" + d);
        }
    }

    public static String getFileNumber(int amount, int number) {
        StringBuilder numberString = new StringBuilder();
        int maxPlaces = String.valueOf(amount).length();
        int places = maxPlaces - String.valueOf(number).length();
        numberString.append("0".repeat(Math.max(0, places)));
        return numberString.toString() + number;
    }
}