package org.eyematics.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class BundleCompressor {

    public static void compress(String bundleFolder) {
        String fileName = bundleFolder + "/eyematics_test_data.tar";
        try {
            FileOutputStream tarOutputFileStream = new FileOutputStream(fileName);
            TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(new BufferedOutputStream(tarOutputFileStream));

            File[] fileList = new File(bundleFolder).listFiles();

            assert fileList != null;
            for (File fileToTar : fileList) {
                if (!fileToTar.isDirectory() && fileToTar.getName().endsWith(".json")) {
                    TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(fileToTar, fileToTar.getName());
                    tarArchiveEntry.setSize(fileToTar.length());
                    tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
                    FileInputStream fileInputStream = new FileInputStream(fileToTar);
                    tarArchiveOutputStream.write(fileInputStream.readAllBytes());
                    tarArchiveOutputStream.closeArchiveEntry();
                    fileInputStream.close();
                    fileToTar.delete();
                }
            }

            tarArchiveOutputStream.finish();
            tarArchiveOutputStream.close();
            tarOutputFileStream.close();

            byte[] buffer = new byte[1024];
            FileOutputStream gzipOutputFileStream = new FileOutputStream(fileName + ".gz");
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(gzipOutputFileStream);

            FileInputStream in = new FileInputStream(fileName);
            int totalSize;
            while((totalSize = in.read(buffer)) > 0) gzipOutputStream.write(buffer, 0, totalSize);
            in.close();

            gzipOutputStream.finish();
            gzipOutputStream.close();
            gzipOutputFileStream.close();

            new File(fileName).delete();
        } catch (Exception e) {
            new File(fileName).delete();
            new File(fileName + ".gz").delete();
        }
    }
}
