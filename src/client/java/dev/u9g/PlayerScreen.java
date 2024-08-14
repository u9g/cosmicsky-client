package dev.u9g;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import dev.u9g.features.TeamsKt;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerScreen extends BaseOwoScreen<FlowLayout> {

    public JsonValue data;
    public JsonArray usernames;
    public String currentUsername;

    public PlayerScreen(JsonValue data, JsonArray usernames, String currentUsername) {
        super();
        this.data = data;
        this.usernames = usernames;
        this.currentUsername = currentUsername;
    }

    FlowLayout makePv(int n) {
        var fl = Containers.verticalFlow(Sizing.content(), Sizing.content());
        var gl = Containers.grid(Sizing.content(), Sizing.content(), 6, 9);
        List<JsonValue> v = data.asArray().values();
        JsonArray wanted = null;
        for (JsonValue vx : v) {
            JsonObject jj = vx.asObject();
            int x = jj.getInt("pv_number", -1);
            if (x == n) {
                wanted = (JsonArray) Json.parse(jj.get("pv").asString());
                break;
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 9; j++) {
                if (wanted != null) {
                    ItemStack itemStack;
                    try {
                        itemStack = ItemStack.CODEC.parse(NbtOps.INSTANCE, StringNbtReader.parse(wanted.get((i * 9) + j).asString())).getOrThrow(false, (x) -> {
                        });
                    } catch (Exception e) {
                        itemStack = ItemStack.EMPTY;
                    }
                    gl.child(Components.item(itemStack).setTooltipFromStack(true).showOverlay(true).margins(Insets.right(2)).margins(Insets.bottom(2)), i, j);
                } else {
                    gl.child(Components.item(ItemStack.EMPTY).setTooltipFromStack(true).margins(Insets.right(2)).margins(Insets.bottom(2)), i, j);
                }
//                StringNbtReader.parse()
            }
        }

        fl.child(Components.label(Text.of("§lPlayer Vault #" + n)).margins(Insets.bottom(10)));
        fl.child(gl);

        return fl;
    }

    FlowLayout makeInventory() {
        List<JsonValue> v = data.asArray().values();
        JsonArray wanted = null;
        for (JsonValue vx : v) {
            JsonObject jj = vx.asObject();
            int x = jj.getInt("pv_number", -1);
            if (x == 0) {
                wanted = (JsonArray) Json.parse(jj.get("pv").asString());
                break;
            }
        }
        var fl = Containers.verticalFlow(Sizing.content(), Sizing.content());
        var gl = Containers.grid(Sizing.content(), Sizing.content(), 6, 9);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 9; j++) {
                if (wanted != null) {
                    ItemStack itemStack;
                    try {
                        itemStack = ItemStack.CODEC.parse(NbtOps.INSTANCE, StringNbtReader.parse(wanted.get((i * 9) + j).asString())).getOrThrow(false, (x) -> {
                        });
                    } catch (Exception e) {
                        itemStack = ItemStack.EMPTY;
                    }
                    gl.child(Components.item(itemStack).setTooltipFromStack(true).showOverlay(true).margins(Insets.right(2)).margins(Insets.bottom(2)), i, j);
                } else {
                    gl.child(Components.item(ItemStack.EMPTY).setTooltipFromStack(true).margins(Insets.right(2)).margins(Insets.bottom(2)), i, j);
                }
            }
        }

        fl.child(Components.label(Text.of("§lPlayer Inventory")).margins(Insets.bottom(10)));
        fl.child(gl);

        return fl;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this,
                (a, b) -> {
                    FlowLayout fl = Containers.verticalFlow(a, b);
                    fl.surface(Surface.VANILLA_TRANSLUCENT);
                    ArrayList<Component> btns = new ArrayList<>();
                    Component activeOne = null;
                    for (JsonValue v : this.usernames) {
                        Component btn = Components.button(Text.of(v.asString()), (x) -> {
                            TeamsKt.pvInfoOfUsername(v.asString());
                        }).active(!currentUsername.equals(v.asString()));
                        btns.add(btn);
                        if (currentUsername.equals(v.asString())) {
                            activeOne = btn;
                        }
                    }
                    var verticalScroll = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100),
                            Containers.verticalFlow(Sizing.content(), Sizing.content()).children(btns)
                    );
                    if (activeOne != null) {
                        verticalScroll.scrollTo(activeOne);
                    }
                    fl.children(List.of(
                            verticalScroll.positioning(Positioning.relative(0, 0)),
                            Containers.horizontalFlow(Sizing.content(), Sizing.content()).children(List.of(
                                    makePv(1).padding(Insets.right(5)),
                                    makePv(2).padding(Insets.right(5)),
                                    makePv(3).padding(Insets.right(5)),
                                    makePv(4).padding(Insets.right(5)),
                                    makePv(5).padding(Insets.right(5))
                            )).padding(Insets.bottom(5)),
                            makeInventory()
                    ));

                    fl.verticalAlignment(VerticalAlignment.CENTER);
                    fl.horizontalAlignment(HorizontalAlignment.CENTER);

                    return fl;
                });
    }

    @Override
    protected void build(FlowLayout rootComponent) {

    }
}