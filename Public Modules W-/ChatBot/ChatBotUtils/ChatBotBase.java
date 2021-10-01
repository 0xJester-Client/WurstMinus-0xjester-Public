package me.third.right.utils.Client.ModuleUtils.ChatBotUtils;

import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public class ChatBotBase {
    protected final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private final Path path;

    public ChatBotBase(final String name, final Path path) {
        this.name = name;
        this.path = path;
    }

    public void onUpdate() {

    }

    public void onWhisper(String message) {

    }

    public void onMessage(String message) {

    }

    public void onLoad() {

    }

    public void onUnLoad() {

    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
