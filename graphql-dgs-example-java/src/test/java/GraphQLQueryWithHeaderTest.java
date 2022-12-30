/*
 * Copyright 2021 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.example.datafetcher.WithHeader;
import com.netflix.graphql.dgs.pagination.DgsPaginationAutoConfiguration;
import com.netflix.graphql.dgs.webmvc.autoconfigure.DgsWebMvcAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQLQueryWithHeaderTest {

    @Nested
    @SpringBootTest(classes = {WithHeader.class, DgsAutoConfiguration.class, DgsPaginationAutoConfiguration.class})
    @DisplayName("Without DgsWebMvcAutoConfiguration")
    class WithoutDgsWebMvcAutoConfigurationTest {
        @Autowired
        private DgsQueryExecutor queryExecutor;

        @Test
        void helloWithHeadersWithServletRequestTest() {
            final MockHttpServletRequest mockServletRequest = new MockHttpServletRequest();
            mockServletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer 123");
            ServletWebRequest servletWebRequest = new ServletWebRequest(mockServletRequest);
            String data = queryExecutor.executeAndExtractJsonPath("{ helloWithHeaders(name: \"test\") }", "data.helloWithHeaders", servletWebRequest);
            assertThat(data).isEqualTo("hello, null!"); // interesting, by default @RequestHeader(required = true)... no exception here?
        }
    }

    @Nested
    @SpringBootTest(classes = {WithHeader.class, DgsAutoConfiguration.class, DgsWebMvcAutoConfiguration.class, DgsPaginationAutoConfiguration.class})
    @DisplayName("With DgsWebMvcAutoConfiguration")
    class WithDgsWebMvcAutoConfigurationTest {
        @Autowired
        private DgsQueryExecutor queryExecutor;

        @Test
        @DisplayName("Test is passing with executeAndExtractJsonPath(String query, String jsonPath, ServletWebRequest servletWebRequest)")
        void helloWithHeadersAndServletWebRequestParameterTest() {
            final MockHttpServletRequest mockServletRequest = new MockHttpServletRequest();
            mockServletRequest.addHeader(HttpHeaders.AUTHORIZATION, "Bearer 123");
            ServletWebRequest servletWebRequest = new ServletWebRequest(mockServletRequest);
            String data = queryExecutor.executeAndExtractJsonPath("{ helloWithHeaders(name: \"test\") }", "data.helloWithHeaders", servletWebRequest);
            assertThat(data).isEqualTo("hello, Bearer 123!");
        }

        @Test
        @DisplayName("Test is not passing with executeAndExtractJsonPath(String query, String jsonPath, HttpHeaders headers)")
        void helloWithHeadersAndHeadersParameterTest() {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer 123");

            String data = queryExecutor.executeAndExtractJsonPath("{ helloWithHeaders(name: \"test\") }", "data.helloWithHeaders", headers);
            assertThat(data).isEqualTo("hello, Bearer 123!");
        }
    }

}