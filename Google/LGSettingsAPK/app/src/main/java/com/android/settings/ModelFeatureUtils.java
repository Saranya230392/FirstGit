package com.android.settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class ModelFeatureUtils {
    private static final String TAG = "AssetsXmlTest";

    private static final String CONFIG_FILE = "/system/etc/setting_features.xml";

    /* Module names */
    private static final String MOD_DEFAULT = "default";
    private static final String TAG_MODEL = "Model";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_EXTENDS = "extends";

    /** Module map */
    private static HashMap<String, String> modelFeatureMap = new HashMap<String, String>();

    public static void initialize(Context context)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        InputStream featureStream = new FileInputStream(CONFIG_FILE);

        try {
            Document doc = builder.parse(featureStream);
            Element root = doc.getDocumentElement();
            Log.d(TAG, "initialize Build.DEVICE = " + Build.DEVICE);
            parseModelFeature(Build.DEVICE, root);
        } catch (SAXException se) {
            Log.w(TAG, "SAXException");
        } catch (IOException ioe) {
            Log.w(TAG, "IOException");
        } finally {
            if (featureStream != null) {
                featureStream.close();
            }
        }
    }

    private static void parseModelFeature(final String modelName,
            final Element root){
        HashMap<String, Element> modelElementMap = createModelElementMap(root);

        if (modelElementMap.get(modelName) == null) {
            fillModelFeature(MOD_DEFAULT, modelElementMap);
            Log.w(TAG, "Model feature for this model(" + modelName
                    + ") was not found in config XML file. "
                    + "Default feature loaded");
        } else {
            fillModelFeature(modelName, modelElementMap);
        }
    }

    private static HashMap<String, Element> createModelElementMap(Element root) {
        HashMap<String, Element> map = new HashMap<String, Element>();
        NodeList modelNodeList = root.getElementsByTagName(TAG_MODEL);
        for (int i = 0; i < modelNodeList.getLength(); i++) {
//            Log.d(TAG, "createModelElementMap modelNodeList.getLength() = " + modelNodeList.getLength());

            Node modelNode = modelNodeList.item(i);
//            Log.d(TAG, "createModelElementMap modelNode.getNodeName() = " + modelNode.getNodeName());

            if (!(modelNode instanceof Element))
                continue;
            Element elementModel = (Element)modelNode;

            String attrNames = elementModel.getAttribute(ATTR_NAME);
//            Log.d(TAG, "createModelElementMap elementModel.getAttribute(ATTR_NAME) = " + attrNames);

            // For each model name, put element.
            StringTokenizer tokener = new StringTokenizer(attrNames, ",");
            while (tokener.hasMoreTokens()) {
                String name = tokener.nextToken().trim();
                if (map.put(name, elementModel) != null) {
                    Log.w(TAG, "Model name " + name
                            + " is duplicated!!. Check config XML file.");
                }
            }
        }
        return map;
    }

    private static void fillModelFeature(final String modelName,
            final HashMap<String, Element> modelElementMap){

        Element modelElement = modelElementMap.get(modelName);

        String parentModelName = modelElement.getAttribute(ATTR_EXTENDS);
        if (!TextUtils.isEmpty(parentModelName)) {
            fillModelFeature(parentModelName, modelElementMap);
        }
        parseModelElement(modelElement);
    }

    private static void parseModelElement(Element modelElement) {
        NodeList childNodes = modelElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node resChildNode = childNodes.item(i);
            if (resChildNode instanceof Element) {
                Element resChildEle = (Element)resChildNode;
                String tagName = resChildEle.getTagName();
                String tagValue = resChildEle.getTextContent();
//                Log.d(TAG, "parseModelElement tagName = " + tagName + " resChildEle.getTextContent() = " + tagValue);
                putFeature(tagName, tagValue);
            }
        }
    }

    private static void putFeature(String moduleName, String value) {
        modelFeatureMap.put(moduleName, value);
    }

    public static String getFeature(Context context, String moduleName) {
        if (!("true".equals(modelFeatureMap.get("modelfeatureloaded")))) {
            try {
                ModelFeatureUtils.initialize(context);
                Log.d("Settings", "modelFeatureMap initialize");
            } catch (Exception e) {
                Log.e("Settings", "initialize failed", e);
            }
        }

        return modelFeatureMap.get(moduleName);
    }

    public static void printData(Context context){
        for (String mod : modelFeatureMap.keySet()) {
            Log.i(TAG, "[AssetsXmlTest] " + mod.toString() + " : " + getFeature(context, mod.toString()));
        }
    }

}
