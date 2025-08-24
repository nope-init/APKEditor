package com.reandroid.apkeditor.utils;

import com.reandroid.apk.ApkModule;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.apkeditor.compile.BuildOptions;
import com.reandroid.apkeditor.merge.MergerOptions;

import java.util.Iterator;
import java.util.List;

import java.util.function.Consumer;

public class ManifestUtils {

    public interface MetaOptions {
        List<String> getSetMeta();
    }

    public static MetaOptions from(final MergerOptions options) {
        return new MetaOptions() {
            @Override
            public List<String> getSetMeta() {
                return options.setMeta;
            }
        };
    }

    public static MetaOptions from(final BuildOptions options) {
        return new MetaOptions() {
            @Override
            public List<String> getSetMeta() {
                return options.setMeta;
            }
        };
    }

public static void applySetMetaOptions(ApkModule module, MetaOptions options, Consumer<String> logger) {
    if (options == null || options.getSetMeta() == null || options.getSetMeta().isEmpty()) return;

    AndroidManifestBlock manifest = module.getAndroidManifestBlock();
    if (manifest == null) return;

    ResXmlElement appEl = manifest.getApplicationElement();
    if (appEl == null) return;

    for (String entry : options.getSetMeta()) {
        int eq = entry.indexOf('=');
        if (eq <= 0 || eq == entry.length() - 1) continue;

        String name = entry.substring(0, eq).trim();
        String value = entry.substring(eq + 1).trim();

        ResXmlElement meta = findMetaByName(appEl, name);
        if (meta != null) {
            // Iterate attributes
            ResXmlAttribute valueAttr = null;
            Iterator<ResXmlAttribute> iter = meta.getAttributes();
            while (iter.hasNext()) {
                ResXmlAttribute attr = iter.next();
                if ("value".equals(attr.getName())) {
                    valueAttr = attr;
                    break;
                }
            }

            if (valueAttr != null) {
                String oldValue = valueAttr.getValueAsString();
                valueAttr.setValueAsString(value);

                // Log change using the passed logger
                if (logger != null) {
                    logger.accept(String.format("Replaced <meta-data> : %s = %s -> %s",
                            name, oldValue, value));
                }
            }
        }
    }
}


    public static ResXmlElement findMetaByName(ResXmlElement appEl, String metaName) {
        for (ResXmlElement child : appEl.listElements("meta-data")) {
            Iterator<ResXmlAttribute> iter = child.getAttributes();
            while (iter.hasNext()) {
                ResXmlAttribute attr = iter.next();
                if ("name".equals(attr.getName()) && metaName.equals(attr.getValueAsString())) {
                    return child;
                }
            }
        }
        return null;
    }
}
