package club.sk1er.patcher.screen.render.title;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class TitleFix {

    @SubscribeEvent
    public void disconnectEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        final GuiIngame gui = Minecraft.getMinecraft().ingameGUI;

        // these are never cleared when logging out of a server while displaying a title or subtitle,
        // so clear these if they're not already cleared when leaving the server.
        if (!gui.displayedTitle.isEmpty()) gui.displayedTitle = "";
        if (!gui.displayedSubTitle.isEmpty()) gui.displayedSubTitle = "";
    }
}
