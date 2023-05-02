package com.beezlabs.bots;

import com.beezlabs.hiveonserver.libs.JavaBotTemplate;
import com.beezlabs.tulip.libs.models.BotExecutionModel;

import java.util.Arrays;

public class SignatureBot extends JavaBotTemplate {
    @Override
    protected void botLogic(BotExecutionModel botExecutionModel) {
        try {
            success("SignatureBot has been executed successfully");
        } catch (Exception exception) {
            error("while running botLogic method " + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace()));
        }
    }
}
