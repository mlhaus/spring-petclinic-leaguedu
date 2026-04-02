package org.springframework.samples.petclinic.system;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrailingSlashRedirectFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		
		// This gets the URL that the user requested
		String requestUri = request.getRequestURI();

		// If the URL ends with a slash (and isn't just the root "/" page)
		if (requestUri.endsWith("/") && requestUri.length() > 1) {
			String newUrl = requestUri.substring(0, requestUri.length() - 1);

			// Keep any query parameters if they exist (e.g., ?page=2)
			String queryString = request.getQueryString();
			if (queryString != null) {
				newUrl += "?" + queryString;
			}

			// Send a 301 redirect to the URL without the slash
			response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
			response.setHeader(HttpHeaders.LOCATION, newUrl);
			return;
		}

		filterChain.doFilter(request, response);
	}
}