package dev.anye.mc.basecore.block.entity.basecore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

@SuppressWarnings("all")
public class BasecoreBlockEntityData {
    public static final int DefaultRange = 10;
    private static final int DefaultHealth = 500;
    private int health = DefaultHealth;
    private int maxHealth = DefaultHealth;
    private int range = DefaultRange;
    private String name = "";
    private String ownerName = "";
    private int interferenceTime = 0;
    private UUID owner = null;
    private int invincibleTime = 0;
    private int durabilityDamage = 0;
    private final Set<UUID> permittedPlayers = new HashSet<>();
    /** Stores player names for members (keyed by UUID), persisted in NBT */
    private final Map<UUID, String> memberNames = new HashMap<>();

    // ----- Enemy management -----
    private final Set<UUID> enemies = new HashSet<>();
    private int enemyInvincibleTime = 0;

    public BasecoreBlockEntityData() {}

    public BasecoreBlockEntityData(CompoundTag tag) {
        handle(tag);
    }

    public void handle(CompoundTag tag) {
        // Always clear mutable collections; they may be re-populated from the tag below
        permittedPlayers.clear();
        memberNames.clear();
        enemies.clear();
        if (tag.contains("health")) health = tag.getInt("health");
        if (tag.contains("max_health")) maxHealth = tag.getInt("max_health");
        if (tag.contains("range")) range = tag.getInt("range");
        if (tag.contains("name")) name = tag.getString("name");
        if (tag.contains("owner_name")) ownerName = tag.getString("owner_name");
        if (tag.contains("interference_time")) interferenceTime = tag.getInt("interference_time");
        if (tag.contains("owner")) owner = tag.getUUID("owner");
        if (tag.contains("durability_damage")) durabilityDamage = tag.getInt("durability_damage");
        if (tag.contains("permitted_players")) {
            int[] uuids = tag.getIntArray("permitted_players");
            for (int i = 0; i < uuids.length; i += 4) {
                long msb = ((long) uuids[i] << 32) | (uuids[i + 1] & 0xFFFFFFFFL);
                long lsb = ((long) uuids[i + 2] << 32) | (uuids[i + 3] & 0xFFFFFFFFL);
                UUID uid = new UUID(msb, lsb);
                permittedPlayers.add(uid);
            }
        }
        if (tag.contains("member_names")) {
            CompoundTag nameTag = tag.getCompound("member_names");
            for (String key : nameTag.getAllKeys()) {
                memberNames.put(UUID.fromString(key), nameTag.getString(key));
            }
        }
        if (tag.contains("enemies")) {
            enemies.clear();
            int[] uuids = tag.getIntArray("enemies");
            for (int i = 0; i < uuids.length; i += 4) {
                long msb = ((long) uuids[i] << 32) | (uuids[i + 1] & 0xFFFFFFFFL);
                long lsb = ((long) uuids[i + 2] << 32) | (uuids[i + 3] & 0xFFFFFFFFL);
                enemies.add(new UUID(msb, lsb));
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("health", health);
        tag.putInt("max_health", maxHealth);
        tag.putInt("range", range);
        tag.putString("name", name);
        tag.putString("owner_name", ownerName);
        tag.putInt("interference_time", interferenceTime);
        if (owner != null) tag.putUUID("owner", owner);
        tag.putInt("durability_damage", durabilityDamage);
        if (!permittedPlayers.isEmpty()) {
            int[] uuids = new int[permittedPlayers.size() * 4];
            int i = 0;
            for (UUID uuid : permittedPlayers) {
                uuids[i++] = (int)(uuid.getMostSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getMostSignificantBits());
                uuids[i++] = (int)(uuid.getLeastSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getLeastSignificantBits());
            }
            tag.putIntArray("permitted_players", uuids);
        }
        if (!memberNames.isEmpty()) {
            CompoundTag nameTag = new CompoundTag();
            memberNames.forEach((uid, name) -> nameTag.putString(uid.toString(), name));
            tag.put("member_names", nameTag);
        }
        if (!enemies.isEmpty()) {
            int[] uuids = new int[enemies.size() * 4];
            int i = 0;
            for (UUID uuid : enemies) {
                uuids[i++] = (int)(uuid.getMostSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getMostSignificantBits());
                uuids[i++] = (int)(uuid.getLeastSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getLeastSignificantBits());
            }
            tag.putIntArray("enemies", uuids);
        }
        return tag;
    }

    public CompoundTag saveToNbt() {
        CompoundTag tag = save();
        return tag;
    }

    public void saveToNbt(CompoundTag tag) {
        tag.putInt("health", health);
        tag.putInt("max_health", maxHealth);
        tag.putInt("range", range);
        tag.putString("name", name);
        tag.putString("owner_name", ownerName);
        tag.putInt("interference_time", interferenceTime);
        if (owner != null) tag.putUUID("owner", owner);
        tag.putInt("durability_damage", durabilityDamage);
        if (!permittedPlayers.isEmpty()) {
            int[] uuids = new int[permittedPlayers.size() * 4];
            int i = 0;
            for (UUID uuid : permittedPlayers) {
                uuids[i++] = (int)(uuid.getMostSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getMostSignificantBits());
                uuids[i++] = (int)(uuid.getLeastSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getLeastSignificantBits());
            }
            tag.putIntArray("permitted_players", uuids);
        }
        if (!memberNames.isEmpty()) {
            CompoundTag nameTag = new CompoundTag();
            memberNames.forEach((uid, name) -> nameTag.putString(uid.toString(), name));
            tag.put("member_names", nameTag);
        }
        if (!enemies.isEmpty()) {
            int[] uuids = new int[enemies.size() * 4];
            int i = 0;
            for (UUID uuid : enemies) {
                uuids[i++] = (int)(uuid.getMostSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getMostSignificantBits());
                uuids[i++] = (int)(uuid.getLeastSignificantBits() >> 32);
                uuids[i++] = (int)(uuid.getLeastSignificantBits());
            }
            tag.putIntArray("enemies", uuids);
        }
    }

    // ----- Range -----
    public int getRange() { return range; }
    public void setRange(int range) { this.range = Math.max(0, range); }
    public void addRange(int range) { this.range += range; }
    public void setDefaultRange() { this.range = DefaultRange; }

    // ----- Health -----
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public void setHealth(int health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
    public void addHealth(int health) {
        this.health += health;
        if (this.health > maxHealth) this.health = maxHealth;
        if (this.health < 0) this.health = 0;
    }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public void addMaxHealth(int health) { this.maxHealth += health; }
    public void setDefaultMaxHealth() { this.maxHealth = DefaultHealth; }

    public void damage(int health) {
        if (invincibleTime <= 0) {
            addHealth(-health);
            invincibleTime = 20;
        }
    }

    // ----- Name -----
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // ----- Owner Name -----
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    // ----- Owner -----
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }

    // ----- Permission -----
    public boolean hasPermission(UUID uuid) { return permittedPlayers.contains(uuid); }
    public void addPermission(UUID uuid) { permittedPlayers.add(uuid); }
    public void addPermissionWithName(UUID uuid, String name) { permittedPlayers.add(uuid); memberNames.put(uuid, name); }
    public void removePermission(UUID uuid) { permittedPlayers.remove(uuid); memberNames.remove(uuid); }
    public void addUser(UUID uuid) { addPermission(uuid); }
    public void rmUser(UUID uuid) { removePermission(uuid); }
    public Set<UUID> getPermittedPlayers() { return permittedPlayers; }
    public String getMemberName(UUID uuid) { return memberNames.getOrDefault(uuid, "§7(未知)"); }
    public Collection<String> getMemberNameList() { return memberNames.values(); }

    // ----- Enemy -----
    public void addEnemy(UUID uuid) { enemies.add(uuid); }
    public Set<UUID> getEnemies() { return enemies; }
    public void setEnemyInvincibleTime(int time) { this.enemyInvincibleTime = time; }
    public int getEnemyInvincibleTime() { return enemyInvincibleTime; }

    // ----- Interference -----
    public int getInterferenceTime() { return interferenceTime; }
    public void setInterferenceTime(int time) { this.interferenceTime = time; }
    public void addInterferenceTime(int time) { this.interferenceTime += time; }

    // ----- Invincible -----
    public int getInvincibleTime() { return invincibleTime; }
    public void setInvincibleTime(int time) { this.invincibleTime = time; }

    // ----- Durability -----
    public int getDurabilityDamage() { return durabilityDamage; }
    public void setDurabilityDamage(int damage) { this.durabilityDamage = damage; }

    // ----- Tick -----
    public void tick() {
        if (invincibleTime > 0) invincibleTime--;
        if (enemyInvincibleTime > 0) enemyInvincibleTime--;
        if (interferenceTime > 0) interferenceTime--;
    }

    // ----- Self-reference (interface compat) -----
    public BasecoreBlockEntityData getBaseInfo() { return this; }
    public void resetEnemy() {
        enemies.clear();
        enemyInvincibleTime = 0;
    }
}