package bicicletas.apiGateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;

import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayConfig {
    @Bean
    public RouteLocator configurarRutas(RouteLocatorBuilder builder) {
        return builder.routes()
        // Ruteo al Microservicio de Estaciones
                .route(p -> p.path("/api/estacion/**").uri("https://localhost:8081/api/v1/estacion/"))
        // Ruteo al Microservicio de Alquileres
                .route(p -> p.path("/api/alquiler/**").uri("https://localhost:8082/api/v1/alquiler"))
                .build();
    }

    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        //http.authorizeHttpRequests(authorize -> authorize
        http.authorizeExchange(exchanges -> exchanges
// El rol de clientes puede realizar consultas sobre el microservicio estaciones
                    .pathMatchers(HttpMethod.GET, "/api/estacion/**")
                        .hasRole("CLIENTES")
//POST PARA "CLIENTES", puede realizar alquileres y devoluciones
                        .pathMatchers(HttpMethod.POST, "/api/alquiler/**")
                        .hasRole("CLIENTES")
// GET para "ADMINISTRADORES" puede realizar la consulta de listado de alquileres
                        .pathMatchers(HttpMethod.GET, "/api/alquiler/**")
                        .hasRole( "ADMINISTRADORES")
// POST para rol "ADMINISTRADORES", puede agregar estaciones.
                        .pathMatchers(HttpMethod.POST, "/api/estacion/**")
                        .hasRole("ADMINISTRADORES")
                        .anyExchange()
                        .authenticated()
        ).oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        var jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Se especifica el nombre del claim a analizar
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
        // Se agrega este prefijo en la conversión por una convención de Spring
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        // Se asocia el conversor de Authorities al Bean que convierte el toke JWT a un objeto Authorization
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter( new ReactiveJwtGrantedAuthoritiesConverterAdapter(grantedAuthoritiesConverter));

        return jwtAuthenticationConverter;
    }
}
