package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.ContentResolver;
import android.net.Uri;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.InputStream;

public class MetaDataExtractor {
    public static String[] getMetaData(ContentResolver cr, String path) {
        try {
            InputStream input = cr.openInputStream(Uri.parse(path));
            Metadata metadata = ImageMetadataReader.readMetadata(input);

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    tag.getTagType();
                    String name = tag.getTagName();
                }
            }

            input.close();

        } catch (Exception ignored) {
        }

        return new String[]{"Error while reading file"};
    }
}
