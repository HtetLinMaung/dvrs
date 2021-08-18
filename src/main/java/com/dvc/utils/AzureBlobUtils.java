package com.dvc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.EnumSet;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

public class AzureBlobUtils {
    public static String containerName = System.getenv("AzureStorageContainerName");

    public static String createBlob(String file, String fileName) throws IOException {
        deleteBlob(fileName);
        BlobClient blobClient = getBlobClient(fileName);
        blobClient.upload(CommonUtils.getInputStreamFromBase64(file),
                CommonUtils.getByteArrayFromBase64File(file).length);
        return blobClient.getBlobUrl();
    }

    public static String createBlob(ByteArrayOutputStream out, String fileName) throws IOException {
        deleteBlob(fileName);
        BlobClient blobClient = getBlobClient(fileName);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        blobClient.upload(in, out.toByteArray().length);

        return blobClient.getBlobUrl();
    }

    public static BlobClient getBlobClient(String fileName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(System.getenv("AzureStorageConnectionString")).buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(fileName);
    }

    public static void deleteBlob(String filename) {

        try {
            getBlobClient(filename).delete();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void test() throws IOException {
        // Create a BlobServiceClient object which will be used to create a container
        // client
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(System.getenv("AzureStorageConnectionString")).buildClient();

        // Create a unique name for the container
        String containerName = "quickstartblobs" + java.util.UUID.randomUUID();

        // Create the container and return a container client object
        BlobContainerClient containerClient = blobServiceClient.createBlobContainer(containerName);

        // Create a local file in the ./data/ directory for uploading and downloading
        String localPath = "./data/";
        String fileName = "quickstart" + java.util.UUID.randomUUID() + ".txt";
        File localFile = new File(localPath + fileName);

        // Write text to the file
        FileWriter writer = new FileWriter(localPath + fileName, true);
        writer.write("Hello, World!");
        writer.close();

        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        System.out.println("\nUploading to Blob storage as blob:\n\t" + blobClient.getBlobUrl());

        // Upload the blob
        blobClient.uploadFromFile(localPath + fileName);

        // Download the blob to a local file
        // Append the string "DOWNLOAD" before the .txt extension so that you can see
        // both files.
        String downloadFileName = fileName.replace(".txt", "DOWNLOAD.txt");
        File downloadedFile = new File(localPath + downloadFileName);

        System.out.println("\nDownloading blob to\n\t " + localPath + downloadFileName);

        blobClient.downloadToFile(localPath + downloadFileName);

    }

    public static String getSasToken() throws URISyntaxException, StorageException, InvalidKeyException {
        CloudStorageAccount account = CloudStorageAccount.parse(System.getenv("AzureStorageConnectionString"));

        // Create a blob service client
        CloudBlobClient blobClient = account.createCloudBlobClient();

        CloudBlobContainer container = blobClient.getContainerReference(containerName);

        Date expirationTime = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneOffset.UTC).toInstant());
        SharedAccessBlobPolicy sharedAccessPolicy = new SharedAccessBlobPolicy();
        sharedAccessPolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ,
                SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.ADD));
        sharedAccessPolicy.setSharedAccessStartTime(new Date());
        sharedAccessPolicy.setSharedAccessExpiryTime(expirationTime);

        String sasToken = container.generateSharedAccessSignature(sharedAccessPolicy, null);
        return sasToken;
    }
}
