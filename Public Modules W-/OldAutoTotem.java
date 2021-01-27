@Module.Info(name = "AutoTotem", description = "Replaces off hand with items", category = Module.Category.COMBAT)
public class AutoTotem extends Module {
    //(Third_Right | 3rd#1703)
    //Old AutoTotem got rewritten becuase this version has got too long and inefficent
    //Theres a flaw with the health value that causes the AT to fail.
    //Vars
    private final DelayTimer delayTimer = new DelayTimer();
    private boolean swap = false;
    private boolean forceOffhandSwap = false;
    public static AutoTotem INSTANCE;
    public AutoTotem(){ INSTANCE = this; }

    //Settings
    public final Setting<Mode> mode = register(Settings.e("Mode", Mode.TOTEM));
    public final Setting<SubMode> subMode = register(Settings.e("SubMode", SubMode.ALL));
    private final Setting<Integer> healthSwap = this.register(Settings.integerBuilder("HealthSwap").withMinimum(0).withMaximum(20).withValue(10).build());
    private final Setting<Integer> healthSwapReverse = this.register(Settings.integerBuilder("HealthSwapReverse").withMinimum(0).withMaximum(20).withValue(14).build());
    private final Setting<Boolean> useCrystal = register(Settings.b("CrystalDamage", true));
    private final Setting<Integer> crystalDamage = this.register(Settings.integerBuilder("CrystalDamage").withMinimum(0).withMaximum(16).withValue(8).build());
    private final Setting<Boolean> onlyEqualsHealth = register(Settings.b("OnlyIfCrystalKills", false));
    private final Setting<Boolean> useFallDistance = register(Settings.b("FallDistance", true));
    private final Setting<Integer> fallDistance = this.register(Settings.integerBuilder("FallDistance").withMinimum(0).withMaximum(256).withValue(40).build());
    private final Setting<ElytraMode> elytraMode = register(Settings.e("ElytraMode", ElytraMode.TOTEM));
    private final Setting<Integer> delay = this.register(Settings.integerBuilder("SwitchBackDelay").withMinimum(0).withMaximum(200).withValue(100).build());
    private final Setting<Boolean> hotBarSwap = register(Settings.b("NoHotBarSwap", true));
    private final Setting<Boolean> offHandFallBack = register(Settings.b("OffHandFallback", false));
    private final Setting<Boolean> allowCrystal = register(Settings.b("OHFTotemCrystal", false));
    public final Setting<Boolean> mapartOffhand = register(Settings.booleanBuilder("MapartOffhand").withValue(false).withVisibility(V -> isDebug()).build());


    @Override
    public void onUpdate() {//Needs clearing up
        if(mc.player == null || mc.world == null || (mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) || mc.player.capabilities.isCreativeMode)return;
        final float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();// Point of failure remove the absorption amount.
        final int numberOfTotems = InventoryUtil.getNumberOfItem(Items.TOTEM_OF_UNDYING);
        final int numberOfSelectedItems = InventoryUtil.getNumberOfItem(mode.getValue().item);
        //Offhand gapple and crapple check
        forceOffhandSwap = checkCrappleAndGapple();
        //Swaps when CA or KA is Active
        checkCrystalSwap();
        //Crystal Damage
        swap = CombatUtil.crystalDamageCheck(crystalDamage.getValue(),onlyEqualsHealth.getValue()) && useCrystal.getValue();
        //Fall Distance simple but does the job.
        if(mc.player.fallDistance != 0 && !mc.player.onGround && useFallDistance.getValue() && (elytraMode.getValue().equals(ElytraMode.DISABLEFALL) && !mc.player.isElytraFlying()))
            swap = mc.player.fallDistance > fallDistance.getValue();
        //AutoTotem
        int range = -1;
        Item offhandItem = Items.TOTEM_OF_UNDYING;
        //OffHand when the player is in danger.
        if (numberOfTotems != 0 && health < healthSwap.getValue() || swap || (elytraMode.getValue().equals(ElytraMode.TOTEM) && mc.player.isElytraFlying())) {
            delayTimer.reset();
            range = 0;
            //OffHand when the players isn't in danger.
        } else if ((health > healthSwapReverse.getValue() && delayTimer.passedMs(delay.getValue()) && numberOfSelectedItems != 0) && !swap || numberOfTotems == 0) {
            if(mapartOffhand.getValue())
                offhandItem = Items.FILLED_MAP;
            else offhandItem = mode.getValue().item;
            if (hotBarSwap.getValue())
                range = 8;
            else range = 0;
            //Backup option for when the Player isn't in danger.
        } else if (offHandFallBack.getValue() && numberOfSelectedItems == 0 && delayTimer.passedMs(delay.getValue())) {
            switch (mode.getValue()) {
                case GAPPLE:
                    offhandItem = Items.END_CRYSTAL;
                    break;
                case CRYSTAL:
                    offhandItem = Items.GOLDEN_APPLE;
                    break;
                case TOTEM:
                    if (InventoryUtil.getNumberOfItem(Items.GOLDEN_APPLE, hotBarSwap.getValue()) != 0)
                        offhandItem = Items.GOLDEN_APPLE;//The best backup option
                    else if (InventoryUtil.getNumberOfItem(Items.END_CRYSTAL, hotBarSwap.getValue()) != 0 && allowCrystal.getValue())
                        offhandItem = Items.END_CRYSTAL;//Worst backup option!!
                    break;
                case CRAPPLE:
                    offhandItem = Items.TOTEM_OF_UNDYING;
                    break;
            }
            if (hotBarSwap.getValue())
                range = 8;
            else range = 0;
        }
        if (mc.player.getHeldItemOffhand().getItem() != offhandItem || forceOffhandSwap) {//TODO update this with InventoryUtil funcs.
            for (int i = 36; i != range; i--) {
                final ItemStack searchStack = mc.player.inventory.getStackInSlot(i);
                if (searchStack.getItem() == offhandItem) {
                    //Gapple and Crapple filters
                    if (offhandItem.equals(Items.GOLDEN_APPLE) && mode.getValue().equals(Mode.CRAPPLE) && !searchStack.getRarity().equals(EnumRarity.RARE))
                        continue; //We skip if the apple is not a crapple
                    if (offhandItem.equals(Items.GOLDEN_APPLE) && mode.getValue().equals(Mode.GAPPLE) && !searchStack.getRarity().equals(EnumRarity.EPIC))
                        continue;
                    InventoryUtil.swapItem(i,45);
                    forceOffhandSwap = false;
                    break;
                }
            }
        }
    }

    @Override
    protected void onEnable() {
        if(MODULE_MANAGER.isModuleEnabled(OffHand.class)){
            sendWarningMessage("Disabling OffHand!");
            MODULE_MANAGER.getModule(OffHand.class).disable();
        }
    }

    private void checkCrystalSwap(){
        switch (subMode.getValue()){
            case AUTO_OLDFAG:
            case AUTO:
                if (MODULE_MANAGER.isModuleEnabled(KillAura2.class))
                    mode.setValue(subMode.getValue().equals(SubMode.AUTO) ? Mode.GAPPLE : Mode.CRAPPLE);
                else if (MODULE_MANAGER.isModuleEnabled(WurstCA2.class))
                    mode.setValue(Mode.CRYSTAL);
                else mode.setValue(Mode.TOTEM);
                break;
            case CRAPPLE_AND_TOTEM:
            case GAPPLE_AND_TOTEM:
            case CRYSTAL_AND_TOTEM:
            case CRYSTAL_AND_GAPPLE:
            case ALL:
                break;//Do nothing.
        }
    }

    private boolean checkCrappleAndGapple(){
        final ItemStack offhandStack = mc.player.getHeldItemOffhand();
        return (mode.getValue().equals(Mode.GAPPLE) && !offhandStack.getRarity().equals(EnumRarity.EPIC))
                || (mode.getValue().equals(Mode.CRAPPLE) && !offhandStack.getRarity().equals(EnumRarity.RARE));
    }

    @Override
    public String getHudInfo() {
        return String.format("%d", InventoryUtil.getNumberOfItem(mode.getValue().item));
    }

    public enum Mode {
        TOTEM(Items.TOTEM_OF_UNDYING),
        CRYSTAL(Items.END_CRYSTAL),
        CRAPPLE(Items.GOLDEN_APPLE),//We'll check if the item during the search.
        GAPPLE(Items.GOLDEN_APPLE);
        private final Item item;
        Mode(Item item) {
            this.item = item;
        }
    }

    public enum SubMode {ALL,CRYSTAL_AND_GAPPLE,CRYSTAL_AND_TOTEM,GAPPLE_AND_TOTEM,AUTO,CRAPPLE_AND_TOTEM, AUTO_OLDFAG}

    private enum ElytraMode {TOTEM,DISABLEFALL}
}
