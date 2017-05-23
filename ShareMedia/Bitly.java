package bot.ShareMedia;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONObject;

public class Bitly {
	private String token;
	
	public Bitly(String t){
		token = t;
	}
	
	public String getShortLink(String link){
		String s = "-";
		try {
			String response = Download.dwn(getFetchUrl(link));
			s = new JSONObject(response)
					.getJSONObject("data")
					.getString("url");
		} catch (Exception e){
			e.printStackTrace();
		}
		return s;
	}
	
	private String getFetchUrl(String url) throws UnsupportedEncodingException{
		return "https://api-ssl.bitly.com/v3/shorten?access_token=" + token + "&longUrl=" + URLEncoder.encode(url, "UTF-8");
	}
}
