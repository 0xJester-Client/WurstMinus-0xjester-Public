package me.third.right.utils.Client.ModuleUtils.ChatBotUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.third.right.ThirdMod;
import me.third.right.modules.Client.SocialCredit;
import me.third.right.utils.Client.File.JsonUtils;
import me.third.right.utils.Client.Utils.DelayTimer;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static me.third.right.modules.Client.SocialCredit.badThink;
import static me.third.right.modules.Client.SocialCredit.goodThink;
import static me.third.right.utils.Client.ModuleUtils.ChatBotUtils.ChatBotUtils.getUserName;
import static me.third.right.utils.Client.ModuleUtils.ChatBotUtils.ChatBotUtils.reply;

public class SocialCreditChatBot extends ChatBotBase{
    private final String[] chinaIdentifiers = { "ccp", "china", "communist party of china", "communist china" };
    private final List<ScoreObject> scoreData = new ArrayList<>();
    private final DelayTimer delayTimer = new DelayTimer();

    public SocialCreditChatBot() {
        super("SocialCredit", ThirdMod.configFolder.resolve("socialCreditScore.json"));
        onLoad();
    }

    @Override
    public void onUpdate() {
        if(mc.player == null || mc.world == null || mc.getConnection() == null) return;
        for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
            if(!containUser(info.getGameProfile().getName())) {
                scoreData.add(new ScoreObject(1000, info.getGameProfile().getName()));
            }
        }
    }

    @Override
    public void onMessage(String message) {
        for(String text : chinaIdentifiers) {
            if(!message.contains(text)) continue;
            final ScoreObject user = getUser(getUserName(message, false));
            if(user != null) {
                for (String text1 : goodThink) {
                    if (!message.contains(text1)) continue;
                    user.incrementScore();
                    break;
                }
                for (String text1 : badThink) {
                    if (!message.contains(text1)) continue;
                    user.deductScore();
                    break;
                }
            }
            break;
        }
    }

    @Override
    public void onWhisper(String message) {
        final String username = getUserName(message, true);
        if(username.isEmpty()) return;
        message = message.replace(String.format("%s whispers to you: ", username), "");
        final String[] args = message.split(" ");
        if(args[0].startsWith(ThirdMod.hax.chatBot.commandPrefix.getString())) {
            final ScoreObject object = getUser(username);
            if(object == null) return;
            switch (args[0].replace(ThirdMod.hax.chatBot.commandPrefix.getString(), "").toLowerCase(Locale.ROOT)) {
                case "score":
                    reply("Score "+object.getScore()+"", username);
                    break;
                case "difficulty":
                    if(args.length < 2 || args.length >= 4 || args[1].isEmpty()) {
                        reply("Your current difficulty is "+object.getDifficulty().toString()+"!", username);
                        return;
                    } else {
                        final SocialCredit.Difficulty dif;
                        switch (args[1]) {
                            case "easy":
                            case "hard":
                            case "medium":
                            case "irl":
                                dif = object.stringToDifficulty(args[1]);
                                object.setDifficulty(dif);
                                reply("Set difficulty to " + dif.toString() + "!", username);
                                break;
                            case "set":
                                if (args[2].isEmpty()) return;
                                dif = object.stringToDifficulty(args[2]);
                                object.setDifficulty(dif);
                                reply("Set difficulty to " + dif.toString() + "!", username);
                                break;
                            case "reset":
                                object.setDifficulty(SocialCredit.Difficulty.Easy);
                                reply("Reset Difficulty.", username);
                                break;
                            case "list":
                                reply("Easy, Medium, Hard and IRL are available.",username);
                                break;
                            case "get":
                            default:
                                reply("Your current difficulty is " + object.getDifficulty().toString() + "!", username);
                                break;
                        }
                    }
                    break;
                case "help":
                    if(args.length < 2 || args.length >= 4 || args[1].isEmpty()) {
                        reply("Score, Difficulty and Help are all the available commands.", username);
                        return;
                    } else {
                        switch (args[1]) {
                            case "score":
                                reply("Replies with you current social credit score.", username);
                                break;
                            case "difficulty":
                                reply("Change or check your difficulty.", username);
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }



    @Override
    public void onLoad() {
        JsonObject json;
        try(BufferedReader reader = Files.newBufferedReader(getPath())) {
            json = JsonUtils.jsonParser.parse(reader).getAsJsonObject();
        } catch(NoSuchFileException e) {
            return;
        } catch(Exception e) {
            System.out.println("Failed to load " + getPath().getFileName());
            e.printStackTrace();
            return;
        }

        for(Map.Entry<String, JsonElement> e : json.entrySet()) {
            if(!e.getValue().isJsonObject()) continue;

            final ScoreObject data = new ScoreObject(e.getKey());
            for(Map.Entry<String, JsonElement> e2 : e.getValue().getAsJsonObject().entrySet()) {
                if(e2.getKey().equals("score")) {
                    data.setScore(e2.getValue().getAsInt());
                }
                if (e2.getKey().equals("difficulty")) {
                    data.setDifficulty(data.stringToDifficulty(e2.getValue().getAsString()));
                }
            }
            scoreData.add(data);
        }
    }

    @Override
    public void onUnLoad() {
        if(scoreData.isEmpty()) return;
        JsonObject json = new JsonObject();
        for(ScoreObject scoreObject : scoreData) {
            JsonObject data = new JsonObject();
            data.add("score", scoreObject.scoreToJson());
            data.add("difficulty", scoreObject.difficultyToJson());
            json.add(scoreObject.getUserName(), data);
        }

        try(BufferedWriter writer = Files.newBufferedWriter(getPath())) {
            JsonUtils.prettyGson.toJson(json, writer);
        } catch(IOException e) {
            System.out.println("Failed to save " + getPath().getFileName());
            e.printStackTrace();
        }
    }

    private boolean containUser(String username) {
        for(ScoreObject score : scoreData) {
            if(score.getUserName().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private ScoreObject getUser(String username) {
        if(username.isEmpty()) return null;
        for (ScoreObject score : scoreData) {
            if (score.getUserName().equals(username)) {
                return score;
            }
        }
        return null;
    }

    public static class ScoreObject {
        private int score;
        private final String userName;
        private SocialCredit.Difficulty difficulty = SocialCredit.Difficulty.Easy;

        public ScoreObject(final String userName) {
            this.userName = userName;
        }
        public ScoreObject(final int score, final String userName) {
            this.score = score;
            this.userName = userName;
        }

        public void deductScore() {
            score = (score + difficulty.deduction);
        }

        public void incrementScore() {
            score = (score + difficulty.increment);
        }

        public String getUserName() {
            return userName;
        }

        public void setScore(int score) { this.score = score; }
        public int getScore() { return score; }

        public void setDifficulty(SocialCredit.Difficulty difficulty) { this.difficulty = difficulty; }
        public SocialCredit.Difficulty getDifficulty() { return difficulty; }

        public JsonElement scoreToJson() {
            return new JsonPrimitive(score);
        }
        public JsonElement difficultyToJson() {
            return new JsonPrimitive(difficulty.toString());
        }

        private SocialCredit.Difficulty stringToDifficulty(final String string) {
            for(SocialCredit.Difficulty difficulty : SocialCredit.Difficulty.values()) {
                if(difficulty.toString().toLowerCase(Locale.ROOT).equals(string.toLowerCase(Locale.ROOT))) {
                    return difficulty;
                }
            }
            return SocialCredit.Difficulty.Easy;
        }
    }
}
