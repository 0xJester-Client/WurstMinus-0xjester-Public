package me.third.right.utils.Interpreters.Python;

import me.third.right.ThirdMod;
import me.third.right.modules.Client.BaritoneModule;
import me.third.right.utils.Render.Render3D;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static me.third.right.utils.Client.Utils.Colour.rgbToInt;

//List of Methods for PythonInterpreter to easily execute.
public class PythonUtils {
    private static final ICamera camera = new Frustum();
    protected static Minecraft mc = Minecraft.getMinecraft();
    //Minecraft
    public static EntityPlayerSP getPlayer() {
        return mc.player;
    }
    public static List<Entity> getLoadedEntity() {
        return mc.world.getLoadedEntityList();
    }
    public static IBlockState getBlockInfo(BlockPos pos) { //Fix For an issue that Python Can't use getBlockState (The issue is caused by the Methods and Variables name change to different mappings.)
        return mc.world.getBlockState(pos);
    }
    public static Block getBlock(IBlockState iBlockState) { // Fix for issue that Python Can't use getBlock :shrug:
        return iBlockState.getBlock();
    }

    //Baritone
    public static boolean isBaritonePresent() {
        return BaritoneModule.INSTANCE.isPresent();
    }

    //Wurst-
    public static boolean isPlayerFriend(String name) {return ThirdMod.getFriends().isInTheList(name);}
    public static boolean isPlayerFriend(EntityPlayer player) {return ThirdMod.getFriends().isInTheList(player);}
    public static boolean isPlayerEnemy(String name) {return ThirdMod.getEnemies().isInTheList(name);}
    public static boolean isPlayerEnemy(EntityPlayer player) {return ThirdMod.getEnemies().isInTheList(player);}

    //Draw 3D
    public static void drawBoxFull(BlockPos pos, int red, int green, int blue, int alpha) {
        if(mc.getRenderViewEntity() == null) return;
        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        final IBlockState iBlockState = mc.world.getBlockState(pos);
        final AxisAlignedBB axisAlignedBB = iBlockState.getSelectedBoundingBox(mc.world, pos);
        if(camera.isBoundingBoxInFrustum(axisAlignedBB)) {
            Render3D.prepare();
            Render3D.drawFilledBox(axisAlignedBB, rgbToInt(red,green,blue,alpha));
            Render3D.release();
        }
    }
    public static void drawOutline(BlockPos pos, int red, int green, int blue, int alpha, float width) {
        if(mc.getRenderViewEntity() == null) return;
        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        final IBlockState iBlockState = mc.world.getBlockState(pos);
        final AxisAlignedBB axisAlignedBB = iBlockState.getSelectedBoundingBox(mc.world, pos);
        if(camera.isBoundingBoxInFrustum(axisAlignedBB)) {
            Render3D.prepare();
            Render3D.drawBoundingBox(axisAlignedBB, width, rgbToInt(red,green,blue,alpha));
            Render3D.release();
        }
    }
    public static void drawGradiantFilled(BlockPos pos, int red, int green, int blue, int alpha, int red1, int green1, int blue1, int alpha1) {
        if(mc.getRenderViewEntity() == null) return;
        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        final IBlockState iBlockState = mc.world.getBlockState(pos);
        final AxisAlignedBB axisAlignedBB = iBlockState.getSelectedBoundingBox(mc.world, pos);
        if(camera.isBoundingBoxInFrustum(axisAlignedBB)) {
            Render3D.prepare();
            Render3D.drawGradiantBox(axisAlignedBB, rgbToInt(red,green,blue,alpha), rgbToInt(red1,green1,blue1,alpha1));
            Render3D.release();
        }
    }
    public static void drawGradiantOutline(BlockPos pos, int red, int green, int blue, int alpha, int red1, int green1, int blue1, int alpha1, float width) {
        if(mc.getRenderViewEntity() == null) return;
        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        final IBlockState iBlockState = mc.world.getBlockState(pos);
        final AxisAlignedBB axisAlignedBB = iBlockState.getSelectedBoundingBox(mc.world, pos);
        if(camera.isBoundingBoxInFrustum(axisAlignedBB)) {
            Render3D.prepare();
            Render3D.drawGradiantBounds(axisAlignedBB, width, rgbToInt(red,green,blue,alpha), rgbToInt(red1,green1,blue1,alpha1));
            Render3D.release();
        }
    }
}
