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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticarComGoogleUseCase {

    private final GoogleOAuthPort googleOAuth;
    private final UsuarioRepositoryPort usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.auth.google.admin-emails:}")
    private String adminGoogleEmails;

    @Transactional
    public TokenResponse executar(LoginGoogleRequest request) {
        GoogleUserInfo googleInfo = googleOAuth.validarTokenEObterDados(request.googleToken());
        log.info("Login Google para: {}", googleInfo.email());

        Usuario usuario = usuarioRepository
                .buscarPorProviderId("google", googleInfo.providerId())
                .orElseGet(() -> criarNovoUsuario(googleInfo));

        aplicarPoliticaDePapel(usuario, googleInfo.email());
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
                info.providerId());
        return usuarioRepository.salvar(novoUsuario);
    }

    private void aplicarPoliticaDePapel(Usuario usuario, String emailGoogle) {
        if (ehEmailAdmin(emailGoogle)) {
            usuario.promoverParaAdmin();
            return;
        }

        if (usuario.ehAdmin()) {
            usuario.rebaixarParaUsuario();
        }
    }

    private boolean ehEmailAdmin(String emailGoogle) {
        if (emailGoogle == null || emailGoogle.isBlank()) {
            return false;
        }

        Set<String> adminsConfigurados = Arrays.stream(adminGoogleEmails.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return adminsConfigurados.contains(emailGoogle.trim().toLowerCase());
    }
}
