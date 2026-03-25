package dingwan.easy.ai.core.chat.message;


import dingwan.easy.ai.core.cntent.Content;

public interface Message extends Content  {

    MessageType getMessageType();

}
