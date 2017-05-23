package bot.ShareMedia;

import com.pengrad.telegrambot.model.Message;

public class Media {
	byte type = 0;
	String fileId;
	
	public void setFileId(String s){
		fileId = s;
	}
	public void setType(Byte b){
		type = b;
	}
	
	public Media(){
		//u lost the game HHIHIHIIHIHI OMG IM SO FUNNYYY XDDDDDDDD
	}
	public Media(Message m){
		setType(m);
		setFileId(m);
	}
	
    private void setType(Message m){
    	if(m.audio() != null ) type = 1;
    	if(m.document() != null ) type = 2;
    	if(m.photo() != null) type = 3;
    	if(m.video() != null) type = 4;
    }
    
    private void setFileId(Message m){
    	switch(type){
    		case 1:{
    			fileId = m.audio().fileId();
    			break;
    		}
    		case 2:{
    			fileId = m.document().fileId();
    			break;
    		}
    		case 3:{
    			fileId = m.photo()[m.photo().length - 1].fileId();
    			break;
    		}
    		case 4:{
    			fileId = m.video().fileId();
    			break;
    		}
    	}
    }
    
    public byte getType(){
    	return type;
    }
    public String getFileId(){
    	return fileId;
    }
    public String toString(){
    	String[] types = {"audio", "document", "photo", "video"};
    	return "Media{typeId=" + type + ", typeName='" + types[type - 1] + "', fileId='" + fileId + "'}";
    }
}
