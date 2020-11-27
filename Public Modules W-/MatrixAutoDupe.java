package me.zeroeightsix.kami.module.modules.player;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.New.Client.MessageSendHelper;
import me.zeroeightsix.kami.util.New.Player.InventoryUtil;
import me.zeroeightsix.kami.util.New.World.BlockInteractionHelper;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

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

    // DUO simply turn on the module with the secone player on it with there inventory open ez.
    // SOLO just sit on a donkey and open and close your inventory and it'll dupe the items ease.
    @Override
    public void onUpdate() {
        if(mc.player == null || mc.world == null || ((mc.currentScreen instanceof GuiContainer)))return;
        if(delayCounter >= delay.getValue()){
            delayCounter=0;
            final int range;
            if(hotbar.getValue())range = 0;
            else range = 8;
            for(int i= range;i!=36;i++){
                switch (mode.getValue()) {
                    case ALL:
                        if (!mc.player.inventory.getStackInSlot(i).isEmpty()){
                            //InventoryUtil.moveItem(i,ClickType.THROW);// Simpley remove this and rewrite this with click GUI Methods
                            mc.playerController.windowClick(0,i < 9 ? i + 36 : i,0,ClickType.THROW,mc.player);//Done it for u twatz
                            return;
                        }
                        break;
                    case SHULKERS:
                        for(Block shulkers: BlockInteractionHelper.shulkerList){
                            ItemStack stack = mc.player.inventory.getStackInSlot(i);
                            if(stack.getItem() instanceof ItemBlock){
                                Block block = ((ItemBlock) stack.getItem()).getBlock();
                                if(block == shulkers){
                                    //InventoryUtil.moveItem(i,ClickType.THROW);// Simpley remove this and rewrite this with click GUI Methods
                                    mc.playerController.windowClick(0,i < 9 ? i + 36 : i,0,ClickType.THROW,mc.player);
                                    return;
                                }
                            }
                        }
                        break;
                    case OFF://does nothing wow!!!
                        break;
                }
            }
            for (Entity donkeys : mc.world.loadedEntityList) {
                if (!(donkeys instanceof EntityDonkey))
                    continue;

                if(mc.player.getDistance(donkeys) >= 7)//So we're not sending useless packets. reach is roughly 7-8max for matrix
                    continue;

                if (!((EntityDonkey) donkeys).hasChest())
                    continue;

                if (((EntityDonkey) donkeys).riddenByEntities.size() == 0)
                    continue;

                mc.player.connection.sendPacket(new CPacketUseEntity(donkeys, EnumHand.MAIN_HAND));
            }
        } else {
            delayCounter++;
        }
    }
}
