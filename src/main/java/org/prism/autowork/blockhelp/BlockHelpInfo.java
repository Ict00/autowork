package org.prism.autowork.blockhelp;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mutable;

import java.util.ArrayList;
import java.util.List;

public record BlockHelpInfo(List<Component> technical, List<Component> details) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<Component> tech;
        private List<Component> details;

        protected Builder() {
            details = new ArrayList<>();
            tech = new ArrayList<>();
        }


        public BlockHelpInfo build() {
            return new BlockHelpInfo(tech, details);
        }

        public Builder details(String translation) {
            details.add(Component.translatable(translation).withColor(0xb6f030));
            return this;
        }

        public Builder multiblock() {
            tech.add(Component.translatable("blockhelp.multiblock").withColor(0xb3b3b3));
            return this;
        }

        public Builder storage() {
            tech.add(Component.translatable("blockhelp.storage").withColor(0xb3b3b3));
            return this;
        }

        public Builder no_storage() {
            tech.add(Component.translatable("blockhelp.no_storage").withColor(0xb3b3b3));
            return this;
        }

        public Builder storage_required(Direction direction) {
            return direction("blockhelp.storage_required", direction);
        }
        public Builder storage_required_front() {
            return front("blockhelp.storage_required");
        }

        public Builder storage_required_back() {
            return back("blockhelp.storage_required");
        }

        public Builder power_depends_on_signal() {
            tech.add(Component.translatable("blockhelp.power_depends").withColor(0xb3b3b3));
            return this;
        }

        public Builder only_when_unpowered() {
            tech.add(Component.translatable("blockhelp.works_when_unpowered").withColor(0xb3b3b3));
            return this;
        }

        public Builder only_when_powered() {
            tech.add(Component.translatable("blockhelp.works_when_powered").withColor(0xb3b3b3));
            return this;
        }

        public Builder other(String translation) {
            tech.add(Component.translatable(translation).withColor(0xb3b3b3));
            return this;
        }

        public Builder direction(String translation, Direction direction) {
            tech.add(Component.empty()
                    .append(Component.translatable(String.format("blockhelp.direction.%s", direction.getName())).withColor(0x47ffd1)
                    .append(": ")
                    .append(
                    Component.translatable(translation).withColor(0xffa647))

                    ));
            return this;
        }

        public Builder front(String translation) {
            tech.add(Component.empty()
                    .append(Component.translatable("blockhelp.direction.front").withColor(0x47ffd1)
                            .append(": ")
                            .append(
                                    Component.translatable(translation).withColor(0xffa647))

                    ));
            return this;
        }

        public Builder back(String translation) {
            tech.add(Component.empty()
                    .append(Component.translatable("blockhelp.direction.back").withColor(0x47ffd1)
                            .append(": ")
                            .append(
                                    Component.translatable(translation).withColor(0xffa647))
                    ));
            return this;
        }
    }
}
