package dev.u9g.mixin.client;

import dev.u9g.events.DecorateNameAboveHeadCallback;
import dev.u9g.events.DecorateNameAboveHeadEvent;
import dev.u9g.features.Settings;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Team.class)
public class TeamMixin {
    @Inject(at = @At("TAIL"), method = "decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;", cancellable = true)
    private static void skyplus$decorateName(AbstractTeam team, Text name, CallbackInfoReturnable<MutableText> cir) {
        if (team instanceof Team t) {
            if (!t.getPlayerList().isEmpty()) {
                String s = t.getPlayerList().iterator().next();
                DecorateNameAboveHeadEvent dnahe = new DecorateNameAboveHeadEvent(s, cir.getReturnValue());
                DecorateNameAboveHeadCallback.getEvent().invoker().invoke(dnahe);

                if (dnahe.getTextToSend().getString().trim().isEmpty()) {
                    System.out.println("Unexpected: username=" + dnahe.getUsername() + " became an empty string");
                    dnahe.setTextToSend(cir.getReturnValue());
                }


                if (Settings.INSTANCE.getTeamMembers().contains(s)) {
                    cir.setReturnValue(dnahe.getTextToSend());
                }
            }
        }
    }
}
