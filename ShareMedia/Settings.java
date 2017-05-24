package bot.ShareMedia;

import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.response.GetMeResponse;

public class Settings {
	
	private String tToken, dir;
	private User botUser;
	
	public boolean loadSettings() throws IOException{
		
		if(!Files.exists(Paths.get("config.json"))) {
			System.out.println("config.json not found");
			Files.write(Paths.get("config.json"), (
					"{\n" +
					"    \"telegramToken\" : \"INSERIT YOUR TOKEN HERE\",\n" +
					"}"
				).getBytes(), CREATE);
			return false;
		}
		
		JSONObject config = new JSONObject(new String(Files.readAllBytes(Paths.get("config.json"))));
		tToken = config.getString("telegramToken");
		dir = System.getProperty("user.dir") + File.separator;
		return true;
	}
	public void setMe(GetMeResponse gmr){
		botUser = gmr.user();
	}
	
	public User getBot(){
		return botUser;
	}
	public long getBotId(){
		return botUser.id();
	}
	public String getTelegramToken(){
		return tToken;
	}
	public String getDirectory(){
		return dir;
	}
	
}
