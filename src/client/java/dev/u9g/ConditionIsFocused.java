package dev.u9g;

import dev.u9g.features.Settings;
import net.minecraft.entity.player.PlayerEntity;
import shcm.shsupercm.fabric.citresewn.api.CITConditionContainer;
import shcm.shsupercm.fabric.citresewn.cit.CITContext;
import shcm.shsupercm.fabric.citresewn.cit.builtin.conditions.BooleanCondition;

import java.util.Locale;

public class ConditionIsFocused extends BooleanCondition {
    public static final CITConditionContainer<ConditionIsFocused> CONTAINER = new CITConditionContainer<>(ConditionIsFocused.class, ConditionIsFocused::new,
            "is_focused");

    @Override
    protected boolean getValue(CITContext context) {
        return context.entity instanceof PlayerEntity pe && Settings.INSTANCE.getFocusedPlayerUsername().toLowerCase(Locale.ROOT).equals(pe.getGameProfile().getName().toLowerCase(Locale.ROOT));
    }
}
