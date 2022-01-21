package me.third.right.utils.Interpreters.Python;

import me.third.right.settings.Setting;

public class JModuleObject implements IModuleBase {
    private final String name;
    private final String description;
    private final Setting[] settings;
    private boolean isEnabled;

    public JModuleObject(final String name, final String description, boolean isEnabled, Setting[] settings) {
        this.name = name;
        this.description = description;
        this.isEnabled = isEnabled;
        this.settings = settings;
    }

    @Override
    public String getName() {return name;}

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Setting[] getSettings() {
        return settings;
    }

    public void setToggle(boolean state) {
        isEnabled = state;
        JythonInterpreter.INSTANCE.setToggleJModule(name, state);
    }

    public void toggle() {
        isEnabled = !isEnabled;
        JythonInterpreter.INSTANCE.toggleJModule(this);
    }
}
