/**
 * Copyright (C) 2013 Open WhisperSystems
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.whispersystems.textsecuregcm.configuration.AttachmentsConfiguration;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class UrlSigner {

    private static final int DURATION = 60 * 60 * 1000;

    private final AWSCredentials credentials;
    private final String bucket;
    private final String endpoint;

    public UrlSigner(AttachmentsConfiguration config) {
        this.credentials = new BasicAWSCredentials(config.getAccessKey(), config.getAccessSecret());
        this.bucket = config.getBucket();
        this.endpoint = config.getEndpoint();
    }

    public URL getPreSignedUrl(long attachmentId, HttpMethod method) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        AmazonS3 client = new AmazonS3Client(credentials, clientConfiguration);
        Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
        client.setRegion(euWest1);
        client.setEndpoint(endpoint);
        //client.setEndpoint("http://localhost:9000");
        client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).build());

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, String.valueOf(attachmentId), method);

        request.setExpiration(new Date(System.currentTimeMillis() + DURATION));
        request.setContentType("application/octet-stream");

        return client.generatePresignedUrl(request);
    }

    /*public String getPreSignedUrl(long attachmentId, HttpMethod method)
            throws InvalidKeyException, NoSuchAlgorithmException, IOException, XmlPullParserException, MinioException {
        String request = geturl(bucket, String.valueOf(attachmentId), method);

        return request;
    }

    public String geturl(String bucketname, String attachemtnId, HttpMethod method)
            throws NoSuchAlgorithmException, IOException, InvalidKeyException, XmlPullParserException, MinioException {
        String url = null;

        MinioClient minioClient = new MinioClient("http://localhost:9000", "minioadmin", "minioadmin");

        try {

            if (!minioClient.bucketExists(bucketname)) {
                minioClient.makeBucket("signalbucket1234", "eu-west-1");
            }

            if (method == HttpMethod.PUT) {
                url = minioClient.presignedPutObject("signalbucket1234", attachemtnId, DURATION);
            }
            if (method == HttpMethod.GET) {
                url = minioClient.presignedGetObject("signalbucket1234", attachemtnId);
            }
        } catch (MinioException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return url;
    }*/
}