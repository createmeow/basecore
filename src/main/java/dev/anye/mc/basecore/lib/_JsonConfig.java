package dev.anye.mc.basecore.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class _JsonConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(_JsonConfig.class);
    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    public static final Gson GSON_PRETTY = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
}