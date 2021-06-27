package me.third.right.modules.Combat;

import me.third.right.events.event.RenderEvent;
import me.third.right.modules.HackStandard;
import me.third.right.settings.setting.SliderSetting;
import me.third.right.utils.Client.Enums.Category;
import me.third.right.utils.Render.GeometryMasks;
import me.third.right.utils.Render.ThirdTessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static me.third.right.utils.Client.Utils.BlockInteraction.getSphere;
import static me.third.right.utils.Client.Utils.Colour.rgbToInt;
import static me.third.right.utils.Client.Utils.CombatUtil.calculateDamage;
import static me.third.right.utils.Client.Utils.PlayerInformation.getPlayerPos;
import static me.third.right.utils.Render.ThirdTessellator.drawDamageText;
/*
    Made for the lolz
 */

public class SpookyCA extends HackStandard {
    //Vars
    private BlockPos targetPos = null;
    private EntityPlayer targetPlayer = null;
    private EntityEnderCrystal targetCrystal = null;
    private boolean spookStage = false;
    private int spookyDelay = 0;
    private final ResourceLocation sound = new ResourceLocation("minecraft", "entity.generic.explode");
    private final SoundEvent boom = new SoundEvent(sound);
    //Settings
    private final SliderSetting spookyLevel = setting(new SliderSetting("SpookyLevel", "More spooky = better CA", 1,1,10,1, SliderSetting.ValueDisplay.INTEGER));

    public SpookyCA() {
        super("SpookyCA","Pyro CA but more scary!", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if(mc.player == null || mc.world == null) return;
        getTargetPlayer();
        if(targetPlayer != null) {
            doAutoCrystal();
        }
    }

    private void doAutoCrystal() {
        getPlacement(targetPlayer);
        if(targetPos != null) {
            if(spookyDelay >= spookyLevel.getValueI()) {
                spookyDelay = 0;
                mc.player.swingArm(EnumHand.MAIN_HAND);
                if (!spookStage) {
                    targetCrystal = new EntityEnderCrystal(mc.world);
                    targetCrystal.setPosition(targetPos.x + .5, targetPos.y + 1, targetPos.z + .5);
                    mc.world.addEntityToWorld(-69420, targetCrystal);
                    spookStage = true;
                } else {
                    if (targetCrystal != null)
                        targetCrystal.isDead = true;
                    spookStage = false;
                    mc.player.playSound(boom, 1, 1);
                }
            } else spookyDelay++;
        }
    }

    private void getPlacement(final EntityPlayer entityPlayer) {
        BlockPos potential = null;
        double blockDamage = 6.0;
        for (BlockPos pos : getSphere(getPlayerPos().up(), 6, 6, false, true, 0).stream().filter(this::canPlace).collect(Collectors.toCollection(ArrayList::new))) {
            final double tempDistance = entityPlayer.getDistance(pos.x, pos.y, pos.z);
            if (tempDistance < blockDamage) {
                blockDamage = tempDistance;
                potential = pos;
            }
        }
        this.targetPos = potential;
    }

    private boolean canPlace(final BlockPos blockPos) {
        final BlockPos boost = blockPos.up();
        final BlockPos boost2 = blockPos.up(2);
        return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR
                && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();

    }

    private void getTargetPlayer() {
        Entity potential = null;
        double distance = 16;
        for(Entity entity : mc.world.getLoadedEntityList()) {
            if(!(entity instanceof EntityPlayer) || entity.isDead || entity == mc.player) continue;
            final double tempDistance = mc.player.getDistance(entity);
            if(tempDistance < distance) {
                distance = tempDistance;
                potential = entity;
            }
        }
        targetPlayer = (EntityPlayer) potential;
    }

    @Override
    public void onWorldRender(RenderEvent event) {//Wurst- 2 CA rendering leaked omg!
        if (targetPos != null && mc.player.getDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 6.5f) {
            ThirdTessellator.prepare(GL11.GL_QUADS);
            ThirdTessellator.drawBox(targetPos, rgbToInt(150, 50, 255, 100), GeometryMasks.Quad.ALL);
            ThirdTessellator.release();
            ThirdTessellator.prepare(GL11.GL_QUADS);
            ThirdTessellator.drawBoundingBox(targetPos, 1.5F, rgbToInt(255, 0, 255, 100));
            ThirdTessellator.release();
            if(targetPlayer != null && mc.player.getDistance(targetPlayer) <= 15F) {
                ThirdTessellator.prepareGL();
                drawDamageText(targetPos, calculateDamage(targetPos.x + .5, targetPos.y + 1, targetPos.z + .5, targetPlayer, false, false));
                ThirdTessellator.releaseGL();
            }
        }
    }
}
