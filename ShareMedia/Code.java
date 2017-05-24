package bot.ShareMedia;

import java.util.ArrayList;

public class Code {
	private ArrayList<Media> m = new ArrayList<Media>();
	String raw;

	
	public boolean mediaExist(Media media){
		int i = 0;
		boolean b = true;
		while(i < m.size() && b) if(m.get(i).getFileId().equals(media.getFileId())) b = false;
		return !b;
	}
	public String getRaw(){
		return raw;
	}
	public void setRaw(String bin){
		raw = bin;
	}
	public void appendMedia(Media media){
		m.add(media);
	}
	public Media getMedia(int index){
		return m.get(index);
	}
	public int getMediaNo(){
		return m.size();
	}
	
	public void calcoulateRaw(){
		String bin = fixLChar(Integer.toString(m.size(), 2), 10);
		for(int i = 0; i < m.size(); i++)
			
			bin = bin +
					fixLChar(Integer.toString(m.get(i).getType(), 2), 2) +
					fixLChar(Integer.toString(m.get(i).getFileId().length(), 2), 6) +
					getBin(m.get(i).getFileId());
		
		toArray(fixRChar(bin, 8));
	}
	
	public void calcoulateMedia(){
		String bin = fromArray();
		m.clear();
		int mediaNo = Integer.parseInt(bin.substring(0, 10), 2);
		int position = 10;
		for(int i = 0; i < mediaNo; i++){

			m.add(new Media());
			m.get(i).setType((byte) Integer.parseInt(bin.substring(position++, ++position), 2));
			
			int charNo = Integer.parseInt(bin.substring(position, position + 6), 2);
			position += 6;
			String fileId = "";
			for(int n = 0; n < charNo; n++){
				fileId += getChar(Integer.parseInt(bin.substring(position, position + 6), 2));
				position += 6;
			}
			m.get(i).setFileId(fileId);
		}
	}
	public String getChar(int n){
		String c = "-";
		if(n == 63) c = "_";
			else if(n > 0 && n < 11) c = String.valueOf(n - 1);
			else if(n > 10 && n < 37) c = String.valueOf((char)(n - 11 + 65));
			else if(n > 36 && n < 63) c = String.valueOf((char)(n - 37 + 97));
		return c;
	}
	
	private String fromArray(){
		String s = "";
		for(int i = 0; i < raw.length(); i++){
			s += fixLChar(Integer.toString(raw.charAt(i), 2), 8);
		}
		return s;
	}
	private void toArray(String bin){
		raw = "";
		int position = 0;
		while(position < bin.length()){
			raw += (char) Integer.parseInt(bin.substring(position, position + 8), 2);
			position += 8;
		}
	}	
	private String getBin(String s){
		String bin = "";
		for(int i = 0; i < s.length(); i++)
			bin += fixLChar(Integer.toString(decodeChar(s.charAt(i)), 2), 6);
		return bin;
	}
	private int decodeChar(char c){
		int n = 0;
		if(c == '_') n = 63;
			else if(Character.isDigit(c)) n = Character.getNumericValue(c) + 1;
				else {
					n = getAlphabetPosition(c);
					if(Character.isUpperCase(c)) n += 11; else n += 37;
				}
		return n;
	}
	private int getAlphabetPosition(char c){
		return ((int) Character.toUpperCase(c)) - 65;
	}
	private String fixLChar(String bin, int n){
		String s = "";
		while((bin.length() + s.length()) % n != 0)
			s += "0";
		return s + bin;
	}
	private String fixRChar(String bin, int n){
		while(bin.length() % n != 0)
			bin += "0";
		return bin;
	}
}
