package dev.anye.mc.basecore.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class BasecoreArgument implements ArgumentType<String> {
    @Override
    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return "";
    }
}