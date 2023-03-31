package com.vincentengelsoftware.androidimagecompare.MetaData;

import android.content.ContentResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Preparer {
    public static ArrayList<HashMap<String, HashMap<String, String>>> load(ContentResolver cr, String imagePathFirst, String imagePathSecond)
    {
        ArrayList<HashMap<String, HashMap<String, String>>> metaData = new ArrayList<>();

        try {
            HashMap<String, HashMap<String, String>> metaDataFirst = Extractor.getMetaData(cr, imagePathFirst);
            HashMap<String, HashMap<String, String>> metaDataSecond = Extractor.getMetaData(cr, imagePathSecond);

            Preparer.addMissingMetaDataFromTo(metaDataFirst, metaDataSecond);
            Preparer.addMissingMetaDataFromTo(metaDataSecond, metaDataFirst);


            metaData.add(0, metaDataFirst);
            metaData.add(1, metaDataSecond);
        } catch (Exception ignored) {
        }

        return metaData;
    }

    // This is bad, should actually return the values instead of manipulating them
    private static void addMissingMetaDataFromTo(
            HashMap<String, HashMap<String, String>> fromHashMap,
            HashMap<String, HashMap<String, String>> toHashMap
    ) {
        Set<String> groupKeys = fromHashMap.keySet();

        for (String groupName : groupKeys) {
            if (!toHashMap.containsKey(groupName)) {
                toHashMap.put(groupName, new HashMap<>());
            }

            Set<String> valueKeys = fromHashMap.get(groupName).keySet();
            for (String valueName : valueKeys) {
                if (!toHashMap.get(groupName).containsKey(valueName)) {
                    toHashMap.get(groupName).put(valueName, "");
                }
            }
        }
    }

    public static String[] getSortedKeys(Set<String> keySet)
    {
        String[] groupNames = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(groupNames);

        return groupNames;
    }
}
