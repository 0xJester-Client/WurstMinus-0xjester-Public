package me.third.right.utils.Client.ModuleUtils.ChatBotUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Locale;

public class ChatBotUtils {
    protected static Minecraft mc = Minecraft.getMinecraft();

    public static String getUserName(String message, boolean whisper) {
        if(whisper) {
            if(!message.isEmpty() && mc.getConnection() != null) {
                for(NetworkPlayerInfo info :  mc.getConnection().getPlayerInfoMap()) {
                    if(info == null) continue;
                    final String username = info.getGameProfile().getName().toLowerCase(Locale.ROOT);
                    if(message.toLowerCase(Locale.ROOT).startsWith(String.format("%s whispers to you:", username))) {
                        return info.getGameProfile().getName();
                    }
                }
            }
            return "";
        }
        return message.substring(message.indexOf("<") + 1, message.indexOf(">"));
    }

    public static boolean isMessageWhisper(String message) {
        if(!message.isEmpty() && mc.getConnection() != null) {
            for(NetworkPlayerInfo info :  mc.getConnection().getPlayerInfoMap()) {
                if(info == null) continue;
                final String username = info.getGameProfile().getName().toLowerCase(Locale.ROOT);
                if(message.toLowerCase(Locale.ROOT).startsWith(String.format("%s whispers to you:", username))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void reply(final String message) {
        mc.player.sendChatMessage("/r" + message);
    }
    public static void reply(final String message, final String to) {
        mc.player.sendChatMessage("/msg "+to+" "+message);
    }
}
