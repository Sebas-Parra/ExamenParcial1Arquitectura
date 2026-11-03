package ec.edu.monster.controlador;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter("/*")
public class FiltroCsrf implements Filter {
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpRequest.setCharacterEncoding("UTF-8");
        httpResponse.setCharacterEncoding("UTF-8");

        var sesion = httpRequest.getSession(true);
        String token = (String) sesion.getAttribute("csrf");
        if (token == null) {
            byte[] bytes = new byte[32];
            RNG.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            sesion.setAttribute("csrf", token);
        }
        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String enviado = httpRequest.getParameter("csrf");
            if (enviado == null || !MessageDigest.isEqual(
                    token.getBytes(StandardCharsets.UTF_8), enviado.getBytes(StandardCharsets.UTF_8))) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Formulario vencido. Recarga la página.");
                return;
            }
        }
        chain.doFilter(httpRequest, httpResponse);
    }
}
