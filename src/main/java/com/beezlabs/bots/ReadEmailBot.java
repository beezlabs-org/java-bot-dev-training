package com.beezlabs.bots;

import com.beezlabs.hiveonserver.libs.JavaBotTemplate;
import com.beezlabs.hiveonserver.libs.TulipService;
import com.beezlabs.tulip.libs.models.BotExecutionModel;
import com.beezlabs.tulip.libs.models.BotIdentity;

import java.util.*;

public class ReadEmailBot extends JavaBotTemplate {
    BotExecutionModel botExecutionModel;

    @Override
    protected void botLogic(BotExecutionModel botExecutionModel) {
        try {
            this.botExecutionModel = botExecutionModel;
            String messageID = fetchMessageID();
            addVariable("messageID", messageID);
            success("messageID has been fetched and passed to the execution state");
        } catch (Exception exception) {
            error("while running botLogic method " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
    }

    private Map<String, String> fetchRequiredInputs() {
        Map<String, String> requiredInputs = new HashMap<>();
        try {
            requiredInputs.put("fromAddress", botExecutionModel.getProposedBotInputs().get("From address").getValue().toString());
        } catch (NullPointerException nullPointerException) {
            error("because the key 'From address' was not found in bot inputs map " + nullPointerException.getMessage());
        } catch (Exception exception) {
            error("while fetching input 'From address " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
        try {
            requiredInputs.put("subject", botExecutionModel.getProposedBotInputs().get("Subject").getValue().toString());
        } catch (NullPointerException nullPointerException) {
            error("because the key 'Subject' was not found in bot inputs map " + nullPointerException.getMessage());
        } catch (Exception exception) {
            error("while fetching input 'Subject' " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
        return requiredInputs;
    }

    private String fetchMessageID() {
        String messageID;
        Map<String, String> requiredInputs = fetchRequiredInputs();
        Map<String, Object> responseBody = new HashMap<>();
        try {
            info("process to fetch the messageID has been initiated");
            Map<String, String> hostUserAndPassword = fetchHostCredentials();
            String hostName = hostUserAndPassword.get("hostName");
            String username = hostUserAndPassword.get("username");
            String password = hostUserAndPassword.get("password");
            TulipService tulipService = new TulipService(hostName, username, password);
            responseBody = tulipService
                    .getEmailReader()
                    .filterBy()
                    .folder("Inbox")
                    // MAKE SURE INPUT PROPOSALS HAVE THE SAME NAME "From address" and "Subject"

                    .fromAddress(requiredInputs.get("fromAddress"))
                    .subject(requiredInputs.get("subject"))
                    .newOnly(true)
                    .getEmails();
        } catch (Exception exception) {
            error("while fetching messageID " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
        messageID = (String) responseBody.get("messageID");
        return messageID;
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