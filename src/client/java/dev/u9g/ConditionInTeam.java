package dev.u9g;

import dev.u9g.features.Settings;
import net.minecraft.entity.player.PlayerEntity;
import shcm.shsupercm.fabric.citresewn.api.CITConditionContainer;
import shcm.shsupercm.fabric.citresewn.cit.CITContext;
import shcm.shsupercm.fabric.citresewn.cit.builtin.conditions.BooleanCondition;

public class ConditionInTeam extends BooleanCondition {
    public static final CITConditionContainer<ConditionInTeam> CONTAINER = new CITConditionContainer<>(ConditionInTeam.class, ConditionInTeam::new,
            "in_team");

    @Override
    protected boolean getValue(CITContext context) {
        return context.entity instanceof PlayerEntity pe && Settings.INSTANCE.getTeamMembers().contains(pe.getGameProfile().getName());
    }
}
