package me.third.right.utils.Interpreters.Python;

import me.third.right.settings.Setting;

public interface IModuleBase {
    public String getName();
    public String getDescription();
    public boolean isEnabled();
    public Setting[] getSettings();
}
