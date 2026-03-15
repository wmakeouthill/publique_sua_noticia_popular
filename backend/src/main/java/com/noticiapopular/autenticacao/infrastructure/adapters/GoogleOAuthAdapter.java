package com.noticiapopular.autenticacao.infrastructure.adapters;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.noticiapopular.autenticacao.application.ports.out.GoogleOAuthPort;
import com.noticiapopular.kernel.domain.exceptions.ValidacaoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class GoogleOAuthAdapter implements GoogleOAuthPort {

    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthAdapter(@Value("${app.google.client-id}") String googleClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    @Override
    public GoogleUserInfo validarTokenEObterDados(String googleToken) {
        try {
            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                throw new ValidacaoException("Token Google inválido");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            return new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (ValidacaoException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Erro ao validar token Google", ex);
            throw new ValidacaoException("Falha ao validar token Google: " + ex.getMessage());
        }
    }
}
