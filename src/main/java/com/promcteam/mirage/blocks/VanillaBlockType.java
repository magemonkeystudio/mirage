package com.promcteam.mirage.blocks;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class VanillaBlockType extends BlockType {
    private final Material material;
    private final short    data;

    VanillaBlockType(String str) {
        super(str);
        String matStr = str;
        short  data   = 0;
        if (str.contains(":")) {
            String[] split = str.split(":");
            matStr = split[0];
            try {
                data = Short.parseShort(str.split(":")[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Ignoring material " + str + " with unknown data \"" + split[1] + "\"");
            }
        }
        try {
            this.material = Material.valueOf(matStr
                    .trim()
                    .toUpperCase()
                    .replace(' ', '_')
                    .replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ignoring unknown material \"" + matStr + '"');
        }
        if (!this.material.isBlock()) {
            throw new IllegalArgumentException("Ignoring non-block material \"" + id + '"');
        }
        this.data = data;
    }

    @Override
    public String getPrefix() {return "";}

    @Override
    public boolean isInstance(Block block) {
        return block.getType().equals(this.material) && (
                this.data == 0 ||
                        block.getData() == this.data
        );
    }

    @Override
    public void place(Block block) {
        block.setType(material);
        block.getState().setRawData((byte) this.data);
        block.getState().update();
    }
}
