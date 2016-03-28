/*
 * (C) Quartet FS 2012-2015
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.qfs.sandbox.security.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import com.qfs.content.service.IContentService;
import com.quartetfs.biz.pivot.security.IRoleComparator;
import com.quartetfs.biz.pivot.security.impl.RoleComparatorAdapter;
import com.quartetfs.biz.pivot.spring.ActivePivotConfig;
import com.quartetfs.fwk.ordering.impl.CustomComparator;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 *
 * Spring configuration fragment for security.
 *
 * <p>
 * You can import one of the following resources:
 * <ul>
 * <li>SecurityAnonymous.xml (allow anonymous access)
 * <li>SecurityBasic.xml (HTTP basic authentication)
 * </ul>
 *
 * @author Quartet FS
 *
 */
@Configuration
@ImportResource(value = { "classpath:SECURITY-INF/SecurityBasic.xml" })
public class SecurityConfig {

	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	@Bean
	@Qualifier("authenticationManager")
	public AuthenticationManager authenticationManager() {
		final ProviderManager manager = new ProviderManager(Collections.singletonList(authenticationProvider()));
		manager.setEraseCredentialsAfterAuthentication(false);
		return manager;
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		final DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsManager());
		return provider;
	}

	@Bean
	@Qualifier("authenticationEntryPoint")
	public CustomAuthenticationEntryPoint authenticationEntryPoint() {
		final CustomAuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint();
		authenticationEntryPoint.setRealmName("Spring Security Application");
		return authenticationEntryPoint;
	}

	@Bean
	@Qualifier("authenticationFilter")
	public BasicAuthenticationFilter authenticationFilter(
			@Qualifier("authenticationEntryPoint") CustomAuthenticationEntryPoint authenticationEntryPoint,
			@Qualifier("authenticationManager") AuthenticationManager authenticationManager) {
		return new BasicAuthenticationFilter(
				authenticationManager, authenticationEntryPoint);
	}


	/**
	 * Simple use details manager based on an hard coded user list,
	 * stored in memory.
	 *
	 * For production usage this is at least replaced by a database store.
	 *
	 * @return user details manager
	 */
	@Bean
	public UserDetailsManager userDetailsManager() {
		final List<UserDetails> users = new ArrayList<>();
		// ROLE_ROOT allows the admin to do any operations on the content service when it is embedded in AP server
		users.add(new SimpleUser("admin", "admin", ROLE_USER, ROLE_ADMIN, IContentService.ROLE_ROOT));
		users.add(new SimpleUser("user1", "user1", ROLE_USER, "ROLE_DESK_A"));
		users.add(new SimpleUser("user2", "user2", ROLE_USER, "ROLE_EUR_USD"));
		users.add(new SimpleUser("live", "live", "ROLE_TECH")); // Technical user for ActivePivot Live access
		InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager(users);
		return manager;
	}

	/**
	 * Overrides the comparator defined by {@link ActivePivotConfig#roleComparator()}
	 *
	 * @return a comparator that indicates which role prevails over another. <b>NOTICE - a role
	 *         coming AFTER another one prevails over that "previous" role.</b> This role ordering
	 *         definition is essential to resolve possible ambiguity when, for a given user, a
	 *         context value has been defined in more than one role applicable to that user. In such
	 *         case, it is what has been set for the "prevailing" role that will be effectively
	 *         retained for that context value for that user.
	 */
	@Bean
	public IRoleComparator roleComparator() {
		final CustomComparator<String> comp = new CustomComparator<>();
		comp.setFirstObjects(Arrays.asList(ROLE_USER));
		comp.setLastObjects(Arrays.asList(ROLE_ADMIN));
		return new RoleComparatorAdapter(comp);
	}

	/**
	 *
	 * Convenient and simple Spring Security user implementation.
	 *
	 * @author Quartet FS
	 *
	 */
	public static class SimpleUser extends User {

		/** serialVersionUID */
		private static final long serialVersionUID = -2171284605087844550L;

		public SimpleUser(String username, String password, String... roles) {
			super(username, password, wrapRoles(roles));
		}

		static List<GrantedAuthority> wrapRoles(String... roles) {
			if (roles != null) {
				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				for (String role : roles) {
					authorities.add(new SimpleGrantedAuthority(role));
				}
				return authorities;
			} else {
				return null;
			}
		}

	}

}
