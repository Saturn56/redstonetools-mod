package tools.redstone.redstonetools.features.commands;


import com.google.auto.service.AutoService;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.antlr.v4.codegen.model.chunk.RulePropertyRef_ctx;
import tools.redstone.redstonetools.features.AbstractFeature;
import tools.redstone.redstonetools.features.Feature;
import tools.redstone.redstonetools.features.feedback.Feedback;
import tools.redstone.redstonetools.utils.ItemUtils;

@AutoService(AbstractFeature.class)
@Feature(command = "itembind", description = "Allows you to bind command to a specific item", name = "Item Bind")
public class ItemBindFeature extends CommandFeature{
    public static boolean waitingForCommand = false;
    private static ServerPlayerEntity player;
    private static CommandContext ctx;


    @Override
    protected Feedback execute(CommandContext ctx) throws CommandSyntaxException {
        this.ctx = ctx;

        player = ctx.source().player();
        waitingForCommand = true;

        return Feedback.success("Please run any command and hold the item you want the command be bound to");
    }

    public static Feedback addCommand(String command) {
        if (!waitingForCommand || ctx.source().getServer() == null) return null;

        if (player == null || ctx.source().getPlayer(player.getUuid()) != player) {
            waitingForCommand = false;
            return null;
        }

        ItemStack mainHandStack = player.getMainHandStack();
        if (mainHandStack == null || mainHandStack.getItem() == Items.AIR) {
            return Feedback.error("You need to be holding an item!");
        }


        either(
                () -> mainHandStack.getOrCreateNbt().put("command", NbtString.of(command)),
                () -> mainHandStack.getOrCreateTag().put("command", NbtString.of(command)) // 1.16 - 1.17
        );
        ItemUtils.addExtraNBTText(mainHandStack,"Command");

        waitingForCommand = false;

        return Feedback.success("Successfully bound command: '{}' to this item ({})!", command, mainHandStack.getItem());
    }
}
