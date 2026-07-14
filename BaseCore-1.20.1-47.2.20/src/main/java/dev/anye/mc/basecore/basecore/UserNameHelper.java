package dev.anye.mc.basecore.basecore;

import com.google.gson.reflect.TypeToken;
import dev.anye.mc.basecore.lib._File;
import dev.anye.mc.basecore.lib._JsonConfig;

import java.util.Map;
import java.util.UUID;

public class UserNameHelper extends _JsonConfig<Map<String,String>> {
    public UserNameHelper() {
        super(_File.getFileFullPathWithRun("usernamecache.json"), """
                    """, new TypeToken<>(){});
    }
    public String getName(UUID uuid){
        return getName(uuid.toString());
    }

    public String getName(String uuid){
        if (this.datas == null) return uuid;
        else return datas.getOrDefault(uuid,uuid);
    }
}