package scarmor.bot.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import scarmor.bot.Bot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class MessageListener extends ListenerAdapter {

    private final String IAM_TOKEN = System.getenv("YANDEX_IAM");
    private final String folder_id = System.getenv("YANDEX_FOLDER_ID");

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
            case "generate_image_ru":
                try {
                    String ruPrompt = event.getOption("prompt").getAsString();
                    String enPrompt = translate(ruPrompt);
                    event.reply("Image by request \"" + ruPrompt + "\"").queue();
                    List<String> images = Bot.generateImages(enPrompt, 1);
                    for (String image : images) {
                        event.getChannel().sendMessage(image).queue();
                    }
                } catch (IOException e) {
                    event.getChannel().sendMessage("Wrong prompt. Either you are using special characters, or you are using invalid statements, or an error occurred while executing the query for some other reason.").queue();
                }
                break;
        }
    }

    private String translate(String ruString) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        String result = "";
        try {
            HttpPost request = new HttpPost("https://translate.api.cloud.yandex.net/translate/v2/translate");
            String body = String.format("{\"targetLanguageCode\":\"%s\",\"texts\":\"%s\",\"folderId\":\"%s\"}", "en", ruString, folder_id);
            StringEntity params = new StringEntity(body, "UTF-8");
            params.setContentType("charset=UTF-8");
            String auth = String.format("Bearer %s", IAM_TOKEN);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", auth);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                Object jsob_obj = new JSONParser().parse(retSrc);
                JSONObject json_res = (JSONObject) jsob_obj;
                JSONArray res_translate = (JSONArray) json_res.get("translations");
                JSONObject res_json_obj = (JSONObject) res_translate.get(0);
                result = (String) res_json_obj.get("text");
                System.out.println(result);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return result;
    }
}
