package com.beezlabs.bots;

import com.beezlabs.hiveonserver.libs.JavaBotTemplate;
import com.beezlabs.hiveonserver.libs.TulipService;
import com.beezlabs.tulip.libs.models.BotExecutionModel;
import com.beezlabs.tulip.libs.models.BotIdentity;
import com.beezlabs.tulip.libs.models.EmailMarkPostBody;


import java.util.*;

public class MarkAsReadBot extends JavaBotTemplate {
    BotExecutionModel botExecutionModel;

    @Override
    protected void botLogic(BotExecutionModel botExecutionModel) {
        try {
            this.botExecutionModel = botExecutionModel;
            markAsRead();
            success("messageID has been marked as read");
        } catch (Exception exception) {
            error("while running botLogic method " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
    }

    private String fetchRequiredInputs() {
        String messageID;
        try {
            try {
                messageID = botExecutionModel.getProposedBotInputs().get("messageID").getValue().toString();
            } catch (NullPointerException nullPointerException) {
                throw new NullPointerException("because the key 'messageID' was not found in bot inputs map " + nullPointerException.getMessage());
            } catch (Exception exception) {
                throw new Exception("while fetching input messageID " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
            }
            return messageID;
        } catch (Exception exception) {
            error("while fetching required inputs " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
        return null;
    }

    private void markAsRead() {
        try {
            String messageID = fetchRequiredInputs();
            Map<String, String> hostCredentials = fetchHostCredentials();
            String hostName = hostCredentials.get("hostName");
            String username = hostCredentials.get("username");
            String password = hostCredentials.get("password");
            TulipService tulipService = new TulipService(hostName, username, password);
            EmailMarkPostBody emailMarkPostBody = new EmailMarkPostBody(
                    "Inbox",
                    messageID,
                    true
            );
            tulipService.markMailAsReadOrUnread(emailMarkPostBody);
        } catch (Exception exception) {
            error("while marking email as read " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
    }

    private Map<String, String> fetchHostCredentials() throws Exception {
        info("process to fetch the hostname username and password to send email has been initiated");
        String genericIdentityKey;
        Map<String, String> hostUserAndPassword = new HashMap<>();
        try {
            info("trying to fetch the the key from inputs to fetch identity from identities");
            genericIdentityKey = botExecutionModel.getProposedBotInputs().get("genericIdentityKey").getValue().toString();
            if (genericIdentityKey == null || genericIdentityKey.equals("")) {
                throw new Exception("because the value for key 'genericIdentityKey' is either null or an empty string");
            }
        } catch (NullPointerException nullPointerException) {
            throw new NullPointerException("because the key 'genericIdentityKey' was not found in bot inputs" + nullPointerException.getMessage());
        } catch (Exception exception) {
            throw new Exception("while fetching inputs " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
        try {
            String tulipHostName = botExecutionModel.getProposedBotInputs().get("tulipHostName").getValue().toString();
            if (tulipHostName == null || tulipHostName.equals("")) {
                throw new Exception("because the value of the key 'tulipHostName' is either null or an empty string");
            }
            hostUserAndPassword.put("hostName", tulipHostName);
            info("host name has been added: " + tulipHostName);
        } catch (NullPointerException nullPointerException) {
            throw new NullPointerException("because the key 'tulipHostName' was not found in bot inputs " + nullPointerException.getMessage());
        } catch (Exception exception) {
            throw new Exception("while fetching inputs " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
        try {
            List<BotIdentity> botIdentityList = botExecutionModel.getIdentityList();
            if (botIdentityList.isEmpty()) {
                throw new Exception("because no identities has been assigned to this bot kindly assign " + "the required identities");
            }
            Optional<BotIdentity> identity = botIdentityList.stream().filter(n -> (n.getIdentityType().name().equals("BASIC_AUTH") && n.getName().equals(genericIdentityKey))).findFirst();
            if (identity.isPresent()) {
                hostUserAndPassword.put("username", identity.get().getCredential().getBasicAuth().getUsername());
                info("User name has been added : {username should not be printed} ");
                hostUserAndPassword.put("password", identity.get().getCredential().getBasicAuth().getPassword());
                info("password has been added : {password should never be printed} ");
            } else {
                throw new Exception("because an identity with key '" + genericIdentityKey + "' " + "was not found in identity list ");
            }
        } catch (Exception exception) {
            throw new Exception("while fetching identity from identities list with the key: '" + genericIdentityKey + "' " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
        return hostUserAndPassword;
    }
}
