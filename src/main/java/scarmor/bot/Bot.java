package scarmor.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import scarmor.bot.listeners.MessageListener;


public class Bot extends ListenerAdapter {
    public static int max_tokens = 1000;
    public static double temperature = 0.6;

    private static final String API_KEY_GPT = System.getenv("OPENAI_API_KEY");
    private static final String API_KEY_BOT = System.getenv("DISCORD_API_KEY");
    private static final String API_URL = "https://api.openai.com/v1/completions";


    public static String generateAnswer(String prompt) throws Exception {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(API_URL);
        post.setHeader("Content-Type", "application/json; charset=utf-8");
        post.setHeader("Authorization", "Bearer " + API_KEY_GPT);

        JSONObject request = new JSONObject();
        request.put("prompt", prompt);
        request.put("model", "text-davinci-003");
        request.put("max_tokens", max_tokens);
        request.put("temperature", temperature);

        post.setEntity(new StringEntity(request.toString(), "UTF-8"));
        HttpResponse response = client.execute(post);

        String responseString = EntityUtils.toString(response.getEntity());
        JSONObject responseJson = new JSONObject(responseString);
        return responseJson.getJSONArray("choices").getJSONObject(0).getString("text");
    }



    public static void main(String[] args) throws Exception {
        JDA jda = JDABuilder.createDefault(API_KEY_BOT)
                .addEventListeners(new MessageListener())
                .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .setBulkDeleteSplittingEnabled(false)
                .setCompression(Compression.NONE)
                .setActivity(Activity.listening("Hentai"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .build();
        jda.updateCommands().addCommands(
                Commands.slash("temperature", "Change the randomness of the bot's statements (0 - 1)")
                        .addOption(OptionType.NUMBER, "temperature", "Choose temperature from 0 (No Random) to 1 (Absolutely Random)", true),
                Commands.slash("max_tokens", "Change the maximum number of generated words")
                        .addOption(OptionType.INTEGER, "max_tokens", "Choose a number from 500 to 4000", true)).queue();
    }
}
