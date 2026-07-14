package dev.anye.mc.basecore.block;

public interface IHealth {
    void addHealth(int health);
    int getHealth();
    int getMaxHealth();
    void damage(int health);
}