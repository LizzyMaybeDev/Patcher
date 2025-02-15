package club.sk1er.patcher.tweaker;

import club.sk1er.patcher.Patcher;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class PatcherTweaker implements IFMLLoadingPlugin {

    public static long clientLoadTime;

    @SuppressWarnings("unchecked")
    public PatcherTweaker() {
        // I need this for mixins to work in dev. why? who knows!
        // breaks shit in prod though, keep it uncommented when pushing
//        MixinBootstrap.init();
//        Mixins.addConfiguration("patcher.mixins.json");

        clientLoadTime = System.currentTimeMillis();
        try {
            // Create a second internal tweaker, creating after OptiFine does its thing.
            FMLLaunchHandler launchHandler = ReflectionHelper.getPrivateValue(FMLLaunchHandler.class, null, "INSTANCE");
            LaunchClassLoader classLoader = ReflectionHelper.getPrivateValue(FMLLaunchHandler.class, launchHandler, "classLoader");
            Method loadCoreMod = ReflectionHelper.findMethod(CoreModManager.class, null, new String[]{"loadCoreMod"}, LaunchClassLoader.class, String.class, File.class);
            URL path = PatcherTweaker.class.getProtectionDomain().getCodeSource().getLocation();
            File mod = new File(path.toURI().getSchemeSpecificPart().split("!")[0]);
            ITweaker coreMod = (ITweaker) loadCoreMod.invoke(null, classLoader, "club.sk1er.patcher.tweaker.other.ModTweaker", mod);
            if (!((List<String>) Launch.blackboard.get("TweakClasses")).contains("net.minecraftforge.fml.common.launcher.FMLInjectionAndSortingTweaker")) {
                ((List<ITweaker>) Launch.blackboard.get("Tweaks")).add(coreMod);
            }
        } catch (Exception e) {
            System.out.println("Failed creating a second tweaker");
            e.printStackTrace();
        }

        this.unlockLwjgl();
        this.detectMods();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @SuppressWarnings("unchecked")
    private void unlockLwjgl() {
        boolean lwjglUnlock = false;
        try {
            // Unlock LWJGL, allowing for it to be transformed.
            Field transformerExceptions = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
            transformerExceptions.setAccessible(true);
            Object o = transformerExceptions.get(Launch.classLoader);
            lwjglUnlock = ((Set<String>) o).remove("org.lwjgl.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (!lwjglUnlock) {
            System.out.println("Failed to unlock LWJGL, several fixes will not work.");
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    private void detectMods() {
        File mods = new File(Launch.minecraftHome, "mods");

        if (!mods.exists()) {
            mods.mkdirs(); // make the mods folder for forge if it doesnt exist already
        }

        File[] coreModList = mods.listFiles((dir, name) -> name.endsWith(".jar"));
        for (File file : coreModList) {
            try {
                try (ZipFile zipFile = new ZipFile(file)) {
                    ZipEntry entry = zipFile.getEntry("mcmod.info");

                    if (zipFile.getEntry("io/framesplus/FramesPlus.class") != null) {
                        halt("Patcher is no longer compatible with Frames+ as of 1.3. The Frames+ enhancements have been rewritten for even greater performance and compatibility, and are now included in Patcher.");
                        continue;
                    }

                    if (zipFile.getEntry("club/sk1er/mods/core/ModCore.class") != null) {
                        halt("ModCore should not be in your mods folder. This will most likely cause issues and crash. Please remove it from the mods folder.");
                        continue;
                    }

                    if (entry != null) {
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            byte[] availableBytes = new byte[inputStream.available()];
                            inputStream.read(availableBytes, 0, inputStream.available());
                            JsonObject modInfo = new JsonParser().parse(new String(availableBytes)).getAsJsonArray().get(0).getAsJsonObject();
                            if (!modInfo.has("modid")) {
                                continue;
                            }

                            String modId = modInfo.get("modid").getAsString();
                            if (modId.equals("the5zigMod") && (modInfo.has("url") && !modInfo.get("url").getAsString().equalsIgnoreCase("https://5zigreborn.eu"))) {
                                halt("<html><p>Patcher is not compatible with old 5zig. Please use 5zig reborn found at <a href=\"https://5zigreborn.eu\">https://5zigreborn.eu</a></p></html>");
                            }
                        }
                    }
                }
            } catch (MalformedJsonException ignored) {
                // powns lol
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void halt(final String message) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(null, message, "Launch Aborted", JOptionPane.ERROR_MESSAGE);
        invokeExit();
    }

    static void invokeExit() {
        try {
            final Class<?> aClass = Class.forName("java.lang.Shutdown");
            final Method exit = aClass.getDeclaredMethod("exit", int.class);
            exit.setAccessible(true);
            exit.invoke(null, 0);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
