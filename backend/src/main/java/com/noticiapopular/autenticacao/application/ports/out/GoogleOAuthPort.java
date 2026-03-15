package com.noticiapopular.autenticacao.application.ports.out;

public interface GoogleOAuthPort {

    GoogleUserInfo validarTokenEObterDados(String googleToken);

    record GoogleUserInfo(
            String providerId,
            String email,
            String nome,
            String avatarUrl
    ) {}
}
