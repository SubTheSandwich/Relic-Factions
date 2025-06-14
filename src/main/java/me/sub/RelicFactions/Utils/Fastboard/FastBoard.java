package me.sub.RelicFactions.Utils.Fastboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

// Modified FastBoard class to use Adventure
public class FastBoard extends FastBoardBase<String> {

    private static final MethodHandle MESSAGE_FROM_STRING;
    private static final Object EMPTY_MESSAGE;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> craftChatMessageClass = FastReflection.obcClass("util.CraftChatMessage");
            MESSAGE_FROM_STRING = lookup.unreflect(craftChatMessageClass.getMethod("fromString", String.class));
            EMPTY_MESSAGE = Array.get(MESSAGE_FROM_STRING.invoke(""), 0);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    public FastBoard(Player player) {
        super(player);
    }

    @Override
    public void updateTitle(String title) {
        Objects.requireNonNull(title, "title");

        if (!VersionType.V1_13.isHigherOrEqual() && title.length() > 32) {
            throw new IllegalArgumentException("Title is longer than 32 chars");
        }

        super.updateTitle(title);
    }

    @Override
    public void updateLines(String... lines) {
        Objects.requireNonNull(lines, "lines");

        if (!VersionType.V1_13.isHigherOrEqual()) {
            int lineCount = 0;
            for (String s : lines) {
                if (s != null && s.length() > 30) {
                    throw new IllegalArgumentException("Line " + lineCount + " is longer than 30 chars");
                }
                lineCount++;
            }
        }

        super.updateLines(lines);
    }

    @Override
    protected void sendLineChange(int score) throws Throwable {
        int maxLength = hasLinesMaxLength() ? 16 : 1024;
        String line = getLineByScore(score);
        String prefix;
        String suffix = "";

        if (line == null || line.isEmpty()) {
            // Use reset code directly
            prefix = COLOR_CODES[score] + "§r";
        } else if (line.length() <= maxLength) {
            prefix = line;
        } else {
            // Prevent splitting color codes
            int index = line.charAt(maxLength - 1) == '§'
                    ? (maxLength - 1) : maxLength;
            prefix = line.substring(0, index);
            String suffixTmp = line.substring(index);

            // Get the last color using Adventure
            String color = getLastColor(prefix);
            boolean addColor = !isFormatCode(suffixTmp);

            // Use reset code if no color, otherwise use the last color
            suffix = (addColor ? (color.isEmpty() ? "§r" : color) : "") + suffixTmp;
        }

        if (prefix.length() > maxLength || suffix.length() > maxLength) {
            prefix = prefix.substring(0, Math.min(maxLength, prefix.length()));
            suffix = suffix.substring(0, Math.min(maxLength, suffix.length()));
        }

        sendTeamPacket(score, TeamMode.UPDATE, prefix, suffix);
    }

    @Override
    protected Object toMinecraftComponent(String line) throws Throwable {
        if (line == null || line.isEmpty()) {
            return EMPTY_MESSAGE;
        }
        return Array.get(MESSAGE_FROM_STRING.invoke(line), 0);
    }

    @Override
    protected String serializeLine(String value) {
        return value;
    }

    @Override
    protected String emptyLine() {
        return "";
    }

    protected boolean hasLinesMaxLength() {
        return !VersionType.V1_13.isHigherOrEqual();
    }

    // --- Adventure color helpers ---

    // Get the last color code in a string (Adventure equivalent)
    private String getLastColor(String input) {
        // Use Adventure to parse the string and get the last color
        Component comp = LegacyComponentSerializer.legacySection().deserialize(input);
        TextColor lastColor = getLastTextColor(comp);
        if (lastColor == null) return "";
        // Convert TextColor to legacy code
        return LegacyComponentSerializer.legacySection().serialize(Component.text("").color(lastColor));
    }

    // Recursively get the last TextColor in a component
    private TextColor getLastTextColor(Component comp) {
        TextColor color = comp.color();
        List<Component> children = comp.children();
        if (!children.isEmpty()) {
            TextColor childColor = getLastTextColor(children.getLast());
            if (childColor != null) return childColor;
        }
        return color;
    }

    // Check if the suffix starts with a format code (bold, italic, etc.)
    private boolean isFormatCode(String s) {
        // Adventure doesn't use format codes in the same way, so just check for §l, §o, etc.
        return s.length() >= 2 && s.charAt(0) == '§' && "lno".indexOf(Character.toLowerCase(s.charAt(1))) != -1;
    }
}