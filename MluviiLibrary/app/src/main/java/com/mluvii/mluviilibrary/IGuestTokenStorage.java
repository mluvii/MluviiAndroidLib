package com.mluvii.mluviilibrary;

/**
 * Used to customize the way the guest identity and active chat tokens are stored.
 * For example, the tokens can be encrypted and stored in customer's profile on the server.
 */
public interface IGuestTokenStorage {

    /**
     * Save identity token. This token is used to fetch past conversations.
     */
    void saveIdentityToken(String token);

    /**
     * Load identity token. This token is used to fetch past conversations.
     */
    String loadIdentityToken();

    /**
     * Save active chat token. This token is used to resume active chat.
     */
    void saveActiveChatToken(String token);

    /**
     * Load active chat token. This token is used to resume active chat.
     */
    String loadActiveChatToken();
}
