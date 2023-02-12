package scarmor.bot.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import scarmor.bot.Bot;

import java.io.IOException;
import java.util.List;


public class MessageListener extends ListenerAdapter {


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                String messageSent = event.getMessage().getContentDisplay();
                if(messageSent.startsWith("@VAI")) {
                    while(messageSent.startsWith("@VAI")) {
                        System.out.println(messageSent);
                        messageSent = messageSent.substring(1 + "@VAI".length());
                        System.out.println(messageSent);
                    }
                    try {
                        event.getChannel().sendTyping().queue();
                        String answer = Bot.generateAnswer(messageSent);
                        System.out.println(answer);
                        event.getChannel().addReactionById(event.getMessageId(),"\uD83C\uDF70").queue();
                        event.getMessage().reply(answer).queue();
                    } catch (Exception e) {
                        event.getMessage().reply("Wrong request").queue();
                    }
                }
                super.run();
            }
        };
        thread.start();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "temperature":
                double optionTemp = event.getOption("temperature").getAsDouble();
                if (optionTemp < 0 || optionTemp > 1) {
                    event.reply("You entered an invalid parameter value").queue();
                } else {
                    event.reply("Successful change of the parameter \"temperature\"").queue();
                    Bot.temperature = optionTemp;
                }
                break;
            case "max_tokens":
                int optionTokens = event.getOption("temperature").getAsInt();
                if (optionTokens < 500 || optionTokens > 4000) {
                    event.reply("You entered an invalid parameter value").queue();
                } else {
                    event.reply("Successful change of the parameter \"temperature\"").queue();
                    Bot.max_tokens = optionTokens;
                }
                break;
            case "generate_image":
                try {
                    String prompt = event.getOption("prompt").getAsString();
                    event.reply("Image by request \"" + prompt + "\"").queue();
                    List<String> images = Bot.generateImages(prompt, 1);
                    for (String image : images) {
                        event.getChannel().sendMessage(image).queue();
                    }
                } catch (IOException e) {
                    event.getChannel().sendMessage("Wrong prompt. Don't use special symbols!").queue();
                }
                break;
        }
    }
}
