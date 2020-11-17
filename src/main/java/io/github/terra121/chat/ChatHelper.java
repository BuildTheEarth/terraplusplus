package io.github.terra121.chat;

import io.github.terra121.TerraConstants;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;

public class ChatHelper {
    public static ITextComponent makeTitleTextComponent(TextElement... text) {
        ITextComponent bar = new TextComponentString(TerraConstants.prefix.replace("&","\u00A7"));
        for(int x = 0; x < text.length; x++) {
            bar.appendSibling(new TextComponentString(text[x].text).setStyle(new Style().setColor(text[x].color)
            .setBold(text[x].bold)));
        }
        return bar;
    }

    public static ITextComponent makeTextComponent(TextElement... text) {
        ITextComponent bar = new TextComponentString("");
        for(int x = 0; x < text.length; x++) {
            bar.appendSibling(new TextComponentString(text[x].text).setStyle(new Style().setColor(text[x].color)
                    .setBold(text[x].bold)));
        }
        return bar;
    }

    public static void infoMessage(ICommandSender sender) {

    }

    public static String capitalize(final String str) {
        final int strLen = length(str);
        if (strLen == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = Character.toTitleCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            // already capitalized
            return str;
        }

        final int[] newCodePoints = new int[strLen]; // cannot be longer than the char array
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint; // copy the first codepoint
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint; // copy the remaining ones
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

}
