package com.noticiapopular.autenticacao.application.usecases;

import com.noticiapopular.autenticacao.application.dtos.LoginGoogleRequest;
import com.noticiapopular.autenticacao.application.dtos.TokenResponse;
import com.noticiapopular.autenticacao.application.ports.out.GoogleOAuthPort;
import com.noticiapopular.autenticacao.application.ports.out.GoogleOAuthPort.GoogleUserInfo;
import com.noticiapopular.autenticacao.application.ports.out.UsuarioRepositoryPort;
import com.noticiapopular.autenticacao.domain.entities.Usuario;
import com.noticiapopular.kernel.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticarComGoogleUseCase {

    private final GoogleOAuthPort googleOAuth;
    private final UsuarioRepositoryPort usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public TokenResponse executar(LoginGoogleRequest request) {
        GoogleUserInfo googleInfo = googleOAuth.validarTokenEObterDados(request.googleToken());
        log.info("Login Google para: {}", googleInfo.email());

        Usuario usuario = usuarioRepository
                .buscarPorProviderId("google", googleInfo.providerId())
                .orElseGet(() -> criarNovoUsuario(googleInfo));

        usuario.atualizarPerfil(googleInfo.nome(), googleInfo.avatarUrl());
        usuarioRepository.salvar(usuario);

        String accessToken = jwtTokenProvider.gerarAccessToken(usuario.getId(), usuario.getPapel().name());
        String refreshToken = jwtTokenProvider.gerarRefreshToken(usuario.getId());

        return new TokenResponse(accessToken, refreshToken, usuario.ehAdmin(), jwtExpirationMs / 1000);
    }

    private Usuario criarNovoUsuario(GoogleUserInfo info) {
        log.info("Criando novo usuário para: {}", info.email());
        Usuario novoUsuario = Usuario.criarComGoogle(
                info.email(),
                info.nome(),
                info.avatarUrl(),
                info.providerId()
        );
        return usuarioRepository.salvar(novoUsuario);
    }
}
