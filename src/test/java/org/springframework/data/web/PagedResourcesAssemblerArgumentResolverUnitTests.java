/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.web;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link PagedResourcesAssemblerArgumentResolver}.
 * 
 * @author Oliver Gierke
 * @since 1.7
 */
public class PagedResourcesAssemblerArgumentResolverUnitTests {

	PagedResourcesAssemblerArgumentResolver resolver;

	public @Rule ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {

		WebTestUtils.initWebTest();

		HateoasPageableHandlerMethodArgumentResolver hateoasPageableHandlerMethodArgumentResolver = new HateoasPageableHandlerMethodArgumentResolver();
		this.resolver = new PagedResourcesAssemblerArgumentResolver(hateoasPageableHandlerMethodArgumentResolver, null);
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void createsPlainAssemblerWithoutContext() throws Exception {

		Method method = Controller.class.getMethod("noContext", PagedResourcesAssembler.class);
		Object result = resolver.resolveArgument(new MethodParameter(method, 0), null, null, null);

		assertThat(result, is(instanceOf(PagedResourcesAssembler.class)));
		assertThat(result, is(not(instanceOf(MethodParameterAwarePagedResourcesAssembler.class))));
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void selectsUniquePageableParameter() throws Exception {

		Method method = Controller.class.getMethod("unique", PagedResourcesAssembler.class, Pageable.class);
		assertSelectsParameter(method, 1);
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void selectsUniquePageableParameterForQualifiedAssembler() throws Exception {

		Method method = Controller.class.getMethod("unnecessarilyQualified", PagedResourcesAssembler.class, Pageable.class);
		assertSelectsParameter(method, 1);
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void selectsUniqueQualifiedPageableParameter() throws Exception {

		Method method = Controller.class.getMethod("qualifiedUnique", PagedResourcesAssembler.class, Pageable.class);
		assertSelectsParameter(method, 1);
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void selectsQualifiedPageableParameter() throws Exception {

		Method method = Controller.class.getMethod("qualified", PagedResourcesAssembler.class, Pageable.class,
				Pageable.class);
		assertSelectsParameter(method, 1);
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void rejectsAmbiguousPageableParameters() throws Exception {
		assertRejectsAmbiguity("unqualifiedAmbiguity");
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void rejectsAmbiguousPageableParametersForQualifiedAssembler() throws Exception {
		assertRejectsAmbiguity("assemblerQualifiedAmbiguity");
	}

	/**
	 * @see DATACMNS-418
	 */
	@Test
	public void rejectsAmbiguityWithoutMatchingQualifiers() throws Exception {
		assertRejectsAmbiguity("noMatchingQualifiers");
	}

	/**
	 * @see DATACMNS-419
	 */
	@Test
	public void doesNotFailForTemplatedMethodMapping() throws Exception {

		Method method = Controller.class.getMethod("methodWithPathVariable", PagedResourcesAssembler.class);
		Object result = resolver.resolveArgument(new MethodParameter(method, 0), null, null, null);

		assertThat(result, is(notNullValue()));
	}

	private void assertSelectsParameter(Method method, int expectedIndex) throws Exception {

		MethodParameter parameter = new MethodParameter(method, 0);

		Object result = resolver.resolveArgument(parameter, null, null, null);
		assertMethodParameterAwarePagedResourcesAssemblerFor(result, new MethodParameter(method, expectedIndex));
	}

	private static void assertMethodParameterAwarePagedResourcesAssemblerFor(Object result, MethodParameter parameter) {

		assertThat(result, is(instanceOf(MethodParameterAwarePagedResourcesAssembler.class)));
		MethodParameterAwarePagedResourcesAssembler<?> assembler = (MethodParameterAwarePagedResourcesAssembler<?>) result;

		assertThat(assembler.getMethodParameter(), is(parameter));
	}

	private void assertRejectsAmbiguity(String methodName) throws Exception {

		Method method = Controller.class.getMethod(methodName, PagedResourcesAssembler.class, Pageable.class,
				Pageable.class);

		exception.expect(IllegalStateException.class);
		resolver.resolveArgument(new MethodParameter(method, 0), null, null, null);
	}

	@RequestMapping("/")
	static interface Controller {

		void noContext(PagedResourcesAssembler<Object> resolver);

		void unique(PagedResourcesAssembler<Object> assembler, Pageable pageable);

		void unnecessarilyQualified(@Qualifier("qualified") PagedResourcesAssembler<Object> assembler, Pageable pageable);

		void qualifiedUnique(@Qualifier("qualified") PagedResourcesAssembler<Object> assembler,
				@Qualifier("qualified") Pageable pageable);

		void qualified(@Qualifier("qualified") PagedResourcesAssembler<Object> resolver,
				@Qualifier("qualified") Pageable pageable, Pageable unqualified);

		void unqualifiedAmbiguity(PagedResourcesAssembler<Object> assembler, Pageable pageable, Pageable unqualified);

		void assemblerQualifiedAmbiguity(@Qualifier("qualified") PagedResourcesAssembler<Object> assembler,
				Pageable pageable, Pageable unqualified);

		void noMatchingQualifiers(@Qualifier("qualified") PagedResourcesAssembler<Object> assembler, Pageable pageable,
				@Qualifier("qualified2") Pageable unqualified);

		@RequestMapping("/{variable}/foo")
		void methodWithPathVariable(PagedResourcesAssembler<Object> assembler);
	}
}
