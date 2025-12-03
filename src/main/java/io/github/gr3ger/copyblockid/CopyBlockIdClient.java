package io.github.gr3ger.copyblockid;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = CopyBlockId.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = CopyBlockId.MODID, value = Dist.CLIENT)
public class CopyBlockIdClient {

    public CopyBlockIdClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        CopyBlockId.LOGGER.info("HELLO FROM CLIENT SETUP");
        CopyBlockId.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    public static final Lazy<KeyMapping> COPY_MAPPING = Lazy.of(() -> new KeyMapping(
            "copyblockid.keymappingtitle.copy",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_F4,
            "key.categories.misc"
    ));
    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(COPY_MAPPING.get());
    }

    @SubscribeEvent // on the game event bus only on the physical client
    public static void onClientTick(ClientTickEvent.Post event) {
        while (COPY_MAPPING.get().consumeClick()) {
            CopyBlockId.LOGGER.info("Started getting block id");
            Minecraft mc = Minecraft.getInstance();
            HitResult hitResult = mc.hitResult;
            Level level = mc.level;

            if(hitResult != null && level != null && hitResult.getType() == HitResult.Type.BLOCK){
                var location = ((BlockHitResult)hitResult).getBlockPos();
                CopyBlockId.LOGGER.info(location.toString());
                String blockId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(location).getBlock()).toString();
                GLFW.glfwSetClipboardString(mc.getWindow().getWindow(), blockId);
                if(mc.player != null){
                    var playerChatMessage = PlayerChatMessage.unsigned(mc.player.getUUID(), "Block ID copied to clipboard");
                    mc.player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(playerChatMessage), false, ChatType.bind(ChatType.CHAT, mc.player));
                }
            }
        }
    }
}
