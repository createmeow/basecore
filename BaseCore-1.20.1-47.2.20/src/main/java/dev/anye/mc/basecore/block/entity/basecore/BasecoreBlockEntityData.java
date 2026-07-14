package dev.anye.mc.basecore.block.entity.basecore;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BasecoreBlockEntityData {
    public static final int DefaultRange = 9;
    private static final int DefaultHealth = 500;
    private int health = DefaultHealth;
    private int range = DefaultRange;
    private int maxHealth = DefaultHealth;
    private int interferenceTime = 0;
    private String name = "";
    private final List<UUID> user = new ArrayList<>();
    public BasecoreBlockEntityData(){}

    public void setDefault(){
        setDefaultHealth();
        setDefaultMaxHealth();
        setDefaultRange();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInterferenceTime(){
        return interferenceTime;
    }
    public void tick(){
        if (interferenceTime > 0) interferenceTime --;
    }

    public void setInterferenceTime(int interferenceTime) {
        this.interferenceTime = interferenceTime;
    }


    public void setDefaultHealth() {
        setHealth(DefaultHealth);
    }
    public void addHealth(int health) {
        if (this.health + health > maxHealth) {
            this.health = maxHealth;
        }else this.health += health;
    }
    public void setHealth(int health) {
        this.health = health;
    }

    public void setDefaultRange() {
        setRange(DefaultRange);
    }
    public void addRange(int range) {
        this.range += range;
    }
    public void setRange(int range) {
        this.range = range;
    }

    public void setDefaultMaxHealth() {
        setMaxHealth(DefaultHealth);
    }
    public void addMaxHealth(int maxHealth) {
        this.maxHealth += maxHealth;
    }
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getRange() {
        return range;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getHealth() {
        return health;
    }
    public void addUser(UUID uuid){
        if (!user.contains(uuid)) user.add(uuid);
    }
    public void rmUser(UUID uuid){
        user.remove(uuid);
    }
    public boolean hasPermission(UUID uuid){
        return user.contains(uuid);
    }

    public void saveToNbt(CompoundTag data){
        data.putInt("health",health);
        data.putInt("max_health",maxHealth);
        data.putInt("range",range);
        data.putString("name",name);
        data.putInt("interference_time",interferenceTime);
        CompoundTag compoundTag = new CompoundTag();
        user.forEach(uuid -> compoundTag.putBoolean(uuid.toString(),true));
        data.put("user",compoundTag);
    }

    public CompoundTag saveToNbt(){
        CompoundTag data = new CompoundTag();
        saveToNbt(data);
        return data;
    }
    public void handle(CompoundTag compoundTag){
        if (compoundTag == null){
            this.range = 0;
        }else {
            setMaxHealth(compoundTag.getInt("max_health"));
            setHealth(compoundTag.getInt("health"));
            setRange(compoundTag.getInt("range"));
            setName(compoundTag.getString("name"));
            setInterferenceTime(compoundTag.getInt("interference_time"));
            CompoundTag userNbt = compoundTag.getCompound("user");
            user.clear();
            userNbt.getAllKeys().forEach(s -> {
                if (userNbt.getBoolean(s)) user.add(UUID.fromString(s));
            });
        }
    }


    @Override
    public String toString() {
        return "BasecoreBlockEntityData{" +
                "health=" + health +
                ", range=" + range +
                ", maxHealth=" + maxHealth +
                ", interferenceTime=" + interferenceTime +
                '}';
    }
}
