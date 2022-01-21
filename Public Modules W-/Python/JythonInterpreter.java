package me.third.right.utils.Interpreters.Python;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.third.right.ThirdMod;
import me.third.right.events.Render.RenderEvent;
import me.third.right.events.Render.RenderGUIEvent;
import me.third.right.events.Client.TickEvent;
import me.third.right.settings.Setting;
import me.third.right.utils.Client.ClientFiles.BootSettings;
import me.third.right.utils.Client.Utils.JsonUtils;
import me.third.right.utils.Client.Utils.LoggerUtils;
import me.third.right.utils.Wrapper;
import me.third.right.event.wurstplus.CommitEvent;
import org.python.core.PyArray;
import org.python.util.PythonInterpreter;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static me.third.right.utils.Client.Utils.FileUtils.folderExists;


//This uses Jython.
public class JythonInterpreter {
    public static JythonInterpreter INSTANCE;
    protected PythonInterpreter interpreter;
    private final boolean pythonPresent;
    private boolean disableSaving;
    private final Path loadFolder = ThirdMod.configFolder.resolve("Scripts");
    private final Path configFolder = ThirdMod.configFolder.resolve("ScriptsConfigs");
    private final Path libraryFolder = ThirdMod.configFolder.resolve("Jython");

    protected final String bootCode =
            // Java
            "from java.lang import Enum\n" +
            "from java.util import List, ArrayList\n" +
            // MC
            "from net.minecraft.util.math import AxisAlignedBB, BlockPos, RayTraceResult, Vec3d\n" +
            "from net.minecraft.block import Block, BlockShulkerBox\n" +
            "from net.minecraft.block.state import IBlockState\n" +
            "from net.minecraft.world import World\n" +
            "from net.minecraft.client.entity import EntityPlayerSP, EntityOtherPlayerMP\n" +
            "from net.minecraft.entity import EntityLivingBase, Entity\n" +
            "from net.minecraft.entity.item import EntityEnderCrystal\n" +
            "from net.minecraft.client.multiplayer import WorldClient\n" +
            // Wurst-
            "from me.third.right.utils.Client.Utils import ChatUtils, LoggerUtils\n" +
            "from me.third.right.utils.Interpreters.Python import PythonUtils, IModuleBase\n" +
            "from me.third.right.settings.setting import SliderSetting, StringSetting, EnumSetting, CheckboxSetting\n" +
            "from me.third.right.utils.Client.Font import FontDrawing\n" +
            "from me.third.right.utils.Render import Render2D\n" +
            "print('Wurst- | Jython Module Manager v1.4 | Jython is present! ')\n" +
            "print(''' \n" +
            "__        __              _          ____  \n" +
            "\\ \\      / /   _ _ __ ___| |_       |___ \\ \n" +
            " \\ \\ /\\ / / | | | '__/ __| __|____    __) |\n" +
            "  \\ V  V /| |_| | |  \\__ \\ ||_____|  / __/ \n" +
            "   \\_/\\_/  \\__,_|_|  |___/\\__|      |_____|\n" +
            "                                           \n" +
            "''')\n" +
            "class ModuleBase(IModuleBase):\n" +
            "    enabled = False\n" +
            "    name = None\n" +
            "    description = None\n" +
            "    def __init__(self, name1, description1):\n" +
            "        self.name = name1\n" +
            "        self.description = description1\n" +
            "    def onUpdate(self):\n" +
            "        pass\n" +
            "    def onRender3D(self):\n" +
            "        pass\n" +
            "    def onRender2D(self):\n" +
            "        pass\n" +
            "    def Toggle(self):\n" +
            "        if(self.enabled == True):\n" +
            "            self.onDisabled()\n" +
            "            self.enabled = False\n" +
            "        else:\n" +
            "            self.onEnabled()\n" +
            "            self.enabled = True\n" +
            "    def setToggle(self, state):\n" +
            "        self.enabled = state\n" +
            "        if self.enabled == True:\n" +
            "            self.onDisabled()\n" +
            "            self.enabled = False\n" +
            "        else:\n" +
            "            self.onEnabled()\n" +
            "            self.enabled = True\n" +
            "    def onEnabled(self):\n" +
            "        pass\n" +
            "    def onDisabled(self):\n" +
            "        pass\n" +
            "    def getName(self):\n" +
            "        return self.name\n" +
            "    def getDescription(self):\n" +
            "        return self.description\n" +
            "    def isEnabled(self):\n" +
            "        return self.enabled\n" +
            "    def getDescription(self):\n" +
            "        return self.description\n" +
            "    def getSettings(self):\n" +
            "        pass\n" +
            "moduleList = []\n" +
            "def register(module):\n" +
            "    moduleList.append(module)\n" +
            "    print('Wurst- | Registered | '+module.getName())\n" +
            "def runUpdate():\n" +
            "    for x in moduleList:\n" +
            "        if x.isEnabled() == True:\n" +
            "           try:\n" +
            "               x.onUpdate()\n" +
            "           except Exception as err:\n" +
            "               ChatUtils.wReplace('Jython Module Error | onUpdate | {name} | {error}'.format(name = x.getName(), error = err),  -1423)\n" +
            "def runRender3D():\n" +
            "    for x in moduleList:\n" +
            "        if x.isEnabled() == True:\n" +
            "           try:\n" +
            "               x.onRender3D()\n" +
            "           except Exception as err:\n" +
            "               ChatUtils.wReplace('Jython Module Error | onRender3D | {name} | {error}'.format(name = x.getName(), error = err),  -1424)\n" +
            "def runRender2D():\n" +
            "    for x in moduleList:\n" +
            "        if x.isEnabled() == True:\n" +
            "           try:\n" +
            "               x.onRender2D()\n" +
            "           except Exception as err:\n" +
            "               ChatUtils.wReplace('Jython Module Error | onRender2D | {name} | {error}'.format(name = x.getName(), error = err),  -1425)\n" +
            "def getModuleByName(name):\n" +
            "    for x in moduleList:\n" +
            "        if x.getName().lower() == name.lower():\n" +
            "            return x\n" +
            "print('Wurst- | Jython boot code finished.')";

    public JythonInterpreter() {
        if(!BootSettings.INSTANCE.getJython()) {
            pythonPresent = false;
            LoggerUtils.logWarning("Wurst- | Jython Disabled!");
            interpreter = null;
            return;
        }

        try {
            loadLibrary(libraryFolder.resolve("jython.jar").toFile());
        } catch (Exception e) {
            pythonPresent = false;
            LoggerUtils.logWarning("Wurst- | Failed to load jython.jar!");
            interpreter = null;
            return;
        }

        if(getClass().getClassLoader().getResource("org/python") != null) {
            folderExists(loadFolder);
            folderExists(configFolder);
            pythonPresent = true;
            interpreter = new PythonInterpreter();
            interpreter.exec(bootCode);
            regScripts();
            loadAll();
            ThirdMod.EVENT_PROCESSOR.addEventListener(this);
        } else {
            pythonPresent = false;
            LoggerUtils.logWarning("Wurst- | Jython isn't present!");
            interpreter = null;
        }
    }

    /*https://newbedev.com/load-jar-dynamically-at-runtime*/
    private synchronized void loadLibrary(File jar) {
        try {
            final URL url = jar.toURI().toURL();
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true); /*promote the method to public access*/
            method.invoke(Thread.currentThread().getContextClassLoader(), new Object[]{url});
            method.setAccessible(false);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot load library from jar file '" + jar.getAbsolutePath() + "'. Reason: " + ex.getMessage());
        }
    }

    public Path getLibraryFolder() {
        return libraryFolder;
    }

    /*
        Jython Management Methods
     */

    /**
     * Destroy old Jython Module Manager and starts a new one.
     */
    public void reboot() {
        if(pythonPresent) {
            ThirdMod.EVENT_PROCESSOR.removeEventListener(this);
            saveAll();
            interpreter = null;
            System.gc();
            INSTANCE = new JythonInterpreter();
            ThirdMod.getGui().rebootScripts();
        }
    }

    /**
     * Reads and registers .py files to the Jython Module Manager
     */
    public void regScripts() {
        if(!Files.exists(loadFolder)) return;
        try {
            final List<String> files = Files.list(loadFolder).filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
            for (String python : files) {
                if (!python.endsWith(".py")) continue;
                final File file = new File(python);
                final StringBuilder readContent = new StringBuilder();
                try {
                    final Scanner reader = new Scanner(file);
                    while(reader.hasNextLine()) {
                        readContent.append("\n").append(reader.nextLine());
                    }
                    reader.close();
                } catch (FileNotFoundException e) {
                    LoggerUtils.logWarning(e.toString());
                }

                try {
                    interpreter.exec(readContent.toString());//TODO add a method to replace MC de-obfuscated methods to MC obfuscated methods
                } catch (Exception e) {
                    LoggerUtils.logWarning("(Reg) Jython Module Manager | "+e);
                }
            }
        } catch (IOException e) {
            LoggerUtils.logWarning(e.toString());
        }
    }

    /**
     * Returns a list of Jython Modules.
     * Null will be returned if the Jython isn't present or no Modules are in the Reg / moduleList.
     */
    public JModuleObject[] getModuleList() {
        if(isPythonPresent()) {
            final PyArray object = PyArray.array(interpreter.get("moduleList"), IModuleBase.class);
            final int size = object.__len__();
            final ArrayList<JModuleObject> list = new ArrayList<>();
            for (int i = 0; i != size; i++) {
                final IModuleBase moduleBase = (IModuleBase) object.__getitem__(i).__tojava__(IModuleBase.class);
                list.add(new JModuleObject(moduleBase.getName(), moduleBase.getDescription(), moduleBase.isEnabled(), moduleBase.getSettings()));
            }
            return list.toArray(new JModuleObject[0]);
        }
        return null;
    }

    /**
     * Gets Jython Module setting by name.
     * @param moduleObject The target Jython Module.
     * @param name The Name of the Jython Modules setting.
     * @return If the setting can't be found or modules doesn't exist Null will be returned.
     */
    public Setting getSetting(JModuleObject moduleObject, String name) {
        final Setting[] settings = moduleObject.getSettings();
        if(settings == null) return null;
        for(Setting setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) return setting;
        }
        return null;
    }

    /**
     * Get Jython Module by name if the module is none existent or Jython isn't setup Null will be returned.
     */
    public JModuleObject getModuleByName(String name) {
        final JModuleObject[] moduleList = getModuleList();
        if(moduleList == null) return null;
        for(JModuleObject module : moduleList) {
            if(module.getName().equalsIgnoreCase(name)) return module;
        }
        return null;
    }

    /**
     * Toggles the Jython Modules on or off.
     */
    public void toggleJModule(String name) {
        if(isPythonPresent()) {
            interpreter.exec("getModuleByName('"+name+"').Toggle()");
        }
    }
    public void toggleJModule(JModuleObject jModuleObject) {
        toggleJModule(jModuleObject.getName());
    }

    /**
     * Set the Toggle state of the Jython Module.
     */
    public void setToggleJModule(String name, boolean state) {
        if(isPythonPresent()) {
            interpreter.exec(state ? "getModuleByName('"+name+"').setToggle(True)" : "getModuleByName('"+name+"').setToggle(False)");
        }
    }

    /**
     * Boolean to check if Jython is usable / present.
     */
    public boolean isPythonPresent() {
        return pythonPresent;
    }

    /**
     * Saves a specific Jython Modules settings.
     * @param moduleObject The target Jython Module.
     */
    public void saveModule(final JModuleObject moduleObject) {
        if(disableSaving) return;
        if(moduleObject.getSettings() == null) return;

        final JsonObject settings = new JsonObject();
        for(Setting setting : moduleObject.getSettings()) {
            final JsonElement value = setting.toJson();
            if(value == null) continue;
            settings.add(setting.getName(), value);
        }

        try(BufferedWriter writer = Files.newBufferedWriter(configFolder.resolve(moduleObject.getName()+".json"))) {
            JsonUtils.prettyGson.toJson(settings, writer);
        } catch(IOException e) {
            System.out.println("Failed to save " + configFolder.getFileName());
            e.printStackTrace();
        }
    }

    /**
     * Saves all the registered Jython Modules settings.
     */
    public void saveAll() {
        for(JModuleObject moduleObject : getModuleList()) {
            if(moduleObject == null || moduleObject.getSettings() == null) continue;
            saveModule(moduleObject);
        }
    }

    /**
     * Loads a specific Jython Modules settings.
     * @param moduleObject The target Jython Module.
     */
    public void loadModule(final JModuleObject moduleObject) {
        final JsonObject json;
        try(BufferedReader reader = Files.newBufferedReader(configFolder.resolve(moduleObject.getName()+".json"))) {
            json = JsonUtils.jsonParser.parse(reader).getAsJsonObject();
        } catch(NoSuchFileException e) {
            saveModule(moduleObject);
            return;
        } catch(Exception e) {
            System.out.println("Failed to load " + configFolder.getFileName());
            e.printStackTrace();
            saveModule(moduleObject);
            return;
        }

        disableSaving = true;
        for(Map.Entry<String, JsonElement> e2 : json.entrySet()) {
            final String key = e2.getKey().toUpperCase();
            final Setting setting = getSetting(moduleObject, key);
            if(setting == null) continue;
            setting.fromJson(e2.getValue());
        }
        disableSaving = false;
        saveModule(moduleObject);
    }

    /**
     * Loads all the registered Jython Modules settings.
     */
    public void loadAll() {
        for(JModuleObject moduleObject : getModuleList()) {
            if(moduleObject == null || moduleObject.getSettings() == null) continue;
            loadModule(moduleObject);
        }
    }

    @CommitEvent
    public void update(TickEvent event) {
        if(Wrapper.getPlayer() == null || Wrapper.getWorld() == null) return;
        if(interpreter != null) {
            interpreter.exec("runUpdate()");
        }
    }

    @CommitEvent
    public void onRender(RenderEvent event) {
        if(Wrapper.getPlayer() == null || Wrapper.getWorld() == null) return;
        if(interpreter != null) {
            interpreter.exec("runRender3D()");
        }
    }

    @CommitEvent
    public void onRenderGUI(RenderGUIEvent event) {
        if(Wrapper.getPlayer() == null || Wrapper.getWorld() == null) return;
        if(Wrapper.getMinecraft().gameSettings.showDebugInfo) return;
        if(interpreter != null) {
            interpreter.exec("runRender2D()");
        }
    }
}
