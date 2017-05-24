package bot.ShareMedia;

import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.GetUpdatesResponse;

public class App{
	
	private static final Logger LOGGER = Logger.getLogger( App.class.getName() );
    public static void main(String[] args){
		int offset = 0;
    	Settings st = new Settings();
    	TelegramBot bot;
    	//Bitly bt;
    	//testCodeClass();
    	 Handler handler = new ConsoleHandler();
    	 handler.setLevel(Level.ALL);
    	 handler.setFormatter(new Format());
    	 LOGGER.addHandler(handler);
    	 LOGGER.setLevel(Level.ALL);
    	 LOGGER.setUseParentHandlers(false);
    	 LOGGER.info("Loading settings");

    	
    	try{
    		FileHandler fh = new FileHandler("log.txt");
    		fh.setFormatter(new Format());
    		LOGGER.addHandler(fh);
    		
    		if(!st.loadSettings()) return;
    		bot = TelegramBotAdapter.build(st.getTelegramToken());
    		st.setMe(bot.execute(new GetMe()));
    		//bt = new Bitly(st.getBitlyToken());
    		
    	}catch(Exception e){
			LOGGER.warning(e.toString());
    		e.printStackTrace();
    		return;
    	}
    	LOGGER.config("Startup done.");
    	LOGGER.config("Current directory: " + st.getDirectory());
    	LOGGER.config("Bot info: " + st.getBot().toString());
    	
    	while(true){
    		try{
    			
    			GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates().offset(offset).timeout(2));
				List<Update> updates = updatesResponse.updates();
				for(Update u : updates){
					offset = u.updateId() + 1;
					Message msg = u.message();
					
					if(isMedia(msg)){
						Code c = new Code();
						String id = "";
						
						if(msg.replyToMessage() != null){
							id = getOldId(msg.replyToMessage().text());
							if(Files.exists(Paths.get("codes" + File.separator + id)) && st.getBotId() == msg.replyToMessage().from().id()){
								
								c.setRaw(new String(Files.readAllBytes(Paths.get("codes" + File.separator + id))));
								c.calcoulateMedia();
								LOGGER.finer("[" + msg.from().id() + "] " + "Appending media to: " + id);
								
							} else  id = generateToken();
						} else id = generateToken();

						if(c.getMediaNo() < 1024){
							
							Media m = new Media(msg);
							LOGGER.finest("[" + msg.from().id() + "] " + "Media data: " + m.toString());
						
							if(!c.mediaExist(m)) c.appendMedia(m); 
								else LOGGER.warning("[" + msg.from().id() + "] Media already exist");
							c.calcoulateRaw();
							Files.write(Paths.get("codes" + File.separator + id), c.getRaw().getBytes(), CREATE);
						
							if(m.getType() != 0){
								if(msg.replyToMessage() != null && !msg.replyToMessage().text().contains("Done!"))
									
									bot.execute(new EditMessageText(msg.chat().id().toString(), msg.replyToMessage().messageId(), 
											"*Link:* https://t.me/ShareMediaBot?start=" + id + "\n\n"
											+ "Append media to this link by replying to this message"
											+ " _(" + (1023 - c.getMediaNo()) + " media remain for this link)_")
										.disableWebPagePreview(true).parseMode(ParseMode.Markdown));
								
								else bot.execute(new SendMessage(msg.chat().id().toString(), 
											"*Done!*\n\n"
											+ "Now share it with your friend :D\n"
											//+ "Short link: " + bt.getShortLink(lnk) + " (Powered by BitLy)\n"
											+ "*Link:* https://t.me/ShareMediaBot?start=" + id + "\n\n"
											+ "If you want to append more media to this link send it replying to this message"
											+ " _(" + (1023 - c.getMediaNo()) + " media remain for this link)_")
										.disableWebPagePreview(true).parseMode(ParseMode.Markdown));
							}
						} else bot.execute(new SendMessage(msg.chat().id().toString(), "*ERROR:* "
										+ "Can't append more than 1023 media to a single link :(\n\n\n"
										+ "_(WTF How did you arrived to this limit lmao)_").parseMode(ParseMode.Markdown));
					} else {
						
						LOGGER.info("[" + msg.from().id() + "] " + msg.text());
						String[] sp = msg.text().split("\\s+");
						if(sp.length == 2 && sp[0].equalsIgnoreCase("/start") && sp[1].length() == 8 && Files.exists(Paths.get("codes" + File.separator + sp[1].replaceAll("[^a-zA-Z0-9_-]+","")))){
							
							Code c = new Code();
							c.setRaw(new String(Files.readAllBytes(Paths.get("codes" + File.separator + sp[1].replaceAll("[^a-zA-Z0-9_-]+","")))));
							c.calcoulateMedia();
							for(int i = 0; i < c.getMediaNo(); i++){
								bot.execute(getMediaRequest(c.getMedia(i), msg.chat().id().toString())).description();
								LOGGER.finest("[" + msg.from().id() + "] Request media: " + c.getMedia(i).toString());
							}
			    			LOGGER.fine("[" + msg.from().id() + "] " + "Request media for id: " + sp[1]);
							
						} else {
							
							if(msg.text().equalsIgnoreCase("/start")) bot.execute(new SendMessage(
									msg.from().id().toString(),
									"*Heya*! Welcome in " + st.getBot().username() + "! :D\n" +
									"With this bot you can easy share with your friends:\n" +
									"*-* Photo\n" +
									"*-* Video\n" +
									"*-* Files\n" +
									"*-* Music\n\n" +
									"*Try me by sending a media or using /media!* :)").parseMode(ParseMode.Markdown));
							
							if(msg.text().equalsIgnoreCase("/share")){
								String id = generateToken();
								Code c = new Code();
								c.calcoulateRaw();
								Files.write(Paths.get("codes" + File.separator + id), c.getRaw().getBytes(), CREATE);
								bot.execute(new SendMessage(msg.chat().id().toString(), 
										"*Link:* https://t.me/ShareMediaBot?start=" + id + "\n\n"
										+ "Append media to this link by replying to this message"
										+ " _(1023 media remain for this link)_")
									.disableWebPagePreview(true).parseMode(ParseMode.Markdown));
							}
							if(msg.text().equalsIgnoreCase("/github")) bot.execute(new SendMessage(
									msg.chat().id().toString(),
									"Hope you enjoy my codes!").replyMarkup(
										new InlineKeyboardMarkup(
											new InlineKeyboardButton[]{
													new InlineKeyboardButton("Github").url("https://github.com/stranck/ShareMediaBot")
											})));
							if(msg.text().equalsIgnoreCase("/news")) bot.execute(new SendMessage(
									msg.chat().id().toString(),
									"News _(in italian)_ of all my bots").parseMode(ParseMode.Markdown).replyMarkup(
										new InlineKeyboardMarkup(
											new InlineKeyboardButton[]{
													new InlineKeyboardButton("Multychat News").url("https://t.me/MultychatNews")
											})));
							if(msg.text().equalsIgnoreCase("/ping")) bot.execute(new SendMessage(msg.chat().id().toString(), "Pong"));
						}
					}
				}
    		} catch(Exception e){
    			LOGGER.warning(getThrow(e));
    		}
    	}
    }
    
    public static boolean isMedia(Message m){
    	return m.text() == null;
    }
    
    public static String getOldId(String text){
    	String id = "";
    	int i = text.indexOf('=') + 1;
    	for(int n = 0; n < 8; n++)
    		id += text.charAt(i + n);
    	return id.replaceAll("[^a-zA-Z0-9_-]+","");
    }
    
    public static BaseRequest<?, ?> getMediaRequest(Media m, String chatId){
    	BaseRequest<?, ?> br = null;
    	
    	switch(m.getType()){
			case 1:{
				br = new SendAudio(chatId, m.getFileId());
				break;
			}
			case 2:{
				br = new SendDocument(chatId, m.getFileId());
				break;
			}
			case 3:{
				br = new SendPhoto(chatId, m.getFileId());
				break;
			}
			case 4:{
				br = new SendVideo(chatId, m.getFileId());
				break;
			}
    	}
    	return br;
    }
    
    public static String generateToken(){
    	Random r = new Random();
    	Code c = new Code();
    	String id;
		do{
			id = "";
			for(int i = 0; i < 8; i++) id += c.getChar(r.nextInt(63));
		}while(Files.exists(Paths.get("codes" + File.separator + id)));
    	return id;
    }
    
	public static String getThrow(Exception e){
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
		
	}
}
