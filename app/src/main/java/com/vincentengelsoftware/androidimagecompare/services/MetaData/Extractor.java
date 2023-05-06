package com.vincentengelsoftware.androidimagecompare.services.MetaData;

import android.content.ContentResolver;
import android.net.Uri;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.InputStream;
import java.util.HashMap;

public class Extractor {
    public static HashMap<String, HashMap<String, String>> getMetaData(ContentResolver cr, String path) {
        HashMap<String, HashMap<String, String>> metaData = new HashMap<>();

        try {
            InputStream input = cr.openInputStream(Uri.parse(path));
            Metadata metadata = ImageMetadataReader.readMetadata(input);

            for (Directory directory : metadata.getDirectories()) {
                HashMap<String, String> keyValue = new HashMap<>();

                for (Tag tag : directory.getTags()) {
                    keyValue.put(tag.getTagName(), tag.getDescription());
                }

                metaData.put(directory.getName(), keyValue);
            }

            input.close();

        } catch (Exception ignored) {
        }

        return metaData;
    }
}
