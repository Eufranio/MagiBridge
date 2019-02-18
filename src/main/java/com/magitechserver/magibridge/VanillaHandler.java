package com.magitechserver.magibridge;

import com.magitechserver.magibridge.util.FormatType;
import com.magitechserver.magibridge.util.ReplacerUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.Map;

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
