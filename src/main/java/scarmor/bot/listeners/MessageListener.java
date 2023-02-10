package scarmor.bot.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import scarmor.bot.Bot;


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
                        event.getChannel().sendMessage(answer).queue();
                    } catch (Exception e) {
                        event.getChannel().sendMessage("Wrong request").queue();
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
                    event.getChannel().sendMessageFormat("You entered an invalid parameter value").queue();
                } else {
                    event.getChannel().sendMessageFormat("Successful change of the parameter \"temperature\"").queue();
                    Bot.temperature = optionTemp;
                }
                break;
            case "max_tokens":
                event.getChannel().sendMessageFormat("Successful change of the parameter \"max_tokens\"").queue();
                int optionTokens = event.getOption("temperature").getAsInt();
                if (optionTokens < 500 || optionTokens > 4000) {
                    event.getChannel().sendMessageFormat("You entered an invalid parameter value").queue();
                } else {
                    event.getChannel().sendMessageFormat("Successful change of the parameter \"temperature\"").queue();
                    Bot.max_tokens = optionTokens;
                }
                break;
        }
    }
}
