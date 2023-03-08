package Utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ComponentCreator {
    String message;

    public ComponentCreator(String message) {
        this.message = message;
    }

    public ComponentBuilder createLinkTextComponent(String hoverText, String link, boolean bold) {
        ComponentBuilder textComponent = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', message));
        if (bold) textComponent.bold(true);
        textComponent.event(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
        textComponent.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&', hoverText))));
        return textComponent;
    }

    public ComponentBuilder createPlainTextComponent(boolean setHoverText, String hoverText) {
        ComponentBuilder textComponent = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', message));
        if (setHoverText) textComponent.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.translateAlternateColorCodes('&', hoverText))));
        return textComponent;
    }

}
