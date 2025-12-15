package com.jsalva.trainerworkload.filter;

import com.jsalva.trainerworkload.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try{
            String jwt = getTokenFromHeader(request);


            //check token exists and is valid
            if(jwt != null && jwtUtils.validateJwtToken(jwt)){
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                String userType = jwtUtils.getUserTypeFromJwtToken(jwt);

                //            ROLE_TRAINER, ROLE_TRAINEE, ROLE_USER
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+userType));

                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, jwt, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in Spring Security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authenticated user={}, authorities={}",
                        authentication.getName(),
                        authentication.getAuthorities()
                );
                logger.debug("Set authentication for user: {} with role: ROLE_{}", username, userType);
            }
        }catch (Exception e){
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        // Continue with the filter chain
        filterChain.doFilter(request, response);

    }

    private String getTokenFromHeader(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        logger.debug("Authorization header = {}", header);


        if(StringUtils.hasText(header) && header.startsWith("Bearer")){
            return header.substring(7);
        }
        return null;
    }
}
