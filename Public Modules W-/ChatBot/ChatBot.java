package me.third.right.modules.Client;

import me.third.right.events.event.PacketEvent;
import me.third.right.modules.HackClient;
import me.third.right.settings.setting.EnumSetting;
import me.third.right.settings.setting.StringSetting;
import me.third.right.utils.Client.ModuleUtils.ChatBotUtils.ChatBotBase;
import me.third.right.utils.Client.ModuleUtils.ChatBotUtils.SocialCreditChatBot;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static me.third.right.utils.Client.ModuleUtils.ChatBotUtils.ChatBotUtils.isMessageWhisper;

public class ChatBot extends HackClient {
    //Vars
    private enum Mode { SocialCredit }
    public ChatBotBase chatBot = null;
    //Settings
    public final StringSetting commandPrefix = setting(new StringSetting("Prefix", "-", 1));
    private final EnumSetting<Mode> mode = setting(new EnumSetting<>("Mode", Mode.values(), Mode.SocialCredit));

    public ChatBot() {
        super("ChatBot", "Interactive chat bots.");
        switch (mode.getSelected()) {
            case SocialCredit:
                chatBot = new SocialCreditChatBot();
                break;
        }
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        switch (mode.getSelected()) {
            case SocialCredit:
                chatBot = new SocialCreditChatBot();
                break;
        }
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        switch (mode.getSelected()) {
            case SocialCredit:
                chatBot.onUnLoad();
                break;
        }
    }

    @Override
    public void onDisconnect() {
        chatBot.onUnLoad();
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event) {
        if(chatBot != null) chatBot.onUpdate();
    }

    @EventHandler
    private final Listener<PacketEvent.Receive> receivedEventListener = new Listener<>(event -> {
        if(mc.player == null || mc.world == null || chatBot == null) return;
        if(event.getPacket() instanceof SPacketChat) {
            final SPacketChat packet = (SPacketChat) event.getPacket();
            if(packet.getType().equals(ChatType.SYSTEM)) {
                final String message = packet.getChatComponent().getUnformattedText();
                final boolean isWhisper = isMessageWhisper(message);
                if(isWhisper) chatBot.onWhisper(message);
                else chatBot.onMessage(message);
            }
        }
    });
}
