package com.kles.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jchau
 */
public class XMLtoJSonConverter {

    public static JSON getJSONFromXMLFile(File f) {
        URL url = null;
        InputStream inputStream = null;
        try {
            url = XMLtoJSonConverter.class.getClassLoader().getResource(f.getAbsolutePath());
            inputStream = url.openStream();
            return getJSONFromXML(IOUtils.toString(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                url = null;
            } catch (IOException ex) {
            }
        }
        return null;
    }

    public static JSON getJSONFromXML(String xml) {
        JSON objJson = new XMLSerializer().read(xml);
        return objJson;
    }
}
