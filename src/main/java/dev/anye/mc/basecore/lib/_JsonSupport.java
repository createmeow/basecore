package dev.anye.mc.basecore.lib;

public class _JsonSupport {
    public static <T> String toJson(T t) {
        return _JsonConfig.GSON.toJson(t);
    }
    public static <T> T fromJson(String json, Class<T> tClass) {
        return _JsonConfig.GSON.fromJson(json, tClass);
    }
}