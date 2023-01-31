package me.third.right.modules.Bot;

import me.bush.eventbus.annotation.EventListener;
import me.third.right.ThirdMod;
import me.third.right.events.client.TickEvent;
import me.third.right.events.render.RenderEvent;
import me.third.right.mixin.client.accessor.IPlayerControllerMP;
import me.third.right.modules.Client.BaritoneModule;
import me.third.right.modules.Client.baritone.Baritone;
import me.third.right.modules.Combat.AutoAgro;
import me.third.right.modules.Combat.AutoEat;
import me.third.right.modules.Combat.KillAura;
import me.third.right.modules.Combat.MHAT;
import me.third.right.modules.Hack;
import me.third.right.modules.HackStandard;
import me.third.right.modules.Other.OffhandMend;
import me.third.right.settings.setting.ActionButton;
import me.third.right.settings.setting.CheckboxSetting;
import me.third.right.settings.setting.EnumSetting;
import me.third.right.settings.setting.SliderSetting;
import me.third.right.utils.client.enums.Category;
import me.third.right.utils.client.enums.SpecialRotations;
import me.third.right.utils.client.utils.ChatUtils;
import me.third.right.utils.client.utils.LoggerUtils;
import me.third.right.utils.client.utils.RotationUtils;
import me.third.right.utils.render.Render3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.third.right.utils.client.utils.BlockUtils.*;
import static me.third.right.utils.client.utils.ColourUtils.rgbToInt;
import static me.third.right.utils.client.utils.EntityUtils.*;
import static me.third.right.utils.client.utils.InventoryUtils.*;
import static me.third.right.utils.client.utils.RotationUtils.lookAtPos;

@Hack.HackInfo(name = "EndermanBot", description = "Automatically farms Enderman and collects the xp.", category = Category.BOT)
public class EndermanBot extends HackStandard {
    //Vars
    protected final BaritoneModule baritoneModule = BaritoneModule.INSTANCE;
    private final Queue<EntityXPOrb> xpOrbs = new ConcurrentLinkedQueue<>();
    protected Baritone baritone;
    private BlockPos startPos = null;
    private boolean isSafe = false;
    private boolean isCollecting = false;

    private boolean isBuilding = false;
    private boolean buildTower = false;
    private BlockPos[] schematic = null;
    private int placeCounter = 0;
    private int offset = 0;

    private int targetID = -1;
    private int prevHealth = -1;

    //Settings
    private final SliderSetting triggerAmount = setting(new SliderSetting("TriggerAmount", 15,5,30,1, SliderSetting.ValueDisplay.INTEGER));
    private final SliderSetting placeDelay = setting(new SliderSetting("PlaceDelay", 2,1,10,1, SliderSetting.ValueDisplay.INTEGER));
    private final SliderSetting blocksPerTick = setting(new SliderSetting("BlocksPerTick", 2,1,4,1, SliderSetting.ValueDisplay.INTEGER));

    public EndermanBot() {
        setting(new ActionButton("LoadSettings", "Loads advised settings.", X -> loadSettings()));
    }

    @Override
    public void onEnable() {
        ThirdMod.EVENT_PROCESSOR.subscribe(this);
        if(!baritoneModule.isPresent()) {
            if(nullCheck()) LoggerUtils.logError("Baritone is not present.");
            else ChatUtils.warning("Baritone is not present. Disabling...");
            disable();
            return;
        }

        if(!OffhandMend.INSTANCE.isEnabled()) OffhandMend.INSTANCE.enable();
        if(!AutoEat.INSTANCE.isEnabled()) AutoEat.INSTANCE.enable();

        if(nullCheck()) return;

        baritone = baritoneModule.baritone;
        startPos = new BlockPos(center(mc.player));
        isSafe = safeCheck(mc.player.getPosition().up(2));

    }

    @Override
    public void onDisable() {
        ThirdMod.EVENT_PROCESSOR.unsubscribe(this);
        reset();
    }

    // Events

    @EventListener
    public void onRender(RenderEvent event) {
        if(nullCheckFull()) return;

        if(isCollecting) {
            if(xpOrbs.isEmpty()) return;
            for(EntityXPOrb xpOrb : xpOrbs) {
                Render3D.prepare();
                Render3D.drawFilledBox(xpOrb.getEntityBoundingBox(), rgbToInt(0,255,0, 145));
                Render3D.release();
            }
        }
    }

    @EventListener
    public void onTick(TickEvent event) {
        if(nullCheckFull()) return;

        if(startPos == null) {
            baritone = baritoneModule.baritone;
            startPos = new BlockPos(center(mc.player));
            isSafe = safeCheck(mc.player.getPosition().up(2));
        }

        if(isBuilding) {
            if(placeCounter >= placeDelay.getValueI()) {
                placeCounter = 0;
                if(!buildTower) {
                    if(canPlace()) {
                        int blocksPlaced = 0;
                        while (blocksPlaced < blocksPerTick.getValueI()) {
                            if (offset >= platform.length) {
                                offset = 0;
                                break;
                            }
                            BlockPos targetPos = startPos.up(2).add(platform[offset]);
                            if (getPlaceableSide(targetPos, false) == null) {
                                for (BlockPos blockPos : holeOffset1[0]) {
                                    final BlockPos scaffoldPos = blockPos.add(targetPos);
                                    if (getPlaceableSide(scaffoldPos, false) != null && canPlaceBlock(scaffoldPos)) {
                                        targetPos = scaffoldPos;
                                        break;
                                    }
                                }
                            }
                            if (canPlaceBlock(targetPos)) {
                                placeBlock(targetPos, true, true, false, getSlot(Blocks.OBSIDIAN));
                                blocksPlaced++;
                            }
                            offset++;
                        }
                    } else buildTower = true;
                } else {
                    //Copied from SelfHoleFill
                    int slotObby = getSlot(Blocks.OBSIDIAN);
                    if(slotObby == -1) {
                        ChatUtils.warning("Missing needed blocks! Disabling...");
                        disable();
                        return;
                    }

                    //Create Schematic.
                    BlockPos[] placeList;
                    if(schematic != null) {
                        placeList = schematic;
                    } else {
                        placeList = getPlaceList(startPos);
                        if(placeList.length > blocksPerTick.getValueI()) {
                            schematic = placeList;
                        }
                    }
                    if(placeList.length <= 0) return;

                    //Place the blocks.
                    int blocksPlaced = 0;
                    while (blocksPlaced < blocksPerTick.getValueI()) {
                        if (offset >= placeList.length) {
                            offset = 0;
                            break;
                        }

                        final BlockPos placePos = placeList[offset];
                        if (canPlaceBlock(placePos)) {
                            placeBlock(placePos, true, true, false, slotObby);
                            blocksPlaced++;
                        }
                        offset++;
                    }

                    //Check if we're complete. Prob move this to the top.
                    int cOffsets = 0;
                    for(BlockPos pos1 : placeList) {
                        if(!canPlaceBlock(pos1)) {
                            cOffsets++;
                        }
                    }
                    if(cOffsets >= placeList.length) {
                        schematic = null;
                        offset = 0;
                    }

                    buildTower = false;
                }
            } else placeCounter++;

            if(safeCheck(new BlockPos(center(mc.player)).up(2))) {
                final BlockPos breakTarget = blockToDestroy();

                if(breakTarget == null) {
                    ChatUtils.warning("No blocks to break!");
                    isBuilding = false;
                    isSafe = true;
                    return;
                }

                final int slot = getSlotPickAxe();
                if(slot == -1) {
                    ChatUtils.warning("Missing needed pickaxe?");
                    return;
                }
                mc.player.inventory.currentItem = slot;

                lookAtPos(breakTarget, true);
                final RayTraceResult result = mc.world.rayTraceBlocks(getPlayerPosAddEyes(), new Vec3d(breakTarget.getX() + .5, breakTarget.getY() - .5, breakTarget.getZ() + .5));
                final EnumFacing f = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;

                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.playerController.onPlayerDamageBlock(breakTarget, f);
                if (((IPlayerControllerMP) mc.playerController).getCurBlockDamageMP() >= 1) return;
                return;
            }

            isBuilding = !safeCheck(mc.player.getPosition().up(2));
        }

        if(!isSafe) {
            if(getNumberOfItem(Blocks.OBSIDIAN) < 12) {
                ChatUtils.warning("Not enough obsidian to build a platform. Disabling...");
                disable();
                return;
            }

            isBuilding = true;
            return;
        }

        if(isCollecting) {
            AutoAgro.INSTANCE.disable();

            if(baritone.isPathing()) {
                if(numberOfAgro() > 0) {
                    if(!MHAT.INSTANCE.isEnabled()) {
                        MHAT.INSTANCE.enable();
                    }

                    if(!KillAura.INSTANCE.isEnabled()) {
                        KillAura.INSTANCE.enable();
                    }

                    xpOrbs.clear();
                    isCollecting = false;
                    baritone.gotoBlock(startPos);
                }

                if(!KillAura.INSTANCE.isAttacking()) RotationUtils.lookAtAngle(90, mc.player.rotationYaw, SpecialRotations.Forced);
                return;
            }

            if(xpOrbs.isEmpty()) {
                isCollecting = false;
                baritone.gotoBlock(startPos);
                return;
            }

            while (!xpOrbs.isEmpty()) {
                EntityXPOrb xpOrb = xpOrbs.poll();
                if (xpOrb == null || xpOrb.isDead) continue;
                baritone.gotoBlock(xpOrb.getPosition());
                break;
            }
            return;
        }

        if(numberOfEXP() >= triggerAmount.getValueI()) {
            if(numberOfAgro() >= 1) return;

            KillAura.INSTANCE.disable();
            AutoAgro.INSTANCE.disable();

            EntityXPOrb orb = getClosest(mc.player);
            while(orb != null) {
                xpOrbs.add(orb);
                orb = getClosest(orb);
            }

            isCollecting = true;
        } else {
            if(baritone.isPathing()) return;
            if(!startPos.equals(new BlockPos(center(mc.player)))) {
                baritone.gotoBlock(startPos);
                return;
            }

            KillAura.INSTANCE.enable();
            AutoAgro.INSTANCE.enable();

            if(KillAura.INSTANCE.isEnabled() || KillAura.INSTANCE.isAttacking()) {
               final EntityLivingBase target = (EntityLivingBase) KillAura.INSTANCE.getTargetEntity();
               if(target == null) return;

               if(targetID == -1) {
                   targetID = target.getEntityId();
               } else if(targetID != target.getEntityId()) {
                   targetID = target.getEntityId();
                   prevHealth = -1;
               }

                if(prevHealth == -1) {
                    prevHealth = (int) target.getHealth();
                } else {
                    int damageDiff = prevHealth - (int) target.getHealth();
                    if(damageDiff <= 0) {
                        final BlockPos targetPos = new BlockPos(center(target));
                        if (targetPos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) <= 5) {
                            if (mc.player.getDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) < 2.3) {
                                moveToPosition(startPos.getX(), startPos.getY(), startPos.getZ(), 0.5F, mc.player);
                                return;
                            }
                            if (mc.player.getDistance(targetPos.getX(), targetPos.getY(), targetPos.getZ()) > 2.2) {
                                moveToPosition(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.5F, mc.player);
                            }
                        }
                    }
                }
            }
        }
    }

    // Methods

    private void loadSettings() {
        // KillAura
        final KillAura killAura = KillAura.INSTANCE;
        ((EnumSetting<KillAura.Bones>) killAura.getSetting("Bones")).setSelected(KillAura.Bones.AUTO);
        ((CheckboxSetting)killAura.getSetting("HitDelay")).setChecked(false);
        ((SliderSetting)killAura.getSetting("TickDelay")).setValue(15);
        ((CheckboxSetting)killAura.getSetting("Raytrace")).setChecked(true);
        ((CheckboxSetting)killAura.getSetting("AutoSwitch")).setChecked(true);
        ((CheckboxSetting)killAura.getSetting("All")).setChecked(true);

        // AutoEat
        final AutoEat autoEat = AutoEat.INSTANCE;
        ((SliderSetting)autoEat.getSetting("Hunger")).setValue(15);
        ((CheckboxSetting)autoEat.getSetting("Health")).setChecked(true);
        ((SliderSetting)autoEat.getSetting("TriggerHealth")).setValue(18);
        ((CheckboxSetting)autoEat.getSetting("ReturnSlot")).setChecked(false);
        ((CheckboxSetting)autoEat.getSetting("AllowGapples")).setChecked(true);
        ((CheckboxSetting)autoEat.getSetting("AllowChorus")).setChecked(false);

        // HMAT
        final MHAT mhat = MHAT.INSTANCE;
        ((SliderSetting)mhat.getSetting("Health Swap")).setValue(16);
        ((SliderSetting)mhat.getSetting("Health Reverse")).setValue(20);
    }

    private BlockPos blockToDestroy() {
        for(int y = 0; y != 2; y++) {
            final BlockPos pos = mc.player.getPosition().up(y);
            for(BlockPos blockPos : platform) {
                final BlockPos tempPos = pos.add(blockPos);
                if(!mc.world.getBlockState(tempPos).getMaterial().isReplaceable()) {
                    return tempPos;
                }
            }
        }
        return null;
    }

    private EntityXPOrb getClosest(Entity from) {
        Entity closest = from;
        double distance = mc.gameSettings.renderDistanceChunks * 16;
        for(Entity entity : mc.world.getLoadedEntityList()) {
            if(entity == null || entity.isDead) continue;
            if(!(entity instanceof EntityXPOrb)) continue;
            if(xpOrbs.contains(entity)) continue;

            double tempDistance = closest.getDistance(entity);

            if(tempDistance < distance) {
                closest = entity;
                distance = tempDistance;
            }
        }

        if(closest.equals(from)) return null;
        return (EntityXPOrb) closest;
    }

    private void reset() {
        xpOrbs.clear();
        startPos = null;
        isSafe = false;
        isCollecting = false;
        isBuilding = false;
        buildTower = false;
        schematic = null;
        placeCounter = 0;
        offset = 0;
    }

    private int numberOfAgro() {
        return (int) mc.world.getLoadedEntityList().stream().filter(V -> (V instanceof EntityEnderman) && ((EntityEnderman) V).isScreaming()).count();
    }

    private int numberOfEXP() {
        return (int) mc.world.getLoadedEntityList().stream().filter(V -> V instanceof EntityXPOrb).count();
    }

    private boolean safeCheck(BlockPos pos) {
        for(BlockPos blockPos : platform) {
            final BlockPos tempPos = pos.add(blockPos);
            if(mc.world.getBlockState(tempPos).getMaterial().isReplaceable()) {
                return false;
            }
        }
        return true;
    }

    private boolean canPlace() {
        final BlockPos pos = mc.player.getPosition().up(2);
        for(BlockPos blockPos : platform) {
            final BlockPos tempPos = pos.add(blockPos);
            if(!mc.world.getBlockState(tempPos).getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }

    // Offsets
    public static final BlockPos[] platform = {
            new BlockPos(0, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(-1, 0, -1)
    };
}
