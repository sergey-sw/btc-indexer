package com.ssau.btc.utils;

import com.intelli.ray.core.Inject;
import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.messages.Messages;

/**
 * Author: Sergey42
 * Date: 24.05.14 22:14
 */
@ManagedComponent(name = "LocaleHelper")
public class LocaleHelper {

    public static final String RU = "RU";
    public static final String EN = "EN";

    @Inject
    protected Messages messages;

    public String getRussianLang() {
        return messages.getMessage("languageRU");
    }

    public String getEnglishLang() {
        return messages.getMessage("languageEN");
    }
}
