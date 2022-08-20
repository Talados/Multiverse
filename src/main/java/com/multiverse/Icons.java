package com.multiverse;

import com.google.common.base.Splitter;
import java.awt.image.BufferedImage;
import lombok.AllArgsConstructor;
import net.runelite.client.util.ImageUtil;

@AllArgsConstructor
enum Icons {
    DISCORD("discord"),
    ;

    private static final Splitter SPLITTER = Splitter.on(" ").trimResults().omitEmptyStrings();

    private final String iconFilename;

    BufferedImage loadImage() {
        return ImageUtil.loadImageResource(getClass(), iconFilename + ".png");
    }
}