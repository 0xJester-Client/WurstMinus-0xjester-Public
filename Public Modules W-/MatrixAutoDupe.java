package me.zeroeightsix.kami.module.modules.player;

import Third.Right.Utils.World.BlockInteraction;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import Third.Right.Utils.Player.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.stream.Collectors;

@Module.Info(name = "MatrixAutoDupe", description = "Thanks to Wurst-", category = Module.Category.PLAYER)
public class MatrixAutoDupe extends Module {
    //( Third_Right ) | 3rd#1703
    //Vars
    private enum DropMode{OFF,ALL,SHULKERS}
    private int delayCounter;
    //Settings
    private final Setting<DropMode> mode = register(Settings.e("DropMode", DropMode.SHULKERS));
    private final Setting<Integer> delay = register(Settings.integerBuilder("TickDelay").withMinimum(1).withMaximum(100).withValue(1).build());
    private final Setting<Boolean> hotbar = register(Settings.booleanBuilder().withName("DropHotbar").withValue(false).build());

    // DUO simple turn on the module near the donkey with a player riding it.
    // SOLO just sit on a donkey and open and close your inventory and it'll dupe the items. ez
    @Override
    public void onUpdate() {
        if(mc.player == null || mc.world == null)return;
        if(delayCounter >= delay.getValue()){
            delayCounter=0;
            final int range;
            if(hotbar.getValue())range = 0;
            else range = 8;
            if(!mode.getValue().equals(DropMode.OFF)) {
                for (int i = range; i != 36; i++) {
                    switch (mode.getValue()) {
                        case ALL:
                            if (!mc.player.inventory.getStackInSlot(i).isEmpty()) {
                                InventoryUtil.moveItem(i, ClickType.THROW);
                                return;
                            }
                            break;
                        case SHULKERS:
                            ItemStack stack = mc.player.inventory.getStackInSlot(i);
                            if (stack.getItem() instanceof ItemBlock) {
                                Block block = ((ItemBlock) stack.getItem()).getBlock();
                                if (BlockInteraction.isShulkerBox(block)) {
                                    InventoryUtil.moveItem(i, ClickType.THROW);
                                    return;
                                }
                            }
                            break;
                    }
                }
            }
            Entity mountEntity = null;
            for (Entity entity : mc.world.loadedEntityList.stream().filter(V -> V instanceof AbstractChestHorse && !V.isDead).collect(Collectors.toList())) {
                if (mc.player.getDistance(entity) >= 6) continue;
                if (!((AbstractChestHorse) entity).hasChest()) continue;
                if (((AbstractChestHorse) entity).riddenByEntities.size() == 0) continue;
                mountEntity = entity;
            }
            if(mountEntity != null) mc.player.connection.sendPacket(new CPacketUseEntity(mountEntity, EnumHand.MAIN_HAND));
        } else {
            delayCounter++;
        }
    }
}
