package dev.u9g;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import kotlin.Triple;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class IslandInfoScreen extends BaseUIModelScreen<FlowLayout> {
    private final List<Triple<String, String, Boolean>> usernameANDroleANDonline;

    public IslandInfoScreen(List<Triple<String, String, Boolean>> members) {
        super(FlowLayout.class, DataSource.asset(new Identifier("skyplus", "island-info")));
        this.usernameANDroleANDonline = members;
    }

    boolean canDemote(String theirRole, @Nullable String yourRole) {
        if (yourRole == null) {
            return false;
        }

        if (yourRole.equals("Co-Leader")) {
            return theirRole.equals("Officer") || theirRole.equals("Member");
        }

        if (yourRole.equals("Owner")) {
            return !theirRole.equals("Owner");
        }

        return false;
    }

    boolean canPromote(String theirRole, @Nullable String yourRole) {
        if (yourRole == null) {
            return false;
        }

        if (yourRole.equals("Co-Leader")) {
            return theirRole.equals("Member") || theirRole.equals("Recruit");
        }

        if (yourRole.equals("Owner")) {
            return !theirRole.equals("Owner") && !theirRole.equals("Co-Leader");
        }

        return false;
    }

    boolean canKick(String theirRole, @Nullable String yourRole) {
        if (yourRole == null) {
            return false;
        } else if (yourRole.equals("Officer")) {
            return theirRole.equals("Member") || theirRole.equals("Recruit");
        } else if (yourRole.equals("Co-Leader")) {
            return theirRole.equals("Officer") || theirRole.equals("Member") || theirRole.equals("Recruit");
        } else if (yourRole.equals("Owner")) {
            return !theirRole.equals("Owner");
        }

        return false;
    }

    ParentComponent memberLines() {
        GridLayout gl = Containers.grid(Sizing.content(), Sizing.content(), usernameANDroleANDonline.size(), 5);

        String username = MinecraftClient.getInstance().getSession().getUsername();
        var onIsland = usernameANDroleANDonline.stream().filter(x -> Objects.equals(x.component1(), username)).findAny();

        @Nullable String yourRole = onIsland.map(Triple::component2).orElse(null);

        for (int i = 0; i < usernameANDroleANDonline.size(); i++) {
            Triple<String, String, Boolean> usernameANDroleANDonline = this.usernameANDroleANDonline.get(i);

            gl.child(Components.button(Text.of("+"), (btn) -> {
                ClientPlayNetworkHandler cpnh = MinecraftClient.getInstance().getNetworkHandler();
                if (cpnh != null) {
                    cpnh.sendChatCommand("is promote " + usernameANDroleANDonline.component1());
                    cpnh.sendChatCommand("is members");
                }
            }).active(canPromote(usernameANDroleANDonline.component2(), yourRole)), i, 0);

            gl.child(Components.button(Text.of("-"), (btn) -> {
                ClientPlayNetworkHandler cpnh = MinecraftClient.getInstance().getNetworkHandler();
                if (cpnh != null) {
                    cpnh.sendChatCommand("is demote " + usernameANDroleANDonline.component1());
                    cpnh.sendChatCommand("is members");
                }
            }).active(canDemote(usernameANDroleANDonline.component2(), yourRole)).margins(Insets.right(10)), i, 1);

            gl.child(Components.label(Text.of(usernameANDroleANDonline.component2())).margins(Insets.right(10)), i, 2);

            gl.child(Components.label(Text.of(usernameANDroleANDonline.component1())).color(usernameANDroleANDonline.component3() ? Color.GREEN : Color.RED).margins(Insets.right(10)), i, 3);

            gl.child(Components.button(Text.of("Kick"), (btn) -> {
                ClientPlayNetworkHandler cpnh = MinecraftClient.getInstance().getNetworkHandler();
                if (cpnh != null) {
                    cpnh.sendChatCommand("is kick " + usernameANDroleANDonline.component1());
                    cpnh.sendChatCommand("is members");
                }
            }).active(canKick(usernameANDroleANDonline.component2(), yourRole)).margins(Insets.right(10)), i, 4);
        }

        gl.verticalAlignment(VerticalAlignment.CENTER);

        return gl;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(FlowLayout.class, "island-members")
                .clearChildren()
                .children(List.of(memberLines()));
    }


}
