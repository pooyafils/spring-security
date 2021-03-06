package com.example.demosecurity.config;

import com.example.demosecurity.jwt.JwtConfig;
import com.example.demosecurity.jwt.JwtTokenVerifier;
import com.example.demosecurity.jwt.JwtUsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.crypto.SecretKey;
import javax.sql.DataSource;

import java.util.concurrent.TimeUnit;

import static com.example.demosecurity.config.ApplicationUserPermission.*;
import static com.example.demosecurity.config.ApplicationUserRole.*;


@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends
        WebSecurityConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final ApplicationUserService applicationUserService;
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;
    public SecurityConfig(PasswordEncoder passwordEncoder,ApplicationUserService applicationUserService
  , SecretKey secretKey, JwtConfig jwtConfig) {
        this.applicationUserService=applicationUserService;
        this.passwordEncoder = passwordEncoder;
        this.secretKey=secretKey;
        this.jwtConfig=jwtConfig;
    }

  /*  @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(securityDataSource);   //other type of the basic auth with db
    }*/

/*    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.inMemoryAuthentication()
                .withUser("user").password("{noop}password").roles("USER")      //other type of the auth with no db
                .and()
                .withUser("admin").password("{noop}password").roles("ADMIN");

    }*/


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(new JwtUsernamePasswordAuthenticationFilter(authenticationManager(),jwtConfig,secretKey))
                .addFilterAfter(new JwtTokenVerifier(secretKey,jwtConfig),JwtUsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/", "index", "/css/*", "/js/*").permitAll()
                .antMatchers(HttpMethod.GET, "/person/getAll").hasRole(ADMIN.name())
                .antMatchers(HttpMethod.GET,"/person/one/{id}").hasRole(USER.name())
                .anyRequest()
                .authenticated();

    }


/*    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                //HTTP Basic authentication
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/person/getAll").hasRole(ADMIN.name())
                .antMatchers(HttpMethod.GET,"/person/one/{id}").hasRole(USER.name())
                .antMatchers(HttpMethod.POST, "/person/register").hasRole(USER.name())
               // .antMatchers(HttpMethod.DELETE,"/management/api/**").hasAuthority(COURSE_WRITE.getPermission())
               // .antMatchers(HttpMethod.POST,"/management/api/**").hasAuthority(COURSE_WRITE.getPermission())
                //.antMatchers(HttpMethod.PUT,"/management/api/**").hasAuthority(COURSE_WRITE.getPermission())
                //.antMatchers(HttpMethod.GET,"/management/api/**").hasAnyRole(ADMIN.name(),ADMINTRAINER.name())

                .antMatchers("/").permitAll()
                .and().exceptionHandling().accessDeniedPage("/person/access-deniedSS")
                .and()
                .csrf().disable()
                .formLogin().disable()
        ;
    }*/

 /*   @Override
    @Bean
    protected UserDetailsService userDetailsService()  { //basic auth with userdetail memory type\
        UserDetails annaSmithUser=User.builder()
                .username("pass").password(passwordEncoder.encode("pass"))
               // .roles(ADMIN.name())
                .authorities(ADMIN.grantedAuthoritySet())
                .build();
        UserDetails tom=User.builder()
                .username("tom").password(passwordEncoder.encode("pass"))
                //.roles(ADMINTRAINER.name())
                .authorities(ADMINTRAINER.grantedAuthoritySet())
                .build();
        UserDetails user=User.builder()
                .username("user").password(passwordEncoder.encode("pass"))
                //.roles(USER.name())
                .authorities(USER.grantedAuthoritySet())
                .build();
        return new InMemoryUserDetailsManager(annaSmithUser,tom,user);
    }*/
@Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
    DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
    provider.setPasswordEncoder(passwordEncoder);
    provider.setUserDetailsService(applicationUserService);
    return provider;
}

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }
}