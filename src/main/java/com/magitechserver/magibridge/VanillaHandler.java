package com.magitechserver.magibridge;

import com.magitechserver.magibridge.config.categories.Messages;
import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.ReplacerUtil;
import flavor.pie.boop.BoopableChannel;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import net.dv8tion.jda.core.entities.Message;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Daniel Widrick on 07/02/2018.
 */
 
 public class VanillaHandler
 {
	 
	 public static void handle(boolean isStaffChannel, FormatType format, Map<String, String> placeholders)
	 {
		 MessageChannel messageChannel = Sponge.getServer().getBroadcastChannel();
		 
		 String msg = ReplacerUtil.replaceEach(format.get(), placeholders);
		 if(messageChannel != null)
		 {
			 Text messageAsText = ReplacerUtil.toText(msg);
			 messageChannel.send(messageAsText);
			 
			 
		 }
	 }
 }
